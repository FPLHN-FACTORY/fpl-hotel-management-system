<template>
  <div class="absolute top-[70px] left-0 right-0"
       :style="{ height: '2000px' }">

    <div
      v-for="(d, index) in dates"
      :key="index"
      class="absolute top-0 bottom-0 border-r"
      :style="{ left: `${index * 160}px`, width:'160px' }">
    </div>

  </div>
</template>

<script setup>
defineProps({ dates: Array })
</script>
