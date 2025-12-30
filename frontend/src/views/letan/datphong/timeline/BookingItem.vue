<template>
  <div
    class="absolute top-1 h-10 rounded-md text-white flex items-center px-3 gap-2"
    :class="bgColor"
    :style="positionStyle"
  >
    • {{ booking.guest }}
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  booking: Object,
  dates: Array
})

// Tính toán vị trí block
const positionStyle = computed(() => {
  const colWidth = 160

  const firstDay = props.dates[0].day
  const startDay = parseInt(props.booking.start.split('-')[2])
  const endDay = parseInt(props.booking.end.split('-')[2])

  const startOffset = (startDay - firstDay) * colWidth
  const width = (endDay - startDay + 1) * colWidth

  return {
    left: startOffset + 'px',
    width: width + 'px'
  }
})

// Màu theo trạng thái
const bgColor = computed(() => {
  switch (props.booking.status) {
    case 'arriving': return 'bg-orange-400'
    case 'using': return 'bg-sky-600'
    case 'checkout-late': return 'bg-red-500'
    default: return 'bg-gray-400'
  }
})
</script>
