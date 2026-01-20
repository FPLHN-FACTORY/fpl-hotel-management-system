<script setup lang="ts">
import { h, ref, reactive, onMounted, watch } from 'vue'
import type { DataTableColumns, FormInst } from 'naive-ui'
import {
  NButton,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NDataTable,
  NPagination,
  NCard,
  NAlert,
  NForm,
  NGrid,
  NPopconfirm
} from 'naive-ui'
import TableModal from './components/TableModal.vue'
import { getAllRoomTypes, deleteRoomType } from '@/service/api/letan/loaiphong'

// ---------------------- Types ----------------------
export interface RoomTypeResponse {
  id: string
  ma: string
  ten: string
  soGiuongDon?: number
  soGiuongDoi?: number
  soNguoiQuyDinh?: number
  soNguoiToiDa?: number
  status?: 'ACTIVE' | 'INACTIVE'
  trangThai?: 'ACTIVE' | 'INACTIVE'
  giaCaNgay?: number
}

// ---------------------- State ----------------------
const loading = ref(false)
const listData = ref<RoomTypeResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')

const initialModel = { tuKhoa: '', trangThai: null as string | null }
const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)

const visible = ref(false)
const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<RoomTypeResponse | null>(null)

const trangThaiLoaiPhongOptions = [
  { label: 'Hoạt động', value: 'ACTIVE' },
  { label: 'Ngưng hoạt động', value: 'INACTIVE' }
]

// ---------------------- Helpers ----------------------
function openAddModal() {
  modalType.value = 'add'
  modalData.value = null
  visible.value = true
}

function openEditModal(row: RoomTypeResponse) {
  modalType.value = 'edit'
  modalData.value = { ...row } // copy dữ liệu
  visible.value = true
}

async function handleDeleteRoomType(id: string) {
  try {
    loading.value = true
    const res = await deleteRoomType(id)
    window.$message.success(res?.message || 'Xóa loại phòng thành công!')
    await fetchRoomTypes(currentPage.value)
  } catch (error: any) {
    window.$message.error(error.message || 'Đã xảy ra lỗi khi xóa loại phòng!')
  } finally {
    loading.value = false
  }
}

// ---------------------- Fetch ----------------------
async function fetchRoomTypes(page = 1) {
  loading.value = true
  errorMessage.value = ''
  try {
    const params: any = { page, size: pageSize.value }
    if (model.tuKhoa) params.tuKhoa = model.tuKhoa
    if (model.trangThai) params.trangThai = model.trangThai

    const res = await getAllRoomTypes(params)
    listData.value = (res?.items || []).map((it: any) => ({
      id: it.id,
      ma: it.ma,
      ten: it.ten,
      soGiuongDon: it.soGiuongDon ?? it.soLuongGiuongDon ?? 0,
      soGiuongDoi: it.soGiuongDoi ?? it.soLuongGiuongDoi ?? 0,
      soNguoiQuyDinh: it.soNguoiQuyDinh ?? it.soLuongNguoiQuyDinh ?? 1,
      soNguoiToiDa: it.soNguoiToiDa ?? it.soLuongNguoiToiDa ?? 1,
      status: it.status ?? it.trangThai ?? 'ACTIVE',
      giaCaNgay: it.giaCaNgay ?? it.giaCaNgay ?? it.price ?? 0
    }))
    totalItems.value = res?.totalItems ?? listData.value.length
    currentPage.value = res?.currentPage ?? page
  } catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách loại phòng'
    window.$message.error(errorMessage.value)
    listData.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => ({ ...model }),
  () => { fetchRoomTypes(1) },
  { deep: true }
)

function changePage(page: number) { fetchRoomTypes(page) }
function handleResetSearch() {
  Object.assign(model, initialModel)
  fetchRoomTypes(1)
}

