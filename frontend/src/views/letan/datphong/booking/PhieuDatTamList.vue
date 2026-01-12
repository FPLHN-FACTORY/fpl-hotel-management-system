<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { PhieuDatTamResponse } from '@/service/api/letan/booking'
import { getAllPhieuDatTam, deletePhieuDatTam } from '@/service/api/letan/booking'
import { phieuDatTamStorage } from '@/service/api/letan/phieuDatTamStorage'
import { useDialog } from 'naive-ui'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'continue-from-step', data: {
    sessionId: string
    step: 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'CONFIRM'
  }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialog = useDialog()
const phieuList = ref<PhieuDatTamResponse[]>([])
const isLoading = ref(false)

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

async function fetchPhieuDatTam() {
  try {
    isLoading.value = true
    phieuList.value = await getAllPhieuDatTam()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tải danh sách phiếu đặt tạm')
  }
  finally {
    isLoading.value = false
  }
}

function handleContinue(phieu: PhieuDatTamResponse) {
  const nextStep = phieuDatTamStorage.determineNextStep({
    ...phieu,
    createdAt: Date.now(),
  })

  emit('continueFromStep', {
    sessionId: phieu.sessionId,
    step: nextStep,
  })
  modalVisible.value = false
}

function handleDelete(sessionId: string, tenKhachHang: string | null) {
  dialog.warning({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc muốn xóa phiếu đặt tạm của ${tenKhachHang || 'khách chưa xác định'}?`,
    positiveText: 'Xóa',
    negativeText: 'Hủy',
    onPositiveClick: async () => {
      try {
        await deletePhieuDatTam(sessionId)
        window.$message.success('Đã xóa phiếu đặt tạm')
        fetchPhieuDatTam()
      }
      catch (error: any) {
        window.$message.error(error.message || 'Không thể xóa phiếu đặt tạm')
      }
    },
  })
}

function getStepLabel(phieu: PhieuDatTamResponse): string {
  const nextStep = phieuDatTamStorage.determineNextStep({
    ...phieu,
    createdAt: Date.now(),
  })

  switch (nextStep) {
    case 'CUSTOMER_INFO':
      return 'Cần nhập thông tin khách'
    case 'PAYMENT_INFO':
      return 'Cần nhập thông tin thanh toán'
    case 'CONFIRM':
      return 'Sẵn sàng xác nhận'
    default:
      return 'Chưa hoàn thiện'
  }
}

function getStepTagType(phieu: PhieuDatTamResponse): 'warning' | 'info' | 'success' {
  const nextStep = phieuDatTamStorage.determineNextStep({
    ...phieu,
    createdAt: Date.now(),
  })

  switch (nextStep) {
    case 'CUSTOMER_INFO':
      return 'warning'
    case 'PAYMENT_INFO':
      return 'info'
    case 'CONFIRM':
      return 'success'
    default:
      return 'warning'
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    fetchPhieuDatTam()
  }
})

function formatDate(timestamp: number) {
  return new Date(timestamp).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getTimeAgo(timestamp: number) {
  const now = Date.now()
  const diff = now - timestamp
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 60) {
    return `${minutes} phút trước`
  }
  if (hours < 24) {
    return `${hours} giờ trước`
  }
  return `${days} ngày trước`
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    preset="card"
    title="Phiếu đặt tạm"
    class="w-1200px modal-custom-font"
    :segmented="{ content: true }"
  >
    <n-spin :show="isLoading">
      <div v-if="phieuList.length === 0" class="text-center py-8">
        <n-empty description="Không có phiếu đặt tạm nào">
          <template #icon>
            <nova-icon icon="carbon:document" :size="64" class="text-gray-400" />
          </template>
        </n-empty>
      </div>

      <div v-else class="space-y-3 max-h-[600px] overflow-y-auto">
        <n-card
          v-for="phieu in phieuList"
          :key="phieu.sessionId"
          size="small"
          :bordered="true"
          class="hover:shadow-lg transition-all"
        >
          <div class="grid grid-cols-12 gap-4">
            <!-- Cột trái: Thông tin chính -->
            <div class="col-span-8">
              <div class="flex justify-between items-start mb-3">
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <h3 class="text-base font-bold">
                      {{ phieu.tenKhachHang || 'Chưa chọn khách hàng' }}
                    </h3>
                    <n-tag
                      :type="getStepTagType(phieu)"
                      size="small"
                    >
                      {{ getStepLabel(phieu) }}
                    </n-tag>
                    <n-tag
                      v-if="phieu.nhanNgay"
                      type="success"
                      size="small"
                    >
                      Nhận ngay
                    </n-tag>
                  </div>
                  <div class="text-xs text-gray-500">
                    Lưu {{ getTimeAgo(phieu.checkInDate) }}
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-lg font-bold text-blue-600">
                    {{ phieu.tongTien.toLocaleString('vi-VN') }} VNĐ
                  </div>
                  <div v-if="phieu.tienKhachTra !== null && phieu.tienKhachTra !== undefined" class="text-xs text-gray-600">
                    Đã trả: {{ phieu.tienKhachTra.toLocaleString('vi-VN') }}
                  </div>
                </div>
              </div>

              <!-- Thông tin thời gian -->
              <div class="grid grid-cols-2 gap-3 mb-3 text-sm">
                <div class="bg-gray-50 p-2 rounded">
                  <div class="text-gray-600 text-xs mb-1">
                    <nova-icon icon="carbon:calendar" class="mr-1" />Nhận phòng
                  </div>
                  <div class="font-semibold">{{ formatDate(phieu.checkInDate) }}</div>
                </div>
                <div class="bg-gray-50 p-2 rounded">
                  <div class="text-gray-600 text-xs mb-1">
                    <nova-icon icon="carbon:calendar" class="mr-1" />Trả phòng
                  </div>
                  <div class="font-semibold">{{ formatDate(phieu.checkOutDate) }}</div>
                </div>
              </div>

              <!-- Danh sách phòng -->
              <div>
                <div class="text-xs text-gray-600 mb-1">Phòng đã chọn:</div>
                <div class="flex flex-wrap gap-1">
                  <n-tag
                    v-for="phong in phieu.danhSachPhong"
                    :key="phong.idPhong"
                    size="small"
                    :bordered="false"
                  >
                    {{ phong.maPhong }}
                  </n-tag>
                </div>
              </div>

              <!-- Ghi chú -->
              <div v-if="phieu.ghiChu" class="mt-2 text-sm">
                <span class="text-gray-600">Ghi chú:</span>
                <span class="ml-1">{{ phieu.ghiChu }}</span>
              </div>
            </div>

            <!-- Cột phải: Thanh toán & Actions -->
            <div class="col-span-4 flex flex-col justify-between">
              <div class="bg-gradient-to-br from-green-50 to-emerald-50 p-3 rounded-lg">
                <div class="space-y-1 text-sm">
                  <div class="flex justify-between">
                    <span class="text-gray-600">Số phòng:</span>
                    <span class="font-semibold">{{ phieu.danhSachPhong.length }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Số khách:</span>
                    <span class="font-semibold">{{ phieu.soLuongKhach }}</span>
                  </div>

                  <n-divider class="!my-1" />

                  <div v-if="phieu.tienThua > 0" class="flex justify-between text-green-600 font-semibold text-xs">
                    <span>Tiền thừa:</span>
                    <span>{{ phieu.tienThua.toLocaleString('vi-VN') }}</span>
                  </div>
                  <div v-if="phieu.congNo > 0" class="flex justify-between text-red-600 font-semibold text-xs">
                    <span>Công nợ:</span>
                    <span>{{ phieu.congNo.toLocaleString('vi-VN') }}</span>
                  </div>
                </div>
              </div>

              <div class="flex gap-2 mt-3">
                <n-button
                  type="primary"
                  size="small"
                  block
                  @click="handleContinue(phieu)"
                >
                  <template #icon>
                    <nova-icon icon="carbon:arrow-right" />
                  </template>
                  Tiếp tục
                </n-button>
                <n-button
                  type="error"
                  size="small"
                  quaternary
                  @click="handleDelete(phieu.sessionId, phieu.tenKhachHang)"
                >
                  <template #icon>
                    <nova-icon icon="carbon:trash-can" />
                  </template>
                  Xoá
                </n-button>
              </div>
            </div>
          </div>
        </n-card>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="end">
        <n-button @click="modalVisible = false">Đóng</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1200px {
  width: 1200px;
  max-width: 95vw;
  max-height: 90vh;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
}

.modal-custom-font :deep(.n-card) {
  border-color: #e5e7eb;
}

.modal-custom-font :deep(.n-card:hover) {
  border-color: #3b82f6;
}
</style>
