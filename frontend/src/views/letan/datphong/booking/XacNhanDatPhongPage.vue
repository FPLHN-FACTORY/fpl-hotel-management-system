<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  useMessage, 
  NSpace, 
  NButton, 
  NSpin, 
  NTag, 
  NGrid, 
  NGridItem, 
  NSelect, 
  NInput, 
  NCheckbox, 
  NIcon,
  NRadio,
  NRadioGroup
} from 'naive-ui'
import { 
  getPhongTheoLoai, 
  searchKhachHang, 
  savePhieuDatTam, 
  confirmBookingFromPhieuTam,
  type ChonLoaiPhong,
  type PhongDatResponse,
  type TimKhachHangResponse,
  type ConfirmBookingRequest 
} from '@/service/api/letan/booking'
import { useDebounceFn } from '@vueuse/core'
import { useDataCombobox } from '@/store/dataCombox'
import { 
  Calendar,
  Time,
  UserAvatar
} from '@vicons/carbon'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { dataCombobox } = useDataCombobox()

// Data from query params
const bookingData = ref<{
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
} | null>(null)

const isLoading = ref(false)
const danhSachPhong = ref<PhongDatResponse[]>([])
const selectedPhongIds = ref<string[]>([])
const keywordKhachHang = ref('')
const khachHangOptions = ref<TimKhachHangResponse[]>([])
const selectedKhachHang = ref<string | null>(null)
const isSearchingKH = ref(false)
const sessionId = ref('')

const formData = ref({
  ghiChu: '',
  nhanNgay: false,
  tienKhachTra: null as number | null,
  hinhThucThanhToan: 'FULL', // FULL, DEPOSIT, LATER
  phuongThucThanhToan: 'CASH', // CASH, CARD, TRANSFER, E-WALLET
  isCheckXacNhan: false
})

// Initialize from route query
onMounted(() => {
  try {
    const query = route.query
    if (query.data) {
      bookingData.value = JSON.parse(query.data as string)
      loadDanhSachPhong()
    } else {
      message.error('Thiếu dữ liệu đặt phòng')
    }
  } catch (error) {
    message.error('Dữ liệu không hợp lệ')
  }
})

// Computed
const soNgayO = computed(() => {
  if (!bookingData.value) return 0
  return Math.ceil((bookingData.value.ngayTra - bookingData.value.ngayNhan) / (1000 * 60 * 60 * 24))
})

const selectedPhongList = computed(() => {
  return danhSachPhong.value.filter(p => selectedPhongIds.value.includes(p.idPhong))
})

const tongTienPhong = computed(() => {
  if (selectedPhongIds.value.length > 0) {
    return selectedPhongList.value.reduce((sum, p) => sum + p.gia, 0)
  }
  
  if (bookingData.value) {
    return bookingData.value.danhSachLoaiPhong.reduce((sum, item) => {
      const roomTypeName = item.tenLoaiPhong || getLoaiPhongName(item.idLoaiPhong)
      const roomOfType = danhSachPhong.value.find(p => p.tenLoaiPhong === roomTypeName)
      const price = item.gia || roomOfType?.gia || 0
      return sum + (price * item.soLuong)
    }, 0)
  }
  return 0
})

const tongTien = computed(() => {
  return tongTienPhong.value * soNgayO.value
})

const totalRoomsToDisplay = computed(() => {
  if (selectedPhongIds.value.length > 0) return selectedPhongIds.value.length
  if (bookingData.value) {
    return bookingData.value.danhSachLoaiPhong.reduce((sum, item) => sum + item.soLuong, 0)
  }
  return 0
})

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

