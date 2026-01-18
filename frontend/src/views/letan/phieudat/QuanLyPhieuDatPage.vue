<script setup lang="ts">
import { ref, reactive, onMounted, h, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotification, type DataTableColumns } from 'naive-ui'
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
        title: 'Mã phiếu',
        key: 'maPhieu',
        width: 120,
        fixed: 'left'
    },
    {
        title: 'Khách hàng',
        key: 'tenKhachHang',
        width: 180
    },
    {
        title: 'Check-in',
        key: 'ngayCheckIn',
        width: 160,
        render: (row: any) => {
            return new Date(row.ngayCheckIn).toLocaleString('vi-VN')
        }
    },
    {
        title: 'Check-out',
        key: 'ngayCheckOut',
        width: 160,
        render: (row: any) => {
            return new Date(row.ngayCheckOut).toLocaleString('vi-VN')
        }
    },
    {
        title: 'Số phòng',
        key: 'tongSoPhong',
        width: 100,
        align: 'center'
    },
    {
        title: 'Tổng tiền',
        key: 'tongTien',
        width: 140,
        render: (row: any) => {
            return row.tongTien?.toLocaleString() + ' VNĐ'
        }
    },
    {
        title: 'Trạng thái',
        key: 'trangThai',
        width: 140,
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
        title: 'Ngày tạo',
        key: 'ngayTao',
        width: 160,
        render: (row: any) => {
            return new Date(row.ngayTao).toLocaleString('vi-VN')
        }
    },
    {
        title: 'Thao tác',
        key: 'actions',
        width: 200,
        fixed: 'right',
        render: (row: any) => {
            return h(NSpace, {}, () => [
                h(
                    NButton,
                    {
                        size: 'small',
                        onClick: () => viewDetail(row.id)
                    },
                    () => 'Xem'
                ),
                row.trangThai === 'PENDING' || row.trangThai === 'CONFIRMED'
                    ? h(
                        NButton,
                        {
                            size: 'small',
                            type: 'error',
                            onClick: () => handleCancel(row.id, row.maPhieu)
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
    <div class="p-6">
        <n-card title="Quản Lý Phiếu Đặt Phòng">
            <!-- Filter Section -->
            <n-space vertical :size="16">
                <n-space :size="12">
                    <n-input v-model:value="filterForm.keyword" placeholder="Tìm theo mã phiếu, tên khách hàng, SĐT..."
                        style="width: 300px" clearable @keyup.enter="handleSearch">
                        <template #prefix>
                            <nova-icon icon="carbon:search" />
                        </template>
                    </n-input>

                    <n-select v-model:value="filterForm.status" :options="statusOptions" placeholder="Trạng thái"
                        style="width: 160px" clearable />

                    <n-date-picker v-model:value="dateRange" type="daterange" clearable start-placeholder="Từ ngày"
                        end-placeholder="Đến ngày" style="width: 300px" />

                    <n-button type="primary" @click="handleSearch">
                        <template #icon>
                            <nova-icon icon="carbon:search" />
                        </template>
                        Tìm kiếm
                    </n-button>

                    <n-button @click="handleReset">
                        <template #icon>
                            <nova-icon icon="carbon:reset" />
                        </template>
                        Làm mới
                    </n-button>

                    <n-button type="success" @click="createNew">
                        <template #icon>
                            <nova-icon icon="carbon:add" />
                        </template>
                        Tạo phiếu mới
                    </n-button>
                </n-space>

                <!-- Stats -->
                <div class="text-sm text-gray-600">
                    Tổng số: <span class="font-semibold">{{ totalElements }}</span> phiếu đặt
                </div>
            </n-space>

            <!-- Data Table -->
            <n-data-table class="mt-4" :columns="columns" :data="data" :loading="loading" :bordered="false"
                :single-line="false" :scroll-x="1400" />

            <!-- Pagination -->
            <div class="flex justify-end mt-4">
                <n-pagination v-model:page="filterForm.page" :page-count="totalPages" :page-size="filterForm.size"
                    show-size-picker :page-sizes="[10, 20, 50, 100]" @update:page="handlePageChange" @update:page-size="
                        (size: number) => {
                            filterForm.size = size
                            filterForm.page = 0
                            fetchData()
                        }
                    " />
            </div>
        </n-card>
    </div>
</template>

<style scoped>
:deep(.n-data-table) {
    font-size: 14px;
}
</style>
