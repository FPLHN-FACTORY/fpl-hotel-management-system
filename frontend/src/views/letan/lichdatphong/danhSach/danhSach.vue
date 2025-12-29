<template>
  <div>
    <h3>Sơ đồ phòng</h3>
    <div class="grid grid-cols-4 gap-2">
      <div v-for="room in rooms" :key="room.id" class="border p-2 text-center">
        {{ room.name }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      rooms: [
        { id: 1, name: 'Phòng 101' },
        { id: 2, name: 'Phòng 102' },
        { id: 3, name: 'Phòng 103' },
        { id: 4, name: 'Phòng 104' },
      ]
    }
  }
}
</script>