// ---------------------- Table columns ----------------------
const columns: DataTableColumns<RoomTypeResponse> = [
  { title: 'Mã', key: 'ma', align: 'center' },
  { title: 'Tên loại phòng', key: 'ten', align: 'center' },
  { title: 'Giường đơn', key: 'soGiuongDon', align: 'center' },
  { title: 'Giường đôi', key: 'soGiuongDoi', align: 'center' },
  { title: 'Người quy định', key: 'soNguoiQuyDinh', align: 'center' },
  { title: 'Người tối đa', key: 'soNguoiToiDa', align: 'center' },
  {
    title: 'Giá phòng (VNĐ)',
    key: 'giaCaNgay',
    align: 'center',
    render: row => (row.giaCaNgay ?? 0).toLocaleString('vi-VN') + ' VNĐ'
  },
  {
    title: 'Trạng thái',
    key: 'status',
    align: 'center',
    render: row => {
      const map: Record<string, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
        ACTIVE: { label: 'Hoạt động', type: 'success' },
        INACTIVE: { label: 'Ngưng hoạt động', type: 'error' }
      }
      const st = map[row.status ?? 'ACTIVE'] ?? { label: row.status ?? '-', type: 'info' }
      return h(NTag, { type: st.type }, { default: () => st.label })
    }
  },
  {
    title: 'Thao tác',
    key: 'actions',
    align: 'center',
    render: row =>
      h(NSpace, { justify: 'center' }, {
        default: () => [
          h(NButton, { size: 'small', type: 'primary', onClick: () => openEditModal(row) }, { default: () => 'Sửa' }),
          h(NPopconfirm, {
            onPositiveClick: () => handleDeleteRoomType(row.id),
            positiveText: 'Xác nhận',
            negativeText: 'Hủy'
          }, {
            default: () => 'Xác nhận xóa loại phòng?',
            trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => 'Xóa' })
          })
        ]
      })
  }
]

// ---------------------- Mounted ----------------------
onMounted(() => { fetchRoomTypes(1) })
</script>

<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="left" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="8">
          <n-form-item-gi :span="12" label="Mã / Tên" path="tuKhoa">
            <NInput v-model:value="model.tuKhoa" placeholder="Nhập mã hoặc tên" clearable />
          </n-form-item-gi>
          <n-form-item-gi :span="8" label="Trạng thái" path="trangThai">
            <NSelect v-model:value="model.trangThai" :options="trangThaiLoaiPhongOptions" placeholder="Chọn trạng thái"
              clearable />
          </n-form-item-gi>
          <n-form-item-gi :span="4" class="flex items-center justify-end">
            <NButton strong secondary @click="handleResetSearch">Làm mới</NButton>
          </n-form-item-gi>
        </n-grid>
      </n-form>
    </n-card>

    <n-card>
      <div class="flex gap-4 mb-3">
        <NButton type="primary" @click="openAddModal">Thêm loại phòng</NButton>
      </div>

      <n-alert v-if="errorMessage && listData.length === 0" type="warning" :title="errorMessage" />

      <n-data-table :columns="columns" :data="listData" :loading="loading" />

      <div class="mt-4 flex justify-end">
        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size) => { pageSize = size; fetchRoomTypes(1) }">
          <template #prefix>Tổng {{ totalItems }} loại phòng</template>
        </n-pagination>
      </div>
    </n-card>

    <TableModal v-model:visible="visible" :type="modalType" :modal-data="modalData"
      @refresh="fetchRoomTypes(currentPage)" />
  </NSpace>
</template>

<style scoped>
/* Tăng font size cho toàn bộ trang loại phòng */
:deep(.n-card-header__main) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.n-form-item-label) {
  font-size: 17px;
}

:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-input-number-input),
:deep(.n-button__content) {
  font-size: 17px;
}

:deep(.n-data-table-th),
:deep(.n-data-table-td) {
  font-size: 17px;
}

:deep(.n-data-table-th__title) {
  font-size: 17px;
  font-weight: 600;
}

:deep(.n-tag) {
  font-size: 17px;
}

:deep(.n-pagination) {
  font-size: 17px;
}

:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-alert) {
  font-size: 17px;
}
</style>
