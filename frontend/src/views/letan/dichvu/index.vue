<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import type { DataTableColumns } from 'naive-ui'
import { useBoolean } from '@/hooks'
import {
  NButton,
  NInput,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  NAlert,
  NIcon,
  NTooltip,
} from 'naive-ui'
import { ShoppingBag, Money, DataTable } from '@vicons/carbon'
import DichVuModal from './components/DichVuModal.vue'
import { searchDichVu, deleteDichVu } from '@/service/api/letan/dichvu'
import type { DichVuResponse } from '@/service/api/letan/dichvu'

const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal } = useBoolean(false)

const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<{ id: string } | null>(null)

const initialModel = {
  q: '',
  trangThai: null as number | null,
}

const model = reactive({ ...initialModel })

const trangThaiOptions = [
  { label: 'Hoạt động', value: 0 },
  { label: 'Ngưng hoạt động', value: 1 },
  { label: 'Đã xóa', value: 2 },
]

const listData = ref<DichVuResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')
const sortBy = ref<string>('')
const sortOrder = ref<'asc' | 'desc'>('asc')
const expandedRowKeys = ref<string[]>([])

async function fetchDichVu(page = 1) {
  startLoading()
  errorMessage.value = ''
  try {
    const params: any = { page: page - 1, size: pageSize.value }
    if (model.q) params.q = model.q
    if (model.trangThai !== null) params.trangThai = model.trangThai

    const res = await searchDichVu(params)
    if (res.data.content.length === 0 && page === 1)
      errorMessage.value = 'Không có dịch vụ phù hợp với tiêu chí lọc'

    listData.value = res.data.content
    totalItems.value = res.data.totalElements
    currentPage.value = res.data.number + 1

    if (sortBy.value) {
      const key = sortBy.value
      const order = sortOrder.value
      listData.value.sort((a: any, b: any) => {
        const valA = a[key] ?? 0
        const valB = b[key] ?? 0
        if (typeof valA === 'string' && typeof valB === 'string')
          return order === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA)
        if (typeof valA === 'number' && typeof valB === 'number')
          return order === 'asc' ? valA - valB : valB - valA
        return 0
      })
    }
  }
  catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách dịch vụ'
    window.$message.error(errorMessage.value)
    listData.value = []
  }
  finally {
    endLoading()
  }
}

watch(() => ({ ...model }), () => fetchDichVu(1), { deep: true })

function handleEditTable(row: DichVuResponse) {
  modalType.value = 'edit'
  modalData.value = { id: row.id }
  openModal()
}

function handleAddTable() {
  modalType.value = 'add'
  modalData.value = null
  openModal()
}

async function handleDeleteDichVu(id: string) {
  try {
    const res = await deleteDichVu(id)
    window.$message.success(res?.message || 'Xóa dịch vụ thành công!')
    fetchDichVu(currentPage.value)
  }
  catch (error: any) {
    window.$message.error(error.message || 'Đã xảy ra lỗi khi xóa dịch vụ!')
  }
}

function handleResetSearch() {
  Object.assign(model, initialModel)
  sortBy.value = ''
  sortOrder.value = 'asc'
  fetchDichVu(1)
}

function changePage(page: number) {
  fetchDichVu(page)
}

function handleSort(column: string) {
  if (sortBy.value === column)
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  else {
    sortBy.value = column
    sortOrder.value = 'asc'
  }
  fetchDichVu(currentPage.value)
}

