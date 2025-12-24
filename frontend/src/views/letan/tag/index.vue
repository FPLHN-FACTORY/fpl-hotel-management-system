<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import type { DataTableColumns, FormInst } from 'naive-ui'
import { useBoolean } from '@/hooks'
import {
  NButton,
  NIcon,
  NInput,
  NInputNumber,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
} from 'naive-ui'

import TableModal from './components/TableModal.vue'

import type { TagResponse } from '@/service/api/letan/tag'
import { getAllTags, changeStatusTag, addTag, updateTag } from '@/service/api/letan/tag'
import { downloadFile } from '@/utils/dowload'
import { ReloadOutline } from '@vicons/ionicons5'


// --- Loading & Modal ---
const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal } = useBoolean(false)

// --- Form tìm kiếm ---
const initialModel = {
  maOrTen: '',
  status: null
}
interface Tag {
  id?: string
  tenTag: string
  mauTag?: string
}

const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)

const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<Tag | null>(null)
const statusOptions = [
  { label: 'Tất cả', value: null },
  { label: 'Hoạt động', value: 0 },
  { label: 'Ngưng hoạt động', value: 1 }
]


// --- Dữ liệu bảng ---
const listData = ref<TagResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')


// --- Hàm fetch phòng ---
async function fetchTags(page = 1) {
  startLoading()
  errorMessage.value = ''
  try {
    const params: any = {
      page,
      size: pageSize.value,
    }

    if (model.maOrTen) params.maOrTen = model.maOrTen
    if (model.status !== null && model.status !== undefined) {
      params.status = model.status
    }


    const res = await getAllTags(params)
    console.log(res)
    if (res.items.length === 0 && page === 1) {
      errorMessage.value = 'Không có phòng phù hợp với tiêu chí lọc'
    }

    listData.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage



  } catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách tag'
    window.$message.error(errorMessage.value)
    listData.value = []
  } finally {
    endLoading()
  }
}

// --- Watch tự động lọc ---
watch(
  model,
  () => fetchTags(1),
  { deep: true }
)

// --- Hành động ---
function handleEditTable(row: TagResponse) {
  modalType.value = "edit"
  modalData.value = {
    id: row.id,
    tenTag: row.ten,
    mauTag: row.mau
  }
  openModal()
}

function handleAddTable() {
  modalType.value = "add"
  modalData.value = null
  openModal()
}

async function handleChangeStatus(id: string) {
  try {
    const res = await changeStatusTag(id)
    window.$message.success(res?.message || 'Thay đổi trạng thái tag!')
    fetchTags(currentPage.value)
  } catch (error: any) {
    const msg = error.message || 'Đã xảy ra lỗi khi xóa tag!'
    window.$message.error(msg)
  }
}

//Reset toàn bộ filters + reload bảng
function handleResetSearch() {
  Object.assign(model, initialModel)

  fetchTags(1)
}

function changePage(page: number) {
  fetchTags(page)
}

// --- Cột bảng ---
const columns: DataTableColumns<TagResponse> = [
  {
    title: 'STT',
    align: 'center',
    key: 'rowNumber',
    render: row => row.rowNumber

  },
  {
    title: 'Mã',
    align: 'center',
    key: 'ma',
    render: row =>
      h(
        'div',
        {
          style: {
            backgroundColor: row.mau, // tên field màu
            padding: '6px 10px',
            borderRadius: '6px',
            color: '#fff',
            display: 'inline-block',
            minWidth: '70px'
          }
        },
        row.ma || '-'
      )
  },
  {
    title: 'Tên',
    align: 'center',
    key: 'ten',
    render: row => row.ten || '-',
  },



  {
    title: 'Trạng thái',
    align: 'center',
    key: 'status',
    render: (row) => {
      const statusMap: Record<string, { label: string, type: 'success' | 'error' }> = {
        "0": { label: 'Hoạt động', type: 'success' },
        "1": { label: 'Ngưng hoạt động', type: 'error' },
      }

      const status = statusMap[row.status] || { label: '-', type: 'info' }
      return h(NTag, { type: status.type }, { default: () => status.label })
    },
  }
  ,
  {
    title: 'Thao tác',
    align: 'center',
    width: '200px',
    key: 'actions',
    render: row =>
      h(NSpace, { justify: 'center' }, {
        default: () => [
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => handleEditTable(row) },
            { default: () => 'Sửa' },
          ),
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleChangeStatus(row.id),
              positiveText: 'Xác nhận',
              negativeText: 'Hủy'
            },
            {
              default: () => 'Xác nhận thay đổi trạng thái tag?',
              trigger: () =>
                h(
                  NButton,
                  { size: 'small', type: 'error' },   // ❗ Không dùng quaternary → luôn hiện
                  {
                    default: () =>
                      h(
                        NIcon,
                        { size: 18 },
                        { default: () => h(ReloadOutline) }
                      )
                  }
                )
            }
          )


        ],
      }),
  },
]

// --- Mounted ---
onMounted(() => {
  fetchTags()

})
</script>


<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
        <n-grid :cols="25" :x-gap="12" :y-gap="12">

          <!-- Hàng 1: Thông tin cơ bản -->
          <n-form-item-gi :span="12" label="Mã / Tên tag" path="maOrTen">
            <NInput v-model:value="model.maOrTen" placeholder="Nhập mã hoặc tên tag" clearable />
          </n-form-item-gi>

          <n-form-item-gi :span="12" label="Trạng thái" path="status">
            <NSelect v-model:value="model.status" placeholder="Chọn trạng thái" clearable :options="statusOptions" />
          </n-form-item-gi>

          <!-- Hàng 4: Nút hành động -->
          <n-form-item-gi :span="1" class="flex items-center justify-end">
            <NButton strong secondary @click="handleResetSearch">Reset</NButton>
          </n-form-item-gi>


        </n-grid>
      </n-form>
    </n-card>

    <!-- Bảng danh sách -->
    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">
            Thêm tag
          </NButton>

        </div>


        <n-data-table :columns="columns" :data="listData" :loading="loading" />

        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchTags(1) }">
          <template #prefix>
            Tổng {{ totalItems }} tag
          </template>
        </n-pagination>

        <TableModal v-model:visible="visible" :type=modalType :modal-data=modalData @refresh="fetchTags(currentPage)" />
      </NSpace>
    </n-card>
  </NSpace>
</template>


<style scoped>
/* Tăng font size cho toàn bộ trang tag */
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

/* Tăng font cho cột Mã có background màu */
:deep(.n-data-table-td) div[style*="backgroundColor"] {
  font-size: 17px;
}
</style>
