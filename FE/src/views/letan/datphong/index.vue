<template>
  <div class="full-container p-4">
    <!-- Nút chọn màn hình -->
    <div class="flex space-x-4">
      <n-button-group>
        <n-button :type="currentView === 'timeline' ? 'primary' : 'default'" @click="currentView = 'timeline'">
          Timeline
        </n-button>

        <n-button :type="currentView === 'map' ? 'primary' : 'default'" @click="currentView = 'map'">
          Sơ đồ
        </n-button>
      </n-button-group>

      <!-- Input tìm kiếm -->
      <div class="flex-1">
        <n-input v-model="searchQuery" placeholder="Tìm kiếm khách hàng, mã đặt phòng..." clearable
          @input="handleSearch">
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
            <n-input v-model.number="stateSearch.checkInDate" placeholder="Giá nhỏ nhất"></n-input>
          </div>
          <div class="basis-1/5">
            <n-input v-model.number="stateSearch.checkoutDate" placeholder="Giá lớn nhất"></n-input>
          </div>
          <div class="basis-1/5">
            <n-select placeholder="Chọn loại phòng"
              :options="dataCombobox && dataCombobox.loaiPhong as SelectMixedOption[]" />
          </div>
        </div>
      </div>

    </div>

    <!-- Màn hình hiển thị -->
    <div class="mt-4">
      <component :floors="floors" :is="currentComponent" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDataCombobox } from '@/store/dataCombox';
import SoDo from './sodo/soDo.vue';
import Timeline from './timeline/timeline.vue';
import { SelectMixedOption } from 'naive-ui/es/select/src/interface';
import { getSoDoPhong, SoDoPhongResponse } from '@/service/api/letan/sodophong';

const currentView = ref<string>('map')
const { dataCombobox, fetchDataLoaiPhong } = useDataCombobox()
const currentComponent = computed(() => {
  switch (currentView.value) {
    case 'timeline': return Timeline;
    case 'map': return SoDo;
    default: return SoDo; // fallback mặc định
  }
})

const searchQuery = ref<string>()

const handleSearch = () => {

}

const stateSearch = reactive({
  stayDate: undefined as [number, number] | null | undefined,
  checkInDate: undefined as number | undefined,
  checkoutDate: undefined as number | undefined,

})

const floors = ref<{ floor: number; rooms: SoDoPhongResponse[] }[]>([])

const notification = useNotification()

const fetchDataSoDoPhong = async () => {
  try {
    const data = await getSoDoPhong()
    // Map trạng thái vệ sinh từ 0|1|2 sang string + cleanStatus
    const mappedData = data.map(room => {
      let cleanStatus: 'clean' | 'notClean'
      switch (room.trangThaiVeSinh) {
        case '0':
          cleanStatus = 'clean'
          room.trangThaiVeSinh = 'SACH'
          break
        case '1':
          cleanStatus = 'notClean'
          room.trangThaiVeSinh = 'DANG_DON'
          break
        case '2':
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
      .map(([floor, rooms]) => ({ floor: Number(floor), rooms: sortRoomsZigZag(rooms) }))
      .sort((a, b) => a.floor - b.floor)
  } catch (error: any) {
    notification.error({ content: error.message || 'Không thể tải sơ đồ phòng', duration: 3000 })
  }
}

// Hàm sắp xếp phòng theo thứ tự “zic-zac” (hàng lẻ / chẵn, giảm dần)
const sortRoomsZigZag = (rooms: SoDoPhongResponse[]) => {
  const oddRooms = rooms.filter(r => Number(r.ma) % 2 === 1).sort((a, b) => Number(b.ma) - Number(a.ma))
  const evenRooms = rooms.filter(r => Number(r.ma) % 2 === 0).sort((a, b) => Number(b.ma) - Number(a.ma))
  return [...oddRooms, ...evenRooms] // ghép lại, FloorRow sẽ render theo hàng lẻ / chẵn
}

onMounted(() => {
  fetchDataLoaiPhong()
  fetchDataSoDoPhong()
})
</script>
