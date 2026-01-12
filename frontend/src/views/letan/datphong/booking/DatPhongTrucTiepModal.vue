<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { PhieuDatTamResponse, SavePhieuDatTamRequest, TimKhachHangResponse } from '@/service/api/letan/booking'
import { savePhieuDatTam, searchKhachHang, getPhieuDatTam } from '@/service/api/letan/booking'
import { useDebounceFn } from '@vueuse/core'

interface Props {
  visible: boolean
  selectedRooms: Array<{
    idPhong: string
    maPhong: string
    tenPhong: string
    tenLoaiPhong: string
    tang: number
    gia: number
  }>
  sessionId?: string 
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'continue', sessionId: string): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const formData = ref({
  ngayNhan: null as [number, number] | null,
  soLuongKhach: 1,
  ghiChu: '',
  nhanNgay: false,
  tienKhachTra: null as number | null,
})

const keywordKhachHang = ref('')
const khachHangOptions = ref<TimKhachHangResponse[]>([])
const selectedKhachHang = ref<string | null>(null)
const isSearchingKH = ref(false)
const isLoading = ref(false)
const currentSessionId = ref<string>('')

const soNgayO = computed(() => {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1])
    return 0
  return Math.ceil((formData.value.ngayNhan[1] - formData.value.ngayNhan[0]) / (1000 * 60 * 60 * 24))
})

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

const tongTienPhong = computed(() => {
  return props.selectedRooms.reduce((sum, p) => sum + p.gia, 0)
})

const tongTien = computed(() => {
  return tongTienPhong.value * soNgayO.value
})

const tienThua = computed(() => {
  if (!formData.value.tienKhachTra || formData.value.tienKhachTra <= 0) return 0
  const thua = formData.value.tienKhachTra - tongTien.value
  return thua > 0 ? thua : 0
})

const congNo = computed(() => {
  if (!formData.value.tienKhachTra || formData.value.tienKhachTra <= 0) return tongTien.value
  const no = tongTien.value - formData.value.tienKhachTra
  return no > 0 ? no : 0
})

function closeModal() {
  modalVisible.value = false
  resetForm()
}

function resetForm() {
  formData.value = {
    ngayNhan: null,
    soLuongKhach: 1,
    ghiChu: '',
    nhanNgay: false,
    tienKhachTra: null,
  }
  keywordKhachHang.value = ''
  khachHangOptions.value = []
  selectedKhachHang.value = null
  currentSessionId.value = ''
}

const debouncedSearchKH = useDebounceFn(async () => {
  if (!keywordKhachHang.value || keywordKhachHang.value.length < 2) {
    khachHangOptions.value = []
    return
  }

  try {
    isSearchingKH.value = true
    const data = await searchKhachHang(keywordKhachHang.value)
    khachHangOptions.value = data
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tìm kiếm khách hàng')
  }
  finally {
    isSearchingKH.value = false
  }
}, 500)

watch(() => keywordKhachHang.value, () => {
  debouncedSearchKH()
})

function canNhanNgay() {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0]) return false
  const now = Date.now()
  const oneHourBeforeCheckIn = formData.value.ngayNhan[0] - (60 * 60 * 1000)
  return now >= oneHourBeforeCheckIn
}

async function handleLuuTam() {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1]) {
    window.$message.warning('Vui lòng chọn ngày nhận và trả phòng')
    return
  }

  if (props.selectedRooms.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  try {
    isLoading.value = true

    const soNgay = Math.ceil((formData.value.ngayNhan[1] - formData.value.ngayNhan[0]) / (1000 * 60 * 60 * 24))

    let currentStep: 'SELECT_ROOM' | 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'READY_TO_CONFIRM' = 'CUSTOMER_INFO'
    if (selectedKhachHang.value) {
      if (formData.value.tienKhachTra !== null && formData.value.tienKhachTra !== undefined) {
        currentStep = 'READY_TO_CONFIRM'
      } else {
        currentStep = 'PAYMENT_INFO'
      }
    }

    const phieuData: SavePhieuDatTamRequest = {
      sessionId: currentSessionId.value || undefined,
      checkInDate: formData.value.ngayNhan[0],
      checkOutDate: formData.value.ngayNhan[1],
      soLuongKhach: formData.value.soLuongKhach,
      idKhachHang: selectedKhachHang.value || null,
      ghiChu: formData.value.ghiChu || null,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || null,
      danhSachIdPhong: props.selectedRooms.map(r => r.idPhong),
      isFromRoomClick: true,
      currentStep,

      roomDetails: props.selectedRooms.map(r => ({
        idPhong: r.idPhong,
        maPhong: r.maPhong,
        tenPhong: r.tenPhong,
        tenLoaiPhong: r.tenLoaiPhong,
        tang: r.tang,
        gia: r.gia,
        soNgay,
      })),
    }

    const result = await savePhieuDatTam(phieuData)
    currentSessionId.value = result.sessionId

    window.$message.success('Đã lưu phiếu đặt tạm! Bạn có thể quay lại sau.')
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể lưu phiếu đặt tạm')
  }
  finally {
    isLoading.value = false
  }
}

