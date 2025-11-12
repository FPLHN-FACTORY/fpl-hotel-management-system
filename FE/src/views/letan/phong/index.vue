<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import type { DataTableColumns, FormInst } from 'naive-ui'
import { useBoolean } from '@/hooks'
import {
  NButton,
  NPopconfirm,
  NSpace,
  NTag,
  NSelect,
  NInputNumber,
  NInput,
} from 'naive-ui'
import TableModal from './components/TableModal.vue'
import { deleteRoom, getAllRooms } from '@/service/api/letan/phong' // service gọi API backend
import type { PhongResponse } from '@/service/api/letan/phong'

// --- Loading & Modal ---
const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal } = useBoolean(false)

// --- Form tìm kiếm ---
const initialModel = {
  keyword: '',
  tang: '',
  loaiPhong: '',
  trangThai: '',
  giaMin: undefined,
  giaMax: undefined,
  sucChuaMin: undefined,
  sucChuaMax: undefined,
}
const model = ref({ ...initialModel })
const formRef = ref<FormInst | null>()

// --- Options combobox ---
const tangOptions = ['1', '2', '3', '4', '5'].map(t => ({ label: t, value: t }))
const loaiPhongOptions = [
  { label: 'Phòng 3 giường', value: 'Phòng 3 giường' },
  { label: 'Phòng 2 giường đơn', value: 'Phòng 2 giường đơn' },
  { label: 'Phòng 1 giường đôi 1 giường đơn', value: 'Phòng 1 giường đôi 1 giường đơn' },
]
const trangThaiOptions = [
  { label: 'Trống', value: 'TRONG' },
  { label: 'Đã đặt', value: 'DA_DAT' },
  { label: 'Đang sử dụng', value: 'DANG_SU_DUNG' },
  { label: 'Đang sửa chữa', value: 'DANG_SUA_CHUA' },
  { label: 'Đang dọn', value: 'DANG_DON' },
  { label: 'Tạm khóa', value: 'TAM_KHOA' },
]

// --- Dữ liệu bảng ---
const listData = ref<PhongResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// --- Hàm fetch phòng từ backend ---
async function fetchRooms(page = 1) {
  startLoading()
  try {
    const res = await getAllRooms({
      q: model.value.keyword || undefined,
      tang: model.value.tang ? Number(model.value.tang) : undefined,
      loaiPhong: model.value.loaiPhong || undefined,
      trangThaiPhong: model.value.trangThai || undefined,
      giaMin: model.value.giaMin,
      giaMax: model.value.giaMax,
      page: page,
      size: pageSize.value,
    })
    listData.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage
  } finally {
    endLoading()
  }
}

// --- Hành động ---
function handleEditTable(row: PhongResponse) {
  window.$message.info(`Chỉnh sửa phòng: ${row.ten}`)
  openModal()
}

function handleAddTable() {
  openModal()
}

// Xóa mềm phòng
async function handleDeleteRoom(id: string) {
  try {
    const res = await deleteRoom(id)
    window.$message.success(res?.message || `Xóa mềm phòng id: ${id} thành công!`)
    fetchRooms(currentPage.value)
  } catch (error: any) {
    const msg =
      error.response?.data?.message ||
      error.message ||
      'Đã xảy ra lỗi khi xóa phòng!'
    window.$message.error(msg)
  }
}

function handleResetSearch() {
  model.value = { ...initialModel }
  fetchRooms(1)
}

function changePage(page: number) {
  fetchRooms(page)
}

// --- Cột bảng ---
const columns: DataTableColumns<PhongResponse> = [
  {
    title: 'Mã / Tên phòng',
    align: 'center',
    key: 'maTenPhong',
    render: (row) => `${row.ma} - ${row.ten}`,
  },
  {
    title: 'Loại phòng',
    align: 'center',
    key: 'loaiPhong',
    render: (row) => row.loaiPhong || '-',
  },
  {
    title: 'Giá',
    align: 'center',
    key: 'price',
    render: (row) => `${row.price?.toLocaleString()} VNĐ`,
  },
  {
    title: 'Sức chứa',
    align: 'center',
    key: 'sucChua',
  },
  {
    title: 'Trạng thái',
    align: 'center',
    key: 'trangThai',
    render: (row) => {
      const colorMap = {
        TRONG: 'success',
        DA_DAT: 'warning',
        DANG_SU_DUNG: 'error',
      } as const
      return h(NTag, { type: colorMap[row.trangThai!] }, { default: () => row.trangThai })
    },
  },
  {
    title: 'Thao tác',
    align: 'center',
    key: 'actions',
    render: (row) =>
      h(NSpace, { justify: 'center' }, {
        default: () => [
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => handleEditTable(row) },
            { default: () => 'Sửa' }
          ),
          h(NPopconfirm, { onPositiveClick: () => handleDeleteRoom(row.id) }, {
            default: () => 'Xác nhận xóa phòng?',
            trigger: () =>
              h(NButton, { size: 'small', type: 'error' }, { default: () => 'Xóa' }),
          }),
        ],
      }),
    },
]

// --- Mounted ---
onMounted(() => {
  fetchRooms()
})
</script>

<template>
  <NSpace vertical size="large">
    <!-- Bộ lọc -->
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="left" inline :show-feedback="false">
        <n-flex wrap gap="12px">
          <n-form-item label="Mã / Tên phòng" path="keyword">
            <n-input v-model:value="model.keyword" placeholder="Nhập mã hoặc tên phòng" />
          </n-form-item>
          <n-form-item label="Tầng" path="tang" style="width: 150px;">
            <n-select v-model:value="model.tang" :options="tangOptions" placeholder="Chọn tầng" />
          </n-form-item>
          <n-form-item label="Loại phòng" path="loaiPhong" style="width: 350px;">
            <n-select v-model:value="model.loaiPhong" :options="loaiPhongOptions" placeholder="Chọn loại" />
          </n-form-item>
          <n-form-item label="Trạng thái" path="trangThai" style="width: 250px;">
            <n-select v-model:value="model.trangThai" :options="trangThaiOptions" placeholder="Chọn trạng thái" />
          </n-form-item>
          <n-form-item label="Giá từ" path="giaMin">
            <n-input-number v-model:value="model.giaMin" placeholder="0" :min="0" />
          </n-form-item>
          <n-form-item label="Đến" path="giaMax">
            <n-input-number v-model:value="model.giaMax" placeholder="0" :min="0" />
          </n-form-item>
          <n-form-item label="Sức chứa từ" path="sucChuaMin">
            <n-input-number v-model:value="model.sucChuaMin" placeholder="0" :min="0" />
          </n-form-item>
          <n-form-item label="Đến" path="sucChuaMax">
            <n-input-number v-model:value="model.sucChuaMax" placeholder="0" :min="0" />
          </n-form-item>

          <n-flex class="ml-auto gap-2">
            <NButton type="primary" @click="() => fetchRooms(1)">Search</NButton>
            <NButton strong secondary @click="handleResetSearch">Reset</NButton>
          </n-flex>
        </n-flex>
      </n-form>
    </n-card>

    <!-- Bảng danh sách -->
    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">Thêm phòng</NButton>
          <NButton strong secondary>Batch Import</NButton>
          <NButton strong secondary class="ml-a">Download</NButton>
        </div>

        <n-data-table :columns="columns" :data="listData" :loading="loading" />

        <Pagination
          :count="totalItems"
          :page.sync="currentPage"
          :page-size="pageSize"
          @change="changePage"
        />

        <TableModal v-model:visible="visible" :type="'add'" :modal-data="null" />
      </NSpace>
    </n-card>
  </NSpace>
</template>
