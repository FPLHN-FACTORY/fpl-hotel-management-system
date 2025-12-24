<template>
  <div class="full-container p-4">
    <!-- Nút chọn màn hình -->
    <div class="flex space-x-4">
      <!-- Input tìm kiếm -->
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
            <n-input-number v-model:value="stateSearch.maxPrice" placeholder="Giá lớn nhất" clearable></n-input-number>
          </div>
          <div class="basis-1/5">
            <n-select
              placeholder="Chọn loại phòng"
              v-model:value="stateSearch.idLoaiPhong"
              clearable
              :options="dataCombobox && dataCombobox.loaiPhong as SelectMixedOption[]"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-end mt-2 gap-x-12px">
      <n-button @click="resetFilter">
        Làm mới bộ lọc
      </n-button>
    </div>
    <!-- Màn hình hiển thị -->
    <div class="mt-4">
      <component :floors="floors" :is="currentComponent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getSoDoPhong, SoDoPhongResponse } from '@/service/api/letan/sodophong';
import { useDataCombobox } from '@/store/dataCombox';
import { SelectMixedOption } from 'naive-ui/es/select/src/interface';
import SoDo from './sodo/soDo.vue';
import Timeline from './timeline/timeline.vue';

const currentView = ref<string>('map')
const { dataCombobox, fetchDataLoaiPhong } = useDataCombobox()
const currentComponent = computed(() => {
  switch (currentView.value) {
    case 'timeline': return Timeline;
    case 'map': return SoDo;
    default: return SoDo;
  }
})

const stateSearch = reactive({
  stayDate: undefined as [number, number] | null | undefined,
  minPrice: undefined as number | undefined | null,
  maxPrice: undefined as number | undefined | null,
  searchQuery: undefined as string | undefined | null,
  idLoaiPhong: undefined as string | undefined | null,
})

const floors = ref<{ floor: number; rooms: SoDoPhongResponse[] }[]>([])

const notification = useNotification()

// Hàm lấy khoảng thời gian đầu-cuối ngày hiện tại
const getTodayRange = (): [number, number] => {
  const now = new Date()
  const startOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
  const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999)
  return [startOfDay.getTime(), endOfDay.getTime()]
}

const fetchDataSoDoPhong = async () => {
  try {
    const data = await getSoDoPhong({
      q: stateSearch.searchQuery,
      idLoaiPhong: stateSearch.idLoaiPhong,
      minPrice: stateSearch.minPrice,
      maxPrice: stateSearch.maxPrice,
      ngayDen: stateSearch.stayDate && stateSearch.stayDate[0],
      ngayDi: stateSearch.stayDate && stateSearch.stayDate[1],
    })
    // Map trạng thái vệ sinh từ 0|1|2 sang string + cleanStatus
    const mappedData = data.map(room => {
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

    // Gom theo tầng
    const grouped: Record<number, typeof mappedData[0][]> = {}
    mappedData.forEach(room => {
      const t = room.tang || 0
      if (!grouped[t]) grouped[t] = []
      grouped[t].push(room)
    })

    floors.value = Object.entries(grouped)
      .map(([floor, rooms]) => {
        return { floor: Number(floor), rooms: sortRoomsZigZag(rooms) }
      })
      .sort((a, b) => a.floor - b.floor)
  } catch (error: any) {
    notification.error({ content: error.message || 'Không thể tải sơ đồ phòng', duration: 3000 })
  }
}

// Hàm sắp xếp phòng theo thứ tự "zic-zac" (hàng lẻ / chẵn, giảm dần)
const sortRoomsZigZag = (rooms: SoDoPhongResponse[]) => {
  const oddRooms = rooms.filter(r => Number(r.ma) % 2 === 1).sort((a, b) => Number(b.ma) - Number(a.ma))
  const evenRooms = rooms.filter(r => Number(r.ma) % 2 === 0).sort((a, b) => Number(b.ma) - Number(a.ma))
  return [...oddRooms, ...evenRooms]
}

// Tìm kiếm tự động với debounce
const debouncedSearch = useDebounceFn(() => {
  fetchDataSoDoPhong()
}, 500)

// Watch các trường tìm kiếm để tự động gọi API
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

// Khởi tạo với ngày hiện tại
const initializeWithTodayFilter = () => {
  stateSearch.stayDate = getTodayRange()
  fetchDataSoDoPhong()
}

onMounted(() => {
  fetchDataLoaiPhong()
  initializeWithTodayFilter()
})

const resetFilter = () => {
  stateSearch.minPrice = null
  stateSearch.maxPrice = null
  stateSearch.searchQuery = ''
  stateSearch.idLoaiPhong = null
  stateSearch.stayDate = null
  fetchDataSoDoPhong()
}
</script>

<style scoped>
/* Tăng font size cho các input và select trong bộ lọc */
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

/* Date picker */
:deep(.n-date-picker-panel-month__month-item) {
  font-size: 17px;
}

:deep(.n-date-panel-date__date) {
  font-size: 17px;
}
</style>
