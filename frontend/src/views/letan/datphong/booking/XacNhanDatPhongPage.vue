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
  NRadioGroup,
  NInputNumber,
  NCard,
  NScrollbar,
  NAlert
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
  UserAvatar,
  Home,
  Currency,
  User
} from '@vicons/carbon'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const { dataCombobox } = useDataCombobox()

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
  hinhThucThanhToan: 'FULL',
  phuongThucThanhToan: 'CASH',
  isCheckXacNhan: false,
})

onMounted(() => {
  try {
    const query = route.query
    if (query.data) {
      bookingData.value = JSON.parse(query.data as string)
      // Nếu có số lượng khách từ bookingData thì đồng bộ, không thì để mặc định = 2
        bookingData.value.soLuongKhach

      loadDanhSachPhong()
    } else {
      message.error('Thiếu dữ liệu đặt phòng')
    }
  } catch (error) {
    message.error('Dữ liệu không hợp lệ')
  }
})

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

const isPaymentValid = computed(() => {
  if (!selectedKhachHang.value || !formData.value.isCheckXacNhan) return false

  // Kiểm tra số lượng khách
// isPaymentValid
if (!bookingData.value?.soLuongKhach || bookingData.value.soLuongKhach <= 0) return false

  if (formData.value.hinhThucThanhToan === 'FULL') {
    return formData.value.tienKhachTra !== null && formData.value.tienKhachTra >= tongTien.value
  } else if (formData.value.hinhThucThanhToan === 'DEPOSIT') {
    return formData.value.tienKhachTra !== null && formData.value.tienKhachTra > 0 && formData.value.tienKhachTra <= tongTien.value
  } else if (formData.value.hinhThucThanhToan === 'LATER') {
    return true
  }
  return false
})

const tienConLai = computed(() => {
  if (formData.value.hinhThucThanhToan === 'DEPOSIT' && formData.value.tienKhachTra) {
    return tongTien.value - formData.value.tienKhachTra
  }
  return 0
})

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

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
  if (!bookingData.value || !selectedKhachHang.value) {
    message.warning('Vui lòng chọn khách hàng')
    return
  }

  // Validate số lượng khách
  if (!bookingData.value?.soLuongKhach || bookingData.value.soLuongKhach <= 0) {
    message.warning('Vui lòng nhập số lượng khách')
    return
  }


  // Validate payment amount based on payment type
  if (formData.value.hinhThucThanhToan === 'FULL') {
    if (formData.value.tienKhachTra === null || formData.value.tienKhachTra < tongTien.value) {
      message.warning('Số tiền thanh toán phải bằng tổng tiền phòng')
      return
    }
  } else if (formData.value.hinhThucThanhToan === 'DEPOSIT') {
    if (formData.value.tienKhachTra === null || formData.value.tienKhachTra <= 0) {
      message.warning('Vui lòng nhập số tiền đặt cọc')
      return
    }
    if (formData.value.tienKhachTra > tongTien.value) {
      message.warning('Số tiền đặt cọc không được vượt quá tổng tiền')
      return
    }
  } else if (formData.value.hinhThucThanhToan === 'LATER') {
    formData.value.tienKhachTra = 0
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
      soLuongKhach: bookingData.value.soLuongKhach,
      ghiChu: formData.value.ghiChu || undefined,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra || undefined,
      danhSachIdPhong: selectedPhongIds.value,
      danhSachLoaiPhong: selectedPhongIds.value.length === 0 ? bookingData.value.danhSachLoaiPhong : undefined,
    } as any

    console.log('Confirm Data being sent:', confirmData)

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

function getCustomerAddress(customer: TimKhachHangResponse): string {
  if (!customer.diaChi) return 'N/A'
  try {
    const addressObj = JSON.parse(customer.diaChi)
    const parts = []
    if (addressObj.ward?.name) parts.push(addressObj.ward.name)
    if (addressObj.province?.name) parts.push(addressObj.province.name)
    return parts.join(', ') || 'N/A'
  } catch {
    return 'N/A'
  }
}

</script>

