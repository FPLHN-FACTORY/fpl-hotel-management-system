<template>
  <DivCustom label="Danh sách phòng" customClasses="mt-5">
    <template #icon>
      <HomeOutlined />
    </template>

    <div class="min-h-[400px] bg-white rounded-2xl shadow-sm border border-gray-100 p-4">
      <a-table
        :columns="columns"
        :data-source="rooms"
        :loading="loading"
        bordered
        :pagination="false"
        :scroll="{ x: 1200 }"
        row-key="id"
      >
        <template #bodyCell="{ column, record, index }">

          <template v-if="column.key === 'stt'">
            <span class="font-semibold text-gray-600">
              {{ (paginationParams.page - 1) * paginationParams.size + index + 1 }}
            </span>
          </template>

          <template v-if="column.key === 'maPhong'">
            <span class="font-medium text-gray-700">{{ record.maPhong }}</span>
          </template>

          <template v-if="column.key === 'tenPhong'">
            <span class="text-gray-700">{{ record.tenPhong }}</span>
          </template>

          <template v-if="column.key === 'loaiPhong'">
            <a-tag color="blue" class="font-medium">
              {{ record.loaiPhong }}
            </a-tag>
          </template>

          <template v-if="column.key === 'giaHienTai'">
            <span class="text-green-600 font-semibold">
              {{ formatCurrency(record.giaHienTai) }}
            </span>
          </template>

          <template v-if="column.key === 'sucChua'">
            <span class="text-gray-600">{{ record.sucChua }} người</span>
          </template>

          <template v-if="column.key === 'trangThaiPhong'">
            <a-tag :color="getRoomStatusColor(record.trangThaiPhong)" class="font-medium">
              {{ getRoomStatusText(record.trangThaiPhong) }}
            </a-tag>
          </template>
         <template v-if="column.key === 'operation'">
            <div class="flex gap-1 items-center justify-center text-center">

              <a-tooltip title="Xóa phòng">
                <a-button
                  @click="handleDeleteClick(record.id)"
                  class="flex items-center justify-center w-8 h-8 "
                 style="
    background-color: #fee2e2 !important;
    color: #d81a6c !important;
    border: none !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
  "
                >
                  <DeleteOutlined />
                </a-button>
              </a-tooltip>


            </div>
          </template>
        </template>

        <template #emptyText>
          <a-empty description="Không có dữ liệu phòng" />
        </template>
      </a-table>

<div class="mt-4 w-full">
  <GlobalPagination
    v-model:current="currentPage"
    v-model:page-size="currentPageSize"
    :total="totalItems"
    @change="handlePaginationChange"
  />
</div>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import DivCustom from '@/components/custom/Div/DivCustom.vue'
import GlobalPagination from '@/components/custom/Table/GlobalPagination.vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps } from 'vue'
import {
  DeleteOutlined
} from '@ant-design/icons-vue'

const props = defineProps<{
  rooms: any[]
  paginationParams: { page: number; size: number }
  totalItems: number
  loading?: boolean
}>()

const emit = defineEmits(['page-change','delete'])

const currentPage = computed({
  get: () => props.paginationParams.page,
  set: (val) => emit('page-change', { page: val, pageSize: props.paginationParams.size })
})

const currentPageSize = computed({
  get: () => props.paginationParams.size,
  set: (val) => emit('page-change', { page: 1, pageSize: val })
})

const columns: TableColumnsType = [
  { title: 'STT', key: 'stt', align: 'center', width: 70 },
  { title: 'Mã phòng', key: 'maPhong', dataIndex: 'maPhong', align: 'center', width: 120 },
  { title: 'Tên phòng', key: 'tenPhong', dataIndex: 'tenPhong', align: 'left', width: 250, ellipsis: true },
  { title: 'Loại phòng', key: 'loaiPhong', dataIndex: 'loaiPhong', align: 'center', width: 240 },
  { title: 'Giá hiện tại', key: 'giaHienTai', dataIndex: 'giaHienTai', align: 'right', width: 150 },
  { title: 'Sức chứa', key: 'sucChua', dataIndex: 'sucChua', align: 'center', width: 120 },
  { title: 'Trạng thái', key: 'trangThaiPhong', dataIndex: 'trangThaiPhong', align: 'center', width: 140 },
  {
    title: 'Hành động',
    key: 'operation',
    width: 130,
    align: 'center',
    fixed: 'right'
  }
]

const handlePaginationChange = async (newPage: number, newPageSize: number) => {
  if (newPageSize !== props.paginationParams.size) {
    emit('page-change', { page: 1, pageSize: newPageSize })
  } else {
    emit('page-change', { page: newPage, pageSize: newPageSize })
  }
}
const handleDeleteClick = (id: string) => {
  emit('delete', id)
}

const formatCurrency = (value: number) => {
  if (!value && value !== 0) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(value)
}

const getRoomStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'TRONG': 'Trống',
    'DA_DAT': 'Đã đặt',
    'DANG_SU_DUNG': 'Đang sử dụng'
  }
  return statusMap[status] || 'Không xác định'
}

const getRoomStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'TRONG': 'green',
    'DA_DAT': 'orange',
    'DANG_SU_DUNG': 'red'
  }
  return colorMap[status] || 'default'
}
</script>

<style scoped>
:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa !important;
  font-weight: 600;
  color: #444;
  text-align: center;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f5faff !important;
}

:deep(.ant-table) {
  font-size: 13px;
}
</style>
