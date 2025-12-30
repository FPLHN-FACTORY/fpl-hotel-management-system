<template>
  <div class="mb-8">
    <!-- Tên tầng -->
    <div class="flex items-center mb-3">
      <span class="font-bold text-lg text-gray-700 whitespace-nowrap">
        Tầng {{ floor }} ({{ rooms.length }} phòng)
      </span>
      <div class="flex-1 ml-4 h-[2px] bg-gray-300"></div>
    </div>

    <!-- Grid hiển thị phòng -->
    <div class="grid gap-4 grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6" 
          :style="{ gridAutoRows: 'minmax(160px, 1fr)', justifyItems: 'center', alignItems: 'start' }">
      <RoomCard
        v-for="(room, index) in rooms"
        :key="index"
        :room="room"
        @click="$emit('room-click', room)"
        @updateCleanStatus="$emit('updateCleanStatus', room.code, $event)"
      />
    </div>
  </div>
</template>

<script setup>
import RoomCard from './roomCard.vue'

defineProps({
  floor: Number,
  rooms: Array
})

defineEmits(['room-click', 'updateCleanStatus'])
</script>

<style scoped>
div + div {
  margin-top: 2rem;
}
</style>
