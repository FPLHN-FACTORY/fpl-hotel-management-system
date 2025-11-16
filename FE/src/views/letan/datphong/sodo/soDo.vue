<script setup lang="ts">
import FloorRow from './components/floorRow.vue'
import { ref, computed, onMounted } from 'vue'
import { getSoDoPhong, SoDoPhongResponse } from '@/service/api/letan/sodophong'
import { useMessage } from 'naive-ui'

const message = useMessage()

const props = defineProps<{
  floors: { floor: number; rooms: SoDoPhongResponse[] }[]
}>()
// Lấy danh sách phòng từ API

// Trạng thái được chọn để lọc
const selectedStatuses = ref<string[]>([])

// Các trạng thái hiển thị và màu sắc
const statuses = [
  { key: 'TRONG', label: 'Trống', color: '#34D399' },
  { key: 'SAP_NHAN', label: 'Sắp nhận', color: '#60A5FA' },
  { key: 'DANG_SU_DUNG', label: 'Đang sử dụng', color: '#FBBF24' },
  { key: 'SAP_TRA', label: 'Sắp trả', color: '#F97316' },
  { key: 'QUA_GIO_TRA', label: 'Quá giờ trả', color: '#EF4444' }
]

// Toggle trạng thái lọc
const toggleStatus = (statusKey: string) => {
  if (selectedStatuses.value.includes(statusKey)) {
    selectedStatuses.value = selectedStatuses.value.filter(s => s !== statusKey)
  } else {
    selectedStatuses.value.push(statusKey)
  }
}

// Đếm số phòng theo trạng thái
const countRooms = (statusKey: string) => {
  return props.floors.reduce((total, floor) => {
    return total + floor.rooms.filter(room => room.trangThaiPhong === statusKey).length
  }, 0)
}

// Lọc tầng hiển thị theo trạng thái
const filteredprops = computed(() => {
  if (selectedStatuses.value.length === 0) return props.floors
  return props.floors.map(floor => ({
    ...floor,
    rooms: floor.rooms.filter(room => selectedStatuses.value.includes(room.trangThaiPhong))
  }))
})

// Click chọn phòng
const handleRoomClick = (room: SoDoPhongResponse) => {
  console.log('Phòng được chọn:', room.ma)
}

// Cập nhật trạng thái dọn phòng
const updateRoomCleanStatus = (roomCode: string, newStatus: 'clean' | 'notClean') => {
  props.floors.forEach(floor => {
    floor.rooms.forEach(room => {
      if (room.ma === roomCode) {
        room.cleanStatus = newStatus
      }
    })
  })
  message.success(`Cập nhật trạng thái dọn phòng: ${roomCode} → ${newStatus === 'clean' ? 'Sạch' : 'Chưa dọn'}`)
}

// Fetch dữ liệu từ API

</script>

<template>
  <div class="p-8 space-y-8 bg-gray-50 min-h-screen">
    <!-- Nút lọc trạng thái phòng -->
    <div class="flex space-x-4 mb-4">
      <n-button
        v-for="status in statuses"
        :key="status.key"
        :style="{ 
          backgroundColor: selectedStatuses.includes(status.key) ? status.color : 'white', 
          color: selectedStatuses.includes(status.key) ? 'white' : 'black',
          fontWeight: '600'
        }"
        @click="toggleStatus(status.key)"
      >
        {{ status.label }} ({{ countRooms(status.key) }})
      </n-button>
    </div>

    <!-- Danh sách tầng -->
    <FloorRow
      v-for="floor in filteredprops"
      :key="floor.floor"
      :floor="floor.floor"
      :rooms="floor.rooms"
      @room-click="handleRoomClick"
      @updateCleanStatus="updateRoomCleanStatus"
    />
  </div>
</template>
