<script setup lang="ts">
import { ref, onMounted, computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotification, NTag, NButton, NDataTable } from 'naive-ui'
import {
    apiGetChiTietPhieuDat,
    apiGanKhachHang,
    apiXacNhanPhieuDat,
    apiSearchKhachHang
} from '@/service/api/letan/phieudat'

const route = useRoute()
const router = useRouter()
const notification = useNotification()

const phieuDat = ref<any>(null)
const loading = ref(false)
const showKhachHangModal = ref(false)

// Customer search
const searchKhach = ref('')
const khachHangList = ref<any[]>([])
const searchingKhach = ref(false)

const isConfirming = ref(false)

const statusColor: Record<string, { type: any; text: string }> = {
    PENDING: { type: 'warning', text: 'Chờ xử lý' },
    CONFIRMED: { type: 'info', text: 'Đã xác nhận' },
    CHECKIN: { type: 'success', text: 'Đã check-in' },
    CHECKOUT: { type: 'default', text: 'Đã check-out' },
    CANCELLED: { type: 'error', text: 'Đã hủy' }
}

const canEdit = computed(() => {
    return phieuDat.value?.trangThai === 'PENDING'
})

const canConfirm = computed(() => {
    if (!phieuDat.value || phieuDat.value.trangThai !== 'PENDING') return false

    // Check if has customer
    if (!phieuDat.value.khachHang) return false

    // Check if all rooms are assigned
    const tongSoLuong = phieuDat.value.danhSachLoaiPhong?.reduce(
        (sum: number, lp: any) => sum + lp.soLuong,
        0
    ) || 0
    const soPhongDaGan = phieuDat.value.danhSachPhongDaGan?.length || 0

    return soPhongDaGan >= tongSoLuong
})

onMounted(() => {
    fetchDetail()
})

async function fetchDetail() {
    loading.value = true
    try {
        const response = await apiGetChiTietPhieuDat(route.params.id as string)
        phieuDat.value = response.data.data || response.data
    } catch (error: any) {
        notification.error({
            content: error.message || 'Lỗi khi tải chi tiết phiếu',
            duration: 3000
        })
    } finally {
        loading.value = false
    }
}

const columns = [
    { title: 'Mã', key: 'maNguoiDung' },
    { title: 'Họ tên', key: 'hoTen' },
    { title: 'SĐT', key: 'soDienThoai' },
    { title: 'Email', key: 'email' },
    {
        title: 'Thao tác',
        key: 'actions',
        render(row: any) {
            return h(NButton, {
                size: 'small',
                type: 'primary',
                onClick: () => handleGanKhachHang(row.id)
            }, { default: () => 'Chọn' })
        }
    }
]

// Customer Assignment
async function openGanKhachHang() {
    showKhachHangModal.value = true
    searchKhach.value = ''
    await searchKhachHang()
}

async function searchKhachHang() {
    searchingKhach.value = true
    try {
        const response = await apiSearchKhachHang(searchKhach.value?.trim() || '')
        khachHangList.value = response.data.data || []
    } catch (error: any) {
        console.error('Search error:', error)
        notification.error({
            content: error.message || 'Lỗi khi tìm kiếm khách hàng',
            duration: 3000
        })
    } finally {
        searchingKhach.value = false
    }
}

async function handleGanKhachHang(khachHangId: string) {
    try {
        await apiGanKhachHang({
            idPhieuDat: route.params.id as string,
            idKhachHang: khachHangId
        })

        notification.success({
            content: 'Gắn khách hàng thành công',
            duration: 2000
        })

        showKhachHangModal.value = false
        fetchDetail()
    } catch (error: any) {
        notification.error({
            content: error.message || 'Lỗi khi gắn khách hàng',
            duration: 3000
        })
    }
}

// Associated functions removed because assignment is handled in management page

