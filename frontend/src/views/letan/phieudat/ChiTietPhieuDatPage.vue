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
            return 3
        case 'CANCELLED':
            return 2
        default:
            return 1
    }
})
</script>

<template>
    <div class="chi-tiet-page">
        <n-spin :show="loading">
            <div v-if="phieuDat" class="page-container">
                <!-- Header Section - Compact -->
                <n-card class="header-card" size="small">
                    <div class="header-content">
                        <div class="header-left">
                            <h2 class="phieu-title">{{ phieuDat.maPhieu }}</h2>
                            <div class="meta-info">
                                <span>{{ new Date(phieuDat.ngayTao).toLocaleString('vi-VN') }}</span>
                                <span v-if="phieuDat.tenNhanVienTao" class="separator">•</span>
                                <span v-if="phieuDat.tenNhanVienTao">{{ phieuDat.tenNhanVienTao }}</span>
                            </div>
                        </div>
                        <n-tag :type="statusColor[phieuDat.trangThai]?.type" size="large" class="status-tag">
                            {{ statusColor[phieuDat.trangThai]?.text }}
                        </n-tag>
                    </div>
                </n-card>

                <!-- Workflow Steps - Full Width -->
                <n-card title="Quy trình xử lý" size="small" class="workflow-card">
                    <n-steps :current="currentStep" :status="phieuDat.trangThai === 'CANCELLED' ? 'error' : 'process'">
                        <n-step title="Tạo phiếu" description="Đã tạo phiếu đặt" />
                        <n-step title="Gắn khách hàng và phòng" :description="phieuDat.khachHang ? 'Đã gắn khách hàng' : 'Chưa gắn khách hàng'" />
                        <n-step title="Xác nhận" :description="phieuDat.trangThai === 'CONFIRMED' ? 'Đã xác nhận' : 'Chưa xác nhận'" />
                    </n-steps>
                </n-card>

                <!-- Main Grid Layout - 3 Columns -->
                <div class="main-grid">
                    <!-- Column 1: Basic Info -->
                    <n-card title="Thông tin đặt phòng" size="small" class="info-card">
                        <div class="info-list">
                            <div class="info-row">
                                <span class="info-label">
                                    <nova-icon icon="carbon:calendar" :size="16" />
                                    Check-in
                                </span>
                                <span class="info-value">{{ new Date(phieuDat.ngayCheckIn).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">
                                    <nova-icon icon="carbon:calendar" :size="16" />
                                    Check-out
                                </span>
                                <span class="info-value">{{ new Date(phieuDat.ngayCheckOut).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }) }}</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">
                                    <nova-icon icon="carbon:user-multiple" :size="16" />
                                    Số khách
                                </span>
                                <span class="info-value highlight">{{ phieuDat.soLuongKhach }} người</span>
                            </div>
                            <div class="info-row total-row">
                                <span class="info-label">
                                    <nova-icon icon="carbon:currency" :size="16" />
                                    Tổng tiền
                                </span>
                                <span class="info-value price">{{ phieuDat.tongTien?.toLocaleString() }} VNĐ</span>
                            </div>
                        </div>
                        <div v-if="phieuDat.ghiChu" class="note-section">
                            <div class="note-header">
                                <nova-icon icon="carbon:document" :size="16" />
                                <span>Ghi chú</span>
                            </div>
                            <p class="note-content">{{ phieuDat.ghiChu }}</p>
                        </div>
                    </n-card>

                    <!-- Column 2: Customer Info -->
                    <n-card title="Thông tin khách hàng" size="small" class="customer-card">
                        <div v-if="phieuDat.khachHang" class="customer-info">
                            <div class="customer-row">
                                <span class="label">
                                    <nova-icon icon="carbon:user" :size="16" />
                                    Họ tên
                                </span>
                                <span class="value">{{ phieuDat.khachHang.hoTen }}</span>
                            </div>
                            <div class="customer-row">
                                <span class="label">
                                    <nova-icon icon="carbon:phone" :size="16" />
                                    Số điện thoại
                                </span>
                                <span class="value">{{ phieuDat.khachHang.soDienThoai }}</span>
                            </div>
                            <div class="customer-row">
                                <span class="label">
                                    <nova-icon icon="carbon:email" :size="16" />
                                    Email
                                </span>
                                <span class="value">{{ phieuDat.khachHang.email || 'Chưa có' }}</span>
                            </div>
                        </div>
                        <n-empty v-else description="Chưa gắn khách hàng" size="small">
                            <template #extra>
                                <n-button v-if="canEdit" type="primary" size="small" @click="openGanKhachHang">
                                    <template #icon>
                                        <nova-icon icon="carbon:user-follow" />
                                    </template>
                                    Gắn khách hàng
                                </n-button>
                            </template>
                        </n-empty>

                        <!-- Room Types in Customer Card -->
                        <div class="divider"></div>
                        <div class="section-title">
                            <nova-icon icon="carbon:category" :size="16" />
                            <span>Loại phòng đã đặt</span>
                        </div>
                        <div class="room-types-compact">
                            <div v-for="loaiPhong in phieuDat.danhSachLoaiPhong" :key="loaiPhong.idLoaiPhong" class="room-type-compact">
                                <div class="type-header">
                                    <span class="type-name">{{ loaiPhong.tenLoaiPhong }}</span>
                                    <span class="type-count">{{ loaiPhong.soLuongDaGan }}/{{ loaiPhong.soLuong }}</span>
                                </div>
                                <div class="type-footer">
                                    <span class="type-price">{{ loaiPhong.gia?.toLocaleString() }} VNĐ</span>
                                    <n-progress
                                        :percentage="(loaiPhong.soLuongDaGan / loaiPhong.soLuong) * 100"
                                        :show-indicator="false"
                                        :status="loaiPhong.soLuongDaGan === loaiPhong.soLuong ? 'success' : 'info'"
                                        :height="4"
                                        style="flex: 1; max-width: 80px;"
                                    />
                                </div>
                            </div>
                        </div>
                    </n-card>

                    <!-- Column 3: Assigned Rooms -->
                    <n-card title="Danh sách phòng đã gắn" size="small" class="rooms-card">
                        <div v-if="phieuDat.danhSachPhongDaGan?.length > 0" class="rooms-list">
                            <div v-for="phong in phieuDat.danhSachPhongDaGan" :key="phong.idPhong" class="room-item">
                                <div class="room-header">
                                    <span class="room-badge">{{ phong.tenPhong }}</span>
                                </div>
                                <div class="room-details">
                                    <span class="detail-item">
                                        <nova-icon icon="carbon:category" :size="14" />
                                        {{ phong.tenLoaiPhong }}
                                    </span>
                                    <span class="detail-item">
                                        <nova-icon icon="carbon:building" :size="14" />
                                        Tầng {{ phong.tang }}
                                    </span>
                                </div>
                                <div class="room-price">{{ phong.gia?.toLocaleString() }} VNĐ</div>
                            </div>
                        </div>
                        <n-empty v-else description="Chưa có phòng nào được gắn" size="small">
                            <template #icon>
                                <nova-icon icon="carbon:hotel" :size="48" style="opacity: 0.3;" />
                            </template>
                        </n-empty>
                    </n-card>
                </div>

                <!-- Action Footer -->

            </div>
        </n-spin>

        <!-- Modal gắn khách hàng -->
        <n-modal v-model:show="showKhachHangModal" preset="card" title="Gắn khách hàng" style="width: 900px">
            <div class="modal-content">
                <n-input v-model:value="searchKhach" placeholder="Nhập tên, SĐT, email để tìm kiếm..." size="large"
                    @keyup.enter="searchKhachHang">
                    <template #suffix>
                        <n-button text @click="searchKhachHang" :loading="searchingKhach">
                            <nova-icon icon="carbon:search" />
                        </n-button>
                    </template>
                </n-input>

                <n-data-table :columns="columns" :data="khachHangList" :loading="searchingKhach"
                    :pagination="{ pageSize: 10 }" :max-height="400" size="small" class="mt-4" />
            </div>
        </n-modal>
    </div>
