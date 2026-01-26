<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { PhieuDatTamResponse, ConfirmBookingRequest } from '@/service/api/letan/booking'
import { getPhieuDatTam, confirmBookingFromPhieuTam } from '@/service/api/letan/booking'

interface Props {
  visible: boolean
  sessionId: string | null | undefined
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const phieuData = ref<PhieuDatTamResponse | null>(null)
const isLoading = ref(false)

function closeModal() {
  modalVisible.value = false
  phieuData.value = null
}

async function loadPhieuDatTam() {
  if (!props.sessionId) {
    console.error('XacNhanDatPhongModal: No sessionId provided')
    return
  }

  try {
    isLoading.value = true

    phieuData.value = await getPhieuDatTam(props.sessionId)

  }
  catch (error: any) {
    window.$message.error('Không thể tải thông tin đặt phòng')
    closeModal()
  }
  finally {
    isLoading.value = false
  }
}

function canNhanNgay() {
  if (!phieuData.value) return false
  const now = Date.now()
  const oneHourBeforeCheckIn = phieuData.value.checkInDate - (60 * 60 * 1000)
  return now >= oneHourBeforeCheckIn
}

async function handleConfirm(nhanNgay: boolean) {
  if (!phieuData.value || !phieuData.value.idKhachHang) {
    window.$message.warning('Thiếu thông tin khách hàng')
    return
  }

  if (nhanNgay && !canNhanNgay()) {
    window.$message.warning('Chỉ có thể nhận phòng ngay khi còn tối đa 1 giờ trước check-in')
    return
  }

  try {
    isLoading.value = true

    const requestData: ConfirmBookingRequest = {
      sessionId: phieuData.value.sessionId,
      idKhachHang: phieuData.value.idKhachHang!,
      tenDoan: null,
      idChiTietDoan: null,
      checkInDate: phieuData.value.checkInDate,
      checkOutDate: phieuData.value.checkOutDate,
      soLuongKhach: phieuData.value.soLuongKhach,
      ghiChu: phieuData.value.ghiChu || null,
      nhanNgay,
      tienKhachTra: (phieuData.value.tienKhachTra !== null && phieuData.value.tienKhachTra !== undefined) ? phieuData.value.tienKhachTra : null,
      danhSachIdPhong: phieuData.value.danhSachPhong.map(p => p.idPhong),
    }

    await confirmBookingFromPhieuTam(requestData)

    emit('success')
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể xác nhận đặt phòng')
  }
  finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.sessionId) {
    loadPhieuDatTam()
  } else if (!val) {
    phieuData.value = null
  }
})

