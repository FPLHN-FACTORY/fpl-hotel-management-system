<script setup lang="ts">
import { ref, reactive, onMounted, h, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  useNotification,
  type DataTableColumns,
  NIcon,
  NButton,
  NTag,
  NSpace,
  NInput,
  NSelect,
  NDatePicker,
  NForm,
  NFormItemGridItem,
  NGrid,
  NCard,
  NPagination,
  NDataTable,
  NTooltip,
  NDropdown
} from 'naive-ui'
import { Calendar, Currency, Hotel, Time, OverflowMenuVertical } from '@vicons/carbon'
import { apiGetDanhSachPhieuDat, apiHuyPhieuDat } from '@/service/api/letan/phieudat'
import type { PhieuDatPhongFilterRequest } from '@/service/api/letan/phieudat'
import AssignRoomModal from './components/AssignRoomModal.vue'

const router = useRouter()
const notification = useNotification()

const filterForm = reactive<PhieuDatPhongFilterRequest>({
    keyword: '',
    status: undefined,
    tuNgay: undefined,
    denNgay: undefined,
    page: 1,
    size: 10
})

const dateRange = ref<[number, number] | null>(null)
const data = ref<any[]>([])
const loading = ref(false)
const totalPages = ref(0)
const totalElements = ref(0)
const expandedRowKeys = ref<string[]>([])

function handleRowClick(row: any) {
    const index = expandedRowKeys.value.indexOf(row.id)
    if (index > -1) {
        expandedRowKeys.value.splice(index, 1)
    } else {
        expandedRowKeys.value.push(row.id)
    }
}

const statusOptions = [
    { label: 'Tất cả', value: undefined },
    { label: 'Chờ xử lý', value: 'PENDING' },
    { label: 'Đã xác nhận', value: 'CONFIRMED' },
    { label: 'Đã check-in', value: 'CHECKIN' },
    { label: 'Đã check-out', value: 'CHECKOUT' },
    { label: 'Đã hủy', value: 'CANCELLED' }
]

