<script setup lang="ts">
import type { SoDoPhongResponse, TrangThaiVeSinh } from '@/service/api/letan/lichdatphong/sodophong'
import { computed, ref } from 'vue'
import FloorRow from './components/floorRow.vue'

const props = defineProps<{
  floors: { floor: number, rooms: SoDoPhongResponse[] }[]
}>()

const emits = defineEmits()

// Filter trạng thái phòng
const selectedStatuses = ref<string[]>([])

const statuses = [
  { key: 'TRONG', label: 'Trống', color: '#34D399' },
  { key: 'SAP_NHAN', label: 'Sắp nhận', color: '#60A5FA' },
  { key: 'DANG_SU_DUNG', label: 'Đang sử dụng', color: '#FBBF24' },
  { key: 'SAP_TRA', label: 'Sắp trả', color: '#F97316' },
  { key: 'QUA_GIO_TRA', label: 'Quá giờ trả', color: '#EF4444' },
]

function toggleStatus(statusKey: string) {
  if (selectedStatuses.value.includes(statusKey)) {
    selectedStatuses.value = selectedStatuses.value.filter(s => s !== statusKey)
  }
  else {
    selectedStatuses.value.push(statusKey)
  }
}

// Cập nhật trạng thái vệ sinh
function updateRoomCleanStatus(roomId: string, status: TrangThaiVeSinh) {
  for (const floor of props.floors) {
    const room = floor.rooms.find(r => r.id === roomId)
    if (room) {
      room.trangThaiVeSinh = status
      break
    }
  }
}

// Đếm phòng theo trạng thái
function countRooms(statusKey: string) {
  return props.floors.reduce((total, floor) => {
    return total + floor.rooms.filter(room => room.trangThaiPhong === statusKey).length
  }, 0)
}

// Lọc tầng hiển thị
const filteredFloors = computed(() => {
  if (selectedStatuses.value.length === 0)
    return props.floors
  return props.floors.map(floor => ({
    ...floor,
    rooms: floor.rooms.filter(room => selectedStatuses.value.includes(room.trangThaiPhong)),
  }))
})

// Click chọn phòng
function handleRoomClick(room: SoDoPhongResponse) {
  console.log('Phòng được chọn:', room.ma)
}
</script>

<template>
  <div class="p-8 space-y-8 bg-gray-50 min-h-screen">
    <div class="flex space-x-4 mb-4">
      <n-button
        v-for="status in statuses"
        :key="status.key"
        :style="{
          backgroundColor: selectedStatuses.includes(status.key) ? status.color : 'white',
          color: selectedStatuses.includes(status.key) ? 'white' : 'black',
          fontWeight: '600',
        }"
        @click="toggleStatus(status.key)"
      >
        {{ status.label }} ({{ countRooms(status.key) }})
      </n-button>
    </div>

    <FloorRow
      v-for="floor in filteredFloors"
      :key="floor.floor"
      :floor="floor.floor"
      :rooms="floor.rooms"
      @room-click="handleRoomClick"
      @update-clean-status="updateRoomCleanStatus"
    />
  </div>
</template>

<style scoped>
/* Tăng font size cho button bộ lọc */
:deep(.n-button__content) {
  font-size: 17px;
}
</style>
