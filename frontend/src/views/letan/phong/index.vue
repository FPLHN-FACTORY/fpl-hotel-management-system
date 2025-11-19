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
} from 'naive-ui'
import { Tag as TagIcon, Identification, Home, Building, Category, Currency, UserMultiple, ChartLineData, Hotel, GroupPresentation } from '@vicons/carbon'
import TableModal from './components/TableModal.vue'
import { deleteRoom, getAllRooms, getAllTags, getRoomTypes } from '@/service/api/letan/phong'
import type { PhongResponse } from '@/service/api/letan/phong'

const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal, setFalse: closeModal } = useBoolean(false)

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
const tagOptions = ref<{ label: string; value: string }[]>([])

async function fetchLoaiPhong() {
  try {
    const data = await getRoomTypes()
    loaiPhongOptions.value = data.map(lp => ({ label: lp.ten, value: lp.id }))
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}

async function fetchTags() {
  try {
    const data = await getAllTags()
    tagOptions.value = data.map(tag => ({ label: tag.ma, value: tag.id }))
  }
  catch (err: any) {
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
    const params: any = {
      page,
      size: pageSize.value,
    }

    if (model.tuKhoa)
      params.tuKhoa = model.tuKhoa
    if (model.tang !== null)
      params.tang = model.tang
    if (model.loaiPhong)
      params.loaiPhong = model.loaiPhong
    if (model.trangThaiHoatDong)
      params.trangThaiHoatDong = model.trangThaiHoatDong
    if (model.giaMin !== null)
      params.giaMin = model.giaMin
    if (model.giaMax !== null)
      params.giaMax = model.giaMax
    if (model.sucChuaMin !== null)
      params.sucChuaMin = model.sucChuaMin
    if (model.sucChuaMax !== null)
      params.sucChuaMax = model.sucChuaMax
    if (model.tagIds && model.tagIds.length > 0)
      params.tagIds = model.tagIds

    const res = await getAllRooms(params)

    if (res.items.length === 0 && page === 1)
      errorMessage.value = 'Không có phòng phù hợp với tiêu chí lọc'

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
        if (typeof valA === 'number' && typeof valB === 'number')
          return order === 'asc' ? valA - valB : valB - valA

        return 0
      })
    }
  }
  catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách phòng'
    window.$message.error(errorMessage.value)
    listData.value = []
  }
  finally {
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
  }
  catch (error: any) {
    const msg = error.message || 'Đã xảy ra lỗi khi xóa phòng!'
    window.$message.error(msg)
  }
}

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
  if (sortBy.value === column)
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  else {
    sortBy.value = column
    sortOrder.value = 'asc'
  }
  fetchRooms(currentPage.value)
}

function handleRowClick(row: PhongResponse) {
  const index = expandedRowKeys.value.indexOf(row.id)
  if (index > -1) {
    expandedRowKeys.value.splice(index, 1)
  } else {
    expandedRowKeys.value.push(row.id)
  }
}

// Helper function to format capacity range
function getCapacityRange(row: PhongResponse): string {
  if (row.soNguoiQuyDinh && row.sucChua) {
    return `${row.soNguoiQuyDinh}~${row.sucChua}`
  } else if (row.sucChua) {
    return `${row.sucChua}`
  }
  return '-'
}