// Confirm booking
async function handleXacNhan() {
    const dialog = window.$dialog.warning({
        title: 'Xác nhận phiếu đặt',
        content: 'Bạn có chắc chắn muốn xác nhận phiếu đặt này? Sau khi xác nhận sẽ không thể chỉnh sửa.',
        positiveText: 'Xác nhận',
        negativeText: 'Hủy',
        onPositiveClick: async () => {
            dialog.loading = true
            isConfirming.value = true
            try {
                await apiXacNhanPhieuDat(route.params.id as string)

                notification.success({
                    content: 'Xác nhận phiếu đặt thành công!',
                    duration: 2000
                })

                fetchDetail()
            } catch (error: any) {
                notification.error({
                    content: error.message || 'Lỗi khi xác nhận',
                    duration: 3000
                })
            } finally {
                isConfirming.value = false
            }
        }
    })
}

const currentStep = computed(() => {
    if (!phieuDat.value) return 1

    switch (phieuDat.value.trangThai) {
        case 'PENDING':
            return 2
        case 'CONFIRMED':
        case 'CHECKIN':
        case 'CHECKOUT':
            return 4
        case 'CANCELLED':
            return 2
        default:
            return 1
    }
})
</script>

<template>
    <div class="p-6">
        <n-spin :show="loading">
            <div v-if="phieuDat" class="space-y-4">
                <!-- Header -->
                <n-card>
                    <div class="flex justify-between items-start">
                        <div>
                            <h2 class="text-2xl font-bold">{{ phieuDat.maPhieu }}</h2>
                            <div class="mt-2 text-gray-600">
                                Ngày tạo: {{ new Date(phieuDat.ngayTao).toLocaleString('vi-VN') }}
                            </div>
                            <div v-if="phieuDat.tenNhanVienTao" class="text-gray-600">
                                Người tạo: {{ phieuDat.tenNhanVienTao }}
                            </div>
                        </div>
                        <n-tag :type="statusColor[phieuDat.trangThai]?.type" size="large">
                            {{ statusColor[phieuDat.trangThai]?.text }}
                        </n-tag>
                    </div>

                    <n-divider />

                    <!-- Basic Info -->
                    <n-descriptions :column="2" bordered>
                        <n-descriptions-item label="Check-in">
                            {{ new Date(phieuDat.ngayCheckIn).toLocaleString('vi-VN') }}
                        </n-descriptions-item>
                        <n-descriptions-item label="Check-out">
                            {{ new Date(phieuDat.ngayCheckOut).toLocaleString('vi-VN') }}
                        </n-descriptions-item>
                        <n-descriptions-item label="Số lượng khách">
                            {{ phieuDat.soLuongKhach }} người
                        </n-descriptions-item>
                        <n-descriptions-item label="Tổng tiền">
                            <span class="text-lg font-semibold text-green-600">
                                {{ phieuDat.tongTien?.toLocaleString() }} VNĐ
                            </span>
                        </n-descriptions-item>
                        <n-descriptions-item v-if="phieuDat.ghiChu" label="Ghi chú" :span="2">
                            {{ phieuDat.ghiChu }}
                        </n-descriptions-item>
                    </n-descriptions>
                </n-card>

                <!-- Workflow Steps -->
                <n-card title="Quy trình">
                    <n-steps :current="currentStep" :status="phieuDat.trangThai === 'CANCELLED' ? 'error' : 'process'">
                        <n-step title="Tạo phiếu" description="Đã tạo phiếu đặt theo loại phòng" />
                        <n-step title="Gắn khách hàng và phòng"
                            :description="phieuDat.khachHang ? 'Đã gắn khách hàng' : 'Chưa gắn khách hàng'" />
                        <n-step title="Xác nhận"
                            :description="phieuDat.trangThai === 'CONFIRMED' ? 'Đã xác nhận' : 'Chưa xác nhận'" />
                    </n-steps>
                </n-card>

                <!-- Customer Section -->
                <n-card title="Thông tin khách hàng">
                    <div v-if="phieuDat.khachHang">
                        <n-descriptions :column="2" bordered>
                            <n-descriptions-item label="Họ tên">
                                {{ phieuDat.khachHang.hoTen }}
                            </n-descriptions-item>
                            <n-descriptions-item label="Số điện thoại">
                                {{ phieuDat.khachHang.soDienThoai }}
                            </n-descriptions-item>
                            <n-descriptions-item label="Email" :span="2">
                                {{ phieuDat.khachHang.email || 'Chưa có' }}
                            </n-descriptions-item>
                        </n-descriptions>
                    </div>
                    <div v-else>
                        <n-empty description="Chưa gắn khách hàng">
                            <template #extra>
                                <n-button v-if="canEdit" type="primary" @click="openGanKhachHang">
                                    Gắn khách hàng
                                </n-button>
                            </template>
                        </n-empty>
                    </div>
                </n-card>

                <!-- Room Types Section -->
                    <n-space vertical>
                        <n-card v-for="loaiPhong in phieuDat.danhSachLoaiPhong" :key="loaiPhong.idLoaiPhong"
                            size="small" :bordered="true">
                            <div class="flex justify-between items-center">
                                <div class="flex-1">
                                    <div class="font-semibold text-lg">{{ loaiPhong.tenLoaiPhong }}</div>
                                    <div class="text-sm text-gray-600 mt-1">
                                        Số lượng: {{ loaiPhong.soLuongDaGan }}/{{ loaiPhong.soLuong }} phòng
                                        • Giá: {{ loaiPhong.gia?.toLocaleString() }} VNĐ/phòng
                                    </div>
                                    <n-progress :percentage="(loaiPhong.soLuongDaGan / loaiPhong.soLuong) * 100"
                                        :show-indicator="false"
                                        :status="loaiPhong.soLuongDaGan === loaiPhong.soLuong ? 'success' : 'info'"
                                        class="mt-2" />
                                </div>
                            </div>
                        </n-card>
                    </n-space>

                <!-- Assigned Rooms Section -->
                <n-card v-if="phieuDat.danhSachPhongDaGan?.length > 0" title="Danh sách phòng đã gắn">
                    <n-space vertical>
                        <n-card v-for="phong in phieuDat.danhSachPhongDaGan" :key="phong.idPhong" size="small"
                            :bordered="true">
                            <div class="flex justify-between items-center">
                                <div>
                                    <div class="font-semibold">
                                        Phòng {{ phong.maPhong }} - {{ phong.tenPhong }}
                                    </div>
                                    <div class="text-sm text-gray-600">
                                        {{ phong.tenLoaiPhong }} • Tầng {{ phong.tang }}
                                        • {{ phong.gia?.toLocaleString() }} VNĐ
                                    </div>
                                </div>
                            </div>
                        </n-card>
                    </n-space>
                </n-card>

                <!-- Action Buttons -->
                <n-card v-if="canEdit">
                    <n-space>
                        <n-button type="success" size="large" :disabled="!canConfirm" :loading="isConfirming"
                            @click="handleXacNhan">
                            <template #icon>
                                <nova-icon icon="carbon:checkmark" />
                            </template>
                            Xác nhận phiếu đặt
                        </n-button>
                        <n-button size="large" @click="router.back()">
                            Quay lại
                        </n-button>
                    </n-space>

                    <div v-if="!canConfirm" class="mt-4 text-orange-600 text-sm">
                        <nova-icon icon="carbon:warning" class="mr-1" />
                        Vui lòng gắn khách hàng và đủ số lượng phòng trước khi xác nhận
                    </div>
                </n-card>
            </div>
        </n-spin>

        <!-- Modal gắn khách hàng -->
        <n-modal v-model:show="showKhachHangModal" preset="card" title="Gắn khách hàng" style="width: 900px">
            <div class="space-y-4">
                <n-input v-model:value="searchKhach" placeholder="Nhập tên, SĐT, email để tìm kiếm..."
                    @keyup.enter="searchKhachHang">
                    <template #suffix>
                        <n-button text @click="searchKhachHang" :loading="searchingKhach">
                            <nova-icon icon="carbon:search" />
                        </n-button>
                    </template>
                </n-input>

                <n-data-table :columns="columns" :data="khachHangList" :loading="searchingKhach"
                    :pagination="{ pageSize: 10 }" :max-height="500" size="small" />
            </div>
        </n-modal>

    </div>
</template>

<style scoped>
.space-y-4>*+* {
    margin-top: 1rem;
}
</style>
