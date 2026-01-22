<script setup lang="ts">
import { nextTick } from 'vue'
import { useRouter } from 'vue-router'
import type { SoDoPhongResponse } from '@/service/api/letan/sodophong'
import { getSoDoPhong } from '@/service/api/letan/sodophong'
import { useDataCombobox } from '@/store/dataCombox'
import type { SelectMixedOption } from 'naive-ui/es/select/src/interface'
import SoDo from './sodo/soDo.vue'
import Timeline from './timeline/timeline.vue'
import ChonLoaiPhongModal from './booking/ChonLoaiPhongModal.vue'
import DatPhongChiTietModal from './booking/DatPhongChiTiet.vue'
import DatPhongTrucTiepModal from './booking/DatPhongTrucTiepModal.vue'
import XacNhanDatPhongModal from './booking/XacNhanDatPhongModal.vue'
import PhieuDatTamList from './booking/PhieuDatTamList.vue'
import CustomerPaymentModal from './booking/CustomerPaymentModal.vue'
import QuanLyDatPhongModal from './booking/QuanLyDatPhongModal.vue'
import { getActiveBookingByRoom } from '@/service/api/letan/incurredService'
import type { ChonLoaiPhong } from '@/service/api/letan/booking'

const router = useRouter()

const currentView = ref<string>('map')
const { dataCombobox, fetchDataLoaiPhong } = useDataCombobox()
const currentComponent = computed(() => {
  switch (currentView.value) {
    case 'timeline': return Timeline
    case 'map': return SoDo
    default: return SoDo
  }
})

const stateSearch = reactive({
  stayDate: null as [number, number] | null,
  minPrice: null as number | null,
  maxPrice: null as number | null,
  searchQuery: null as string | null,
  idLoaiPhong: null as string | null,
})

const floors = ref<{ floor: number, rooms: SoDoPhongResponse[] }[]>([])
const notification = useNotification()

// Modal đặt phòng theo loại
const showChonLoaiPhongModal = ref(false)
const showDatPhongModal = ref(false)

const bookingData = ref<{
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
} | null>(null)
const sessionIdDatTheoLoai = ref<string | null>(null)

// Modal quản lý đặt phòng
const showQuanLyModal = ref(false)
const selectedBookingForManagement = ref<{
  id: string
  roomId: string
  roomName: string
  customerName?: string
  status: string
} | null>(null)


// Modal đặt phòng trực tiếp (từ click phòng hoặc chọn nhiều)
const showDatPhongTrucTiepModal = ref(false)
const showXacNhanModal = ref(false)
const selectedRoomsForBooking = ref<Array<{
  idPhong: string
  maPhong: string
  tenPhong: string
  tenLoaiPhong: string
  tang: number
  gia: number
}>>([])
const currentSessionId = ref<string | null>(null)

const showPhieuDatTamList = ref(false)

const showCustomerPaymentModal = ref(false)
const customerPaymentStep = ref<'CUSTOMER_INFO' | 'PAYMENT_INFO'>('CUSTOMER_INFO')

async function fetchDataSoDoPhong() {
  try {
    const data = await getSoDoPhong({
      q: stateSearch.searchQuery,
      idLoaiPhong: stateSearch.idLoaiPhong,
      minPrice: stateSearch.minPrice,
      maxPrice: stateSearch.maxPrice,
      ngayDen: stateSearch.stayDate?.[0] || null,
      ngayDi: stateSearch.stayDate?.[1] || null,
    })

    const mappedData = data.map((room) => {
      let cleanStatus: 'clean' | 'notClean'
      switch (room.trangThaiVeSinh) {
        case 'SACH':
          cleanStatus = 'clean'
          room.trangThaiVeSinh = 'SACH'
          break
        case 'DANG_DON':
          cleanStatus = 'notClean'
          room.trangThaiVeSinh = 'DANG_DON'
          break
        case 'CHUA_DON':
          cleanStatus = 'notClean'
          room.trangThaiVeSinh = 'CHUA_DON'
          break
        default:
          cleanStatus = 'notClean'
          room.trangThaiVeSinh = 'CHUA_DON'
      }
      return { ...room, cleanStatus }
    })

    const grouped: Record<number, typeof mappedData[0][]> = {}
    mappedData.forEach((room) => {
      const t = room.tang || 0
      if (!grouped[t])
        grouped[t] = []
      grouped[t].push(room)
    })

    floors.value = Object.entries(grouped)
      .map(([floor, rooms]) => {
        return { floor: Number(floor), rooms: sortRoomsZigZag(rooms) }
      })
      .sort((a, b) => a.floor - b.floor)
  }
  catch (error: any) {
    notification.error({ content: error.message || 'Không thể tải sơ đồ phòng', duration: 3000 })
  }
}