async function handleTiepTuc() {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1]) {
    window.$message.warning('Vui lòng chọn ngày nhận và trả phòng')
    return
  }

  if (props.selectedRooms.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  if (!selectedKhachHang.value) {
    window.$message.warning('Vui lòng chọn khách hàng để tiếp tục')
    return
  }

  if (formData.value.tienKhachTra === null || formData.value.tienKhachTra === undefined) {
    window.$message.warning('Vui lòng nhập số tiền khách trả')
    return
  }

  try {
    isLoading.value = true

    const soNgay = Math.ceil((formData.value.ngayNhan[1] - formData.value.ngayNhan[0]) / (1000 * 60 * 60 * 24))

    const phieuData: SavePhieuDatTamRequest = {
      sessionId: currentSessionId.value || undefined,
      checkInDate: formData.value.ngayNhan[0],
      checkOutDate: formData.value.ngayNhan[1],
      soLuongKhach: formData.value.soLuongKhach,
      idKhachHang: selectedKhachHang.value || null,
      ghiChu: formData.value.ghiChu || null,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || null,
      danhSachIdPhong: props.selectedRooms.map(r => r.idPhong),
      isFromRoomClick: true,
      currentStep: 'READY_TO_CONFIRM',
      roomDetails: props.selectedRooms.map(r => ({
        idPhong: r.idPhong,
        maPhong: r.maPhong,
        tenPhong: r.tenPhong,
        tenLoaiPhong: r.tenLoaiPhong,
        tang: r.tang,
        gia: r.gia,
        soNgay,
      })),
    }

    const result = await savePhieuDatTam(phieuData)



    emit('continue', result.sessionId)
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tiếp tục')
  }
  finally {
    isLoading.value = false
  }
}

async function loadPhieuDatTam() {
  if (!props.sessionId) return

  try {
    isLoading.value = true
    const data = await getPhieuDatTam(props.sessionId)

    currentSessionId.value = data.sessionId
    formData.value = {
      ngayNhan: [data.checkInDate, data.checkOutDate],
      soLuongKhach: data.soLuongKhach,
      ghiChu: data.ghiChu || '',
      nhanNgay: data.nhanNgay || false,
      tienKhachTra: data.tienKhachTra || null,
    }
    selectedKhachHang.value = data.idKhachHang

    if (data.tenKhachHang) {
      khachHangOptions.value = [{
        id: data.idKhachHang!,
        hoTen: data.tenKhachHang,
      } as TimKhachHangResponse]
    }
  }
  catch (error: any) {
    window.$message.error('Không thể tải phiếu đặt tạm')
  }
  finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.sessionId) {
      loadPhieuDatTam()
    }
  } else {
    resetForm()
  }
})

