<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChonLoaiPhong, PhongDatResponse, TimKhachHangResponse, SavePhieuDatTamRequest, ConfirmBookingRequest } from '@/service/api/letan/booking'
import { getPhongTheoLoai, searchKhachHang, savePhieuDatTam, confirmBookingFromPhieuTam } from '@/service/api/letan/booking'
import { useDebounceFn } from '@vueuse/core'
import { useDataCombobox } from '@/store/dataCombox'

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

const { dataCombobox } = useDataCombobox()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

// Room data
const danhSachPhong = ref<PhongDatResponse[]>([])
const selectedPhongIds = ref<string[]>([])
const isLoading = ref(false)

// Customer data
const keywordKhachHang = ref('')
const khachHangOptions = ref<TimKhachHangResponse[]>([])
const selectedKhachHang = ref<string | null>(null)
const isSearchingKH = ref(false)

// Form data
const formData = ref({
  ghiChu: '',
  nhanNgay: false,
  tienKhachTra: null as number | null,
})

const sessionId = ref('')

// Computed
const soNgayO = computed(() => {
  if (!props.bookingData) return 0
  return Math.ceil((props.bookingData.ngayTra - props.bookingData.ngayNhan) / (1000 * 60 * 60 * 24))
})

const selectedPhongList = computed(() => {
  return danhSachPhong.value.filter(p => selectedPhongIds.value.includes(p.idPhong))
})

