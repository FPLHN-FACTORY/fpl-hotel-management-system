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
import { Tag as TagIcon, Identification, Home, Building, Category, Currency, UserMultiple, ChartLineData, Hotel, GroupPresentation, Row } from '@vicons/carbon'
import TableModal from './components/TableModal.vue'
import { changeStatusKhachHang, addKhachHang, updateKhachHang, getAllCustomers, fetchLoaiKhachHang, fetchTinhThanhPho } from '@/service/api/nhansu/khachhang'
import type { KhachHangResponse } from '@/service/api/nhansu/khachhang'
import CccdScanner from '@/components/custom/CccdScanner.vue';


const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal, setFalse: closeModal } = useBoolean(false)

const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<{ id: string } | null>(null)

const initialModel = {
  ten: '',
  sdtEmail: '',
  loaiGiayTo: null,
  soGiayTo: '',


  status: null,
  idLoaiKhachHang: null,
}

const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)
const statusOptions = [
  { label: 'Tất cả', value: null },
  { label: 'Hoạt động', value: 0 },
  { label: 'Ngưng hoạt động', value: 1 }
]

const loaiGiayToOptions = [
  { label: 'CCCD', value: 0 },
  { label: 'Hộ chiếu', value: 1 }
]
const loaiKhachHangOptions = ref([]);


async function loaiKhachHangCombobox() {
  const data = await fetchLoaiKhachHang();
  loaiKhachHangOptions.value = [
    { label: "Không có", value: '-1' },
    ...data
  ];
}




