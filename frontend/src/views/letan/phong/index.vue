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
  NTooltip,
  NIcon,
  NAlert,
} from 'naive-ui'
import { Tag as TagIcon, Home, Category, Currency, UserMultiple, ChartLineData, Hotel } from '@vicons/carbon'
import TableModal from './components/TableModal.vue'
import { deleteRoom, getAllRooms, getAllTags, getRoomTypes } from '@/service/api/letan/phong'
import type { PhongResponse } from '@/service/api/letan/phong'

const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal } = useBoolean(false)

const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<{ id: string } | null>(null)

const initialModel = {
  tuKhoa: '',
  tang: null as number | null,
  loaiPhong: null as string | null,
  trangThaiHoatDong: null as string | null,
  giaMin: null as number | null,
  giaMax: null as number | null,
  sucChuaMin: null as number | null,
  sucChuaMax: null as number | null,
  tagIds: [] as string[],
}

const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)

const tangOptions = [1, 2, 3].map(t => ({ label: `Tầng ${t}`, value: t }))
const loaiPhongOptions = ref<{ label: string; value: string }[]>([])
const tagOptions = ref<{ label: string; value: string; color?: string }[]>([])

async function fetchLoaiPhong() {
  try {
    const data = await getRoomTypes()
    loaiPhongOptions.value = data.map(lp => ({ label: lp.ten, value: lp.id }))
  } catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}

async function fetchTags() {
  try {
    const data = await getAllTags()
    tagOptions.value = data.map(tag => ({
      label: tag.ma,
      value: tag.id,
      color: (tag as any).mau || undefined
    }))
  } catch (err: any) {
    window.$message.error(err.message || 'Không thể tải danh sách tags')
  }
}

const trangThaiOptions = [
  { label: 'Hoạt động', value: 'DANG_HOAT_DONG' },
  { label: 'Bảo trì', value: 'DANG_SUA' },
  { label: 'Ngưng hoạt động', value: 'NGUNG_HOAT_DONG' },
]

const listData = ref<PhongResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')
const sortBy = ref<string>('')
const sortOrder = ref<'asc' | 'desc'>('asc')
const expandedRowKeys = ref<string[]>([])

