<template>
  <div class="flex w-full h-full overflow-hidden bg-gray-50">

    <!-- Sidebar -->
    <!-- <SidebarFloors :floors="floors" class="w-48 border-r" /> -->

    <!-- Timeline -->
    <!-- <div class="flex-1 overflow-auto relative"> -->

      <!-- Header ngày -->
      <!-- <TimelineHeader :dates="dates" /> -->

      <!-- Grid timeline -->
      <!-- <TimelineGrid :dates="dates" /> -->

      <!-- Danh sách phòng -->
      <!-- <div>
        <RoomRow
          v-for="room in rooms"
          :key="room.code"
          :room="room"
          :dates="dates"
        />
      </div>
    </div> -->
  </div>
</template>

<script setup>
import SidebarFloors from './SidebarFloors.vue'
import TimelineHeader from './TimelineHeader.vue'
import TimelineGrid from './TimelineGrid.vue'
import RoomRow from './RoomRow.vue'

const floors = [
  {
    name: "Tầng 2-3",
    rooms: ["PH123", "STD201", "STD202", "STD203", "STD204"]
  },
  {
    name: "Tầng 4-5",
    rooms: ["SUP401", "SUP402", "SUP403", "SUP404"]
  },
  {
    name: "Tầng 6-7",
    rooms: ["DLX601"]
  }
]

const rooms = [
  { code: "STD202", bookings: [
    { guest: "Khách lẻ", start: "2025-11-15", end: "2025-11-16", status:"using" }
  ]},
  { code: "SUP401", bookings: [
    { guest: "Khách lẻ", start: "2025-11-15", end: "2025-11-17", status:"using" }
  ]},
  { code: "SUP402", bookings: [
    { guest: "Khách lẻ", start: "2025-11-15", end: "2025-11-15", status:"arriving" }
  ]},
  { code: "SUP404", bookings: [
    { guest: "Khách lẻ", start: "2025-11-15", end: "2025-11-16", status:"using" }
  ]},
  { code: "DLX601", bookings: [
    { guest: "Khách lẻ", start: "2025-11-15", end: "2025-11-18", status:"arriving" }
  ]}
]

const dates = [
  { day: 15, weekday: "Thứ bảy" },
  { day: 16, weekday: "Chủ nhật" },
  { day: 17, weekday: "Thứ hai" },
  { day: 18, weekday: "Thứ ba" },
  { day: 19, weekday: "Thứ tư" },
  { day: 20, weekday: "Thứ năm" },
  { day: 21, weekday: "Thứ sáu" },
]
</script>