<template>
  <div class="xac-nhan-page">
    <n-spin :show="isLoading">
      <n-space vertical size="large">
        <!-- Header Card -->
        <n-card v-if="bookingData">
          <div class="header-info">
            <div class="info-item">
              <n-icon :size="20" :component="Calendar" color="#3b82f6" />
              <span class="label">Check-in:</span>
              <span class="value">{{ formatDate(bookingData.ngayNhan) }}</span>
            </div>

            <div class="divider"></div>

            <div class="info-item">
              <n-icon :size="20" :component="Calendar" color="#f43f5e" />
              <span class="label">Check-out:</span>
              <span class="value">{{ formatDate(bookingData.ngayTra) }}</span>
            </div>

            <div class="divider"></div>

            <div class="info-item">
              <n-icon :size="20" :component="Time" color="#10b981" />
              <span class="value highlight">{{ soNgayO }} đêm</span>
            </div>

            <div class="divider"></div>

            <div class="info-item">
              <n-icon :size="20" :component="UserAvatar" color="#6366f1" />
              <span class="value">{{ bookingData.soLuongKhach  }} khách</span>
            </div>

            <div style="margin-left: auto;">
              <n-tag type="warning" :bordered="false">PENDING</n-tag>
            </div>
          </div>
        </n-card>

        <!-- Main Content -->
        <div v-if="bookingData" class="main-content">
          <!-- Left Column 70% -->
          <div class="left-column">
            <n-space vertical size="large">
              <!-- Danh sách phòng -->
              <n-card title="Danh sách loại phòng">
                <template #header-extra>
                  <n-tag type="info" :bordered="false">
                    {{ totalRoomsToDisplay }} phòng
                  </n-tag>
                </template>
                <n-space vertical size="medium">
                  <div v-for="(loai, index) in bookingData.danhSachLoaiPhong" :key="index" class="room-item">
                    <div class="room-info">
                      <div class="room-header">
                        <n-icon :size="18" :component="Home" color="#3b82f6" />
                        <span class="room-name">{{ loai.tenLoaiPhong || getLoaiPhongName(loai.idLoaiPhong) }}</span>
                      </div>
                      <div class="room-details">
                        <span class="quantity">{{ loai.soLuong }} phòng</span>
                        <span class="separator">×</span>
                        <span class="price">{{ formatCurrency(loai.gia || 0) }}</span>
                        <span class="separator">×</span>
                        <span class="nights">{{ soNgayO }} đêm</span>
                      </div>
                    </div>
                    <div class="room-total">
                      {{ formatCurrency((loai.gia || 0) * loai.soLuong * soNgayO) }}
                    </div>
                  </div>
                </n-space>
              </n-card>

              <!-- Thông tin khách hàng -->
              <n-card title="Thông tin khách hàng">
                <n-space vertical size="large">
                  <div class="search-customer">
                    <n-select v-model:value="selectedKhachHang" filterable
                      placeholder="Tìm kiếm theo SĐT hoặc tên khách hàng..." :options="khachHangOptions.map(kh => ({
                        label: `${kh.hoTen} - ${kh.soDienThoai}`,
                        value: kh.id,
                      }))" :loading="isSearchingKH" clearable remote class="flex-1"
                      @search="(val: string) => keywordKhachHang = val" />
                    <n-button type="primary">
                      <template #icon>
                        <n-icon :component="User" />
                      </template>
                      Tìm kiếm
                    </n-button>
                  </div>

                  <n-alert v-if="selectedKhachHangInfo" type="info" :bordered="false">
                    <div class="customer-details-grid">
                      <div class="customer-column">
                        <div class="detail-row">
                          <span class="detail-label">Họ tên:</span>
                          <span class="detail-value">{{ selectedKhachHangInfo.hoTen }}</span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">Email:</span>
                          <span class="detail-value">{{ selectedKhachHangInfo.email || 'N/A' }}</span>
                        </div>
                      </div>
                      <div class="customer-column">
                        <div class="detail-row">
                          <span class="detail-label">Số điện thoại:</span>
                          <span class="detail-value">{{ selectedKhachHangInfo.soDienThoai || 'N/A' }}</span>
                        </div>
                        <div class="detail-row">
                          <span class="detail-label">Địa chỉ:</span>
                          <span class="detail-value">{{ getCustomerAddress(selectedKhachHangInfo) }}</span>
                        </div>
                      </div>
                    </div>
                  </n-alert>
                  <n-alert v-else type="warning" :bordered="false">
                    Vui lòng chọn khách hàng để tiếp tục
                  </n-alert>
                </n-space>
              </n-card>

              <!-- Ghi chú & Số lượng khách -->
              <n-card title="Ghi chú & Thông tin bổ sung">
                <n-space vertical size="large">
                  <div>
                    <div class="payment-label">Số lượng khách</div>
                    <n-input-number v-model:value="bookingData!.soLuongKhach" :min="1" :max="50" class="w-full"
                      placeholder="Nhập số lượng khách...">
                      <template #prefix>
                        <n-icon :component="UserAvatar" />
                      </template>
                      <template #suffix>
                        <span style="color: #999;">người</span>
                      </template>
                    </n-input-number>
                  </div>

                  <div>
                    <div class="payment-label">Ghi chú</div>
                    <n-input v-model:value="formData.ghiChu" type="textarea"
                      placeholder="Nhập ghi chú cho đơn đặt phòng..." :rows="4" />
                  </div>
                </n-space>
                <template #footer>
                  <div class="note-footer">
                    <span class="note-label">Nhân viên lập phiếu:</span>
                    <span class="note-value">Lễ tân hệ thống</span>
                  </div>
                  <div class="note-footer">
                    <span class="note-label">Ngày lập:</span>
                    <span class="note-value">{{ new Date().toLocaleDateString('vi-VN') }}</span>
                  </div>
                </template>
              </n-card>
            </n-space>
          </div>

          <!-- Right Column 30% -->
          <div class="right-column">
            <n-card title="Thanh toán">
              <n-space vertical size="large">
                <!-- Hình thức thanh toán -->
                <div>
                  <div class="payment-label">Hình thức thanh toán</div>
                  <n-radio-group v-model:value="formData.hinhThucThanhToan">
                    <n-space vertical>
                      <n-radio value="FULL">Thanh toán toàn bộ</n-radio>
                      <n-radio value="DEPOSIT">Đặt cọc trước</n-radio>
                      <n-radio value="LATER">Thanh toán sau</n-radio>
                    </n-space>
                  </n-radio-group>
                </div>

                <!-- Phương thức thanh toán -->
                <div>
                  <div class="payment-label">Phương thức thanh toán</div>
                  <n-radio-group v-model:value="formData.phuongThucThanhToan">
                    <n-grid :cols="2" :x-gap="12" :y-gap="12">
                      <n-grid-item><n-radio value="CASH">Tiền mặt</n-radio></n-grid-item>
                      <n-grid-item><n-radio value="TRANSFER">Chuyển khoản</n-radio></n-grid-item>
                      <n-grid-item><n-radio value="CARD">Thẻ / POS</n-radio></n-grid-item>
                      <n-grid-item><n-radio value="E-WALLET">Ví điện tử</n-radio></n-grid-item>
                    </n-grid>
                  </n-radio-group>
                </div>

                <!-- Số tiền thanh toán -->
                <div v-if="formData.hinhThucThanhToan === 'FULL'">
                  <div class="payment-label">Số tiền thanh toán</div>
                  <n-input-number v-model:value="formData.tienKhachTra" :min="tongTien" class="w-full"
                    placeholder="Nhập số tiền..." :show-button="false">
                    <template #suffix>
                      <span style="color: #999;">VNĐ</span>
                    </template>
                  </n-input-number>
                  <div class="payment-hint">
                    <span class="hint-icon">ℹ️</span>
                    <span>Nhập chính xác số tiền khách đưa (tối thiểu {{ formatCurrency(tongTien) }})</span>
                  </div>
                  <div v-if="formData.tienKhachTra && formData.tienKhachTra > tongTien" class="change-money">
                    <span>Tiền thừa trả khách:</span>
                    <span class="change-amount">{{ formatCurrency(formData.tienKhachTra - tongTien) }}</span>
                  </div>
                </div>

                <!-- Số tiền đặt cọc -->
                <div v-else-if="formData.hinhThucThanhToan === 'DEPOSIT'">
                  <div class="payment-label">Số tiền đặt cọc</div>
                  <n-input-number v-model:value="formData.tienKhachTra" :min="1" :max="tongTien" class="w-full"
                    placeholder="Nhập số tiền đặt cọc..." :show-button="false">
                    <template #suffix>
                      <span style="color: #999;">VNĐ</span>
                    </template>
                  </n-input-number>
                  <div class="payment-hint">
                    <span class="hint-icon">ℹ️</span>
                    <span>Số tiền tối đa: {{ formatCurrency(tongTien) }}</span>
                  </div>
                  <div v-if="tienConLai > 0" class="remaining-money">
                    <span>Còn lại cần thanh toán:</span>
                    <span class="remaining-amount">{{ formatCurrency(tienConLai) }}</span>
                  </div>
                </div>

                <!-- Thanh toán sau -->
                <div v-else-if="formData.hinhThucThanhToan === 'LATER'">
                  <n-alert type="warning" :bordered="false">
                    Khách hàng sẽ thanh toán toàn bộ {{ formatCurrency(tongTien) }} khi nhận phòng
                  </n-alert>
                </div>

                <!-- Tổng kết -->
                <div class="summary-section">
                  <div class="summary-row">
                    <span>Tiền phòng ({{ totalRoomsToDisplay }} phòng):</span>
                    <span class="amount">{{ formatCurrency(tongTien) }}</span>
                  </div>
                  <div class="summary-row">
                    <span>Số đêm ở:</span>
                    <span class="nights-count">{{ soNgayO }} đêm</span>
                  </div>
                  <div class="summary-total">
                    <span>Tổng cộng:</span>
                    <span class="total-amount">{{ formatCurrency(tongTien) }}</span>
                  </div>
                </div>

                <!-- Checkbox xác nhận -->
                <n-checkbox v-model:checked="formData.isCheckXacNhan">
                  Tôi xác nhận thông tin đặt phòng chính xác
                </n-checkbox>

                <!-- Action buttons -->
                <n-space>
                  <n-button type="error" @click="router.back()">
                    Hủy bỏ
                  </n-button>
                  <n-button type="primary" :disabled="!isPaymentValid" :loading="isLoading" @click="handleDatPhong"
                    style="flex: 1;">
                    Xác nhận đặt phòng
                  </n-button>
                </n-space>
              </n-space>
            </n-card>
          </div>
        </div>
      </n-space>
    </n-spin>
  </div>
