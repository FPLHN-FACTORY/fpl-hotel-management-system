<script setup lang="ts">
import { reactive, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useNotification } from 'naive-ui'

import { useDataCombobox } from '@/store/dataCombox'
import { apiTaoPhieuDat } from '@/service/api/letan/phieudat'
import { getSoDoPhong } from '@/service/api/letan/sodophong'

const router = useRouter()
const notification = useNotification()
const { fetchDataLoaiPhong } = useDataCombobox()

const formData = reactive({
    ngayCheckIn: null as number | null,
    ngayCheckOut: null as number | null,
    soLuongKhach: 1,
    ghiChu: '',
    danhSachLoaiPhong: [] as { idLoaiPhong: string; soLuong: number }[]
})

const dateRange = ref<[number, number] | null>(null)
const loaiPhongKhaDung = ref<any[]>([])
const isCheckingRooms = ref(false)
const isSubmitting = ref(false)

onMounted(() => {
    fetchDataLoaiPhong()
})

// Watch date range changes
watch(dateRange, (newRange) => {
    if (newRange && newRange.length === 2) {
        formData.ngayCheckIn = newRange[0]
        formData.ngayCheckOut = newRange[1]
    }
})

async function checkPhongTrong() {
    if (!formData.ngayCheckIn || !formData.ngayCheckOut) {
        notification.warning({
            content: 'Vui lòng chọn ngày check-in và check-out',
            duration: 2000
        })
        return
    }

    if (!formData.soLuongKhach) {
        notification.warning({
            content: 'Vui lòng nhập số lượng khách',
            duration: 2000
        })
        return
    }

    isCheckingRooms.value = true

    try {
        // Call check phòng trống API from existing booking module
        const response = await getSoDoPhong({
            ngayDen: formData.ngayCheckIn,
            ngayDi: formData.ngayCheckOut
        })

        // Group by loại phòng and count available rooms
        const loaiPhongMap = new Map()

        response.forEach((room: any) => {
            if (room.trangThaiPhong === 'TRONG') {
                const loaiPhongId = room.loaiPhongId
                if (!loaiPhongMap.has(loaiPhongId)) {
                    loaiPhongMap.set(loaiPhongId, {
                        id: loaiPhongId,
                        ten: room.loaiPhong,
                        soPhongTrong: 0,
                        gia: room.price
                    })
                }
                loaiPhongMap.get(loaiPhongId).soPhongTrong++
            }
        })

        loaiPhongKhaDung.value = Array.from(loaiPhongMap.values())

        if (loaiPhongKhaDung.value.length === 0) {
            notification.warning({
                content: 'Không có phòng trống trong khoảng thời gian này',
                duration: 3000
            })
        } else {
            notification.success({
                content: `Tìm thấy ${loaiPhongKhaDung.value.length} loại phòng khả dụng`,
                duration: 2000
            })
        }
    } catch (error: any) {
        notification.error({
            content: error.message || 'Lỗi khi kiểm tra phòng trống',
            duration: 3000
        })
    } finally {
        isCheckingRooms.value = false
    }
}

function addLoaiPhong(loaiPhong: any) {
    const exists = formData.danhSachLoaiPhong.find(lp => lp.idLoaiPhong === loaiPhong.id)

    if (exists) {
        notification.warning({
            content: 'Loại phòng này đã được thêm',
            duration: 2000
        })
        return
    }

    formData.danhSachLoaiPhong.push({
        idLoaiPhong: loaiPhong.id,
        soLuong: 1
    })
}

function removeLoaiPhong(index: number) {
    formData.danhSachLoaiPhong.splice(index, 1)
}

function getLoaiPhongName(id: string) {
    const loaiPhong = loaiPhongKhaDung.value.find(lp => lp.id === id)
    return loaiPhong?.ten || 'N/A'
}

function getMaxSoLuong(id: string) {
    const loaiPhong = loaiPhongKhaDung.value.find(lp => lp.id === id)
    return loaiPhong?.soPhongTrong || 1
}