const columns: DataTableColumns<PhongResponse> = [
  {
    type: 'expand',
    renderExpand: (row) => {
      return h(
        'div',
        { style: 'padding: 20px 32px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; margin: 8px 0;' },
        [
          h('div', { style: 'display: grid; grid-template-columns: repeat(2, minmax(0, max-content)); gap: 32px; justify-content: start;' }, [

            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Home) }),
                  h('span', 'Tên phòng:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.ten)
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#ec4899' }, { default: () => h(Category) }),
                  h('span', 'Loại phòng:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.loaiPhong || '-')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(Currency) }),
                  h('span', 'Giá:')
                ]),
                h('span', { style: 'color: #10b981; font-weight: 600;' }, `${row.price?.toLocaleString('vi-VN')} VNĐ`)
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#f59e0b' }, { default: () => h(UserMultiple) }),
                  h('span', 'Sức chứa (cơ bản~tối đa):')
                ]),
                h('span', { style: 'color: #6b7280;' }, `${getCapacityRange(row)} người`)
              ])
            ]),

            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#06b6d4' }, { default: () => h(Hotel) }),
                  h('span', 'Số giường đơn:')
                ]),
                h('span', { style: 'color: #6b7280;' }, (row as any).soGiuongDon || '-')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Hotel) }),
                  h('span', 'Số giường đôi:')
                ]),
                h('span', { style: 'color: #6b7280;' }, (row as any).soGiuongDoi || '-')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#3b82f6' }, { default: () => h(ChartLineData) }),
                  h('span', 'Trạng thái:')
                ]),
                h(
                  NTag,
                  {
                    type: row.trangThaiHoatDong === 'DANG_HOAT_DONG' ? 'success' : row.trangThaiHoatDong === 'DANG_SUA' ? 'warning' : 'error',
                    size: 'small'
                  },
                  {
                    default: () => row.trangThaiHoatDong === 'DANG_HOAT_DONG' ? 'Hoạt động' : row.trangThaiHoatDong === 'DANG_SUA' ? 'Bảo trì' : 'Ngưng hoạt động'
                  }
                )
              ]),
              row.tags && row.tags.length > 0
                ? h('div', { style: 'display: flex; align-items: flex-start; gap: 12px;' }, [
                    h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                      h(NIcon, { size: 18, color: '#667eea' }, { default: () => h(TagIcon) }),
                      h('span', 'Tags:')
                    ]),
                    h(
                      'div',
                      { style: 'display: flex; flex-wrap: wrap; gap: 6px;' },
                      row.tags.map(tag =>
                        h(
                          NTag,
                          { size: 'small', type: 'info', round: true },
                          { default: () => tag.ma }
                        )
                      )
                    )
                  ])
                : null
            ])
          ])
        ]
      )
    }
  },
  {
    title: 'Tên phòng',
    align: 'center',
    key: 'maTenPhong',
    render: (row) => {
      const tags = row.tags || []

      return h(
        'div',
        {
          style: 'display: inline-flex; align-items: center; position: relative; padding-left: 38px;'
        },
        [
          tags.length > 0
            ? h(
                NTooltip,
                {
                  trigger: 'hover',
                  placement: 'right',
                },
                {
                  trigger: () => h(
                    'div',
                    {
                      style: 'position: absolute; left: -2px; display: inline-flex; align-items: center; justify-content: center; gap: 3px; padding: 3px 6px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 5px; cursor: pointer; transition: all 0.2s; box-shadow: 0 2px 4px rgba(102, 126, 234, 0.2);',
                      onMouseenter: (e: MouseEvent) => {
                        const target = e.currentTarget as HTMLElement
                        target.style.transform = 'translateY(-1px)'
                        target.style.boxShadow = '0 4px 8px rgba(102, 126, 234, 0.3)'
                      },
                      onMouseleave: (e: MouseEvent) => {
                        const target = e.currentTarget as HTMLElement
                        target.style.transform = 'translateY(0)'
                        target.style.boxShadow = '0 2px 4px rgba(102, 126, 234, 0.2)'
                      }
                    },
                    [
                      h(
                        NIcon,
                        {
                          size: 13,
                          color: '#ffffff'
                        },
                        { default: () => h(TagIcon) }
                      ),
                      h(
                        'span',
                        {
                          style: 'font-size: 10px; color: #ffffff; font-weight: 600; line-height: 1;'
                        },
                        tags.length.toString()
                      )
                    ]
                  ),
                  default: () => h(
                    'div',
                    {
                      style: 'display: flex; flex-wrap: wrap; gap: 6px; max-width: 320px; padding: 4px;'
                    },
                    tags.map(tag =>
                      h(
                        NTag,
                        {
                          size: 'small',
                          type: 'info',
                          round: true,
                          bordered: false,
                        },
                        { default: () => tag.ma }
                      )
                    )
                  )
                }
              )
            : null,
          h('div', { style: 'font-weight: 500;' }, row.ten)
        ]
      )
    },
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
          h('span', 'Sức chứa (cơ bản~tối đa)'),
          h('span', { style: `opacity: ${opacity}; font-size: 14px;` }, icon),
        ],
      )
    },
    align: 'center',
    key: 'sucChua',
    render: row => `${getCapacityRange(row)} người`,
  },
  {
    title: 'Trạng thái phòng',
    align: 'center',
    key: 'trangThaiHoatDong',
    render: (row) => {
      const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'error' }> = {
        DANG_HOAT_DONG: { label: 'Hoạt động', type: 'success' },
        DANG_SUA: { label: 'Bảo trì', type: 'warning' },
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

onMounted(() => {
  fetchRooms()
  fetchLoaiPhong()
  fetchTags()
})
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

          <n-form-item-gi :span="12" label="Lọc theo Tags" path="tagIds">
            <NSelect
              v-model:value="model.tagIds"
              :options="tagOptions"
              placeholder="Chọn tags"
              multiple
              clearable
            />
          </n-form-item-gi>

          <n-gi :span="24" class="flex justify-end gap-3">
            <NButton type="primary" @click="fetchRooms(1)">
              Tìm kiếm
            </NButton>
            <NButton strong secondary @click="handleResetSearch">
              Reset
            </NButton>
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

        <n-data-table
          :columns="columns"
          :data="listData"
          :loading="loading"
          :row-key="(row: PhongResponse) => row.id"
          :expanded-row-keys="expandedRowKeys"
          @update:expanded-row-keys="(keys: string[]) => expandedRowKeys = keys"
          :row-props="(row: PhongResponse) => {
            return {
              style: 'cursor: pointer;',
              onClick: () => handleRowClick(row)
            }
          }"
        />

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
          :type="modalType"
          :modal-data="modalData"
          @refresh="fetchRooms(currentPage)"
        />
      </NSpace>
    </n-card>
  </NSpace>
</template>
