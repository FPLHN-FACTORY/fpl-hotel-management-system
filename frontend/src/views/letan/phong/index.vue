<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import type { DataTableColumns, FormInst } from 'naive-ui'
import { useBoolean } from '@/hooks'
import {
  NButton,
  NInput,
  NInputNumber,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
} from 'naive-ui'
import TableModal from './components/TableModal.vue'
import { deleteRoom, getAllRooms, getRoomTypes } from '@/service/api/letan/phong'
import type { PhongResponse } from '@/service/api/letan/phong'

// --- Loading & Modal ---
const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal } = useBoolean(false)

// --- Form tìm kiếm ---
const initialModel = {
  tuKhoa: '',
  tang: null as number | null,
  loaiPhong: null as string | null,
  trangThaiHoatDong: null as string | null,
  giaMin: null as number | null,
  giaMax: null as number | null,
  sucChuaMin: null as number | null,
  sucChuaMax: null as number | null,
}

const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)

// --- Options combobox ---
const tangOptions = [1, 2, 3].map(t => ({ label: `Tầng ${t}`, value: t }))
// --- Loại phòng fetch từ API ---
const loaiPhongOptions = ref<{ label: string; value: string }[]>([])

async function fetchLoaiPhong() {
  try {
    const data = await getRoomTypes()
    loaiPhongOptions.value =  data.map(lp => ({ label: lp.ten, value: lp.id }))
  } catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}
const trangThaiOptions = [
  { label: 'Hoạt đông', value: 'HOAT_DONG' },
  { label: 'Bảo trì', value: 'BAO_TRI' },
  { label: 'Ngưng hoạt động', value: 'NGUNG_HOAT_DONG' },
]

// --- Dữ liệu bảng ---
const listData = ref<PhongResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')

// --- Sorting ---
const sortBy = ref<string>('')
const sortOrder = ref<'asc' | 'desc'>('asc')

// --- Hàm fetch phòng ---
async function fetchRooms(page = 1) {
  startLoading()
  errorMessage.value = ''
  try {
    const params: any = {
      page,
      size: pageSize.value,
    }

    if (model.tuKhoa) params.tuKhoa = model.tuKhoa
    if (model.tang !== null) params.tang = model.tang
    if (model.loaiPhong) params.loaiPhong = model.loaiPhong
    if (model.trangThaiHoatDong) params.trangThaiHoatDong = model.trangThaiHoatDong
    if (model.giaMin !== null) params.giaMin = model.giaMin
    if (model.giaMax !== null) params.giaMax = model.giaMax
    if (model.sucChuaMin !== null) params.sucChuaMin = model.sucChuaMin
    if (model.sucChuaMax !== null) params.sucChuaMax = model.sucChuaMax

    const res = await getAllRooms(params)

    if (res.items.length === 0 && page === 1) {
      errorMessage.value = 'Không có phòng phù hợp với tiêu chí lọc'
    }

    listData.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage

if (sortBy.value) {
  const key = sortBy.value
  const order = sortOrder.value
  listData.value.sort((a: any, b: any) => {
    const valA = a[key] ?? 0
    const valB = b[key] ?? 0
    if (typeof valA === 'string' && typeof valB === 'string') {
      return order === 'asc'
        ? valA.localeCompare(valB)
            : valB.localeCompare(valA)
    }
    if (typeof valA === 'number' && typeof valB === 'number') {
      return order === 'asc' ? valA - valB : valB - valA
    }
    return 0
  })
}

  } catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách phòng'
    window.$message.error(errorMessage.value)
    listData.value = []
  } finally {
    endLoading()
  }
}

// --- Watch tự động lọc ---
watch(
  () => ({ ...model }),
  () => {
    fetchRooms(1)
  },
  { deep: true },
)

// --- Hành động ---
function handleEditTable(row: PhongResponse) {
  window.$message.info(`Chỉnh sửa phòng: ${row.ten}`)
  openModal()
}

function handleAddTable() {
  openModal()
}

async function handleDeleteRoom(id: string) {
  try {
    const res = await deleteRoom(id)
    window.$message.success(res?.message || 'Xóa phòng thành công!')
    fetchRooms(currentPage.value)
  } catch (error: any) {
    const msg = error.message || 'Đã xảy ra lỗi khi xóa phòng!'
    window.$message.error(msg)
  }
}

// ✅ Reset toàn bộ filters + reload bảng
function handleResetSearch() {
  Object.assign(model, initialModel)
  sortBy.value = ''
  sortOrder.value = 'asc'
  fetchRooms(1)
}

function changePage(page: number) {
  fetchRooms(page)
}

function handleSort(column: string) {
  if (sortBy.value === column) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = column
    sortOrder.value = 'asc'
  }
  fetchRooms(currentPage.value)
}

function getSortIcon(column: string) {
  if (sortBy.value !== column) return ''
  return sortOrder.value === 'asc' ? ' ↑' : ' ↓'
}

