<script setup lang="ts">
import { h, onMounted, ref, reactive } from 'vue'
import { NButton, NSpace, NTag, NIcon, NDataTable, NCard, NForm, NGrid, NFormItemGi, NInput, DataTableColumns, FormInst, NDatePicker } from 'naive-ui'
import { Tag as TagIcon, Identification, Home, Building, Category, Currency, UserMultiple, ChartLineData, Hotel, GroupPresentation, Row, Add, Rotate } from '@vicons/carbon'
import { getAllGroups, ParamsGetGroups, type DoanLuuTru } from '@/service/api/nhansu/doanluutru'
import MemberDrawer from './components/MemberDrawer.vue'
import CreateGroupModal from './components/CreateGroupModal.vue'

const data = ref<DoanLuuTru[]>([])
const loading = ref(false)
const showMemberDrawer = ref(false)
const showCreateModal = ref(false)
const selectedGroupId = ref('')
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const timeRange = ref<[number, number] | null>(null)

const searchModel = reactive({
  tuKhoa: '',
  thoiGianCheckIn: null as number | null,
  thoiGianCheckOut: null as number | null
})
watch(
  () => timeRange.value,
  (val) => {
    searchModel.thoiGianCheckIn = val?.[0] ?? null
    searchModel.thoiGianCheckOut = val?.[1] ?? null
  }
)

watch(
  searchModel,
  () => {
    fetchData(1)
  },
  { deep: true }
)

async function fetchData(page = 1) {
  loading.value = true
  try {
    const params: ParamsGetGroups = {
      page,
      size: pageSize.value

    }
    if (searchModel.tuKhoa) {
      params.tuKhoa = searchModel.tuKhoa
    }
    if (searchModel.thoiGianCheckIn && searchModel.thoiGianCheckOut) {
      params.thoiGianCheckIn = searchModel.thoiGianCheckIn
      params.thoiGianCheckOut = searchModel.thoiGianCheckOut
    }
    const res: any = await getAllGroups(params)
    data.value = res.items
    totalItems.value = res.totalItems
    currentPage.value = res.currentPage
    console.log("doanluutru", res.items)

  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchModel.tuKhoa = ''
  searchModel.thoiGianCheckIn = null
  searchModel.thoiGianCheckOut = null
  timeRange.value = null
  fetchData(1)
}

function handleManageMembers(row: DoanLuuTru) {
  selectedGroupId.value = row.id
  console.log("selectedGroupId", selectedGroupId.value)
  showMemberDrawer.value = true
}

// const columns: any = [
//   { title: 'STT', key: 'orderNumber', align: 'center' },
//   { title: 'Mã đoàn', key: 'maDoan', align: 'center' },
//   { title: 'Tên đoàn', key: 'tenDoan', align: 'center' },
//   { title: 'Trưởng đoàn', key: 'hoTen', align: 'center', render: (row: any) => row.hoTen || '-' },
//   { title: 'Booking', key: 'maDatPhong', align: 'center', render: (row: any) => row.maDatPhong ? 'Booking #' + row.maDatPhong.substring(0, 8) : '-' },
//   { title: 'Ghi chú', key: 'ghiChu', align: 'center' },
//   {
//     title: 'Trạng thái', key: 'trangThai', align: 'center',
//     render(row: any) {
//       const map: any = {
//         0: { label: 'Chưa check-in', type: 'info' },
//         1: { label: 'Đang ở', type: 'success' },
//         2: { label: 'Đã check-out', type: 'default' }
//       }
//       const st = map[row.trangThai ?? 0]
//       return h(NTag, { type: st.type }, { default: () => st.label })
//     }
//   },
// {
//   title: 'Thời gian lưu trú',
//   align: 'center',
//   render(row: any) {
//     const checkIn = row.thoiGianCheckIn
//     const checkOut = row.thoiGianCheckOut

//     if (!checkIn || !checkOut) return '-'

//     const format = (value: string) =>
//       new Date(value).toLocaleString('vi-VN', {
//         day: '2-digit',
//         month: '2-digit',
//         year: 'numeric',
//         hour: '2-digit',
//         minute: '2-digit',
//         second: '2-digit'
//       })

//     return `${format(checkIn)} - ${format(checkOut)}`
//   }
// }

// ,
//   {
//     title: 'Thao tác',
//     key: 'actions',
//     align: 'center',
//     render(row: DoanLuuTru) {
//       return h(NButton, {
//         size: 'small',
//         type: 'info',
//         onClick: () => handleManageMembers(row)
//       }, { default: () => 'Chi tiết thành viên' })
//     }
//   }
// ]
async function changePage(page: number) {
  fetchData(page)
}

onMounted(fetchData)
const columns: DataTableColumns<DoanLuuTru> = [
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

              // Số điện thoại
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h(NIcon, { size: 18, color: '#22c55e' }, { default: () => h(UserMultiple) }),
                h('div', [
                  h('span', { style: 'font-weight: 600; color: #374151;' }, 'SĐT: '),
                  h('span', { style: 'color: #6b7280;' }, row.soDienThoai || '-')
                ])
              ]),

            ])
          ])
        ]
      )
    }
  }
  ,


  { title: 'STT', key: 'orderNumber', align: 'center' },
  { title: 'Mã đoàn', key: 'maDoan', align: 'center' },
  { title: 'Tên đoàn', key: 'tenDoan', align: 'center' },
  { title: 'Trưởng đoàn', key: 'hoTen', align: 'center', render: (row: any) => row.hoTen || '-' },
  { title: 'Booking', key: 'maDatPhong', align: 'center', render: (row: any) => row.maDatPhong ? 'Booking #' + row.maDatPhong.substring(0, 8) : '-' },
  { title: 'Ghi chú', key: 'ghiChu', align: 'center' },
  {
    title: 'Trạng thái', key: 'trangThai', align: 'center',
    render(row: any) {
      const map: any = {
        0: { label: 'Chưa check-in', type: 'info' },
        1: { label: 'Đang ở', type: 'success' },
        2: { label: 'Đã check-out', type: 'default' }
      }
      const st = map[row.trangThai ?? 0]
      return h(NTag, { type: st.type }, { default: () => st.label })
    }
  },
  {
    title: 'Thời gian lưu trú',
    align: 'center',
    render(row: any) {
      const checkIn = row.thoiGianCheckIn
      const checkOut = row.thoiGianCheckOut

      if (!checkIn || !checkOut) return '-'

      const format = (value: string) =>
        new Date(value).toLocaleString('vi-VN', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })

      return `${format(checkIn)} - ${format(checkOut)}`
    }
  }

  ,
  {
    title: 'Thao tác',
    key: 'actions',
    align: 'center',
    render(row: DoanLuuTru) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'info',
          onClick: (e: MouseEvent) => {
            e.stopPropagation() // ⭐ RẤT QUAN TRỌNG
            handleManageMembers(row)
          }
        },
        { default: () => 'Chi tiết thành viên' }
      )
    }

  }
]

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
</script>

