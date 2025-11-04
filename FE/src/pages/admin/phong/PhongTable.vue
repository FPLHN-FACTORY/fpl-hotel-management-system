<template>
  <DivCustom label="Danh sách phòng" customClasses="mt-5">
    <template #icon>
      <HomeOutlined />
    </template>

    <div
      style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <div></div> <!-- div trống ép nút sang phải -->
      <div>
        <a-button style="background-color: #54bddb;"type="primary" @click="showHistory = true"
                  class="d-flex justify-content-center items-center px-4">
          <template #icon>
            <i class="ri-history-line"></i>
          </template>
          Lịch sử đặt phòng
        </a-button>
      </div>
    </div>

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
    <!-- Modal hiển thị lịch sử -->
    <a-modal
      v-model:open="showHistory"
      title="Lịch sử sử dụng phòng"
      :width="950"
      centered
      :footer="null"
    >
      <!-- Bộ lọc -->
      <div class="flex items-center mb-4" style="column-gap: 16px;">
        <a-input
          v-model:value="filterParams.tenKhachHang"
          placeholder="Tên khách hàng"
          allow-clear
          style="width: 350px"
        />

        <a-date-picker
          v-model:value="filterParams.tuNgay"
          placeholder="Từ ngày"
          format="DD/MM/YYYY"
          style="width: 220px"
        />

        <a-date-picker
          v-model:value="filterParams.denNgay"
          placeholder="Đến ngày"
          format="DD/MM/YYYY"
          style="width: 220px"
        />

        <a-button
          type="primary"
          @click="fetchRoomHistory"
          class="flex items-center justify-center"
          style="height: 32px"
        >
          <template #icon>
            <i class="ri-search-line"></i>
          </template>
          Tìm kiếm
        </a-button>
      </div>


      <!-- Bảng lịch sử -->
      <a-table
        :columns="columnsHistory"
        :data-source="roomHistory"
        :loading="loadingHistory"
        :pagination="false"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'tongTienPhatSinh'">
            {{ formatCurrency(record.tongTienPhatSinh) }}
          </template>
          <template v-if="column.key === 'trangThaiPhong'">
            <a-tag :color="getRoomStatusColor(record.trangThaiPhong)">
              {{ getRoomStatusText(record.trangThaiPhong) }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-modal>
  </DivCustom>
</template>

<script setup lang="ts">
import {ref, computed, watch, reactive} from 'vue'
import DivCustom from '@/components/custom/Div/DivCustom.vue'
import GlobalPagination from '@/components/custom/Table/GlobalPagination.vue'
import { HomeOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { defineEmits, defineProps } from 'vue'
import {
  DeleteOutlined
} from '@ant-design/icons-vue'
import {getAllRoomUsageHistory} from "@/services/api/admin/phong.api.ts";
import type {LeTanResponse, LsDatPhongRequest} from '@/services/api/admin/phong.api.ts'
import dayjs from "dayjs";

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
const showHistory = ref(false)
const roomHistory = ref<LeTanResponse[]>([])
const loadingHistory = ref(false)

/* Bộ lọc trong modal */
const filterParams = reactive<LsDatPhongRequest>({
  tenKhachHang: '',
  tuNgay: null,
  denNgay: null
})

/* Cột của bảng lịch sử */
const columnsHistory: TableColumnsType = [
  {title: 'Khách hàng', dataIndex: 'hoTen', key: 'hoTen', align: 'center'},
  {title: 'Mã phòng', dataIndex: 'maPhong', key: 'maPhong', align: 'center'},
  {title: 'Tên phòng', dataIndex: 'tenPhong', key: 'tenPhong', align: 'center'},
  {title: 'Thời gian đặt', dataIndex: 'thoiGianDat', key: 'thoiGianDat', align: 'center'},
  {title: 'Giá hiện tại', dataIndex: 'giaHienTai', key: 'giaHienTai', align: 'center'},
  {title: 'Thời gian check in', dataIndex: 'thoiGianCheckIn', key: 'thoiGianCheckIn', align: 'center'},
  {title: 'Thời gian check in', dataIndex: 'thoiGianCheckOut', key: 'thoiGianCheckOut', align: 'center'},
  {
    title: 'Dịch vụ phát sinh',
    dataIndex: 'dichVuPhatSinh',
    key: 'dichVuPhatSinh',
    align: 'center',
    customRender: ({ text }) =>
      Array.isArray(text) && text.length > 0 ? text.join(', ') : 'Không có'
  },
  {title: 'Trạng thái', dataIndex: 'trangThaiPhong', key: 'trangThaiPhong', align: 'center'},
  {
    title: 'Tổng tiền phát sinh',
    dataIndex: 'tongTienPhatSinh',
    key: 'tongTienPhatSinh',
    align: 'center'
  }
]

/* Hàm gọi API lịch sử */
const fetchRoomHistory = async () => {
  try {
    loadingHistory.value = true

    const params: LsDatPhongRequest = {
      tenKhachHang: filterParams.tenKhachHang || undefined,
      tuNgay: filterParams.tuNgay ? dayjs(filterParams.tuNgay).format('YYYY-MM-DD') : null,
      denNgay: filterParams.denNgay ? dayjs(filterParams.denNgay).format('YYYY-MM-DD') : null
    }

    const res = await getAllRoomUsageHistory(params)
    console.log('📦 Lịch sử phòng response:', res)

    const data =
      res?.data?.data?.data ||
      res?.data?.data ||
      res?.data ||
      []

    roomHistory.value = Array.isArray(data) ? data : []
    console.log('✅ Dữ liệu hiển thị:', roomHistory.value)

  } catch (err) {
    console.error('❌ Lỗi khi load lịch sử phòng:', err)
    roomHistory.value = []
  } finally {
    loadingHistory.value = false
  }
}

/* Tự load dữ liệu khi mở modal */
watch(showHistory, (val) => {
  if (val) fetchRoomHistory()
})
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

