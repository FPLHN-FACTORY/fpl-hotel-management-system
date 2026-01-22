<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { SavePhieuDatTamRequest, TimKhachHangResponse } from '@/service/api/letan/booking'
import { savePhieuDatTam, searchKhachHang, getPhieuDatTam } from '@/service/api/letan/booking'
import type { SoDoPhongResponse } from '@/service/api/letan/sodophong'
import { getSoDoPhong } from '@/service/api/letan/sodophong'
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
const availableRooms = ref<SoDoPhongResponse[]>([])
const showRoomSelectionModal = ref(false)
const tempSelectedRooms = ref<typeof props.selectedRooms>([])

const soNgayO = computed(() => {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1])
    return 0
  return Math.ceil((formData.value.ngayNhan[1] - formData.value.ngayNhan[0]) / (1000 * 60 * 60 * 24))
})

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

const allSelectedRooms = computed(() => {
  return tempSelectedRooms.value
})

const tongTienPhong = computed(() => {
  return allSelectedRooms.value.reduce((sum, p) => sum + p.gia, 0)
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
  availableRooms.value = []
  tempSelectedRooms.value = []
  showRoomSelectionModal.value = false
}

function openRoomSelection() {
  showRoomSelectionModal.value = true
}

function removeRoom(roomId: string) {
  tempSelectedRooms.value = tempSelectedRooms.value.filter(r => r.idPhong !== roomId)
}

function addRoom(room: SoDoPhongResponse) {
  const exists = tempSelectedRooms.value.some(r => r.idPhong === room.id)
  if (!exists) {
    tempSelectedRooms.value.push({
      idPhong: room.id,
      maPhong: room.ma,
      tenPhong: room.ten,
      tenLoaiPhong: room.loaiPhong,
      tang: room.tang,
      gia: room.price || 0
    })
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

  if (allSelectedRooms.value.length === 0) {
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

  if (allSelectedRooms.value.length === 0) {
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
      danhSachIdPhong: allSelectedRooms.value.map(r => r.idPhong),
      isFromRoomClick: true,
      currentStep: 'READY_TO_CONFIRM',
      roomDetails: allSelectedRooms.value.map(r => ({
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
    tempSelectedRooms.value = [...props.selectedRooms]
    if (props.sessionId) {
      loadPhieuDatTam()
    }
    loadSimilarRooms()
  } else {
    resetForm()
  }
})

async function loadSimilarRooms() {
  if (props.selectedRooms.length === 0) return

  try {
    const response = await getSoDoPhong()
    const firstRoom = props.selectedRooms[0]
    availableRooms.value = response.filter((room: SoDoPhongResponse) =>
      room.loaiPhong === firstRoom.tenLoaiPhong &&
      room.trangThaiPhong === 'TRONG' &&
      !tempSelectedRooms.value.some(r => r.idPhong === room.id)
    )
  } catch (error: any) {
    console.error('Cannot load similar rooms:', error)
  }
}

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
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card" title="Đặt phòng"
    class="modal-custom-font compact-modal" :segmented="{ content: true, action: true }">
    <n-spin :show="isLoading">
      <div class="grid-container relative">
        <!-- Vertical Divider -->
        <div class="absolute left-1/2 top-0 bottom-0 w-px bg-gray-200"></div>

        <!-- Column 1: Rooms, Notes -->
        <div class="flex flex-col gap-2 h-full justify-between">
          <!-- Rooms - Expanded height -->
          <n-card size="small" :bordered="true" class="compact-card flex-1 flex flex-col"
            content-style="padding: 0; display: flex; flex-direction: column;"
            style="border: 2px solid #e5e7eb; min-height: 160px;">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="text-base font-semibold">🏨 Phòng đã chọn ({{ allSelectedRooms.length }})</span>
                <n-button size="small" type="primary" @click="openRoomSelection">
                  <template #icon>
                    <nova-icon icon="carbon:add" :size="16" />
                  </template>
                </n-button>
              </div>
            </template>

            <div class="space-y-2 p-2 overflow-y-auto bg-gray-50 flex-1">
              <div v-for="phong in allSelectedRooms" :key="phong.idPhong"
                class="border rounded-md p-2 bg-white border-blue-200 cursor-pointer hover:border-red-400 transition-all shadow-sm"
                @click="removeRoom(phong.idPhong)">
                <div class="flex justify-between items-center gap-2">
                  <div class="flex-1 min-w-0">
                    <div class="font-bold text-sm truncate">{{ phong.maPhong }}</div>
                    <div class="text-xs text-gray-500 flex items-center gap-1">
                      {{ phong.tenLoaiPhong }} - T{{ phong.tang }}
                    </div>
                  </div>
                  <div class="text-right">
                    <div class="text-sm font-bold text-blue-600">
                      {{ (phong.gia / 1000).toFixed(0) }}k
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="allSelectedRooms.length === 0" class="text-center text-gray-400 py-10">
                <nova-icon icon="carbon:hotel" :size="32" class="opacity-30" />
                <p class="mt-1 text-xs">Chưa chọn phòng</p>
              </div>
            </div>
          </n-card>

          <!-- Notes -->
          <n-card size="small" :bordered="true" class="compact-card flex-shrink-0" style="border: 2px solid #e5e7eb;">
            <template #header>
              <span class="text-base font-semibold">📝 Ghi chú</span>
            </template>
            <n-input v-model:value="formData.ghiChu" type="textarea" size="small" :rows="2" :maxlength="300"
              placeholder="Ghi chú..." />
            <div class="mt-2">
              <n-checkbox v-model:checked="formData.nhanNgay" :disabled="!canNhanNgay()" size="small">
                <span class="text-sm">Nhận phòng ngay</span>
              </n-checkbox>
            </div>
          </n-card>
        </div>

        <!-- Column 2: Time, Customer, Payment -->
        <div class="flex flex-col gap-2 h-full">
          <!-- Time Duration (Moved here) -->
          <n-card size="small" :bordered="true" class="compact-card flex-shrink-0" style="border: 2px solid #e5e7eb;">
            <template #header>
              <span class="text-base font-semibold">📅 Thời gian</span>
            </template>
            <n-form :model="formData" size="medium" label-placement="left" :show-feedback="false">
              <div class="mb-2">
                <n-date-picker v-model:value="formData.ngayNhan" type="datetimerange" size="small"
                  style="width: 100%" />
              </div>

              <div class="bg-blue-50 p-2 rounded text-sm border border-blue-100 flex justify-between items-center">
                <span class="text-gray-700 font-medium">{{ soNgayO }} đêm</span>
                <div class="flex items-center gap-2">
                  <span class="text-xs text-gray-400 mr-1">Khách:</span>
                  <n-input-number v-model:value="formData.soLuongKhach" size="tiny" :min="1" style="width: 60px" />
                </div>
              </div>
            </n-form>
          </n-card>

          <!-- Customer -->
          <n-card size="small" :bordered="true" class="compact-card flex-shrink-0" style="border: 2px solid #e5e7eb;">
            <template #header>
              <span class="text-base font-semibold">👤 Khách hàng</span>
            </template>
            <div class="mb-2">
              <n-select v-model:value="selectedKhachHang" filterable size="small" placeholder="Tìm khách hàng..."
                :options="khachHangOptions.map(kh => ({
                  label: `${kh.hoTen} - ${kh.soDienThoai || kh.email}`,
                  value: kh.id,
                }))" :loading="isSearchingKH" clearable remote :consistent-menu-width="true" class="w-full"
                placement="bottom-start" @search="(val: string) => keywordKhachHang = val" />
            </div>

            <div v-if="selectedKhachHangInfo" class="bg-indigo-50 p-2 rounded border border-indigo-100 mb-2">
              <div class="text-sm font-bold text-gray-800">{{ selectedKhachHangInfo.hoTen }}</div>
              <div class="text-xs text-gray-600 flex items-center gap-1">
                <nova-icon icon="carbon:phone" :size="12" />
                {{ selectedKhachHangInfo.soDienThoai }}
              </div>
            </div>
          </n-card>

          <!-- Payment -->
          <n-card size="small" :bordered="true" class="compact-card bg-green-50 flex-1 flex flex-col"
            style="border: 2px solid #86efac;">
            <template #header>
              <span class="text-base font-semibold">💳 Thanh toán</span>
            </template>

            <div class="flex flex-col h-full gap-2">
              <div class="bg-white rounded p-2 border border-green-200 text-sm space-y-1">
                <div class="flex justify-between">
                  <span class="text-gray-600">Phòng:</span>
                  <span class="font-bold">{{ allSelectedRooms.length }}</span>
                </div>
                <!-- <div class="flex justify-between">
                  <span class="text-gray-600">Đêm:</span>
                  <span class="font-bold">{{ soNgayO }}</span>
                </div> -->
                <div class="flex justify-between border-t border-dashed pt-1 mt-1">
                  <span class="text-gray-600">Đơn giá:</span>
                  <span class="font-bold text-green-700">{{ (tongTienPhong / 1000).toFixed(0) }}k/đêm</span>
                </div>
              </div>

              <div class="mt-0">
                <div class="text-xs font-semibold mb-1 text-gray-600">Khách trả (VNĐ):</div>
                <n-input-number v-model:value="formData.tienKhachTra" :min="0" size="small" placeholder="0"
                  style="width: 100%" :show-button="false">
                  <template #suffix>₫</template>
                </n-input-number>
              </div>

              <div v-if="formData.tienKhachTra !== null" class="mt-1 space-y-1 text-xs">
                <div v-if="tienThua > 0" class="flex justify-between text-green-700">
                  <span>Thừa:</span>
                  <span class="font-bold">{{ tienThua.toLocaleString('vi-VN') }}</span>
                </div>
                <div v-if="congNo > 0" class="flex justify-between text-red-600">
                  <span>Thiếu:</span>
                  <span class="font-bold">{{ congNo.toLocaleString('vi-VN') }}</span>
                </div>
              </div>

              <div class="mt-auto pt-2 border-t border-green-300">
                <div class="flex justify-between items-center">
                  <span class="text-sm font-bold text-gray-700">TỔNG:</span>
                  <span class="text-xl font-bold text-green-700">
                    {{ tongTien.toLocaleString('vi-VN') }}
                  </span>
                </div>
              </div>
            </div>
          </n-card>
        </div>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%; gap: 16px;">
        <n-button size="large" @click="closeModal">Hủy</n-button>
        <n-space style="gap: 12px;">
          <n-button size="large" @click="handleLuuTam">
            <template #icon>
              <nova-icon icon="carbon:save" />
            </template>
            Lưu tạm
          </n-button>
          <n-button type="primary" size="large"
            :disabled="!selectedKhachHang || formData.tienKhachTra === null || formData.tienKhachTra === undefined"
            @click="handleTiepTuc">
            <template #icon>
              <nova-icon icon="carbon:arrow-right" />
            </template>
            Tiếp tục
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>

  <!-- Room Selection Modal -->
  <n-modal v-model:show="showRoomSelectionModal" preset="card" title="Chọn phòng cùng loại" class="w-800px"
    :segmented="{ content: true, action: true }">
    <div v-if="availableRooms.length > 0" class="space-y-2 max-h-[400px] overflow-y-auto">
      <div v-for="room in availableRooms" :key="room.id"
        class="border rounded-lg p-3 cursor-pointer transition-all hover:border-blue-400 hover:shadow-sm"
        :class="tempSelectedRooms.some(r => r.idPhong === room.id) ? 'bg-green-50 border-green-500' : 'border-gray-200'"
        @click="addRoom(room)">
        <div class="flex justify-between items-center">
          <div class="flex-1">
            <div class="font-bold text-base mb-1">{{ room.ma }} - {{ room.ten }}</div>
            <div class="text-gray-600 text-sm flex items-center gap-3">
              <span class="flex items-center gap-1">
                <nova-icon icon="carbon:building" :size="14" />
                Tầng {{ room.tang }}
              </span>
              <n-tag size="small" type="success">{{ room.loaiPhong }}</n-tag>
              <span v-if="tempSelectedRooms.some(r => r.idPhong === room.id)"
                class="text-green-600 flex items-center gap-1">
                <nova-icon icon="carbon:checkmark" :size="14" />
                Đã chọn
              </span>
            </div>
          </div>
          <div class="text-right ml-3">
            <div class="text-lg font-bold text-blue-600">
              {{ (room.price || 0).toLocaleString('vi-VN') }}
            </div>
            <div class="text-xs text-gray-500">VNĐ/đêm</div>
          </div>
        </div>
      </div>
    </div>
    <n-empty v-else description="Không có phòng cùng loại" class="py-8" />

    <template #action>
      <n-space justify="end">
        <n-button @click="showRoomSelectionModal = false">Đóng</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style>
.modal-custom-font {
  width: 800px !important;
  max-width: 95vw !important;
}

.w-800px {
  width: 800px;
  max-width: 90vw;
}
</style>

<style scoped>
/* internal styles scoped */
.modal-custom-font :deep(.n-card-header) {
  font-size: 15px;
  padding: 8px 12px;
}

.modal-custom-font :deep(.n-card__content) {
  padding: 8px 12px;
}

.modal-custom-font :deep(.n-form-item-label) {
  font-size: 13px;
  font-weight: 600;
}

.modal-custom-font :deep(.n-button) {
  font-size: 14px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 6px;
  /* Compact spacing */
}

/* Card styling improvements */
.compact-card {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 0px !important;
}

/* No main scrollbar - internal containers scroll */
.grid-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  height: 570px;
  /* Fixed safe height */
  border-radius: 4px;
  overflow: hidden;
  /* Hide main scrollbar */
  padding: 2px;
}
</style>