function formatDate(timestamp: number) {
  return new Date(timestamp).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    title="Xác nhận đặt phòng"
    class="w-900px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div v-if="phieuData" class="space-y-4">

        <n-card size="small" title="Thông tin khách hàng" :bordered="false" class="bg-blue-50">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <div class="text-gray-600 mb-1">Họ tên</div>
              <div class="font-semibold">{{ phieuData.tenKhachHang || 'Chưa có' }}</div>
            </div>
            <div>
              <div class="text-gray-600 mb-1">Số khách</div>
              <div class="font-semibold">{{ phieuData.soLuongKhach }} người</div>
            </div>
          </div>
        </n-card>

        <n-card size="small" title="Thời gian lưu trú" :bordered="false">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <div class="text-gray-600 mb-1">
                <nova-icon icon="carbon:calendar" class="mr-1" />Nhận phòng
              </div>
              <div class="font-semibold">{{ formatDate(phieuData.checkInDate) }}</div>
            </div>
            <div>
              <div class="text-gray-600 mb-1">
                <nova-icon icon="carbon:calendar" class="mr-1" />Trả phòng
              </div>
              <div class="font-semibold">{{ formatDate(phieuData.checkOutDate) }}</div>
            </div>
          </div>
        </n-card>

        <n-card size="small" :bordered="false">
          <template #header>
            <span class="font-semibold">Danh sách phòng ({{ phieuData.danhSachPhong.length }})</span>
          </template>

          <div class="space-y-2 max-h-[250px] overflow-y-auto">
            <div
              v-for="phong in phieuData.danhSachPhong"
              :key="phong.idPhong"
              class="border rounded-lg p-3 bg-gray-50"
            >
              <div class="flex justify-between items-start">
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-1">
                    <h4 class="font-bold text-base">{{ phong.tenPhong }}</h4>
                    <n-tag size="small" type="info">{{ phong.tenLoaiPhong }}</n-tag>
                  </div>
                  <div class="text-gray-600 text-sm">
                    <span>Tầng {{ phong.tang }} • {{ phong.soNgay }} đêm</span>
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-sm text-gray-600">{{ phong.gia.toLocaleString('vi-VN') }} VNĐ/đêm</div>
                  <div class="text-base font-bold text-blue-600">
                    {{ phong.thanhTien.toLocaleString('vi-VN') }} VNĐ
                  </div>
                </div>
              </div>
            </div>
          </div>
        </n-card>

        <n-card size="small" title="Thanh toán" :bordered="false" class="bg-gradient-to-r from-green-50 to-emerald-50">
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span>Tổng tiền phòng:</span>
              <span class="font-semibold">{{ phieuData.tongTien.toLocaleString('vi-VN') }} VNĐ</span>
            </div>

            <div v-if="phieuData.tienKhachTra !== null && phieuData.tienKhachTra !== undefined" class="flex justify-between">
              <span>Tiền khách trả:</span>
              <span class="font-semibold">{{ phieuData.tienKhachTra.toLocaleString('vi-VN') }} VNĐ</span>
            </div>

            <n-divider class="my-2" />

            <div v-if="phieuData.tienThua > 0" class="flex justify-between text-green-600 font-semibold">
              <span>Tiền thừa trả khách:</span>
              <span>{{ phieuData.tienThua.toLocaleString('vi-VN') }} VNĐ</span>
            </div>

            <div v-if="phieuData.congNo > 0" class="flex justify-between text-red-600 font-semibold">
              <span>Công nợ còn lại:</span>
              <span>{{ phieuData.congNo.toLocaleString('vi-VN') }} VNĐ</span>
            </div>

            <div class="flex justify-between items-center pt-2 border-t-2 border-green-300">
              <span class="text-base font-semibold">Tổng cộng:</span>
              <span class="text-2xl font-bold text-green-600">
                {{ phieuData.tongTien.toLocaleString('vi-VN') }} VNĐ
              </span>
            </div>
          </div>
        </n-card>

        <n-card v-if="phieuData.ghiChu" size="small" title="Ghi chú" :bordered="false">
          <n-text class="text-sm">{{ phieuData.ghiChu }}</n-text>
        </n-card>

        <n-alert v-if="!canNhanNgay()" type="warning" size="small">
          <template #icon>
            <nova-icon icon="carbon:warning" />
          </template>
          Chỉ có thể nhận phòng ngay khi còn tối đa 1 giờ trước thời gian check-in
        </n-alert>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-button size="large" @click="closeModal">Hủy</n-button>
        <n-space>
          <n-button
            type="default"
            size="large"
            @click="handleConfirm(false)"
          >
            <template #icon>
              <nova-icon icon="carbon:calendar-add" />
            </template>
            Đặt phòng trước
          </n-button>
          <n-button
            type="primary"
            size="large"
            :disabled="!canNhanNgay()"
            @click="handleConfirm(true)"
          >
            <template #icon>
              <nova-icon icon="carbon:license-draft" />
            </template>
            Nhận phòng ngay
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-900px {
  width: 900px;
  max-width: 95vw;
  max-height: 90vh;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 16px;
  font-weight: 600;
  padding: 10px 16px;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-button__content),
.modal-custom-font :deep(p),
.modal-custom-font :deep(span) {
  font-size: 14px;
}
</style>
