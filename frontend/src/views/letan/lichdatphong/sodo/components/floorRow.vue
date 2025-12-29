<script setup lang="ts">
import RoomCard from './roomCard.vue'
import type { SoDoPhongResponse } from '@/service/api/letan/lichdatphong/sodophong'

defineProps<{
  floor: number
  rooms: SoDoPhongResponse[]
}>()

const emit = defineEmits(['room-click', 'updateCleanStatus'])

function handleUpdateCleanStatus(roomId: string, status: string) {
  emit('updateCleanStatus', roomId, status)
}
</script>

<template>
  <div class="mb-8">
    <div class="flex items-center mb-3">
      <span class="font-bold text-lg text-gray-700 whitespace-nowrap">
        Tầng {{ floor }} ({{ rooms.length }} phòng)
      </span>
      <div class="flex-1 ml-4 h-[2px] bg-gray-300" />
    </div>

    <div
      class="grid gap-4 grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6"
      :style="{ gridAutoRows: '170px', justifyItems: 'center', alignItems: 'center' }"
    >
      <RoomCard
        v-for="room in rooms"
        :key="room.id"
        :room="room"
        @click="$emit('room-click', room)"
        @update-clean-status="handleUpdateCleanStatus"
      />
    </div>
  </div>
</template>

<style scoped>
/* Tăng font size cho tiêu đề tầng */
.font-bold.text-lg {
  font-size: 20px !important;
}
</style>