function formatDate(timestamp: number) {
  return new Date(timestamp).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    title="Đặt phòng"
    class="w-1100px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="grid grid-cols-12 gap-4">

        <div class="col-span-5">
          <n-card size="small" :bordered="false">
            <template #header>
              <span class="text-base font-semibold">Phòng đã chọn ({{ selectedRooms.length }})</span>
            </template>

            <div class="space-y-2 max-h-[400px] overflow-y-auto pr-2">
              <div
                v-for="phong in selectedRooms"
                :key="phong.idPhong"
                class="border rounded-lg p-3 bg-blue-50 border-blue-200"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <h4 class="font-bold text-base">
                        {{ phong.tenPhong }}
                      </h4>
                      <n-tag size="small" type="success">
                        {{ phong.tenLoaiPhong }}
                      </n-tag>
                    </div>
                    <div class="text-gray-600 text-sm">
                      <span class="flex items-center gap-1">
                        <nova-icon icon="carbon:building" :size="14" />
                        Tầng {{ phong.tang }}
                      </span>
                    </div>
                  </div>
                  <div class="text-right ml-3">
                    <div class="text-lg font-bold text-blue-600">
                      {{ phong.gia.toLocaleString('vi-VN') }}
                    </div>
                    <div class="text-xs text-gray-500">VNĐ / đêm</div>
                  </div>
                </div>
              </div>
            </div>
          </n-card>
        </div>

        <div class="col-span-7 space-y-3">

          <n-card size="small" title="Thời gian lưu trú" :bordered="false">
            <n-form label-placement="left" :model="formData" label-width="120">
              <n-form-item label="Ngày nhận - trả" path="ngayNhan" required>
                <n-date-picker
                  v-model:value="formData.ngayNhan"
                  type="datetimerange"
                  start-placeholder="Ngày nhận"
                  end-placeholder="Ngày trả"
                  clearable
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item label="Số khách" path="soLuongKhach" required>
                <n-input-number
                  v-model:value="formData.soLuongKhach"
                  :min="1"
                  placeholder="Số khách"
                  style="width: 100%"
                />
              </n-form-item>

              <div v-if="formData.ngayNhan" class="bg-blue-50 p-3 rounded-lg">
                <div class="grid grid-cols-2 gap-2 text-sm">
                  <div>
                    <div class="text-gray-600">Thời gian lưu trú</div>
                    <div class="font-bold text-blue-600">{{ soNgayO }} đêm</div>
                  </div>
                  <div>
                    <div class="text-gray-600">Số khách</div>
                    <div class="font-semibold">{{ formData.soLuongKhach }} người</div>
                  </div>
                </div>
              </div>
            </n-form>
          </n-card>

          <n-card size="small" title="Thông tin khách hàng" :bordered="false">
            <n-form-item label="Tìm khách hàng">
              <n-select
                v-model:value="selectedKhachHang"
                filterable
                placeholder="Nhập tên, SĐT, CCCD hoặc Email..."
                :options="khachHangOptions.map(kh => ({
                  label: `${kh.hoTen} - ${kh.soDienThoai || kh.email}`,
                  value: kh.id,
                }))"
                :loading="isSearchingKH"
                clearable
                remote
                :clear-filter-after-select="false"
                @search="(val: string) => keywordKhachHang = val"
              />
            </n-form-item>

            <n-card v-if="selectedKhachHangInfo" size="small" class="mt-2 bg-gray-50 text-sm">
              <div class="grid grid-cols-2 gap-2">
                <div><div class="text-gray-600">Họ tên</div><div class="font-semibold">{{ selectedKhachHangInfo.hoTen }}</div></div>
                <div><div class="text-gray-600">SĐT</div><div class="font-semibold">{{ selectedKhachHangInfo.soDienThoai }}</div></div>
              </div>
            </n-card>
          </n-card>

          <!-- Tính tiền -->
          <n-card size="small" title="Thanh toán" :bordered="false" class="bg-gradient-to-r from-green-50 to-emerald-50">
            <div class="space-y-3">
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span>Tiền phòng ({{ selectedRooms.length }} phòng × {{ soNgayO }} đêm):</span>
                  <span class="font-semibold">{{ tongTien.toLocaleString('vi-VN') }} VNĐ</span>
                </div>

                <n-form-item label="Tiền khách trả">
                  <n-input-number
                    v-model:value="formData.tienKhachTra"
                    :min="0"
                    placeholder="Nhập số tiền"
                    style="width: 100%"
                    :format-value="(value: number) => value?.toLocaleString('vi-VN')"
                  />
                </n-form-item>

                <n-divider class="my-2" />

                <div v-if="formData.tienKhachTra !== null && formData.tienKhachTra !== undefined" class="space-y-1">
                  <div v-if="tienThua > 0" class="flex justify-between text-green-600 font-semibold">
                    <span>Tiền thừa trả khách:</span>
                    <span>{{ tienThua.toLocaleString('vi-VN') }} VNĐ</span>
                  </div>
                  <div v-if="congNo > 0" class="flex justify-between text-red-600 font-semibold">
                    <span>Công nợ:</span>
                    <span>{{ congNo.toLocaleString('vi-VN') }} VNĐ</span>
                  </div>
                </div>

                <div class="flex justify-between items-center pt-2 border-t-2 border-green-300">
                  <span class="text-base font-semibold">Tổng tiền:</span>
                  <span class="text-2xl font-bold text-green-600">
                    {{ tongTien.toLocaleString('vi-VN') }} VNĐ
                  </span>
                </div>
              </div>
            </div>
          </n-card>

          <!-- Ghi chú -->
          <n-card size="small" title="Thông tin bổ sung" :bordered="false">
            <n-form-item label="Ghi chú">
              <n-input
                v-model:value="formData.ghiChu"
                type="textarea"
                placeholder="Nhập ghi chú (tùy chọn)..."
                :rows="2"
                :maxlength="500"
                show-count
              />
            </n-form-item>

            <n-form-item>
              <n-checkbox v-model:checked="formData.nhanNgay" :disabled="!canNhanNgay()">
                <span class="text-sm">
                  <nova-icon icon="carbon:license-draft" class="mr-1" />
                  Nhận phòng ngay (Check-in)
                </span>
              </n-checkbox>
              <template #feedback>
                <n-text v-if="!canNhanNgay()" type="warning" class="text-xs">
                  Chỉ áp dụng khi còn tối đa 1 giờ trước check-in
                </n-text>
              </template>
            </n-form-item>
          </n-card>
        </div>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-button size="large" @click="closeModal">Hủy</n-button>
        <n-space>
          <n-button size="large" @click="handleLuuTam">
            <template #icon>
              <nova-icon icon="carbon:save" />
            </template>
            Lưu tạm
          </n-button>
          <n-button
            type="primary"
            size="large"
            :disabled="!selectedKhachHang || formData.tienKhachTra === null || formData.tienKhachTra === undefined"
            @click="handleTiepTuc"
          >
            <template #icon>
              <nova-icon icon="carbon:arrow-right" />
            </template>
            Tiếp tục
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1100px {
  width: 1100px;
  max-width: 95vw;
  max-height: 90vh;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 16px;
  font-weight: 600;
  padding: 10px 16px;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-button__content) {
  font-size: 14px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 12px;
}
</style>