const listData = ref<KhachHangResponse[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')







async function fetchCustomers(page = 1) {
  startLoading()
  errorMessage.value = ''
  try {
    const params: any = {
      page,
      size: pageSize.value,
    }

    if (model.ten)
      params.ten = model.ten
    if (model.sdtEmail)
      params.sdtEmail = model.sdtEmail
    if (model.soGiayTo)
      params.soGiayTo = model.soGiayTo
    if (model.loaiGiayTo !== null)
      params.loaiGiayTo = model.loaiGiayTo

    if (model.idLoaiKhachHang)
      params.idLoaiKhachHang = model.idLoaiKhachHang
    if (model.status !== null) {
      params.status = model.status
    }
    const res = await getAllCustomers(params)

    if (res.items.length === 0 && page === 1)
      errorMessage.value = 'Không có khách hàng phù hợp với tiêu chí lọc'

    listData.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage


  }
  catch (error: any) {
    errorMessage.value = error.message || 'Không thể tải danh sách khách hàng'
    window.$message.error(errorMessage.value)
    listData.value = []
  }
  finally {
    endLoading()
  }
}

// --- Watch tự động lọc ---
watch(
  model,
  () => {
    fetchCustomers(1)
  },
  { deep: true },
)

function handleEditTable(row: KhachHangResponse) {
  modalType.value = 'edit'
  modalData.value = { ...row }

  openModal()
}

function handleAddTable() {
  modalType.value = 'add'
  Object.assign(model, initialModel)
  modalData.value = null

  openModal()
}




async function handleChangeStatusCustomer(id: string) {
  try {
    const res = await changeStatusKhachHang(id)
    window.$message.success(res?.message || 'Thay đổi trạng thái khách hàng thành công!')
    fetchCustomers(currentPage.value)
  }
  catch (error: any) {
    const errorMessage = error.message || 'Đã xảy ra lỗi khi thay đổi trạng thái khách hàng!'

    // Hiển thị thông báo lỗi chi tiết hơn

    window.$message.error(errorMessage)


    console.error('Lỗi khi thay đổi trạng thái khách hàng:', error)
  } finally {
    // Ẩn loading
    window.$loadingBar.finish()
  }
}

function handleResetSearch() {
  Object.assign(model, initialModel)

  fetchCustomers(1)
}

function changePage(page: number) {
  fetchCustomers(page)
}


const expandedRowKeys = ref<string[]>([])

function handleRowClick(row: any, event: MouseEvent) {
  // Nếu click vào button, popconfirm, input, svg icon,... thì bỏ qua
  const tag = (event.target as HTMLElement).tagName.toLowerCase()
  if (['button', 'svg', 'path'].includes(tag)) return

  const index = expandedRowKeys.value.indexOf(row.id)
  if (index === -1) {
    expandedRowKeys.value.push(row.id)
  } else {
    expandedRowKeys.value.splice(index, 1)
  }
}




// Helper function to format capacity range


const columns: DataTableColumns<KhachHangResponse> = [
  {
    type: 'expand',
    renderExpand: (row) => {
      return h(
        'div',
        {
          style: `
          padding: 20px 32px;
          background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
          border-radius: 8px;
          margin: 8px 0;
        `
        },
        [
          h('div', {
            style: `
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 24px;
          `
          }, [
            // Cột 1
            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              // Mã khách hàng
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#6366f1' }, { default: () => h(Identification) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Mã KH: '),
                  h('span', { style: 'color: #6b7280;' }, row.ma || '-')
                ])
              ]),
              // Họ tên
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#3b82f6' }, { default: () => h(Identification) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Họ tên: '),
                  h('span', { style: 'color: #6b7280;' }, row.hoTen || '-')
                ])
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#22c55e' }, { default: () => h(UserMultiple) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Giới tính: '),
                  h('span', { style: 'color: #6b7280;' }, row.gioiTinh === 0 ? 'Nam' : (row.gioiTinh === 1 ? 'Nữ' : 'Khác'))
                ])
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#f59e0b' }, { default: () => h(Identification) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Ngày sinh: '),
                  h('span', { style: 'color: #6b7280;' }, row.ngaySinh || '-')
                ])
              ]),
              // Số điện thoại
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#22c55e' }, { default: () => h(UserMultiple) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'SĐT: '),
                  h('span', { style: 'color: #6b7280;' }, row.soDienThoai || '-')
                ])
              ]),


            ]),

            // Cột 2
            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              // CCCD
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Identification) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Loại giấy tờ: '),
                  h('span', { style: 'color: #6b7280;' }, row.loaiGiayTo === 0 ? 'CCCD' : (row.loaiGiayTo === 1 ? 'Hộ chiếu' : 'Không có'))
                ])
              ]),

              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#ef4444' }, { default: () => h(Identification) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Số giấy tờ: '),
                  h('span', { style: 'color: #6b7280;' }, row.soGiayTo || 'Không có')
                ])
              ]),
              // Email
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#f97316' }, { default: () => h(GroupPresentation) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Email: '),
                  h('span', { style: 'color: #6b7280;' }, row.email || 'Không có')
                ])
              ]),
              // Địa chỉ
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#0ea5e9' }, { default: () => h(Home) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Địa chỉ: '),
                  h('span', { style: 'color: #6b7280;' }, (() => {
                    try {
                      const d = JSON.parse(row.diaChi)
                      return `${d.province?.name || ''} - ${d.ward?.name || ''}`
                    } catch (e) {
                      return row.diaChi || '-'
                    }
                  })())
                ])
              ]),
              // Loại khách hàng
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#eab308' }, { default: () => h(TagIcon) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'Loại khách hàng: '),
                  h('span', { style: 'color: #6b7280;' }, row.tenLoaiKhachHang || 'Không có')
                ])
              ])
            ])
          ])
        ]
      )
    }
  }
  ,



  {
    title: 'STT',
    align: 'center',
    key: 'orderNumber',
    render: row => row.orderNumber

  },
  {
    title: 'Họ tên',
    align: 'center',
    key: 'hoTen',
    render: row => row.hoTen || '-',
  },
  {
    title: 'Giới tính',
    align: 'center',
    key: 'gioiTinh',
    render: row => row.gioiTinh === 0 ? 'Nam' : (row.gioiTinh === 1 ? 'Nữ' : 'Khác'),
  },
  {
    title: 'Ngày sinh',
    align: 'center',
    key: 'ngaySinh',
    render: row => row.ngaySinh || '-',
  },
  {
    title: 'Loại khách hàng',
    align: 'center',
    key: 'tenLoaiKhachHang',
    render: row => row.tenLoaiKhachHang || 'Không có',
  },
  {
    title: 'Số điện thoại',
    align: 'center',
    key: 'soDienThoai',
    render: row => row.soDienThoai || '-',
  },
  {
    title: 'Email',
    align: 'center',
    key: 'email',
    render: row => row.email || '-',
  },




  // {
  //   title: 'Địa chỉ',
  //   align: 'center',
  //   key: 'diaChi',
  //   render: (row: any) => {
  //     if (!row.diaChi) return ''
  //     try {
  //       const diaChi = JSON.parse(row.diaChi)
  //       return `${diaChi.province.name} - ${diaChi.ward.name}`
  //     } catch (e) {
  //       // Dữ liệu cũ chưa JSON
  //       return row.diaChi
  //     }
  //   }
  // },


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
          h(NButton,
            { size: 'small', type: 'primary', onClick: (e: MouseEvent) => { e.stopPropagation(); handleEditTable(row) } },
            { default: () => 'Sửa' }
          )
          ,
          h(NPopconfirm, { onPositiveClick: () => handleChangeStatusCustomer(row.id), positiveText: 'Xác nhận', negativeText: 'Hủy' }, {
            default: () => 'Xác nhận thay đổi trạng thái khách hàng?',
            trigger: () =>

              h(NButton,
                { size: 'small', type: 'error', onClick: (e: MouseEvent) => e.stopPropagation() },
                { default: () => 'Thay đổi' }
              )
            ,
          }),
        ],
      }),
  },
]