function sortRoomsZigZag(rooms: SoDoPhongResponse[]) {
  const oddRooms = rooms.filter(r => Number(r.ma) % 2 === 1).sort((a, b) => Number(b.ma) - Number(a.ma))
  const evenRooms = rooms.filter(r => Number(r.ma) % 2 === 0).sort((a, b) => Number(b.ma) - Number(a.ma))
  return [...oddRooms, ...evenRooms]
}

const debouncedSearch = useDebounceFn(() => {
  fetchDataSoDoPhong()
}, 500)

watch(() => stateSearch.searchQuery, () => {
  debouncedSearch()
})

watch(() => stateSearch.stayDate, () => {
  fetchDataSoDoPhong()
})

watch(() => stateSearch.minPrice, () => {
  debouncedSearch()
})

watch(() => stateSearch.maxPrice, () => {
  debouncedSearch()
})

watch(() => stateSearch.idLoaiPhong, () => {
  fetchDataSoDoPhong()
})

onMounted(() => {
  fetchDataLoaiPhong()
  fetchDataSoDoPhong()
})

function resetFilter() {
  stateSearch.minPrice = null
  stateSearch.maxPrice = null
  stateSearch.searchQuery = null
  stateSearch.idLoaiPhong = null
  stateSearch.stayDate = null
  fetchDataSoDoPhong()
}

// ========== Flow 1: Đặt phòng theo loại ==========
function handleOpenDatPhong() {
  showChonLoaiPhongModal.value = true
}

function handleChonLoaiPhongSubmit(data: {
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
}) {
  bookingData.value = data
  showDatPhongModal.value = true
}

function handleDatPhongChiTietNext(data: {
  sessionId: string
  danhSachIdPhong: string[]
  tongTien: number
}) {
  sessionIdDatTheoLoai.value = data.sessionId
  currentSessionId.value = data.sessionId

  // Mở CustomerPaymentModal để nhập thông tin khách hàng
  customerPaymentStep.value = 'CUSTOMER_INFO'

  nextTick(() => {
    showCustomerPaymentModal.value = true
  })
}

function handleDatPhongSuccess() {
  fetchDataSoDoPhong()
  notification.success({
    content: 'Đặt phòng thành công!',
    duration: 3000,
  })
}

// ========== Flow 2: Đặt phòng trực tiếp (từ click phòng hoặc chọn nhiều) ==========
async function handleRoomClick(room: SoDoPhongResponse) {
  // Nếu phòng đang sử dụng, mở modal quản lý
  if (['DANG_SU_DUNG', 'SAP_TRA', 'QUA_GIO_TRA'].includes(room.trangThaiPhong)) {
    try {
      const res = await getActiveBookingByRoom(room.id)
      if (res.data) {
        selectedBookingForManagement.value = {
          id: res.data.id,
          roomId: room.id,
          roomName: `${room.ma} - ${room.ten}`,
          status: room.trangThaiPhong,
          customerName: res.data.phieuDatPhong?.khachHang?.hoTen,
          phieuDatPhongId: res.data.phieuDatPhong?.id
        }
        showQuanLyModal.value = true
      }
    } catch (error: any) {
      window.$message.error('Không tìm thấy thông tin đặt phòng')
    }
    return
  }

  // Chỉ cho phép đặt phòng trống
  if (room.trangThaiPhong !== 'TRONG') {
    notification.warning({
      content: 'Chỉ có thể đặt phòng trống',
      duration: 2000,
    })
    return
  }

  selectedRoomsForBooking.value = [{
    idPhong: room.id,
    maPhong: room.ma,
    tenPhong: room.ten,
    tenLoaiPhong: room.loaiPhong,
    tang: room.tang,
    gia: room.price || 0,
  }]

  currentSessionId.value = null
  showDatPhongTrucTiepModal.value = true
}

function handleMultiRoomSelect(rooms: Array<{
  idPhong: string
  maPhong: string
  tenPhong: string
  tenLoaiPhong: string
  tang: number
  gia: number
}>) {
  selectedRoomsForBooking.value = rooms
  currentSessionId.value = null
  showDatPhongTrucTiepModal.value = true
}

function handleContinueFromDatTrucTiep(sessionId: string) {
  currentSessionId.value = sessionId

  // Use nextTick to ensure sessionId is set before opening modal
  nextTick(() => {
    showXacNhanModal.value = true
  })
}

function handleConfirmSuccess() {
  fetchDataSoDoPhong()
  notification.success({
    content: 'Đặt phòng thành công!',
    duration: 3000,
  })
}

// ========== Flow 3: Tiếp tục từ phiếu đặt tạm ==========
function handleOpenPhieuDatTamList() {
  showPhieuDatTamList.value = true
}

function handleContinueFromPhieuTam(data: {
  sessionId: string
  step: 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'CONFIRM'
}) {
  currentSessionId.value = data.sessionId

  nextTick(() => {
    if (data.step === 'CONFIRM') {
      // Đã đủ thông tin, mở modal xác nhận
      showXacNhanModal.value = true
    } else {
      // Chưa đủ thông tin, mở modal nhập khách hàng/thanh toán
      customerPaymentStep.value = data.step
      showCustomerPaymentModal.value = true
    }
  })
}