const tongTienPhong = computed(() => {
  return selectedPhongList.value.reduce((sum, p) => sum + p.gia, 0)
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

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

// Functions
function generateSessionId() {
  return `booking_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

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
    tienKhachTra: null,
  }
  sessionId.value = ''
}

function getLoaiPhongName(idLoaiPhong: string): string {
  const loaiPhongArray = dataCombobox.value?.loaiPhong
  if (!loaiPhongArray) return 'Không xác định'
  const loaiPhong = loaiPhongArray.find((lp: any) => lp.value === idLoaiPhong)
  return loaiPhong?.label || 'Không xác định'
}

async function loadDanhSachPhong() {
  if (!props.bookingData) return
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
    sessionId.value = generateSessionId()
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

function canNhanNgay() {
  if (!props.bookingData) return false
  const now = Date.now()
  const oneHourBeforeCheckIn = props.bookingData.ngayNhan - (60 * 60 * 1000)
  return now >= oneHourBeforeCheckIn
}

async function handleLuuTam() {
  if (!props.bookingData) {
    window.$message.warning('Dữ liệu đặt phòng không hợp lệ')
    return
  }
  if (selectedPhongIds.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  try {
    isLoading.value = true

    const phieuData: SavePhieuDatTamRequest = {
      sessionId: sessionId.value || undefined,
      checkInDate: props.bookingData.ngayNhan,
      checkOutDate: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachIdPhong: selectedPhongIds.value,
      idKhachHang: selectedKhachHang.value || null,
      ghiChu: formData.value.ghiChu || null,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || null,
      isFromRoomClick: false,
      currentStep: selectedKhachHang.value 
        ? (formData.value.tienKhachTra !== null ? 'READY_TO_CONFIRM' : 'PAYMENT_INFO')
        : 'CUSTOMER_INFO',
      roomDetails: selectedPhongList.value.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: soNgayO.value,
      })),
    }

    await savePhieuDatTam(phieuData)
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

async function handleDatPhong() {
  if (!props.bookingData) {
    window.$message.warning('Dữ liệu đặt phòng không hợp lệ')
    return
  }
  if (selectedPhongIds.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }
  if (!selectedKhachHang.value) {
    window.$message.warning('Vui lòng chọn khách hàng')
    return
  }
  if (formData.value.tienKhachTra === null || formData.value.tienKhachTra === undefined) {
    window.$message.warning('Vui lòng nhập số tiền khách trả')
    return
  }

  try {
    isLoading.value = true

    // Save temp first
    const phieuData: SavePhieuDatTamRequest = {
      sessionId: sessionId.value || undefined,
      checkInDate: props.bookingData.ngayNhan,
      checkOutDate: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachIdPhong: selectedPhongIds.value,
      idKhachHang: selectedKhachHang.value,
      ghiChu: formData.value.ghiChu || null,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra,
      isFromRoomClick: false,
      currentStep: 'READY_TO_CONFIRM',
      roomDetails: selectedPhongList.value.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: soNgayO.value,
      })),
    }

    const result = await savePhieuDatTam(phieuData)

    // Then confirm
    const confirmData: ConfirmBookingRequest = {
      sessionId: result.sessionId,
      idKhachHang: selectedKhachHang.value!,
      checkInDate: props.bookingData.ngayNhan,
      checkOutDate: props.bookingData.ngayTra,
      ghiChu: formData.value.ghiChu || undefined,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || undefined,
      danhSachIdPhong: selectedPhongIds.value,
    }
    await confirmBookingFromPhieuTam(confirmData)

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
  else {
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
    class="w-1200px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="grid grid-cols-12 gap-4">
        <!-- Left: Summary (35%) -->
        <div class="col-span-4 space-y-3">
          <!-- Booking Summary -->
          <n-card v-if="bookingData" size="small" title="📋 Chi tiết đặt phòng" :bordered="false" class="bg-blue-50 compact-card">
            <div class="space-y-2 text-xs">
              <div class="flex justify-between">
                <span class="text-gray-600">Nhận:</span>
                <span class="font-semibold">{{ formatDate(bookingData.ngayNhan) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Trả:</span>
                <span class="font-semibold">{{ formatDate(bookingData.ngayTra) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Lưu trú:</span>
                <span class="font-semibold text-blue-600">{{ soNgayO }} đêm</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Số khách:</span>
                <span class="font-semibold">{{ bookingData.soLuongKhach }} người</span>
              </div>
            </div>
          </n-card>

          <!-- Room Types Selected -->
          <n-card v-if="bookingData" size="small" title="🏨 Loại phòng đã chọn" :bordered="false" class="compact-card">
            <div class="space-y-1.5">
              <div
                v-for="(loai, index) in bookingData.danhSachLoaiPhong"
                :key="index"
                class="flex justify-between items-center text-xs py-1 border-b last:border-0"
              >
                <span class="font-medium">{{ getLoaiPhongName(loai.idLoaiPhong) }}</span>
                <span class="text-blue-600 font-semibold">{{ loai.soLuong }} phòng</span>
              </div>
              <div class="pt-2 mt-2 border-t-2 flex justify-between font-semibold text-sm">
                <span>Tổng số phòng:</span>
                <span class="text-green-600">{{ selectedPhongIds.length }} phòng</span>
              </div>
            </div>
          </n-card>

          <!-- Price Summary -->
          <div class="bg-gradient-to-r from-green-50 to-emerald-50 p-3 rounded-lg border border-green-200">
            <div class="space-y-1.5 text-xs">
              <div class="flex justify-between">
                <span>Tiền phòng ({{ selectedPhongIds.length }} × 1 đêm):</span>
                <span class="font-semibold">{{ tongTienPhong.toLocaleString('vi-VN') }} VNĐ</span>
              </div>
              <div class="flex justify-between">
                <span>Số đêm:</span>
                <span class="font-semibold">{{ soNgayO }} đêm</span>
              </div>
              <div class="pt-2 mt-2 border-t-2 border-green-300 flex justify-between items-center">
                <span class="text-sm font-semibold">TỔNG CỘNG:</span>
                <span class="text-xl font-bold text-green-600">
                  {{ tongTien.toLocaleString('vi-VN') }} VNĐ
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Right: Forms (65%) -->
        <div class="col-span-8 space-y-2.5">
          <!-- Customer Info -->
          <n-card size="small" title="👤 Thông tin khách hàng" :bordered="false" class="compact-card">
            <n-form-item label="Tìm khách hàng" class="!mb-2">
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

            <n-card v-if="selectedKhachHangInfo" size="small" class="mt-2 bg-gray-50 text-xs">
              <div class="grid grid-cols-2 gap-2">
                <div><div class="text-gray-600">Họ tên</div><div class="font-semibold">{{ selectedKhachHangInfo.hoTen }}</div></div>
                <div><div class="text-gray-600">SĐT</div><div class="font-semibold">{{ selectedKhachHangInfo.soDienThoai }}</div></div>
              </div>
            </n-card>
          </n-card>

          <!-- Payment -->
          <n-card size="small" title="💳 Thanh toán" :bordered="false" class="bg-yellow-50 compact-card">
            <div class="space-y-2">
              <n-form-item label="Số tiền khách trả" class="!mb-2">
                <n-input-number
                  v-model:value="formData.tienKhachTra"
                  :min="0"
                  placeholder="Nhập số tiền"
                  style="width: 100%"
                  size="small"
                  :format-value="(value: number) => value?.toLocaleString('vi-VN')"
                />
              </n-form-item>

              <div v-if="formData.tienKhachTra !== null && formData.tienKhachTra !== undefined" class="space-y-0.5 text-xs">
                <div v-if="tienThua > 0" class="flex justify-between text-green-600 font-semibold">
                  <span>Tiền thừa:</span>
                  <span>{{ tienThua.toLocaleString('vi-VN') }} VNĐ</span>
                </div>
                <div v-if="congNo > 0" class="flex justify-between text-red-600 font-semibold">
                  <span>Công nợ:</span>
                  <span>{{ congNo.toLocaleString('vi-VN') }} VNĐ</span>
                </div>
              </div>
            </div>
          </n-card>

          <!-- Additional Info -->
          <n-card size="small" title="📝 Thông tin bổ sung" :bordered="false" class="compact-card">
            <n-form-item label="Ghi chú" class="!mb-2">
              <n-input
                v-model:value="formData.ghiChu"
                type="textarea"
                placeholder="Nhập ghi chú (tùy chọn)..."
                :rows="1"
                size="small"
                :maxlength="500"
                show-count
              />
            </n-form-item>

            <n-form-item class="!mb-0">
              <n-checkbox v-model:checked="formData.nhanNgay" :disabled="!canNhanNgay()">
                <span class="text-xs">
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
            :disabled="!selectedKhachHang || formData.tienKhachTra === null || formData.tienKhachTra === undefined || selectedPhongIds.length === 0"
            @click="handleDatPhong"
          >
            <template #icon>
              <nova-icon icon="carbon:checkmark" />
            </template>
            Đặt phòng
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1200px {
  width: 1200px;
  max-width: 95vw;
}

.w-1200px :deep(.n-card__content) {
  max-height: 68vh;
  overflow-y: auto;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 13px;
  font-weight: 600;
  padding: 7px 12px;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-button__content) {
  font-size: 13px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 6px;
}

.compact-card :deep(.n-card__content) {
  padding: 8px 12px !important;
}

.compact-card :deep(.n-card-header) {
  padding: 5px 12px !important;
}
</style>