const columns: DataTableColumns = [
    {
        type: 'expand',
        renderExpand: (row: any) => {
            return h('div', { style: 'padding: 20px 32px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; margin: 8px 0;' }, [
                h('div', { style: 'display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 32px; width: 100%;' }, [
                    h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
                        h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                                h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Calendar) }),
                                h('span', 'Ngày Check-in:')
                            ]),
                            h('span', { style: 'color: #6b7280;' }, new Date(row.ngayCheckIn).toLocaleString('vi-VN'))
                        ]),
                        h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                                h(NIcon, { size: 18, color: '#ec4899' }, { default: () => h(Calendar) }),
                                h('span', 'Ngày Check-out:')
                            ]),
                            h('span', { style: 'color: #6b7280;' }, new Date(row.ngayCheckOut).toLocaleString('vi-VN'))
                        ]),
                        h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                                h(NIcon, { size: 18, color: '#667eea' }, { default: () => h(Time) }),
                                h('span', 'Ngày tạo:')
                            ]),
                            h('span', { style: 'color: #6b7280;' }, new Date(row.ngayTao).toLocaleString('vi-VN'))
                        ]),
                    ]),
                    h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
                        h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                                h(NIcon, { size: 18, color: '#06b6d4' }, { default: () => h(Hotel) }),
                                h('span', 'Tổng số phòng:')
                            ]),
                            h('span', { style: 'color: #6b7280;' }, row.tongSoPhong)
                        ]),
                        h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                            h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                                h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(Currency) }),
                                h('span', 'Tổng tiền:')
                            ]),
                            h('span', { style: 'color: #10b981; font-weight: 700;' }, row.tongTien?.toLocaleString() + ' VNĐ')
                        ]),
                    ])
                ])
            ])
        }
    },
    {
        title: 'Mã phiếu',
        key: 'maPhieu',
        align: 'center',
        render: (row: any) => {
            return h('span', { style: 'font-weight: 600; color: #1e293b;' }, row.maPhieu)
        }
    },
    {
        title: 'Khách hàng',
        key: 'tenKhachHang',
        align: 'center',
        render: (row: any) => {
            return h('div', { style: 'display: flex; flex-direction: column; align-items: center; gap: 2px;' }, [
                h('span', { style: 'font-weight: 600; color: #1e293b;' }, row.tenKhachHang || 'Chưa có'),
                row.tenKhachHang ? null : h('span', { style: 'font-size: 12px; color: #94a3b8;' }, '(Chưa gắn)')
            ])
        }
    },
    {
        title: 'Số phòng',
        key: 'tongSoPhong',
        align: 'center',
        render: (row: any) => {
            return h('span', { style: 'font-weight: 600; color: #3b82f6;' }, row.tongSoPhong)
        }
    },
    {
        title: 'Tổng tiền',
        key: 'tongTien',
        align: 'center',
        render: (row: any) => {
            return h('span', { style: 'font-weight: 700; color: #10b981;' }, row.tongTien?.toLocaleString() + ' VNĐ')
        }
    },
    {
        title: 'Trạng thái',
        key: 'trangThai',
        align: 'center',
        render: (row: any) => {
            const statusMap: Record<string, { type: any; text: string }> = {
                PENDING: { type: 'warning', text: 'Chờ xử lý' },
                CONFIRMED: { type: 'info', text: 'Đã xác nhận' },
                CHECKIN: { type: 'success', text: 'Đã check-in' },
                CHECKOUT: { type: 'default', text: 'Đã check-out' },
                CANCELLED: { type: 'error', text: 'Đã hủy' }
            }
            const status = statusMap[row.trangThai] || { type: 'default', text: row.trangThai }
            return h(NTag, { type: status.type, bordered: false }, () => status.text)
        }
    },
    {
        title: 'Thao tác',
        key: 'actions',
        width: 80,
        align: 'center',
        render: (row: any) => {
            const dropdownOptions = [
                {
                    label: 'Xem chi tiết',
                    key: 'view',
                    icon: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 32 32', fill: 'currentColor' }, [
                        h('path', { d: 'M30.94 15.66A16.69 16.69 0 0 0 16 5A16.69 16.69 0 0 0 1.06 15.66a1 1 0 0 0 0 .68A16.69 16.69 0 0 0 16 27a16.69 16.69 0 0 0 14.94-10.66a1 1 0 0 0 0-.68M16 25c-5.3 0-10.9-3.93-12.93-9C5.1 10.93 10.7 7 16 7s10.9 3.93 12.93 9C26.9 21.07 21.3 25 16 25' }),
                        h('path', { d: 'M16 10a6 6 0 1 0 6 6a6 6 0 0 0-6-6m0 10a4 4 0 1 1 4-4a4 4 0 0 1-4 4' })
                    ]) })
                }
            ]

            if (row.trangThai === 'PENDING') {
                dropdownOptions.push({
                    label: 'Gán phòng',
                    key: 'assign',
                    icon: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 32 32', fill: 'currentColor' }, [
                        h('path', { d: 'M25 5h-3V4a2 2 0 0 0-2-2h-8a2 2 0 0 0-2 2v1H7a2 2 0 0 0-2 2v21a2 2 0 0 0 2 2h18a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2M12 4h8v4h-8Zm13 24H7V7h3v3h12V7h3Z' }),
                        h('path', { d: 'M14 15h4v2h-4zm0 5h6v2h-6z' })
                    ]) })
                })
            }

            if (row.trangThai === 'PENDING' || row.trangThai === 'CONFIRMED') {
                dropdownOptions.push({
                    label: 'Hủy phiếu',
                    key: 'cancel',
                    icon: () => h(NIcon, null, { default: () => h('svg', { viewBox: '0 0 32 32', fill: 'currentColor' }, [
                        h('path', { d: 'M12 12h2v12h-2zm6 0h2v12h-2z' }),
                        h('path', { d: 'M4 6v2h2v20a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8h2V6zm4 22V8h16v20zm4-26h8v2h-8z' })
                    ]) })
                })
            }

            return h(
                NDropdown,
                {
                    trigger: 'hover',
                    options: dropdownOptions,
                    onSelect: (key: string) => {
                        if (key === 'view') viewDetail(row.id)
                        else if (key === 'assign') openAssignRoom(row)
                        else if (key === 'cancel') handleCancel(row.id, row.maPhieu)
                    }
                },
                {
                    default: () => h(
                        NButton,
                        {
                            text: true,
                            style: 'font-size: 20px; padding: 4px;',
                            onClick: (e: Event) => e.stopPropagation()
                        },
                        { default: () => h(NIcon, { size: 20 }, { default: () => h(OverflowMenuVertical) }) }
                    )
                }
            )
        }
    }
]

watch(dateRange, (newRange) => {
    if (newRange && newRange.length === 2) {
        filterForm.tuNgay = newRange[0]
        filterForm.denNgay = newRange[1]
    } else {
        filterForm.tuNgay = undefined
        filterForm.denNgay = undefined
    }
})

onMounted(() => {
    fetchData()
})

async function fetchData() {
    loading.value = true
    try {
        const response = await apiGetDanhSachPhieuDat({
            ...filterForm,
            page: filterForm.page - 1
        })
        const pageData = response.data.data || response.data
        data.value = pageData.content || []
        totalPages.value = pageData.totalPages || 0
        totalElements.value = pageData.totalElements || 0
        filterForm.page = pageData.number + 1 || pageData.currentPage + 1 || pageData.page + 1 || 1
    } catch (error: any) {
        notification.error({
            content: error.message || 'Lỗi khi tải danh sách',
            duration: 3000
        })
    } finally {
        loading.value = false
    }
}

function handleSearch() {
    filterForm.page = 1
    fetchData()
}