async function handleSubmit() {
    // Validation
    if (!formData.ngayCheckIn || !formData.ngayCheckOut) {
        notification.warning({
            content: 'Vui lòng chọn ngày check-in và check-out',
            duration: 2000
        })
        return
    }

    if (!formData.soLuongKhach) {
        notification.warning({
            content: 'Vui lòng nhập số lượng khách',
            duration: 2000
        })
        return
    }

    if (formData.danhSachLoaiPhong.length === 0) {
        notification.warning({
            content: 'Vui lòng chọn ít nhất một loại phòng',
            duration: 2000
        })
        return
    }

    isSubmitting.value = true

    try {
        const response = await apiTaoPhieuDat(formData as any)

        notification.success({
            content: 'Tạo phiếu đặt thành công!',
            duration: 2000
        })

        // Redirect to chi tiết phiếu đặt (response.data.data contains the ID)
        const phieuDatId = response.data?.data || response.data
        router.push({ name: 'chiTietPhieuDat', params: { id: phieuDatId } })
    } catch (error: any) {
        notification.error({
            content: error.message || 'Lỗi khi tạo phiếu đặt',
            duration: 3000
        })
    } finally {
        isSubmitting.value = false
    }
}
</script>

<template>
    <div class="p-6">
        <n-card title="Tạo Phiếu Đặt Phòng Mới">
            <n-form :model="formData" label-placement="left" label-width="150">
                <!-- Thời gian -->
                <n-form-item label="Thời gian lưu trú" required>
                    <n-date-picker v-model:value="dateRange" type="datetimerange" clearable
                        start-placeholder="Ngày check-in" end-placeholder="Ngày check-out" style="width: 100%" />
                </n-form-item>

                <!-- Số lượng khách -->
                <n-form-item label="Số lượng khách" required>
                    <n-input-number v-model:value="formData.soLuongKhach" :min="1" :max="20"
                        placeholder="Nhập số lượng khách" style="width: 200px" />
                </n-form-item>

                <!-- Ghi chú -->
                <n-form-item label="Ghi chú">
                    <n-input v-model:value="formData.ghiChu" type="textarea" placeholder="Nhập ghi chú (không bắt buộc)"
                        :rows="3" />
                </n-form-item>

                <!-- Button kiểm tra phòng trống -->
                <n-form-item>
                    <n-button type="primary" :loading="isCheckingRooms" @click="checkPhongTrong">
                        Kiểm tra phòng trống
                    </n-button>
                </n-form-item>
            </n-form>

            <!-- Danh sách loại phòng khả dụng -->
            <div v-if="loaiPhongKhaDung.length > 0" class="mt-6">
                <n-divider />
                <h3 class="text-lg font-semibold mb-4">Loại phòng khả dụng</h3>

                <n-space vertical>
                    <n-card v-for="loaiPhong in loaiPhongKhaDung" :key="loaiPhong.id" size="small" hoverable>
                        <div class="flex justify-between items-center">
                            <div>
                                <div class="font-semibold">{{ loaiPhong.ten }}</div>
                                <div class="text-sm text-gray-500">
                                    Còn {{ loaiPhong.soPhongTrong }} phòng trống
                                    • Giá: {{ loaiPhong.gia?.toLocaleString() }} VNĐ/ngày
                                </div>
                            </div>
                            <n-button type="primary" size="small" @click="addLoaiPhong(loaiPhong)">
                                Thêm
                            </n-button>
                        </div>
                    </n-card>
                </n-space>
            </div>

            <!-- Danh sách loại phòng đã chọn -->
            <div v-if="formData.danhSachLoaiPhong.length > 0" class="mt-6">
                <n-divider />
                <h3 class="text-lg font-semibold mb-4">Loại phòng đã chọn</h3>

                <n-space vertical>
                    <n-card v-for="(item, index) in formData.danhSachLoaiPhong" :key="index" size="small">
                        <div class="flex justify-between items-center">
                            <div class="flex-1">
                                <div class="font-semibold">{{ getLoaiPhongName(item.idLoaiPhong) }}</div>
                            </div>
                            <div class="flex items-center gap-4">
                                <n-input-number v-model:value="item.soLuong" :min="1"
                                    :max="getMaxSoLuong(item.idLoaiPhong)" size="small" style="width: 100px">
                                    <template #prefix>
                                        SL:
                                    </template>
                                </n-input-number>
                                <n-button type="error" size="small" @click="removeLoaiPhong(index)">
                                    Xóa
                                </n-button>
                            </div>
                        </div>
                    </n-card>
                </n-space>
            </div>

            <!-- Action buttons -->
            <template #footer>
                <div class="flex justify-end gap-2">
                    <n-button @click="router.back()">
                        Hủy
                    </n-button>
                    <n-button type="primary" :loading="isSubmitting" :disabled="formData.danhSachLoaiPhong.length === 0"
                        @click="handleSubmit">
                        Tạo phiếu đặt
                    </n-button>
                </div>
            </template>
        </n-card>
    </div>
</template>

<style scoped>
:deep(.n-card__footer) {
    padding-top: 16px;
}
</style>
