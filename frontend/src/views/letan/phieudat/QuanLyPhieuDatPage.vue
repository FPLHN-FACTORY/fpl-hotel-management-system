<script setup lang="ts">
import { ref, reactive, onMounted, h, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotification, type DataTableColumns, NIcon } from 'naive-ui'
import { Calendar, Currency, Hotel, Time } from '@vicons/carbon'
import { NButton, NTag, NSpace } from 'naive-ui'
import { apiGetDanhSachPhieuDat, apiHuyPhieuDat } from '@/service/api/letan/phieudat'
import type { PhieuDatPhongFilterRequest } from '@/service/api/letan/phieudat'


const router = useRouter()
const notification = useNotification()

const filterForm = reactive<PhieuDatPhongFilterRequest>({
    keyword: '',
    status: undefined,
    tuNgay: undefined,
    denNgay: undefined,
    page: 0,
    size: 20
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
                h('div', { style: 'display: grid; grid-template-columns: repeat(2, minmax(0, max-content)); gap: 32px;' }, [
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
        width: 100,
        align: 'center'
    },
    {
        title: 'Khách hàng',
        key: 'tenKhachHang',
        width: 160,
        align: 'center'
    },
    {
        title: 'Trạng thái',
        key: 'trangThai',
        width: 100,
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
            return h(NTag, { type: status.type }, () => status.text)
        }
    },
    {
        title: 'Thao tác',
        key: 'actions',
        width: 160,
        align: 'center',
        render: (row: any) => {
            return h(NSpace, { justify: 'center' }, () => [
                h(
                    NButton,
                    {
                        size: 'small',
                        onClick: (e) => {
                            e.stopPropagation()
                            viewDetail(row.id)
                        }
                    },
                    () => 'Xem'
                ),
                row.trangThai === 'PENDING' || row.trangThai === 'CONFIRMED'
                    ? h(
                        NButton,
                        {
                            size: 'small',
                            type: 'error',
                            onClick: (e) => {
                                e.stopPropagation()
                                handleCancel(row.id, row.maPhieu)
                            }
                        },
                        () => 'Hủy'
                    )
                    : null
            ])
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
        const response = await apiGetDanhSachPhieuDat(filterForm)
        const pageData = response.data.data || response.data
        data.value = pageData.content || []
        totalPages.value = pageData.totalPages || 0
        totalElements.value = pageData.totalElements || 0
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
    filterForm.page = 0
    fetchData()
}

function handleReset() {
    filterForm.keyword = ''
    filterForm.status = undefined
    filterForm.tuNgay = undefined
    filterForm.denNgay = undefined
    dateRange.value = null
    filterForm.page = 0
    fetchData()
}

function handlePageChange(page: number) {
    filterForm.page = page - 1
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
</script>

<template>
    <div class="p-4">
        <!-- Filter Section -->
        <n-card class="mb-4" :bordered="false" size="small">
            <n-form :model="filterForm" label-placement="left" :show-feedback="false">
                <n-grid :cols="24" :x-gap="12">
                    <n-form-item-grid-item :span="6" label="Tìm kiếm" path="keyword">
                        <n-input 
                            v-model:value="filterForm.keyword" 
                            placeholder="Mã phiếu, tên KH, SĐT..."
                            clearable 
                            @keyup.enter="handleSearch"
                        >
                            <!-- <template #prefix>
                                <nova-icon icon="carbon:search" />
                            </template> -->
                        </n-input>
                    </n-form-item-grid-item>

                    <n-form-item-grid-item :span="4" label="Trạng thái" path="status">
                        <n-select 
                            v-model:value="filterForm.status" 
                            :options="statusOptions" 
                            placeholder="Tất cả"
                            clearable 
                        />
                    </n-form-item-grid-item>

                    <n-form-item-grid-item :span="8" label="Thời gian" path="dateRange">
                        <n-date-picker 
                            v-model:value="dateRange" 
                            type="daterange" 
                            clearable 
                            start-placeholder="Từ ngày"
                            end-placeholder="Đến ngày" 
                            style="width: 100%"
                        />
                    </n-form-item-grid-item>

                    <n-form-item-grid-item :span="6">
                        <n-space justify="end" style="width: 100%" align="center">
                            <div style="height: 34px; display: flex; align-items: center;">
                                <n-button strong secondary @click="handleReset">
                                    Làm mới
                                </n-button>
                            </div>
                        </n-space>
                    </n-form-item-grid-item>
                </n-grid>
            </n-form>
        </n-card>

        <!-- Table Section -->
        <n-card>
            <!-- Button and Stats Row -->
            <div class="flex justify-between items-center mb-4">
                <n-button type="success" @click="createNew">
                    Tạo phiếu mới
                </n-button>

                <div class="text-sm text-gray-600">
                    Tổng số: <span class="font-semibold text-base">{{ totalElements }}</span> phiếu đặt
                </div>
            </div>

            <!-- Data Table -->
            <n-data-table 
                :columns="columns" 
                :data="data" 
                :loading="loading" 
                :bordered="false"
                :single-line="true" 
                :row-key="(row: any) => row.id"
                :expanded-row-keys="expandedRowKeys"
                @update:expanded-row-keys="(keys: string[]) => expandedRowKeys = keys"
                :row-props="(row: any) => ({
                    style: 'cursor: pointer;',
                    onClick: () => handleRowClick(row)
                })"
            />

            <!-- Pagination -->
            <div class="flex justify-end mt-4">
                <n-pagination 
                    v-model:page="filterForm.page" 
                    :page-count="totalPages" 
                    :page-size="filterForm.size"
                    show-size-picker 
                    :page-sizes="[10, 20, 50, 100]" 
                    @update:page="handlePageChange" 
                    @update:page-size="(size: number) => {
                        filterForm.size = size
                        filterForm.page = 0
                        fetchData()
                    }" 
                />
            </div>
        </n-card>
    </div>
</template>

<style scoped>
:deep(.n-data-table-th) {
    text-align: center;
    font-weight: normal; /* Not bold */
    font-size: 14px;
    background-color: transparent !important; /* Ensure no gray background if any */
    border-bottom: 2px solid #efeff5; /* Add distinct border */
}

:deep(.n-data-table-td) {
    font-size: 14px;
}

:deep(.n-form-item-label) {
    font-size: 13px;
    font-weight: 500;
}
</style>