// --- Cột bảng ---
const columns: DataTableColumns<PhongResponse> = [
  {
    title: 'Mã / Tên phòng',
    align: 'center',
    key: 'maTenPhong',
    render: row => `${row.ma} - ${row.ten}`,
  },
  {
    title: 'Tầng',
    align: 'center',
    key: 'tang',
  },
  {
    title: 'Loại phòng',
    align: 'center',
    key: 'loaiPhong',
    render: row => row.loaiPhong || '-',
  },
  {
    title: () => {
      const isActive = sortBy.value === 'price'
      const icon = isActive ? (sortOrder.value === 'asc' ? ' ↑' : ' ↓') : ' ↕'
      const opacity = isActive ? '1' : '0.3'
      return h(
        'div',
        {
          onClick: () => handleSort('price'),
          style: `cursor: pointer; user-select: none; display: flex; align-items: center; justify-content: center; gap: 4px;`,
        },
        [
          h('span', 'Giá'),
          h('span', { style: `opacity: ${opacity}; font-size: 14px;` }, icon),
        ],
      )
    },
    align: 'center',
    key: 'price',
    render: row => `${row.price?.toLocaleString('vi-VN')} VNĐ`,
  },
  {
    title: () => {
      const isActive = sortBy.value === 'sucChua'
      const icon = isActive ? (sortOrder.value === 'asc' ? ' ↑' : ' ↓') : ' ↕'
      const opacity = isActive ? '1' : '0.3'
      return h(
        'div',
        {
          onClick: () => handleSort('sucChua'),
          style: `cursor: pointer; user-select: none; display: flex; align-items: center; justify-content: center; gap: 4px;`,
        },
        [
          h('span', 'Sức chứa'),
          h('span', { style: `opacity: ${opacity}; font-size: 14px;` }, icon),
        ],
      )
    },
    align: 'center',
    key: 'sucChua',
  },
  {
    title: 'Trạng thái phòng',
    align: 'center',
    key: 'trangThaiHoatDong',
    render: (row) => {
      const statusMap: Record<string, { label: string, type: 'success' | 'warning' | 'error' }> = {
        HOAT_DONG: { label: 'Hoạt động', type: 'success' },
        BAO_TRI: { label: 'Bảo trì', type: 'warning' },
        NGUNG_HOAT_DONG: { label: 'Ngưng hoạt động', type: 'error' },
      }

      const status = statusMap[row.trangThaiHoatDong] || { label: row.trangThaiHoatDong, type: 'info' }
      return h(NTag, { type: status.type }, { default: () => status.label })
    },
  },
  {
    title: 'Thao tác',
    align: 'center',
    key: 'actions',
    render: row =>
      h(NSpace, { justify: 'center' }, {
        default: () => [
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => handleEditTable(row) },
            { default: () => 'Sửa' },
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
  fetchRooms(),
  fetchLoaiPhong()
})
</script>


<template>
  <NSpace vertical size="large">
   <n-card>
  <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
    <n-grid :cols="24" :x-gap="12" :y-gap="12">
      
      <!-- Hàng 1: Thông tin cơ bản -->
      <n-form-item-gi :span="6" label="Mã / Tên phòng" path="tuKhoa">
        <NInput v-model:value="model.tuKhoa" placeholder="Nhập mã hoặc tên phòng" clearable />
      </n-form-item-gi>

      <n-form-item-gi :span="4" label="Tầng" path="tang">
        <NSelect v-model:value="model.tang" :options="tangOptions" placeholder="Chọn tầng" clearable />
      </n-form-item-gi>

      <n-form-item-gi :span="6" label="Loại phòng" path="loaiPhong">
        <NSelect v-model:value="model.loaiPhong" :options="loaiPhongOptions" placeholder="Chọn loại" clearable />
      </n-form-item-gi>

      <n-form-item-gi :span="6" label="Trạng thái" path="trangThaiHoatDong">
        <NSelect v-model:value="model.trangThaiHoatDong" :options="trangThaiOptions" placeholder="Chọn" clearable />
      </n-form-item-gi>

      <!-- Hàng 2: Giá phòng -->
      <n-form-item-gi :span="6" label="Giá từ" path="giaMin">
        <NInputNumber
          v-model:value="model.giaMin"
          placeholder="0"
          :min="0"
          :show-button="false"
          clearable
        />
      </n-form-item-gi>

      <n-form-item-gi :span="6" label="Đến" path="giaMax">
        <NInputNumber
          v-model:value="model.giaMax"
          placeholder="0"
          :min="0"
          :show-button="false"
          clearable
        />
      </n-form-item-gi>

      <!-- Hàng 3: Sức chứa -->
      <n-form-item-gi :span="6" label="Sức chứa từ" path="sucChuaMin">
        <NInputNumber
          v-model:value="model.sucChuaMin"
          placeholder="0"
          :min="0"
          :show-button="false"
          clearable
        />
      </n-form-item-gi>

      <n-form-item-gi :span="6" label="Đến" path="sucChuaMax">
        <NInputNumber
          v-model:value="model.sucChuaMax"
          placeholder="0"
          :min="0"
          :show-button="false"
          clearable
        />
      </n-form-item-gi>

      <!-- Hàng 4: Nút hành động -->
      <n-gi :span="24" class="flex justify-end gap-3">
        <NButton type="primary" @click="fetchRooms(1)">Tìm kiếm</NButton>
        <NButton strong secondary @click="handleResetSearch">Reset</NButton>
      </n-gi>

    </n-grid>
  </n-form>
</n-card>

    <!-- Bảng danh sách -->
    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">
            Thêm phòng
          </NButton>
          <NButton strong secondary>
            Batch Import
          </NButton>
          <NButton strong secondary class="ml-a">
            Download
          </NButton>
        </div>


        <n-data-table :columns="columns" :data="listData" :loading="loading" />

        <n-pagination
          v-model:page="currentPage"
          :page-count="Math.ceil(totalItems / pageSize)"
          :page-size="pageSize"
          show-size-picker
          :page-sizes="[10, 20, 30, 50]"
          @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchRooms(1) }"
        >
          <template #prefix>
            Tổng {{ totalItems }} phòng
          </template>
        </n-pagination>

        <TableModal
          v-model:visible="visible"
          type="add"
          :modal-data="null"
          @refresh="fetchRooms(currentPage)"
        />
      </NSpace>
    </n-card>
  </NSpace>
</template>