<template>
  <NSpace vertical size="large" class="p-4">
    <NCard>
      <NForm :model="searchModel" label-placement="left">
        <NGrid :cols="24" :x-gap="24">
          <NFormItemGi :span="12" label="Tên">
            <NInput v-model:value="searchModel.tuKhoa" placeholder="Tên đoàn, Mã đoàn, Trưởng đoàn..."
              @keyup.enter="fetchData(1)" />
          </NFormItemGi>
          <NFormItemGi :span="12" label="Thời gian lưu trú">
            <NDatePicker style="width: 100%;" v-model:value="timeRange" type="datetimerange" clearable start-placeholder="Ngày đến"
              end-placeholder="Ngày đi" />

          </NFormItemGi>
          <NFormItemGi :span="24" class="flex justify-end gap-3">
            <NButton strong secondary @click="handleReset">
              Làm mới
            </NButton>
          </NFormItemGi>
        </NGrid>
      </NForm>
    </NCard>

    <NCard :bordered="false">
      <div class="flex gap-4 mb-3">
        <NButton type="primary" @click="showCreateModal = true">Thêm đoàn lưu trú</NButton>
      </div>

      <!-- <NDataTable :columns="columns" :data="data" :loading="loading" /> -->
      <NDataTable :columns="columns" :data="data" :loading="loading" :row-key="(row) => row.id"
        :expanded-row-keys="expandedRowKeys" @update:expanded-row-keys="(keys) => expandedRowKeys = keys" :row-props="(row) => ({
          style: 'cursor: pointer;',
          onClick: (event: MouseEvent) => handleRowClick(row, event)
        })" />
      <div class="mt-4">
        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchData(1) }">
          <template #prefix>
            Tổng {{ totalItems }} đoàn
          </template>
        </n-pagination>
      </div>
    </NCard>

    <MemberDrawer v-model:show="showMemberDrawer" :group-id="selectedGroupId" />
    <CreateGroupModal v-model:show="showCreateModal" @success="fetchData" />
  </NSpace>
</template>

<style scoped>
/* Tăng font size cho toàn bộ trang đoàn lưu trú */
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