async function fetchRooms(page = 1) {
  startLoading()
  errorMessage.value = ''
  try {
    const params: any = { page, size: pageSize.value }
    if (model.tuKhoa) params.tuKhoa = model.tuKhoa
    if (model.tang !== null) params.tang = model.tang
    if (model.loaiPhong) params.loaiPhong = model.loaiPhong
    if (model.trangThaiHoatDong) params.trangThaiHoatDong = model.trangThaiHoatDong
    if (model.giaMin !== null) params.giaMin = model.giaMin
    if (model.giaMax !== null) params.giaMax = model.giaMax
    if (model.sucChuaMin !== null) params.sucChuaMin = model.sucChuaMin
    if (model.sucChuaMax !== null) params.sucChuaMax = model.sucChuaMax
    if (model.tagIds && model.tagIds.length > 0) params.tagIds = model.tagIds

    const res = await getAllRooms(params)
    if (res.items.length === 0 && page === 1) errorMessage.value = 'Không có phòng phù hợp với tiêu chí lọc'
    listData.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage

    if (sortBy.value) {
      const key = sortBy.value, order = sortOrder.value
      listData.value.sort((a: any, b: any) => {
        const valA = a[key] ?? 0, valB = b[key] ?? 0
        if (typeof valA === 'string' && typeof valB === 'string')
          return order === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA)
        if (typeof valA === 'number' && typeof valB === 'number')
          return order === 'asc' ? valA - valB : valB - valA
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

watch(() => ({ ...model }), () => fetchRooms(1), { deep: true })

function handleEditTable(row: PhongResponse) {
  modalType.value = 'edit'
  modalData.value = { id: row.id }
  openModal()
}

function handleAddTable() {
  modalType.value = 'add'
  modalData.value = null
  openModal()
}

async function handleDeleteRoom(id: string) {
  try {
    const res = await deleteRoom(id)
    window.$message.success(res?.message || 'Xóa phòng thành công!')
    fetchRooms(currentPage.value)
  } catch (error: any) {
    window.$message.error(error.message || 'Đã xảy ra lỗi khi xóa phòng!')
  }
}

function handleResetSearch() {
  Object.assign(model, initialModel)
  sortBy.value = ''
  sortOrder.value = 'asc'
  fetchRooms(1)
}

function changePage(page: number) { fetchRooms(page) }

function handleSort(column: string) {
  if (sortBy.value === column) sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  else { sortBy.value = column; sortOrder.value = 'asc' }
  fetchRooms(currentPage.value)
}

function getCapacityRange(row: PhongResponse): string {
  if (row.soNguoiQuyDinh && row.sucChua) return `${row.soNguoiQuyDinh}~${row.sucChua}`
  else if (row.sucChua) return `${row.sucChua}`
  return '-'
}

function getTagColor(tag: any): string { return tag.mau || '#667eea' }

const columns: DataTableColumns<PhongResponse> = [
  {
    type: 'expand',
    renderExpand: (row) => h('div', { style: 'padding: 20px 32px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; margin: 8px 0;' }, [
      h('div', { style: 'display: grid; grid-template-columns: repeat(2, minmax(0, max-content)); gap: 32px;' }, [
        h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Home) }), h('span', 'Tên phòng:')]),
            h('span', { style: 'color: #6b7280;' }, row.ten)
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [h(NIcon, { size: 18, color: '#ec4899' }, { default: () => h(Category) }), h('span', 'Loại phòng:')]),
            h('span', { style: 'color: #6b7280;' }, row.loaiPhong || '-')
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(Currency) }), h('span', 'Giá:')]),
            h('span', { style: 'color: #10b981; font-weight: 600;' }, `${row.price?.toLocaleString('vi-VN')} VNĐ`)
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [h(NIcon, { size: 18, color: '#f59e0b' }, { default: () => h(UserMultiple) }), h('span', 'Sức chứa (cơ bản~tối đa):')]),
            h('span', { style: 'color: #6b7280;' }, `${getCapacityRange(row)} người`)
          ])
        ]),
        h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [h(NIcon, { size: 18, color: '#06b6d4' }, { default: () => h(Hotel) }), h('span', 'Số giường đơn:')]),
            h('span', { style: 'color: #6b7280;' }, (row as any).soGiuongDon || '-')
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Hotel) }), h('span', 'Số giường đôi:')]),
            h('span', { style: 'color: #6b7280;' }, (row as any).soGiuongDoi || '-')
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [h(NIcon, { size: 18, color: '#3b82f6' }, { default: () => h(ChartLineData) }), h('span', 'Trạng thái:')]),
            h(NTag, { type: row.trangThaiHoatDong === 'DANG_HOAT_DONG' ? 'success' : row.trangThaiHoatDong === 'DANG_SUA' ? 'warning' : 'error', size: 'small' }, { default: () => row.trangThaiHoatDong === 'DANG_HOAT_DONG' ? 'Hoạt động' : row.trangThaiHoatDong === 'DANG_SUA' ? 'Bảo trì' : 'Ngưng hoạt động' })
          ]),
          row.tags && row.tags.length > 0 ? h('div', { style: 'display: flex; align-items: flex-start; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [h(NIcon, { size: 18, color: '#667eea' }, { default: () => h(TagIcon) }), h('span', 'Tags:')]),
            h('div', { style: 'display: flex; flex-wrap: wrap; gap: 6px;' }, row.tags.map(tag => h(NTag, { size: 'small', round: true, color: { color: getTagColor(tag), textColor: '#fff', borderColor: getTagColor(tag) } }, { default: () => tag.ma })))
          ]) : null
        ])
      ])
    ])
  },
  {
    title: 'Tên phòng', align: 'center', key: 'maTenPhong',
    render: (row) => {
      const tags = row.tags || []
      return h('div', { style: 'display: inline-flex; align-items: center; position: relative; padding-left: 38px;' }, [
        tags.length > 0 ? h(NTooltip, { trigger: 'hover', placement: 'right' }, {
          trigger: () => h('div', { style: 'position: absolute; left: -2px; display: inline-flex; align-items: center; gap: 3px; padding: 3px 6px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 5px; cursor: pointer;' }, [h(NIcon, { size: 13, color: '#fff' }, { default: () => h(TagIcon) }), h('span', { style: 'font-size: 10px; color: #fff; font-weight: 600;' }, tags.length.toString())]),
          default: () => h('div', { style: 'display: flex; flex-wrap: wrap; gap: 6px; max-width: 320px; padding: 4px;' }, tags.map(tag => h(NTag, { size: 'small', round: true, bordered: false, color: { color: getTagColor(tag), textColor: '#fff', borderColor: getTagColor(tag) } }, { default: () => tag.ma })))
        }) : null,
        h('div', { style: 'font-weight: 500;' }, row.ten)
      ])
    }
  },
  { title: 'Loại phòng', align: 'center', key: 'loaiPhong', render: row => row.loaiPhong || '-' },
  {
    title: () => h('div', { onClick: () => handleSort('price'), style: 'cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px;' }, [h('span', 'Giá'), h('span', { style: `opacity: ${sortBy.value === 'price' ? '1' : '0.3'}; font-size: 14px;` }, sortBy.value === 'price' ? (sortOrder.value === 'asc' ? ' ↑' : ' ↓') : ' ↕')]),
    align: 'center', key: 'price', render: row => `${row.price?.toLocaleString('vi-VN')} VNĐ`
  },
  {
    title: () => h('div', { onClick: () => handleSort('sucChua'), style: 'cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px;' }, [h('span', 'Sức chứa'), h('span', { style: `opacity: ${sortBy.value === 'sucChua' ? '1' : '0.3'}; font-size: 14px;` }, sortBy.value === 'sucChua' ? (sortOrder.value === 'asc' ? ' ↑' : ' ↓') : ' ↕')]),
    align: 'center', key: 'sucChua', render: row => `${getCapacityRange(row)} người`
  },
  {
    title: 'Trạng thái', align: 'center', key: 'trangThaiHoatDong',
    render: (row) => {
      const m: Record<string, { label: string; type: 'success' | 'warning' | 'error' }> = { DANG_HOAT_DONG: { label: 'Hoạt động', type: 'success' }, DANG_SUA: { label: 'Bảo trì', type: 'warning' }, NGUNG_HOAT_DONG: { label: 'Ngưng hoạt động', type: 'error' } }
      const s = m[row.trangThaiHoatDong] || { label: row.trangThaiHoatDong, type: 'info' }
      return h(NTag, { type: s.type }, { default: () => s.label })
    }
  },
  {
    title: 'Thao tác', align: 'center', key: 'actions',
    render: row => h(NSpace, { justify: 'center' }, { default: () => [h(NButton, { size: 'small', type: 'primary', onClick: () => handleEditTable(row) }, { default: () => 'Sửa' }), h(NPopconfirm, { onPositiveClick: () => handleDeleteRoom(row.id) }, { default: () => 'Xác nhận xóa phòng?', trigger: () => h(NButton, { size: 'small', type: 'error' }, { default: () => 'Xóa' }) })] })
  }
]