</template>

<style scoped>
.chi-tiet-page {
    padding: 24px;
    background: #f5f7fa;
    min-height: 100vh;
}

.page-container {
    max-width: 1900px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

/* Header Card */
.header-card {
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    border: 1px solid #475569;
}

.header-card :deep(.n-card__content) {
    padding: 20px 24px;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-left {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.phieu-title {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin: 0;
}

.meta-info {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    color: rgba(255, 255, 255, 0.9);
}

.separator {
    color: rgba(255, 255, 255, 0.6);
}

.status-tag {
    font-weight: 600;
    padding: 8px 16px;
}

/* Main Grid */
.main-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
    align-items: start;
}

.left-column,
.right-column {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

/* Workflow Card - Full Width */
.workflow-card {
    margin-bottom: 20px;
}

.workflow-card :deep(.n-card__content) {
    padding: 24px;
}

/* Main Grid - 3 Columns with Equal Heights */
.main-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    align-items: stretch;
}

.info-card,
.customer-card,
.rooms-card {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.info-card :deep(.n-card),
.customer-card :deep(.n-card),
.rooms-card :deep(.n-card) {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.info-card :deep(.n-card__content),
.customer-card :deep(.n-card__content),
.rooms-card :deep(.n-card__content) {
    flex: 1;
    display: flex;
    flex-direction: column;
}

/* Info Card */
.info-card :deep(.n-card__content) {
    padding: 20px;
}

.info-list {
    display: flex;
    flex-direction: column;
    gap: 0;
    flex: 1;
}

.info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 14px;
    border-bottom: 1px solid #f1f5f9;
}

.info-row:last-child {
    border-bottom: none;
}

.info-row.total-row {
    background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
    margin-top: 10px;
    border-radius: 8px;
    border: 1px solid #86efac;
}

.info-label {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    color: #64748b;
    font-weight: 500;
}

.info-value {
    font-size: 16px;
    color: #1e293b;
    font-weight: 600;
}

.info-value.highlight {
    color: #3b82f6;
    font-size: 17px;
}

.info-value.price {
    color: #10b981;
    font-size: 20px;
    font-weight: 700;
}

.note-section {
    margin-top: 18px;
    padding: 14px;
    background: #fffbeb;
    border-radius: 8px;
    border: 1px solid #fde68a;
}

.note-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #92400e;
    font-weight: 600;
    margin-bottom: 10px;
}

.note-content {
    margin: 0;
    font-size: 15px;
    color: #78350f;
    line-height: 1.6;
}

/* Customer Card */
.customer-card :deep(.n-card__content) {
    padding: 20px;
}

.customer-info {
    display: flex;
    flex-direction: column;
    gap: 0;
    flex: 0 0 auto;
}

.customer-row {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding: 14px;
    border-bottom: 1px solid #f1f5f9;
}

.customer-row:last-child {
    border-bottom: none;
}

.customer-row .label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #64748b;
    font-weight: 500;
}

.customer-row .value {
    font-size: 16px;
    color: #1e293b;
    font-weight: 600;
    padding-left: 26px;
}

.divider {
    height: 1px;
    background: #e2e8f0;
    margin: 20px 0;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    color: #1e293b;
    font-weight: 600;
    margin-bottom: 14px;
}

/* Room Types Compact */
.room-types-compact {
    display: flex;
    flex-direction: column;
    gap: 12px;
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
}

.room-type-compact {
    padding: 12px 14px;
    background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
    border-radius: 8px;
    border: 1px solid #fbbf24;
}

.type-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.type-name {
    font-size: 15px;
    font-weight: 600;
    color: #78350f;
}

.type-count {
    font-size: 14px;
    font-weight: 700;
    color: #92400e;
    background: #fff;
    padding: 3px 10px;
    border-radius: 5px;
}

.type-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 14px;
}

