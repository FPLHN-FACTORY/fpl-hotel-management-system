<script setup lang="ts">
import type { SoDoPhongResponse } from '@/service/api/letan/lichdatphong/sodophong'
import { getSoDoPhong } from '@/service/api/letan/lichdatphong/sodophong'
import { useDataCombobox } from '@/store/dataCombox'
import type { SelectMixedOption } from 'naive-ui/es/select/src/interface'
import SoDo from './sodo/soDo.vue'
import Timeline from './timeline/timeline.vue'
import ChonLoaiPhongModal from './datphong/ChonLoaiPhongModal.vue'
import DatPhongChiTietModal from './datphong/DatPhongChiTiet.vue'
import type { ChonLoaiPhong } from '@/service/api/letan/lichdatphong/datphong'

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
  stayDate: undefined as [number, number] | null | undefined,
  minPrice: undefined as number | undefined | null,
  maxPrice: undefined as number | undefined | null,
  searchQuery: undefined as string | undefined | null,
  idLoaiPhong: undefined as string | undefined | null,
})

const floors = ref<{ floor: number, rooms: SoDoPhongResponse[] }[]>([])
const notification = useNotification()

// Modal đặt phòng
const showChonLoaiPhongModal = ref(false)
const showDatPhongModal = ref(false)
const bookingData = ref<{
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
} | null>(null)

function getTodayRange(): [number, number] {
  const now = new Date()
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
  const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999)
  return [startOfDay.getTime(), endOfDay.getTime()]
}

async function fetchDataSoDoPhong() {
  try {
    const data = await getSoDoPhong({
      q: stateSearch.searchQuery,
      idLoaiPhong: stateSearch.idLoaiPhong,
      minPrice: stateSearch.minPrice,
      maxPrice: stateSearch.maxPrice,
      ngayDen: stateSearch.stayDate && stateSearch.stayDate[0],
      ngayDi: stateSearch.stayDate && stateSearch.stayDate[1],
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

function initializeWithTodayFilter() {
  stateSearch.stayDate = getTodayRange()
  fetchDataSoDoPhong()
}

onMounted(() => {
  fetchDataLoaiPhong()
  initializeWithTodayFilter()
})

function resetFilter() {
  stateSearch.minPrice = null
  stateSearch.maxPrice = null
  stateSearch.searchQuery = ''
  stateSearch.idLoaiPhong = null
  stateSearch.stayDate = null
  fetchDataSoDoPhong()
}

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

function handleDatPhongSuccess() {
  fetchDataSoDoPhong()
  notification.success({
    content: 'Đặt phòng thành công!',
    duration: 3000,
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
            <n-date-picker
              v-model:value="stateSearch.stayDate"
              type="datetimerange"
              clearable
              start-placeholder="Ngày đến"
              end-placeholder="Ngày đi"
            />
          </div>
          <div class="basis-1/5">
            <n-input-number v-model:value="stateSearch.minPrice" placeholder="Giá nhỏ nhất" clearable />
          </div>
          <div class="basis-1/5">
            <n-input-number v-model:value="stateSearch.maxPrice" placeholder="Giá lớn nhất" clearable />
          </div>
          <div class="basis-1/5">
            <n-select
              v-model:value="stateSearch.idLoaiPhong"
              placeholder="Chọn loại phòng"
              clearable
              :options="dataCombobox && dataCombobox.loaiPhong as SelectMixedOption[]"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-between mt-2 gap-x-12px">
      <div>
        <n-button type="primary" size="large" @click="handleOpenDatPhong">
          <template #icon>
            <nova-icon icon="carbon:calendar-add" />
          </template>
          Đặt phòng
        </n-button>
      </div>
      <n-button @click="resetFilter">
        Làm mới bộ lọc
      </n-button>
    </div>

    <div class="mt-4">
      <component :is="currentComponent" :floors="floors" />
    </div>

    <!-- Modals -->
    <ChonLoaiPhongModal
      v-model:visible="showChonLoaiPhongModal"
      @submit="handleChonLoaiPhongSubmit"
    />

    <DatPhongChiTietModal
      v-model:visible="showDatPhongModal"
      :booking-data="bookingData"
      @success="handleDatPhongSuccess"
    />
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
