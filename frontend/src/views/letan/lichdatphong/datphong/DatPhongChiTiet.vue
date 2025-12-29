<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChonLoaiPhong, PhongDatResponse, TimKhachHangResponse } from '@/service/api/letan/lichdatphong/datphong'
import { createDatPhong, getPhongTheoLoai, searchKhachHang } from '@/service/api/letan/lichdatphong/datphong'
import { useDebounceFn } from '@vueuse/core'

interface Props {
  visible: boolean
  bookingData: {
    ngayNhan: number
    ngayTra: number
    soLuongKhach: number
    danhSachLoaiPhong: ChonLoaiPhong[]
  } | null
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const danhSachPhong = ref<PhongDatResponse[]>([])
const selectedPhongIds = ref<string[]>([])
const isLoading = ref(false)

const keywordKhachHang = ref('')
const khachHangOptions = ref<TimKhachHangResponse[]>([])
const selectedKhachHang = ref<string | null>(null)
const isSearchingKH = ref(false)

const formData = ref({
  ghiChu: '',
  nhanNgay: false,
})

const soNgayO = computed(() => {
  if (!props.bookingData)
    return 0
  return Math.ceil((props.bookingData.ngayTra - props.bookingData.ngayNhan) / (1000 * 60 * 60 * 24))
})

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

function closeModal() {
  modalVisible.value = false
  resetForm()
}

function resetForm() {
  danhSachPhong.value = []
  selectedPhongIds.value = []
  keywordKhachHang.value = ''
  khachHangOptions.value = []
  selectedKhachHang.value = null
  formData.value = {
    ghiChu: '',
    nhanNgay: false,
  }
}

async function loadDanhSachPhong() {
  if (!props.bookingData)
    return

  try {
    isLoading.value = true
    const data = await getPhongTheoLoai({
      ngayNhan: props.bookingData.ngayNhan,
      ngayTra: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachLoaiPhong: props.bookingData.danhSachLoaiPhong,
    })

    danhSachPhong.value = data
    selectedPhongIds.value = data.map(p => p.idPhong)
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tải danh sách phòng')
  }
  finally {
    isLoading.value = false
  }
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

function togglePhong(idPhong: string) {
  const index = selectedPhongIds.value.indexOf(idPhong)
  if (index > -1) {
    selectedPhongIds.value.splice(index, 1)
  }
  else {
    selectedPhongIds.value.push(idPhong)
  }
}

function canNhanNgay() {
  if (!props.bookingData)
    return false
  const now = Date.now()
  const oneHourBeforeCheckIn = props.bookingData.ngayNhan - (60 * 60 * 1000)
  return now >= oneHourBeforeCheckIn
}

async function handleSubmit() {
  if (!props.bookingData) {
    window.$message.warning('Dữ liệu đặt phòng không hợp lệ')
    return
  }

  if (!selectedKhachHang.value) {
    window.$message.warning('Vui lòng chọn khách hàng')
    return
  }

  if (selectedPhongIds.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  if (formData.value.nhanNgay && !canNhanNgay()) {
    window.$message.warning('Chỉ có thể nhận ngay khi còn tối đa 1 giờ trước thời gian check-in')
    return
  }

  try {
    isLoading.value = true
    await createDatPhong({
      ngayNhan: props.bookingData.ngayNhan,
      ngayTra: props.bookingData.ngayTra,
      idKhachHang: selectedKhachHang.value,
      ghiChu: formData.value.ghiChu || undefined,
      nhanNgay: formData.value.nhanNgay,
      danhSachIdPhong: selectedPhongIds.value,
    })

    window.$message.success('Đặt phòng thành công!')
    emit('success')
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể đặt phòng')
  }
  finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.bookingData) {
    loadDanhSachPhong()
  }
  else if (!val) {
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

const selectedPhongList = computed(() => {
  return danhSachPhong.value.filter(p => selectedPhongIds.value.includes(p.idPhong))
})

const tongTienPhong = computed(() => {
  return selectedPhongList.value.reduce((sum, p) => sum + p.gia, 0)
})

const tongTien = computed(() => {
  return tongTienPhong.value * soNgayO.value
})

function getTagColor(tag: any): string {
  return tag.mau || '#667eea'
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    title="Đặt phòng chi tiết"
    class="w-1100px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="space-y-4">
        <!-- Thông tin đặt phòng -->
        <n-card v-if="bookingData" size="small" title="Thông tin đặt phòng" :bordered="false" class="bg-blue-50">
          <div class="grid grid-cols-2 gap-4">
            <div>
              <div class="text-sm text-gray-600 mb-1">
                <nova-icon icon="carbon:calendar" class="mr-1" />
                Nhận phòng
              </div>
              <div class="font-semibold text-base">
                {{ formatDate(bookingData.ngayNhan) }}
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-600 mb-1">
                <nova-icon icon="carbon:calendar" class="mr-1" />
                Trả phòng
              </div>
              <div class="font-semibold text-base">
                {{ formatDate(bookingData.ngayTra) }}
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-600 mb-1">
                <nova-icon icon="carbon:user-multiple" class="mr-1" />
                Số khách
              </div>
              <div class="font-semibold text-base">
                {{ bookingData.soLuongKhach }} người
              </div>
            </div>
            <div>
              <div class="text-sm text-gray-600 mb-1">
                <nova-icon icon="carbon:time" class="mr-1" />
                Thời gian lưu trú
              </div>
              <div class="font-semibold text-base text-blue-600">
                {{ soNgayO }} đêm
              </div>
            </div>
          </div>
        </n-card>

        <!-- Thông tin khách hàng -->
        <n-card size="small" title="Thông tin khách hàng" :bordered="false">
          <n-form-item label="Tìm kiếm khách hàng">
            <n-select
              v-model:value="selectedKhachHang"
              filterable
              placeholder="Nhập tên, SĐT, CCCD hoặc Email để tìm..."
              :options="khachHangOptions.map(kh => ({
                label: `${kh.hoTen} - ${kh.soDienThoai || kh.email}`,
                value: kh.id,
              }))"
              :loading="isSearchingKH"
              clearable
              remote
              :clear-filter-after-select="false"
              @search="(val: string) => keywordKhachHang = val"
            >
              <template #empty>
                <div class="p-4 text-center text-gray-500">
                  Nhập từ khóa để tìm kiếm khách hàng
                </div>
              </template>
            </n-select>
          </n-form-item>

          <n-card v-if="selectedKhachHangInfo" size="small" class="mt-3 bg-gray-50">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <div class="text-sm text-gray-600">Họ tên</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.hoTen }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-600">Mã khách hàng</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.maNguoiDung }}</div>
              </div>
              <div>
                <div class="text-sm text-gray-600">Số điện thoại</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.soDienThoai }}</div>
              </div>
              <div v-if="selectedKhachHangInfo.email">
                <div class="text-sm text-gray-600">Email</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.email }}</div>
              </div>
              <div v-if="selectedKhachHangInfo.soCCCD">
                <div class="text-sm text-gray-600">CCCD/CMND</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.soCCCD }}</div>
              </div>
              <div v-if="selectedKhachHangInfo.quocTich">
                <div class="text-sm text-gray-600">Quốc tịch</div>
                <div class="font-semibold">{{ selectedKhachHangInfo.quocTich }}</div>
              </div>
            </div>
          </n-card>
        </n-card>

        <!-- Danh sách phòng -->
        <n-card size="small" :bordered="false">
          <template #header>
            <div class="flex justify-between items-center">
              <span>Danh sách phòng</span>
              <n-tag type="info" size="large">
                Đã chọn: {{ selectedPhongIds.length }}/{{ danhSachPhong.length }} phòng
              </n-tag>
            </div>
          </template>

          <div class="space-y-3 max-h-[400px] overflow-y-auto pr-2">
            <div
              v-for="phong in danhSachPhong"
              :key="phong.idPhong"
              class="border rounded-lg p-4 cursor-pointer transition-all"
              :class="[
                selectedPhongIds.includes(phong.idPhong)
                  ? 'border-blue-500 bg-blue-50 shadow-md'
                  : 'border-gray-200 hover:border-blue-300 hover:shadow-sm',
              ]"
              @click="togglePhong(phong.idPhong)"
            >
              <div class="flex justify-between items-start">
                <div class="flex items-start gap-3 flex-1">
                  <n-checkbox
                    :checked="selectedPhongIds.includes(phong.idPhong)"
                    @click.stop
                    @update:checked="() => togglePhong(phong.idPhong)"
                  />
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                      <h4 class="font-bold text-lg">
                        {{ phong.maPhong }} - {{ phong.tenPhong }}
                      </h4>
                      <n-tag size="small" type="success">
                        {{ phong.tenLoaiPhong }}
                      </n-tag>
                    </div>
                    <div class="text-base text-gray-600 space-y-1">
                      <div class="flex items-center gap-4">
                        <span class="flex items-center gap-1">
                          <nova-icon icon="carbon:building" :size="16" />
                          Tầng {{ phong.tang }}
                        </span>
                        <span class="flex items-center gap-1">
                          <nova-icon icon="carbon:user-multiple" :size="16" />
                          Sức chứa: {{ phong.sucChua }} người
                        </span>
                      </div>
                      <div v-if="phong.tags && phong.tags.length > 0" class="flex gap-2 flex-wrap mt-2">
                        <n-tag
                          v-for="tag in phong.tags"
                          :key="tag.id"
                          size="small"
                          round
                          :color="{ color: getTagColor(tag), textColor: '#fff', borderColor: getTagColor(tag) }"
                        >
                          {{ tag.ten }}
                        </n-tag>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="text-right ml-4">
                  <div class="text-xl font-bold text-blue-600 mb-1">
                    {{ phong.gia.toLocaleString('vi-VN') }}
                  </div>
                  <div class="text-sm text-gray-500">
                    VNĐ / đêm
                  </div>
                </div>
              </div>
            </div>
          </div>

          <n-divider />

          <!-- Tổng tiền -->
          <div class="bg-gradient-to-r from-green-50 to-emerald-50 p-4 rounded-lg border border-green-200">
            <div class="space-y-2">
              <div class="flex justify-between text-base">
                <span>Tổng tiền phòng ({{ selectedPhongIds.length }} phòng × 1 đêm):</span>
                <span class="font-semibold">{{ tongTienPhong.toLocaleString('vi-VN') }} VNĐ</span>
              </div>
              <div class="flex justify-between text-base">
                <span>Số đêm:</span>
                <span class="font-semibold">{{ soNgayO }} đêm</span>
              </div>
              <n-divider class="my-2" />
              <div class="flex justify-between items-center">
                <span class="text-lg font-semibold">Tổng cộng:</span>
                <span class="text-3xl font-bold text-green-600">
                  {{ tongTien.toLocaleString('vi-VN') }} VNĐ
                </span>
              </div>
            </div>
          </div>
        </n-card>

        <!-- Ghi chú và tùy chọn -->
        <n-card size="small" title="Thông tin bổ sung" :bordered="false">
          <n-form-item label="Ghi chú">
            <n-input
              v-model:value="formData.ghiChu"
              type="textarea"
              placeholder="Nhập ghi chú (tùy chọn)..."
              :rows="3"
              :maxlength="500"
              show-count
            />
          </n-form-item>

          <n-form-item>
            <n-checkbox v-model:checked="formData.nhanNgay" :disabled="!canNhanNgay()">
              <span class="text-base">
                <nova-icon icon="carbon:license-draft" class="mr-1" />
                Nhận phòng ngay (Check-in)
              </span>
            </n-checkbox>
            <template #feedback>
              <n-text v-if="!canNhanNgay()" type="warning" class="text-sm">
                Chỉ áp dụng khi còn tối đa 1 giờ trước thời gian check-in
              </n-text>
            </template>
          </n-form-item>
        </n-card>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-text class="text-base">
          <strong>{{ selectedPhongIds.length }}</strong> phòng đã chọn
        </n-text>
        <n-space>
          <n-button size="large" @click="closeModal">
            Hủy
          </n-button>
          <n-button
            type="primary"
            size="large"
            :disabled="!selectedKhachHang || selectedPhongIds.length === 0"
            @click="handleSubmit"
          >
            <template #icon>
              <nova-icon :icon="formData.nhanNgay ? 'carbon:license-draft' : 'carbon:calendar-add'" />
            </template>
            {{ formData.nhanNgay ? 'Nhận phòng ngay' : 'Đặt phòng trước' }}
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
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-button__content),
.modal-custom-font :deep(.n-base-selection-label),
.modal-custom-font :deep(p),
.modal-custom-font :deep(span),
.modal-custom-font :deep(div),
.modal-custom-font :deep(.n-text) {
  font-size: 17px;
}

/* Custom scrollbar */
:deep(.n-scrollbar-rail) {
  right: 0 !important;
}
</style>