// Functions
function generateSessionId() {
  return `booking_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

function getLoaiPhongName(idLoaiPhong: string): string {
  const loaiPhongArray = dataCombobox.loaiPhong
  if (!loaiPhongArray) return 'Không xác định'
  const loaiPhong = loaiPhongArray.find((lp: any) => String(lp.value) === String(idLoaiPhong))
  return String(loaiPhong?.label || 'Không xác định')
}

async function loadDanhSachPhong() {
  if (!bookingData.value) return
  try {
    isLoading.value = true
    const data = await getPhongTheoLoai({
      ngayNhan: bookingData.value.ngayNhan,
      ngayTra: bookingData.value.ngayTra,
      soLuongKhach: bookingData.value.soLuongKhach,
      danhSachLoaiPhong: bookingData.value.danhSachLoaiPhong,
    })
    danhSachPhong.value = data
    sessionId.value = generateSessionId()
  }
  catch (error: any) {
    message.error(error.message || 'Không thể tải danh sách phòng')
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
    message.error(error.message || 'Không thể tìm kiếm khách hàng')
  }
  finally {
    isSearchingKH.value = false
  }
}, 500)

watch(() => keywordKhachHang.value, () => {
  debouncedSearchKH()
})

async function handleDatPhong() {
  if (!bookingData.value || !selectedKhachHang.value || formData.value.tienKhachTra === null) {
      message.warning('Vui lòng điền đủ thông tin')
      return
  }

  try {
    isLoading.value = true
    const result = await savePhieuDatTam({
        sessionId: sessionId.value,
        checkInDate: bookingData.value.ngayNhan,
        checkOutDate: bookingData.value.ngayTra,
        soLuongKhach: bookingData.value.soLuongKhach,
        danhSachIdPhong: selectedPhongIds.value,
        idKhachHang: selectedKhachHang.value,
        ghiChu: formData.value.ghiChu,
        nhanNgay: formData.value.nhanNgay,
        tienKhachTra: formData.value.tienKhachTra,
        currentStep: 'READY_TO_CONFIRM',
        isFromRoomClick: false,
        roomDetails: selectedPhongList.value.map(p => ({
            idPhong: p.idPhong,
            maPhong: p.maPhong,
            tenPhong: p.tenPhong,
            tenLoaiPhong: p.tenLoaiPhong,
            tang: p.tang,
            gia: p.gia,
            soNgay: soNgayO.value,
        }))
    })

    const confirmData: ConfirmBookingRequest = {
      sessionId: result.sessionId,
      idKhachHang: selectedKhachHang.value!,
      checkInDate: bookingData.value.ngayNhan,
      checkOutDate: bookingData.value.ngayTra,
      ghiChu: formData.value.ghiChu || undefined,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || undefined,
      danhSachIdPhong: selectedPhongIds.value,
      danhSachLoaiPhong: selectedPhongIds.value.length === 0 ? bookingData.value.danhSachLoaiPhong : undefined,
    }
    await confirmBookingFromPhieuTam(confirmData)

    message.success('Đặt phòng thành công!')
    router.push({ name: 'phieuDatPhong' })
  }
  catch (error: any) {
    message.error(error.message || 'Không thể đặt phòng')
  }
  finally {
    isLoading.value = false
  }
}

function formatDate(timestamp: number) {
  return new Date(timestamp).toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

const formatCurrency = (val: number) => val.toLocaleString('vi-VN') + ' VNĐ'

</script>

<template>
  <div class="xac-nhan-page bg-slate-50 flex flex-col p-3">
    <n-spin :show="isLoading" class="flex-1 min-h-0">
      <div v-if="bookingData" class="flex flex-col h-full space-y-3">
        <!-- Slim Top Bar -->
        <div class="flex items-center justify-between bg-white px-4 h-12 rounded-xl shadow-sm border border-slate-200 flex-none">
            <div class="flex items-center gap-6">
                <div class="flex items-center gap-2">
                    <n-icon :size="16" :component="Calendar" color="#3b82f6" />
                    <span class="text-[11px] text-slate-400 font-bold uppercase">Check-in:</span>
                    <span class="text-[12px] font-black text-slate-700">{{ formatDate(bookingData.ngayNhan) }}</span>
                </div>
                
                <div class="h-4 w-px bg-slate-100"></div>

                <div class="flex items-center gap-2">
                    <n-icon :size="16" :component="Calendar" color="#f43f5e" />
                    <span class="text-[11px] text-slate-400 font-bold uppercase">Check-out:</span>
                    <span class="text-[12px] font-black text-slate-700">{{ formatDate(bookingData.ngayTra) }}</span>
                </div>

                <div class="h-4 w-px bg-slate-100"></div>

                <div class="flex items-center gap-2">
                    <n-icon :size="16" :component="Time" color="#10b981" />
                    <span class="text-[12px] font-black text-emerald-600">{{ soNgayO }} đêm</span>
                </div>

                <div class="h-4 w-px bg-slate-100"></div>

                <div class="flex items-center gap-2">
                    <n-icon :size="16" :component="UserAvatar" color="#6366f1" />
                    <span class="text-[12px] font-black text-slate-700">{{ bookingData.soLuongKhach }} khách</span>
                </div>
            </div>

            <div class="flex items-center gap-4">
                <n-tag size="small" type="warning" :bordered="false" class="font-bold text-[10px]">PENDING</n-tag>
            </div>
        </div>

        <div class="flex-1 min-h-0 flex flex-col gap-3 overflow-hidden">
          <!-- Row 1: Rooms | Customer | Notes (3-column split) -->
          <div class="flex-1 min-h-0 grid grid-cols-3 gap-3">
             <!-- Room Selection -->
             <div class="bg-white rounded-xl shadow-sm border border-slate-200 flex flex-col overflow-hidden min-h-0">
                <div class="p-2 border-b border-slate-100 flex items-center gap-2 flex-none">
                    <div class="w-1 h-3 bg-blue-500 rounded-full"></div>
                    <span class="font-black text-[11px] text-slate-800 uppercase tracking-tight">Danh sách phòng</span>
                </div>
                <n-scrollbar content-style="padding: 8px;">
                    <div class="space-y-1.5">
                        <div v-for="(loai, index) in bookingData.danhSachLoaiPhong" :key="index" 
                            class="p-2 bg-slate-50/50 rounded-lg border border-slate-100 flex justify-between items-center">
                            <div class="min-w-0">
                                <div class="font-bold text-[10px] text-slate-800 truncate">{{ loai.tenLoaiPhong || getLoaiPhongName(loai.idLoaiPhong) }}</div>
                                <div class="text-[9px] text-slate-400">
                                    <span class="font-bold text-slate-500">{{ loai.soLuong }}P</span> × {{ formatCurrency(loai.gia || 0) }}
                                </div>
                            </div>
                            <div class="px-2 py-1 bg-blue-50 rounded text-[11px] font-black text-blue-600 ml-1 border border-blue-100/50">
                                {{ formatCurrency((loai.gia || 0) * loai.soLuong * soNgayO) }}
                            </div>
                        </div>
                    </div>
                </n-scrollbar>
             </div>

             <!-- Customer Info -->
             <div class="bg-white rounded-xl shadow-sm border border-slate-200 flex flex-col overflow-hidden min-h-0">
                <div class="p-2 border-b border-slate-100 flex items-center gap-2 flex-none">
                    <div class="w-1 h-3 bg-indigo-500 rounded-full"></div>
                    <span class="font-black text-[11px] text-slate-800 uppercase tracking-tight">Khách hàng</span>
                </div>
                <div class="p-2.5 flex flex-col gap-2.5 flex-1 min-h-0">
                    <div class="flex gap-1.5 flex-none">
                        <n-select
                            v-model:value="selectedKhachHang"
                            filterable
                            placeholder="SĐT, Tên..."
                            :options="khachHangOptions.map(kh => ({
                                label: `${kh.hoTen} - ${kh.soDienThoai}`,
                                value: kh.id,
                            }))"
                            :loading="isSearchingKH"
                            clearable
                            remote
                            size="small"
                            class="flex-1 custom-select"
                            @search="(val: string) => keywordKhachHang = val"
                        />
                        <n-button type="primary" size="small" class="font-bold">Tìm</n-button>
                    </div>
                    
                    <div v-if="selectedKhachHangInfo" class="p-2 bg-indigo-50/40 rounded-lg border border-indigo-100 flex-1 overflow-auto">
                        <div class="grid grid-cols-2 gap-x-3 gap-y-2">
                            <div>
                                <div class="text-[7px] text-indigo-400 font-bold uppercase mb-0.5">Họ tên</div>
                                <div class="font-black text-slate-800 text-[10px] truncate">{{ selectedKhachHangInfo.hoTen }}</div>
                            </div>
                            <div>
                                <div class="text-[7px] text-indigo-400 font-bold uppercase mb-0.5">SĐT</div>
                                <div class="font-black text-slate-800 text-[10px]">{{ selectedKhachHangInfo.soDienThoai || 'N/A' }}</div>
                            </div>
                            <div class="col-span-2">
                                <div class="text-[7px] text-indigo-400 font-bold uppercase mb-0.5">Email</div>
                                <div class="font-bold text-slate-600 text-[9px] truncate">{{ selectedKhachHangInfo.email || 'N/A' }}</div>
                            </div>
                        </div>
                    </div>
                    <div v-else class="flex-1 flex flex-col items-center justify-center bg-slate-50/50 rounded-lg border border-dashed border-slate-200">
                        <span class="text-[8px] font-bold uppercase tracking-widest text-slate-300">Chưa chọn khách</span>
                    </div>
                </div>
             </div>

             <!-- Quick Note & Stats -->
             <div class="bg-white rounded-xl shadow-sm border border-slate-200 flex flex-col overflow-hidden min-h-0">
                <div class="p-2 border-b border-slate-100 flex items-center justify-between flex-none">
                    <div class="flex items-center gap-2">
                        <div class="w-1 h-3 bg-amber-500 rounded-full"></div>
                        <span class="font-black text-[11px] text-slate-800 uppercase tracking-tight">Ghi chú & Thống kê</span>
                    </div>
                    <div class="text-[9px] font-black text-slate-400 bg-slate-50 px-1.5 py-0.5 rounded">
                        LẬP: {{ new Date().toLocaleDateString('vi-VN') }}
                    </div>
                </div>
                <div class="p-2.5 flex flex-col gap-3 flex-1">
                    <n-input
                        v-model:value="formData.ghiChu"
                        type="textarea"
                        placeholder="Nhập ghi chú nhanh..."
                        :rows="3"
                        size="small"
                        class="flex-1"
                    />
                    <div class="bg-slate-50 p-2 rounded-lg border border-slate-100 flex justify-between items-center text-[10px]">
                        <span class="text-slate-400 font-bold">NHÂN VIÊN:</span>
                        <span class="font-black text-slate-700">Lễ tân hệ thống</span>
                    </div>
                </div>
             </div>
          </div>

          <!-- Row 2: Horizontal Payment Area -->
          <div class="bg-white rounded-xl shadow-md border border-slate-200 overflow-hidden flex-none">
              <div class="flex min-h-[185px]">
                  <!-- Payment Options (Left - 65%) -->
                  <div class="w-[65%] border-r border-slate-100 p-3.5 space-y-3.5">
                      <div class="flex items-center gap-2 mb-1">
                          <div class="w-1 h-3.5 bg-emerald-500 rounded-full"></div>
                          <span class="font-black text-[12px] text-slate-800 uppercase tracking-tight">Phương thức thanh toán</span>
                      </div>
                      
                      <div class="grid grid-cols-2 gap-3">
                          <div class="bg-slate-50/80 p-2.5 rounded-lg border border-slate-100">
                              <div class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-2">Hình thức</div>
                              <n-radio-group v-model:value="formData.hinhThucThanhToan" size="small">
                                  <n-space :size="24">
                                      <n-radio value="FULL"><span class="text-[11px] font-bold">Toàn bộ</span></n-radio>
                                      <n-radio value="DEPOSIT"><span class="text-[11px] font-bold">Đặt cọc</span></n-radio>
                                      <n-radio value="LATER"><span class="text-[11px] font-bold">Thanh toán sau</span></n-radio>
                                  </n-space>
                              </n-radio-group>
                          </div>

                          <div class="bg-slate-50/80 p-2.5 rounded-lg border border-slate-100">
                              <div class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-2">Phương thức</div>
                              <n-radio-group v-model:value="formData.phuongThucThanhToan" size="small">
                                  <n-grid :cols="2" :x-gap="12" :y-gap="6">
                                      <n-grid-item><n-radio value="CASH"><span class="text-[11px] font-bold">Tiền mặt</span></n-radio></n-grid-item>
                                      <n-grid-item><n-radio value="TRANSFER"><span class="text-[11px] font-bold">Chuyển khoản</span></n-radio></n-grid-item>
                                      <n-grid-item><n-radio value="CARD"><span class="text-[11px] font-bold">Thẻ / POS</span></n-radio></n-grid-item>
                                      <n-grid-item><n-radio value="E-WALLET"><span class="text-[11px] font-bold">Ví điện tử</span></n-radio></n-grid-item>
                                  </n-grid>
                              </n-radio-group>
                          </div>
                      </div>
                  </div>

                  <!-- Checkout Summary (Right - 35%) -->
                  <div class="w-[35%] bg-blue-50/50 p-3.5 flex flex-col justify-between border-l border-blue-100">
                      <div class="space-y-1 text-slate-500 text-[11px]">
                          <div class="flex justify-between items-center">
                              <span class="text-slate-400">Tiền phòng ({{ totalRoomsToDisplay }} phòng):</span>
                              <span class="font-black text-slate-800">{{ formatCurrency(tongTien) }}</span>
                          </div>
                          <div class="flex justify-between items-center">
                              <span>Số đêm ở:</span>
                              <span class="font-bold text-slate-700">{{ soNgayO }} đêm</span>
                          </div>
                          <div v-if="formData.hinhThucThanhToan === 'DEPOSIT'" class="flex flex-col gap-1 pt-2 border-t border-blue-100">
                              <span class="text-emerald-600 text-[10px] font-black uppercase tracking-wider">Khách trả trước:</span>
                              <n-input-number 
                                v-model:value="formData.tienKhachTra" 
                                size="small" 
                                :min="0" 
                                class="w-full custom-light-input" 
                                placeholder="Nhập số tiền..."
                                :show-button="false"
                              >
                                <template #suffix>
                                   <span class="text-[10px] text-slate-400 font-bold">VNĐ</span>
                                </template>
                              </n-input-number>
                          </div>
                      </div>

                      <div class="space-y-3">
                           <div class="flex justify-between items-center border-t border-blue-200 pt-3">
                              <div class="flex flex-col">
                                  <span class="text-[9px] font-black text-slate-400 uppercase tracking-tight mb-1">Tổng cộng thanh toán</span>
                                  <span class="text-3xl font-black text-blue-600 leading-none tracking-tighter">{{ formatCurrency(tongTien) }}</span>
                              </div>
                              <div class="flex flex-col items-center gap-1 bg-white p-1.5 rounded-lg border border-blue-200 shadow-sm">
                                <n-checkbox v-model:checked="formData.isCheckXacNhan" size="small" />
                                <span class="text-[8px] font-black text-blue-600 uppercase">Xác nhận</span>
                              </div>
                          </div>

                          <div class="grid grid-cols-2 gap-2">
                              <n-button size="small" ghost type="error" class="font-bold h-9" @click="router.back()">HỦY</n-button>
                              <n-button 
                                  type="primary" 
                                  size="small" 
                                  color="#2563eb"
                                  class="font-black h-9"
                                  :disabled="!formData.isCheckXacNhan || !selectedKhachHang"
                                  :loading="isLoading"
                                  @click="handleDatPhong"
                              >
                                  XÁC NHẬN ĐẶT
                              </n-button>
                          </div>
                      </div>
                  </div>
              </div>
          </div>
        </div>
      </div>
    </n-spin>
  </div>
</template>

<style scoped>
.xac-nhan-page {
  font-family: 'Inter', sans-serif;
  height: 100vh;
  letter-spacing: -0.02em;
}

:deep(.n-spin-content) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.n-button) {
  border-radius: 8px;
}

:deep(.n-input), :deep(.n-select .n-base-selection) {
  border-radius: 8px !important;
  --n-border-radius: 8px !important;
}

:deep(.custom-select .n-base-selection) {
    background-color: #f8fafc !important;
}

:deep(.n-radio-group) {
    display: flex;
}

:deep(.n-radio.n-radio--checked .n-radio__dot) {
    box-shadow: inset 0 0 0 0.125em #fff, 0 0 0 0.125em #3b82f6;
}

:deep(.n-scrollbar .n-scrollbar-content) {
    height: 100%;
}
:deep(.custom-light-input .n-input) {
    background-color: #fff !important;
    --n-text-color: #1e40af !important;
    --n-border: 1px solid #bfdbfe !important;
    --n-border-hover: 1px solid #3b82f6 !important;
    --n-border-focus: 1px solid #3b82f6 !important;
    --n-placeholder-color: #94a3b8 !important;
    font-weight: 900 !important;
}

:deep(.custom-dark-input .n-input__suffix) {
    margin-right: 8px;
}
</style>