.type-price {
    font-size: 14px;
    color: #78350f;
    font-weight: 600;
}

/* Rooms Card */
.rooms-card :deep(.n-card__content) {
    padding: 20px;
}

.rooms-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    flex: 1;
    overflow-y: auto;
    padding-right: 4px;
}

.room-item {
    padding: 14px;
    background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
    border-radius: 8px;
    border: 1px solid #cbd5e1;
    transition: all 0.2s;
}

.room-item:hover {
    border-color: #94a3b8;
    box-shadow: 0 3px 6px rgba(0, 0, 0, 0.08);
}

.room-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;
}

.room-badge {
    font-size: 14px;
    font-weight: 700;
    color: #fff;
    background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
    padding: 5px 12px;
    border-radius: 5px;
    flex-shrink: 0;
}

.room-name {
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
}

.room-details {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 8px;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: #64748b;
}

.room-price {
    font-size: 15px;
    font-weight: 700;
    color: #10b981;
    text-align: right;
}

/* Action Card */
.action-card {
    background: #fff;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.action-card :deep(.n-card__content) {
    padding: 20px 24px;
}

.action-content {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.warning-message {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 14px 18px;
    background: #fef3c7;
    border: 1px solid #fbbf24;
    border-radius: 8px;
    color: #92400e;
    font-size: 15px;
}

.action-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 14px;
}

/* Modal */
.modal-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.mt-4 {
    margin-top: 16px;
}

/* Scrollbar styling */
.rooms-list::-webkit-scrollbar,
.room-types-compact::-webkit-scrollbar {
    width: 5px;
}

.rooms-list::-webkit-scrollbar-track,
.room-types-compact::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 3px;
}

.rooms-list::-webkit-scrollbar-thumb,
.room-types-compact::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 3px;
}

.rooms-list::-webkit-scrollbar-thumb:hover,
.room-types-compact::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}

/* Global font size increases */
:deep(.n-card-header__main) {
    font-size: 18px;
    font-weight: 600;
}

:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-button__content) {
    font-size: 16px;
}

:deep(.n-data-table-th),
:deep(.n-data-table-td) {
    font-size: 16px;
}

:deep(.n-empty__description) {
    font-size: 15px;
}

:deep(.n-steps-item__title) {
    font-size: 16px !important;
}

:deep(.n-steps-item__description) {
    font-size: 14px !important;
}

:deep(.n-button--large) {
    padding: 12px 24px;
    font-size: 16px !important;
}

/* Responsive */
@media (max-width: 1400px) {
    .main-grid {
        grid-template-columns: 1fr 1fr;
    }

    .rooms-card {
        grid-column: 1 / -1;
    }
}

@media (max-width: 900px) {
    .main-grid {
        grid-template-columns: 1fr;
    }
}
</style>
