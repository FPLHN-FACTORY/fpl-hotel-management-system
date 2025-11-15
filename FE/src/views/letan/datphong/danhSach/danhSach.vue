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
      v-for="floor in filteredFloors"
      :key="floor.floor"
      :floor="floor.floor"
      :rooms="floor.rooms"
      @room-click="handleRoomClick"
      @updateCleanStatus="updateRoomCleanStatus"
    />
  </div>
</template>

<script setup>
import FloorRow from './components/floorRow.vue'
import { ref, computed } from 'vue'

const floors = [
  {
    floor: 1,
    rooms: [
      { code: '101', type: 1, price: 350000, status: 'empty', cleanStatus: 'clean' },
      { code: '102', type: 2, price: 400000, status: 'soonCheckin', cleanStatus: 'clean' },
      { code: '103', type: 3, price: 500000, status: 'inUse', cleanStatus: 'notClean' },
      { code: '104', type: 1, price: 350000, status: 'soonCheckout', cleanStatus: 'clean' },
      { code: '105', type: 2, price: 400000, status: 'overdue', cleanStatus: 'notClean' },
            { code: '101', type: 1, price: 350000, status: 'empty', cleanStatus: 'clean' },
      { code: '102', type: 2, price: 400000, status: 'soonCheckin', cleanStatus: 'clean' },
      { code: '103', type: 3, price: 500000, status: 'inUse', cleanStatus: 'notClean' },
      { code: '104', type: 1, price: 350000, status: 'soonCheckout', cleanStatus: 'clean' },
      { code: '105', type: 2, price: 400000, status: 'overdue', cleanStatus: 'notClean' }
    ]
  },
  {
    floor: 2,
    rooms: [
      { code: '201', type: 1, price: 350000, status: 'empty', cleanStatus: 'clean' },
      { code: '202', type: 2, price: 400000, status: 'soonCheckin', cleanStatus: 'clean' },
      { code: '203', type: 3, price: 500000, status: 'inUse', cleanStatus: 'notClean' },
      { code: '204', type: 1, price: 350000, status: 'soonCheckout', cleanStatus: 'clean' },
      { code: '205', type: 2, price: 400000, status: 'overdue', cleanStatus: 'notClean' }
    ]
  }
]

const selectedStatuses = ref([]) // dùng array để chọn nhiều trạng thái

const statuses = [
  { key: 'empty', label: 'Trống', color: '#34D399' },
  { key: 'soonCheckin', label: 'Sắp nhận', color: '#60A5FA' },
  { key: 'inUse', label: 'Đang sử dụng', color: '#FBBF24' },
  { key: 'soonCheckout', label: 'Sắp trả', color: '#F97316' },
  { key: 'overdue', label: 'Quá hạn', color: '#EF4444' }
]

// Toggle trạng thái lọc
const toggleStatus = (statusKey) => {
  if (selectedStatuses.value.includes(statusKey)) {
    // đã có → bỏ ra
    selectedStatuses.value = selectedStatuses.value.filter(s => s !== statusKey)
  } else {
    // chưa có → thêm vào
    selectedStatuses.value.push(statusKey)
  }
}

// Tính số lượng phòng theo trạng thái
const countRooms = (statusKey) => {
  return floors.reduce((total, floor) => {
    return total + floor.rooms.filter(room => room.status === statusKey).length
  }, 0)
}

// Lọc tầng hiển thị theo trạng thái
const filteredFloors = computed(() => {
  if (selectedStatuses.value.length === 0) return floors
  return floors.map(floor => ({
    ...floor,
    rooms: floor.rooms.filter(room => selectedStatuses.value.includes(room.status))
  }))
})

const handleRoomClick = (room) => {
  console.log('Phòng được chọn:', room.code)
}

// Cập nhật trạng thái dọn phòng
const updateRoomCleanStatus = (roomCode, newStatus) => {
  floors.forEach(floor => {
    floor.rooms.forEach(room => {
      if (room.code === roomCode) {
        room.cleanStatus = newStatus
      }
    })
  })
}
</script>
