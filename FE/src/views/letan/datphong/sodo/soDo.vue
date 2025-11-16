<script setup lang="ts">
import FloorRow from './components/floorRow.vue'
import { ref, computed, onMounted } from 'vue'
import { getSoDoPhong, SoDoPhongResponse } from '@/service/api/letan/sodophong'
import { useMessage } from 'naive-ui'

const message = useMessage()

// Lấy danh sách phòng từ API
const floors = ref<{ floor: number; rooms: SoDoPhongResponse[] }[]>([])

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
  return floors.value.reduce((total, floor) => {
    return total + floor.rooms.filter(room => room.trangThaiPhong === statusKey).length
  }, 0)
}

// Lọc tầng hiển thị theo trạng thái
const filteredFloors = computed(() => {
  if (selectedStatuses.value.length === 0) return floors.value
  return floors.value.map(floor => ({
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
  floors.value.forEach(floor => {
    floor.rooms.forEach(room => {
      if (room.ma === roomCode) {
        room.cleanStatus = newStatus
      }
    })
  })
  message.success(`Cập nhật trạng thái dọn phòng: ${roomCode} → ${newStatus === 'clean' ? 'Sạch' : 'Chưa dọn'}`)
}

// Hàm sắp xếp phòng theo thứ tự “zic-zac” (hàng lẻ / chẵn, giảm dần)
const sortRoomsZigZag = (rooms: SoDoPhongResponse[]) => {
  const oddRooms = rooms.filter(r => Number(r.ma) % 2 === 1).sort((a, b) => Number(b.ma) - Number(a.ma))
  const evenRooms = rooms.filter(r => Number(r.ma) % 2 === 0).sort((a, b) => Number(b.ma) - Number(a.ma))
  return [...oddRooms, ...evenRooms] // ghép lại, FloorRow sẽ render theo hàng lẻ / chẵn
}

// Fetch dữ liệu từ API
const fetchData = async () => {
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
    message.error(error.message || 'Không thể tải sơ đồ phòng')
  }
}

onMounted(fetchData)
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
      v-for="floor in filteredFloors"
      :key="floor.floor"
      :floor="floor.floor"
      :rooms="floor.rooms"
      @room-click="handleRoomClick"
      @updateCleanStatus="updateRoomCleanStatus"
    />
  </div>
</template>
