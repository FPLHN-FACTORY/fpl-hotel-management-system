<script setup lang="ts">
import type { SoDoPhongResponse, TrangThaiVeSinh } from '@/service/api/letan/sodophong'
import { computed, ref } from 'vue'
import FloorRow from './components/floorRow.vue'

const props = defineProps<{
  floors: { floor: number, rooms: SoDoPhongResponse[] }[]
}>()

const emit = defineEmits(['room-click', 'multi-room-select'])

// Filter trạng thái phòng
const selectedStatuses = ref<string[]>([])
const statuses = [
  { key: 'TRONG', label: 'Trống', color: '#34D399' },
  { key: 'SAP_NHAN', label: 'Sắp nhận', color: '#60A5FA' },
  { key: 'DANG_SU_DUNG', label: 'Đang sử dụng', color: '#FBBF24' },
  { key: 'SAP_TRA', label: 'Sắp trả', color: '#F97316' },
  { key: 'QUA_GIO_TRA', label: 'Quá giờ trả', color: '#EF4444' },
]

// Chế độ chọn nhiều
const multiSelectMode = ref(false)
const selectedRoomIds = ref<Set<string>>(new Set())
const selectedRooms = ref<SoDoPhongResponse[]>([])

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
  if (!multiSelectMode.value) {
    emit('room-click', room)
  }
}

// Toggle chọn phòng trong chế độ multi-select
function handleToggleSelect(room: SoDoPhongResponse) {
  if (selectedRoomIds.value.has(room.id)) {
    selectedRoomIds.value.delete(room.id)
    selectedRooms.value = selectedRooms.value.filter(r => r.id !== room.id)
  } else {
    selectedRoomIds.value.add(room.id)
    selectedRooms.value.push(room)
  }
}

// Bật/tắt chế độ chọn nhiều
function toggleMultiSelectMode() {
  multiSelectMode.value = !multiSelectMode.value
  if (!multiSelectMode.value) {
    selectedRoomIds.value.clear()
    selectedRooms.value = []
  }
}

// Hủy chọn tất cả
function clearSelection() {
  selectedRoomIds.value.clear()
  selectedRooms.value = []
}

// Đặt phòng cho các phòng đã chọn
function handleBookSelectedRooms() {
  if (selectedRooms.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  const roomsData = selectedRooms.value.map(r => ({
    idPhong: r.id,
    maPhong: r.ma,
    tenPhong: r.ten,
    tenLoaiPhong: r.loaiPhong,
    tang: r.tang,
    gia: r.price || 0,
  }))

  emit('multi-room-select', roomsData)

  // Reset sau khi đặt
  multiSelectMode.value = false
  selectedRoomIds.value.clear()
  selectedRooms.value = []
}
</script>

<template>
  <div class="p-8 space-y-8 bg-gray-50 min-h-screen">
    <!-- Bộ lọc trạng thái -->
    <div class="flex justify-between items-center">
      <div class="flex space-x-4">
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

      <!-- Nút chế độ chọn nhiều -->
      <div class="flex gap-2">
        <n-button
          v-if="multiSelectMode && selectedRooms.length > 0"
          type="error"
          @click="clearSelection"
        >
          <template #icon>
            <nova-icon icon="carbon:close" />
          </template>
          Bỏ chọn ({{ selectedRooms.length }})
        </n-button>

        <n-button
          v-if="multiSelectMode && selectedRooms.length > 0"
          type="primary"
          @click="handleBookSelectedRooms"
        >
          <template #icon>
            <nova-icon icon="carbon:calendar-add" />
          </template>
          Đặt {{ selectedRooms.length }} phòng
        </n-button>

        <n-button
          :type="multiSelectMode ? 'primary' : 'default'"
          @click="toggleMultiSelectMode"
        >
          <template #icon>
            <nova-icon :icon="multiSelectMode ? 'carbon:checkbox-checked' : 'carbon:checkbox'" />
          </template>
          {{ multiSelectMode ? 'Thoát chế độ chọn' : 'Chọn nhiều phòng' }}
        </n-button>
      </div>
    </div>

    <!-- Danh sách tầng -->
    <FloorRow
      v-for="floor in filteredFloors"
      :key="floor.floor"
      :floor="floor.floor"
      :rooms="floor.rooms"
      :multi-select-mode="multiSelectMode"
      :selected-room-ids="selectedRoomIds"
      @room-click="handleRoomClick"
      @update-clean-status="updateRoomCleanStatus"
      @toggle-select="handleToggleSelect"
    />
  </div>
</template>

<style scoped>
:deep(.n-button__content) {
  font-size: 17px;
}
</style>
