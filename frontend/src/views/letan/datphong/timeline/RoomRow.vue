<template>
  <div class="relative flex items-center h-16 border-b">

    <!-- Tên phòng -->
    <div class="text-sm w-20 px-2 text-gray-700 font-semibold">
      {{ room.code }}
    </div>

    <!-- Booking -->
    <BookingItem
      v-for="(b, index) in room.bookings"
      :key="index"
      :booking="b"
      :dates="dates"
    />
  </div>
</template>

<script setup>
import BookingItem from './BookingItem.vue'

defineProps({
  room: Object,
  dates: Array
})
</script>