onMounted(() => {
  fetchCustomers()
  loaiKhachHangCombobox();
})
</script>

<template>

  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="12">
          <n-form-item-gi :span="8" label="Tên khách hàng " path="ten">
            <NInput v-model:value="model.ten" placeholder="Nhập tên khách hàng" clearable />
          </n-form-item-gi>



          <n-form-item-gi :span="8" label="Loại giấy tờ" path="loaiGiayTo">
            <NSelect v-model:value="model.loaiGiayTo" placeholder="Chọn loại giấy tờ" clearable
              :options="loaiGiayToOptions" />
          </n-form-item-gi>


          <n-form-item-gi :span="8" label="Số giấy tờ" path="soGiayTo">
            <NInput v-model:value="model.soGiayTo" placeholder="Nhập số giấy tờ khách hàng" clearable />
          </n-form-item-gi>

          <n-form-item-gi :span="8" label="Số điện thoại hoặc email" path="sdtEmail">
            <NInput v-model:value="model.sdtEmail" placeholder="Nhập số điện thoại hoặc email khách hàng" clearable />
          </n-form-item-gi>


          <n-form-item-gi :span="8" label="Loại khách hàng" path="idLoaiKhachHang">
            <NSelect v-model:value="model.idLoaiKhachHang" placeholder="Chọn loại khách hàng" clearable
              :options="loaiKhachHangOptions" />
          </n-form-item-gi>
          <n-form-item-gi :span="8" label="Trạng thái" path="status">
            <NSelect v-model:value="model.status" placeholder="Chọn trạng thái" clearable :options="statusOptions" />
          </n-form-item-gi>
          <n-gi :span="24" class="flex justify-end gap-3">

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
            Thêm khách hàng
          </NButton>


        </div>

        <!-- <n-data-table :columns="columns" :data="listData" :loading="loading" /> -->
        <n-data-table :columns="columns" :data="listData" :loading="loading" :row-key="(row) => row.id"
          :expanded-row-keys="expandedRowKeys" @update:expanded-row-keys="(keys) => expandedRowKeys = keys" :row-props="(row) => ({
            style: 'cursor: pointer;',
            onClick: (event: MouseEvent) => handleRowClick(row, event)
          })" />


        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchCustomers(1) }">
          <template #prefix>
            Tổng {{ totalItems }} khách hàng
          </template>
        </n-pagination>

        <TableModal v-model:visible="visible" :type="modalType" :modal-data="modalData"
          @refresh="fetchCustomers(currentPage)" />
      </NSpace>
    </n-card>
  </NSpace>
</template>

<style></style>
