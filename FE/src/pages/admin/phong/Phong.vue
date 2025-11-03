<template>
  <div class="p-4">
    <PhongTable :rooms="rooms" :pagination-params="paginationParams" :total-items="totalItems"
      @page-change="handlePageChange" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PhongTable from './PhongTable.vue'
import { getRooms } from '@/services/api/admin/phong.api'
import { toast } from 'vue3-toastify'

const rooms = ref<any[]>([])
const totalItems = ref(0)
const paginationParams = ref({ page: 1, size: 10 })

const fetchRooms = async () => {
  try {
    const res = await getRooms({
      page: paginationParams.value.page,
      size: paginationParams.value.size,
      sort: 'maPhong,asc'
    })

    rooms.value = res.data.data || []
    totalItems.value = res.data.totalElements || 0

  } catch (error) {
    toast.error('Không thể tải danh sách phòng')
    console.error(error)
  }
}




const handlePageChange = (params: { page: number; pageSize: number }) => {
  paginationParams.value.page = params.page
  paginationParams.value.size = params.pageSize
  fetchRooms()
}

onMounted(() => {
  fetchRooms()
})
</script>