function handleCustomerPaymentContinue(sessionId: string) {
  currentSessionId.value = sessionId

  nextTick(() => {
    showXacNhanModal.value = true
  })
}
</script>

<template>
  <div class="full-container p-4">
    <div class="flex space-x-4">
      <div class="flex-1">
        <n-input v-model:value="stateSearch.searchQuery" placeholder="Tìm kiếm khách hàng, mã đặt phòng...">
          <template #prefix>
            <n-icon-wrapper :size="26" color="var(--success-color)" :border-radius="999">
              <nova-icon :size="18" icon="carbon:search" color="black" />
            </n-icon-wrapper>
          </template>
        </n-input>

        <div class="mt-[20px] flex gap-x-2">
          <div class="basis-2/5">
            <n-date-picker v-model:value="stateSearch.stayDate" type="datetimerange" clearable
              start-placeholder="Ngày đến" end-placeholder="Ngày đi" />
          </div>
          <div class="basis-1/5">
            <n-input-number v-model:value="stateSearch.minPrice" placeholder="Giá nhỏ nhất" clearable />
          </div>
          <div class="basis-1/5">
            <n-input-number v-model:value="stateSearch.maxPrice" placeholder="Giá lớn nhất" clearable />
          </div>
          <div class="basis-1/5">
            <n-select v-model:value="stateSearch.idLoaiPhong" placeholder="Chọn loại phòng" clearable
              :options="dataCombobox && dataCombobox.loaiPhong as SelectMixedOption[]" />
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between mt-2 gap-x-12px">
      <div class="flex gap-2">
        <n-button type="primary" size="large" @click="handleOpenDatPhong">
          <template #icon>
            <nova-icon icon="carbon:calendar-add" />
          </template>
          Đặt phòng
        </n-button>

        <n-button type="info" size="large" @click="handleOpenPhieuDatTamList">
          <template #icon>
            <nova-icon icon="carbon:document" />
          </template>
          Phiếu đặt tạm
        </n-button>

        <n-button type="success" size="large" @click="router.push({ name: 'phieuDatPhong' })">
          <template #icon>
            <nova-icon icon="mdi:file-document-multiple-outline" />
          </template>
          Quản lý phiếu đặt
        </n-button>
      </div>
      <n-button @click="resetFilter">
        Làm mới
      </n-button>
    </div>

    <div class="mt-4">
      <component :is="currentComponent" :floors="floors" @room-click="handleRoomClick"
        @multi-room-select="handleMultiRoomSelect" />
    </div>

    <!-- Modals đặt phòng theo loại -->
    <ChonLoaiPhongModal v-model:visible="showChonLoaiPhongModal" @submit="handleChonLoaiPhongSubmit" />

    <DatPhongChiTietModal v-model:visible="showDatPhongModal" :booking-data="bookingData"
      @next="handleDatPhongChiTietNext" />

    <!-- Modals đặt phòng trực tiếp -->
    <DatPhongTrucTiepModal v-model:visible="showDatPhongTrucTiepModal" :selected-rooms="selectedRoomsForBooking"
      :session-id="currentSessionId" @continue="handleContinueFromDatTrucTiep" @success="handleDatPhongSuccess" />

    <!-- Modal xác nhận (dùng chung cho cả 2 flow) -->
    <XacNhanDatPhongModal v-model:visible="showXacNhanModal" :session-id="currentSessionId"
      @success="handleConfirmSuccess" />

    <!-- Modal danh sách phiếu đặt tạm -->
    <PhieuDatTamList v-model:visible="showPhieuDatTamList" @continue-from-step="handleContinueFromPhieuTam" />

    <!-- Modal nhập khách hàng/thanh toán (cho flow tiếp tục từ phiếu đặt tạm) -->
    <CustomerPaymentModal v-model:visible="showCustomerPaymentModal" :session-id="currentSessionId"
      :initial-step="customerPaymentStep" @continue="handleCustomerPaymentContinue" @success="handleDatPhongSuccess" />

    <!-- Modal quản lý đặt phòng (dịch vụ) -->
    <QuanLyDatPhongModal v-model:visible="showQuanLyModal" :booking-details="selectedBookingForManagement"
      @success="fetchDataSoDoPhong" />
    <CustomerPaymentModal v-model:visible="showCustomerPaymentModal" :session-id="currentSessionId"
      :initial-step="customerPaymentStep" @continue="handleCustomerPaymentContinue" @success="handleDatPhongSuccess" />
  </div>
</template>

<style scoped>
:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-input-number-input) {
  font-size: 17px;
}

:deep(.n-input__placeholder) {
  font-size: 17px;
}

:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-button__content) {
  font-size: 17px;
}

:deep(.n-date-picker-panel-month__month-item) {
  font-size: 17px;
}

:deep(.n-date-panel-date__date) {
  font-size: 17px;
}
</style>