</template>

<style scoped>
.xac-nhan-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

/* Header Info */
.header-info {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.info-item .value {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

.info-item .highlight {
  color: #10b981;
  font-weight: 700;
}

.divider {
  width: 1px;
  height: 24px;
  background: #e0e0e6;
}

/* Main Content */
.main-content {
  display: grid;
  grid-template-columns: 70% 30%;
  gap: 20px;
  align-items: start;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
}

/* Room Item */
.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e0e0e6;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.room-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.room-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.quantity {
  font-weight: 600;
  color: #3b82f6;
}

.separator {
  color: #999;
}

.price,
.nights {
  font-weight: 500;
}

.room-total {
  font-size: 18px;
  font-weight: 700;
  color: #10b981;
  padding: 8px 16px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #86efac;
}

/* Customer Search */
.search-customer {
  display: flex;
  gap: 12px;
}

.search-customer .flex-1 {
  flex: 1;
}

/* Customer Details */
.customer-details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.customer-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.detail-value {
  font-size: 15px;
  color: #333;
  font-weight: 600;
}

/* Note Footer */
.note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-top: 1px solid #e0e0e6;
}

.note-footer:first-child {
  border-top: none;
}

.note-label {
  font-size: 14px;
  color: #666;
}

.note-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

/* Payment Section */
.payment-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.summary-section {
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e0e0e6;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
  color: #666;
}

.summary-row .amount {
  font-weight: 600;
  color: #333;
}

.summary-row .nights-count {
  font-weight: 600;
  color: #3b82f6;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  margin-top: 12px;
  border-top: 2px solid #e0e0e6;
}

.summary-total span:first-child {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.total-amount {
  font-size: 24px;
  font-weight: 700;
  color: #10b981;
}

/* Increase font sizes globally */
:deep(.n-card-header__main) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-input-number-input),
:deep(.n-button__content) {
  font-size: 15px;
}

:deep(.n-radio__label),
:deep(.n-checkbox__label) {
  font-size: 15px;
}

:deep(.n-tag) {
  font-size: 14px;
}

:deep(.n-alert__content) {
  font-size: 15px;
}

.w-full {
  width: 100%;
}

/* Payment Hints */
.payment-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #eff6ff;
  border-radius: 6px;
  font-size: 13px;
  color: #3b82f6;
}

.hint-icon {
  font-size: 14px;
}

.change-money,
.remaining-money {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 12px;
  background: #f0fdf4;
  border-radius: 6px;
  border: 1px solid #86efac;
}

.change-money span:first-child,
.remaining-money span:first-child {
  font-size: 14px;
  color: #666;
}

.change-amount {
  font-size: 16px;
  font-weight: 700;
  color: #10b981;
}

.remaining-amount {
  font-size: 16px;
  font-weight: 700;
  color: #f59e0b;
}

/* Responsive */
@media (max-width: 1200px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}
</style>