function handleReset() {
    filterForm.keyword = ''
    filterForm.status = undefined
    filterForm.tuNgay = undefined
    filterForm.denNgay = undefined
    dateRange.value = null
    filterForm.page = 1
    fetchData()
}

function handlePageChange(page: number) {
    filterForm.page = page
    fetchData()
}

function viewDetail(id: string) {
    router.push({ name: 'chiTietPhieuDat', params: { id } })
}

function handleCancel(id: string, maPhieu: string) {
    const dialog = window.$dialog.warning({
        title: 'Xác nhận hủy',
        content: `Bạn có chắc chắn muốn hủy phiếu đặt ${maPhieu}?`,
        positiveText: 'Xác nhận',
        negativeText: 'Hủy',
        onPositiveClick: async () => {
            dialog.loading = true
            try {
                await apiHuyPhieuDat(id)
                notification.success({
                    content: 'Hủy phiếu đặt thành công',
                    duration: 2000
                })
                fetchData()
            } catch (error: any) {
                notification.error({
                    content: error.message || 'Lỗi khi hủy phiếu',
                    duration: 3000
                })
            }
        }
    })
}

function createNew() {
    router.push({ name: 'taoPhieuDat' })
}

const assignRoomVisible = ref(false)
const selectedPhieuForAssign = ref<any>(null)

function openAssignRoom(phieu: any) {
    selectedPhieuForAssign.value = phieu
    assignRoomVisible.value = true
}

function handleAssignSuccess() {
    fetchData()
}
</script>

<template>
    <n-space vertical size="large">
        <!-- Filter Card -->
        <n-card>
            <n-form ref="formRef" :model="filterForm" :show-feedback="false">
                <n-grid :cols="24" :x-gap="12">
                    <n-gi :span="6">
                        <n-input
                            v-model:value="filterForm.keyword"
                            placeholder="Tìm kiếm: Mã phiếu, tên KH, SĐT..."
                            clearable
                            @keyup.enter="handleSearch"
                        />
                    </n-gi>

                    <n-gi :span="4">
                        <n-select
                            v-model:value="filterForm.status"
                            :options="statusOptions"
                            placeholder="Trạng thái"
                            clearable
                        />
                    </n-gi>

                    <n-gi :span="8">
                        <n-date-picker
                            v-model:value="dateRange"
                            type="daterange"
                            clearable
                            start-placeholder="Từ ngày"
                            end-placeholder="Đến ngày"
                            style="width: 100%"
                        />
                    </n-gi>

                    <n-gi :span="6" class="flex justify-end gap-3">
                        <n-tooltip trigger="hover">
                            <template #trigger>
                                <n-button quaternary circle @click="handleReset">
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

        <!-- Table Card -->
        <n-card>
            <n-space vertical size="large">
                <!-- Action Button -->
                <div class="flex gap-4">
                    <n-button type="primary" @click="createNew">
                        <template #icon>
                            <nova-icon icon="carbon:add" />
                        </template>
                        Tạo phiếu đặt mới
                    </n-button>
                </div>

                <!-- Data Table -->
                <n-data-table
                    :columns="columns"
                    :data="data"
                    :loading="loading"
                    :row-key="(row: any) => row.id"
                    :expanded-row-keys="expandedRowKeys"
                    @update:expanded-row-keys="(keys: any) => expandedRowKeys = keys"
                    :row-props="(row: any) => ({
                        style: 'cursor: pointer;',
                        onClick: () => handleRowClick(row)
                    })"
                />

                <!-- Pagination -->
                <div class="flex justify-start mt-4">
                    <n-pagination
                        v-model:page="filterForm.page"
                        :page-count="totalPages"
                        :page-size="filterForm.size"
                        show-size-picker
                        circle
                        :page-sizes="[10, 20, 30, 50]"
                        @update:page="handlePageChange"
                        @update:page-size="(size: number) => {
                            filterForm.size = size
                            filterForm.page = 1
                            fetchData()
                        }"
                    >
                        <template #prefix>
                            Tổng {{ totalElements }} phiếu đặt
                        </template>
                    </n-pagination>
                </div>
            </n-space>
        </n-card>

        <!-- Assign Room Modal -->
        <AssignRoomModal
            v-model:visible="assignRoomVisible"
            :phieu-data="selectedPhieuForAssign"
            @success="handleAssignSuccess"
        />
    </n-space>
</template>

<style scoped>
/* Increase font sizes globally */
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

/* Make all inputs same height */
:deep(.n-input),
:deep(.n-input-number),
:deep(.n-date-picker),
:deep(.n-select) {
    min-height: 40px;
}

:deep(.n-base-selection) {
    min-height: 40px !important;
}

:deep(.n-base-selection .n-base-selection-label) {
    min-height: 40px;
    display: flex;
    align-items: center;
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

:deep(.n-dropdown-option__icon),
:deep(.n-dropdown-option__label) {
    font-size: 16px;
}

/* Hover effect for dropdown trigger */
:deep(.n-button.n-button--text-type:hover) {
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 4px;
}
</style>
