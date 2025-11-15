<template>
  <div class="p-2 text-sm">

    <div
      v-for="floor in floors"
      :key="floor.name"
      class="mb-4"
    >
      <div class="font-semibold text-gray-700 mb-1">
        ▢ {{ floor.name }}
      </div>

      <div v-for="r in floor.rooms" :key="r" class="pl-4 py-1">
        ✦ {{ r }}
      </div>
    </div>

  </div>
</template>

<script setup>
defineProps({ floors: Array })
</script>