const columns: DataTableColumns<DichVuResponse> = [
  {
    type: 'expand',
    renderExpand: row => h('div', { style: 'padding: 20px 32px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; margin: 8px 0;' }, [
      h('div', { style: 'display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 32px; width: 100%;' }, [
        h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [
              h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(ShoppingBag) }),
              h('span', 'Tên dịch vụ:'),
            ]),
            h('span', { style: 'color: #6b7280;' }, row.tenDichVu),
          ]),
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 180px;' }, [
              h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(DataTable) }),
              h('span', 'Đơn vị tính:'),
            ]),
            h('span', { style: 'color: #6b7280;' }, row.donViTinh),
          ]),
        ]),
        h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
          h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
              h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(Money) }),
              h('span', 'Đơn giá:'),
            ]),
            h('span', { style: 'color: #10b981; font-weight: 600;' }, `${row.donGia?.toLocaleString('vi-VN')} VNĐ`),
          ]),
          row.moTa
            ? h('div', { style: 'display: flex; align-items: flex-start; gap: 12px;' }, [
              h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                h('span', 'Mô tả:'),
              ]),
              h('span', { style: 'color: #6b7280; max-width: 300px;' }, row.moTa),
            ])
            : null,
        ]),
      ]),
    ]),
  },
  {
    title: 'Mã dịch vụ',
    align: 'center',
    key: 'maDichVu',
    render: row => h('div', { style: 'font-weight: 500;' }, row.maDichVu),
  },
  {
    title: 'Tên dịch vụ',
    align: 'center',
    key: 'tenDichVu',
  },
  {
    title: 'Đơn vị tính',
    align: 'center',
    key: 'donViTinh',
  },
  {
    title: () => h('div', {
      onClick: () => handleSort('donGia'),
      style: 'cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 4px;',
    }, [
      h('span', 'Đơn giá'),
      h('span', { style: `opacity: ${sortBy.value === 'donGia' ? '1' : '0.3'}; font-size: 14px;` }, sortBy.value === 'donGia' ? (sortOrder.value === 'asc' ? ' ↑' : ' ↓') : ' ↕'),
    ]),
    align: 'center',
    key: 'donGia',
    render: row => `${row.donGia?.toLocaleString('vi-VN')} VNĐ`,
  },
  {
    title: 'Trạng thái',
    align: 'center',
    key: 'trangThai',
    render: (row) => {
      const statusMap: Record<number, { label: string, type: 'success' | 'warning' | 'error' }> = {
        0: { label: 'Hoạt động', type: 'success' },
        1: { label: 'Ngưng hoạt động', type: 'warning' },
        2: { label: 'Đã xóa', type: 'error' },
      }
      const status = statusMap[row.trangThai] || { label: 'Không xác định', type: 'warning' }
      return h(NTag, { type: status.type }, { default: () => status.label })
    },
  },
  {
    title: 'Thao tác',
    align: 'center',
    key: 'actions',
    render: row => h(NSpace, { justify: 'center' }, {
      default: () => [
        h(NButton, {
          size: 'small',
          type: 'primary',
          onClick: () => handleEditTable(row),
        }, { default: () => 'Sửa' }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDeleteDichVu(row.id),
        }, {
          default: () => 'Xác nhận xóa dịch vụ?',
          trigger: () => h(NButton, {
            size: 'small',
            type: 'error',
          }, { default: () => 'Xóa' }),
        }),
      ],
    }),
  },
]

onMounted(() => {
  fetchDichVu()
})
</script>

<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="12">
          <n-form-item-gi :span="8" label="Tìm kiếm" path="q">
            <NInput v-model:value="model.q" placeholder="Nhập mã hoặc tên dịch vụ" clearable />
          </n-form-item-gi>
          <n-form-item-gi :span="6" label="Trạng thái" path="trangThai">
            <NSelect v-model:value="model.trangThai" :options="trangThaiOptions" placeholder="Chọn trạng thái"
              clearable />
          </n-form-item-gi>
          <n-gi :span="24" class="flex justify-end gap-3">
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-button quaternary circle @click="handleResetSearch">
                  <template #icon>
                    <nova-icon icon="carbon:reset" />
                  </template>
                </n-button>
              </template>
              Làm mới
            </n-tooltip>
          </n-gi>
        </n-grid>
      </n-form>
    </n-card>

    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">
            Thêm dịch vụ
          </NButton>
        </div>

        <NAlert v-if="errorMessage && listData.length === 0" type="warning" :bordered="false">
          {{ errorMessage }}
        </NAlert>

        <n-data-table :columns="columns" :data="listData" :loading="loading"
          :row-key="(row: DichVuResponse) => row.id" :expanded-row-keys="expandedRowKeys"
          @update:expanded-row-keys="(keys: any) => expandedRowKeys = keys" />

        <div class="mt-4 flex justify-start">
          <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
            show-size-picker circle :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
            @update:page-size="(size: number) => { pageSize = size; fetchDichVu(1) }">
            <template #prefix>
              Tổng {{ totalItems }} dịch vụ
            </template>
          </n-pagination>
        </div>

        <DichVuModal v-model:visible="visible" :type="modalType" :modal-data="modalData"
          @refresh="fetchDichVu(currentPage)" />
      </NSpace>
    </n-card>
  </NSpace>
</template>

<style scoped>
/* Tăng font size cho toàn bộ trang */
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

:deep(.n-tag),
:deep(.n-pagination),
:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-alert__content) {
  font-size: 17px;
}
</style>