onMounted(() => { fetchRooms(); fetchLoaiPhong(); fetchTags() })
</script>

<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="12">
          <n-form-item-gi :span="6" label="Tên phòng" path="tuKhoa">
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
          <n-form-item-gi :span="6" label="Khoảng giá (VNĐ)">
            <div class="range-picker">
              <NInputNumber v-model:value="model.giaMin" placeholder="Từ" :min="0" :show-button="false" clearable
                class="range-input" />
              <span class="range-separator">→</span>
              <NInputNumber v-model:value="model.giaMax" placeholder="Đến" :min="0" :show-button="false" clearable
                class="range-input" />
            </div>
          </n-form-item-gi>
          <n-form-item-gi :span="6" label="Sức chứa (người)">
            <div class="range-picker">
              <NInputNumber v-model:value="model.sucChuaMin" placeholder="Từ" :min="0" :show-button="false" clearable
                class="range-input" />
              <span class="range-separator">→</span>
              <NInputNumber v-model:value="model.sucChuaMax" placeholder="Đến" :min="0" :show-button="false" clearable
                class="range-input" />
            </div>
          </n-form-item-gi>
          <n-form-item-gi :span="10" label="Lọc theo Tags" path="tagIds">
            <NSelect v-model:value="model.tagIds" :options="tagOptions" placeholder="Chọn tags" multiple clearable />
          </n-form-item-gi>
          <n-gi :span="24" class="flex justify-end gap-3">
            <NButton strong secondary @click="handleResetSearch">Làm mới</NButton>
          </n-gi>
        </n-grid>
      </n-form>
    </n-card>
    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">Thêm phòng</NButton>
        </div>

        <NAlert v-if="errorMessage && listData.length === 0" type="warning" :bordered="false">
          {{ errorMessage }}
        </NAlert>

        <n-data-table :columns="columns" :data="listData" :loading="loading" :row-key="(row: PhongResponse) => row.id"
          :expanded-row-keys="expandedRowKeys" @update:expanded-row-keys="(keys: string[]) => expandedRowKeys = keys" />
        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchRooms(1) }">
          <template #prefix>Tổng {{ totalItems }} phòng</template>
        </n-pagination>
        <TableModal v-model:visible="visible" :type="modalType" :modal-data="modalData"
          @refresh="fetchRooms(currentPage)" />
      </NSpace>
    </n-card>
  </NSpace>
</template>

<style scoped>
.range-picker {
  display: flex;
  align-items: center;
  border: 1px solid #e0e0e6;
  border-radius: 3px;
  background: #fff;
  padding: 0 8px;
  transition: border-color 0.3s, box-shadow 0.3s;
  width: 100%;
}

.range-picker:hover {
  border-color: #18a058;
}

.range-picker:focus-within {
  border-color: #18a058;
  box-shadow: 0 0 0 2px rgba(24, 160, 88, 0.2);
}

.range-input {
  flex: 1;
}

.range-input :deep(.n-input) {
  --n-border: none !important;
  --n-border-hover: none !important;
  --n-border-focus: none !important;
  --n-box-shadow-focus: none !important;
}

.range-input :deep(.n-input-wrapper) {
  padding: 0 4px;
}

.range-separator {
  color: #999;
  padding: 0 8px;
  font-weight: 500;
  flex-shrink: 0;
}

.ml-a {
  margin-left: auto;
}
</style>
