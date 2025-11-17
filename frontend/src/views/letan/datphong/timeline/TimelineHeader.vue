<template>
  <div class="grid border-b bg-white sticky top-0 z-20"
       :style="{ gridTemplateColumns: `repeat(${dates.length}, 160px)` }">
    <div
      v-for="d in dates"
      :key="d.day"
      class="text-center py-2 border-r"
    >
      <div class="text-lg font-semibold">{{ d.day }}</div>
      <div class="text-gray-600 text-sm">{{ d.weekday }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({ dates: Array })
</script>
