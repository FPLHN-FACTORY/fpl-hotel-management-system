<template>
  <BreadcrumbDefault label="Quản lý phòng" :link="ROUTES_CONSTANTS.ADMIN.children.PHONG.name">
    <PhongFilter @filter-change="handleFilterChange" @sort-change="handleSortChange" />

    <PhongTable :rooms="rooms" :pagination-params="paginationParams" :total-items="totalItems" :loading="loading"
      @page-change="handlePageChange" @delete="handleDeleteClick" />


  </BreadcrumbDefault>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import BreadcrumbDefault from '@/components/custom/Div/BreadcrumbDefault.vue'
import PhongTable from './PhongTable.vue'
import PhongFilter from './PhongFilter.vue'
import { deleteRoom, getRooms, type ParamsGetPhong } from '@/services/api/admin/phong.api'
import { ROUTES_CONSTANTS } from '@/constants/path'
import { message, Modal } from 'ant-design-vue'
import { toast } from 'vue3-toastify'


const rooms = ref<any[]>([])
const totalItems = ref(0)
const loading = ref(false)
let debounceTimer: ReturnType<typeof setTimeout> | null = null

const paginationParams = ref({
  page: 1,
  size: 10
})

const filterParams = ref<ParamsGetPhong>({
  page: 1,
  size: 10,
  sortBy: 'maPhong',
  orderBy: 'ASC'
})

const fetchRooms = async (showNoResultMessage = false) => {
  loading.value = true
  try {
    const params: ParamsGetPhong = {
      page: paginationParams.value.page,
      size: paginationParams.value.size,
      sortBy: filterParams.value.sortBy || 'maPhong',
      orderBy: filterParams.value.orderBy || 'ASC',
      q: filterParams.value.q || undefined,
      loaiPhong: filterParams.value.loaiPhong || undefined,
      trangThaiPhong: filterParams.value.trangThaiPhong || undefined,
      giaMin: filterParams.value.giaMin || undefined,
      giaMax: filterParams.value.giaMax || undefined,
      sucChuaMin: filterParams.value.sucChuaMin || undefined,
      sucChuaMax: filterParams.value.sucChuaMax || undefined
    }

    Object.keys(params).forEach(key => {
      if (params[key as keyof ParamsGetPhong] === undefined) {
        delete params[key as keyof ParamsGetPhong]
      }
    })

    const res = await getRooms(params)

    if (res.data) {
      rooms.value = res.data.data || []
      totalItems.value = res.data.totalElements || 0
    } else {
      rooms.value = []
      totalItems.value = 0
    }

    if (showNoResultMessage && rooms.value.length === 0) {
      message.info('Không tìm thấy phòng phù hợp với tiêu chí lọc')
    }

  } catch (error: any) {
    message.error(error?.response?.data?.message || 'Không thể tải danh sách phòng')
    console.error('Error fetching rooms:', error)
    rooms.value = []
    totalItems.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (params: { page: number; pageSize: number }) => {
  paginationParams.value.page = params.page
  paginationParams.value.size = params.pageSize
  fetchRooms()
}
const handleDeleteClick = async (id: string) => {
  Modal.confirm({
    title: 'Xác nhận xóa phòng',
    content: 'Bạn có chắc chắn muốn xóa phòng này không?',
    okText: 'Xóa',
    cancelText: 'Hủy',
    okType: 'danger',
    onOk: async () => {
      try {
        const res = await deleteRoom(id); // local variable
        toast.success(res.message);
        fetchRooms();
      } catch (error: any) {
        toast.error(error?.response?.data?.message || 'Xóa phòng thất bại');
      }
    }
  });
};

const handleFilterChange = (filters: any) => {
  paginationParams.value.page = 1

  filterParams.value = {
    ...filterParams.value,
    q: filters.q,
    loaiPhong: filters.loaiPhong,
    trangThaiPhong: filters.trangThaiPhong,
    giaMin: filters.giaMin,
    giaMax: filters.giaMax,
    sucChuaMin: filters.sucChuaMin,
    sucChuaMax: filters.sucChuaMax
  }

  // Clear previous timer
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  // Set new timer
  debounceTimer = setTimeout(() => {
    const hasFilters = !!(
      filters.q ||
      filters.loaiPhong ||
      filters.trangThaiPhong ||
      filters.giaMin ||
      filters.giaMax ||
      filters.sucChuaMin ||
      filters.sucChuaMax
    )
    fetchRooms(hasFilters)
  }, 900)
}

const handleSortChange = (sort: { sortBy: string; orderBy: string }) => {
  filterParams.value.sortBy = sort.sortBy
  filterParams.value.orderBy = sort.orderBy
  fetchRooms()
}

onMounted(() => {
  fetchRooms()
})

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
})
</script>
