<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChonLoaiPhong, LoaiPhongAvailableResponse } from '@/service/api/letan/booking'
import { checkPhongTrong } from '@/service/api/letan/booking'

interface Props {
  visible: boolean
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'submit', data: {
    ngayNhan: number
    ngayTra: number
    soLuongKhach: number
    danhSachLoaiPhong: ChonLoaiPhong[]
  }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const formData = ref({
  ngayNhan: null as [number, number] | null,
  soLuongKhach: 1,
})

const loaiPhongList = ref<LoaiPhongAvailableResponse[]>([])
const isLoading = ref(false)
const hasSearched = ref(false)

const selectedLoaiPhong = ref<Map<string, number>>(new Map())

const soNgayO = computed(() => {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1])
    return 0
  return Math.ceil((formData.value.ngayNhan[1] - formData.value.ngayNhan[0]) / (1000 * 60 * 60 * 24))
})

function closeModal() {
  modalVisible.value = false
  resetForm()
}

function resetForm() {
  formData.value = {
    ngayNhan: null,
    soLuongKhach: 1,
  }
  loaiPhongList.value = []
  selectedLoaiPhong.value = new Map()
  hasSearched.value = false
}

async function handleSearch() {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1]) {
    window.$message.warning('Vui lòng chọn ngày nhận và trả phòng')
    return
  }

  if (formData.value.soLuongKhach <= 0) {
    window.$message.warning('Số lượng khách phải lớn hơn 0')
    return
  }

  try {
    isLoading.value = true
    const data = await checkPhongTrong({
      ngayNhan: formData.value.ngayNhan[0],
      ngayTra: formData.value.ngayNhan[1],
      soLuongKhach: formData.value.soLuongKhach,
    })

    loaiPhongList.value = data
    hasSearched.value = true

    if (data.length === 0) {
      window.$message.warning('Không có phòng trống trong khoảng thời gian này')
    }
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể kiểm tra phòng trống')
  }
  finally {
    isLoading.value = false
  }
}

function handleQuantityChange(idLoaiPhong: string, value: number | null) {
  if (value === null || value <= 0) {
    selectedLoaiPhong.value.delete(idLoaiPhong)
  }
  else {
    const loaiPhong = loaiPhongList.value.find(lp => lp.idLoaiPhong === idLoaiPhong)
    if (loaiPhong && value <= loaiPhong.soPhongTrong) {
      selectedLoaiPhong.value.set(idLoaiPhong, value)
    }
    else {
      window.$message.warning(`Chỉ còn ${loaiPhong?.soPhongTrong} phòng trống`)
    }
  }
}

function handleSubmit() {
  if (!formData.value.ngayNhan || !formData.value.ngayNhan[0] || !formData.value.ngayNhan[1]) {
    window.$message.warning('Vui lòng chọn ngày nhận và trả phòng')
    return
  }

  if (selectedLoaiPhong.value.size === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một loại phòng')
    return
  }

  const danhSachLoaiPhong: ChonLoaiPhong[] = Array.from(selectedLoaiPhong.value.entries()).map(
    ([idLoaiPhong, soLuong]) => {
      const info = loaiPhongList.value.find(lp => lp.idLoaiPhong === idLoaiPhong)
      return {
        idLoaiPhong,
        soLuong,
        tenLoaiPhong: info?.tenLoaiPhong,
        gia: info?.giaCaNgay,
      }
    },
  )

  emit('submit', {
    ngayNhan: formData.value.ngayNhan[0],
    ngayTra: formData.value.ngayNhan[1],
    soLuongKhach: formData.value.soLuongKhach,
    danhSachLoaiPhong,
  })

  closeModal()
}

watch(() => props.visible, (val) => {
  if (!val) {
    resetForm()
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
    title="Đặt phòng theo loại"
    :style="{ width: '90vw', maxWidth: '2000px' }"
    class="modal-custom-font compact-modal chon-loai-phong-modal"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="grid grid-cols-[1fr_350px] gap-8 h-[600px]">
        <!-- Cột trái: Danh sách loại phòng -->
        <div class="flex flex-col bg-white rounded-lg border border-gray-100 p-4 h-full overflow-hidden">
          <div class="overflow-y-auto pr-2 flex flex-col gap-4 h-full no-scrollbar pointer-events-auto">
            <template v-if="loaiPhongList.length > 0">
              <n-card
                v-for="loaiPhong in loaiPhongList"
                :key="loaiPhong.idLoaiPhong"
                :bordered="true"
                class="hover:shadow-md transition-all border-gray-200"
                size="small"
                content-style="padding: 14px;"
              >
                <div class="flex flex-col gap-3">
                  <!-- Row 1: Name & Price (same row) -->
                  <div class="flex items-start justify-between gap-3">
                    <h4 class="font-bold text-base text-gray-800 line-clamp-1 flex-1" :title="loaiPhong.tenLoaiPhong">
                      {{ loaiPhong.tenLoaiPhong }}
                    </h4>
                    <div class="flex flex-col items-end">
                      <span class="text-lg font-bold text-blue-600 whitespace-nowrap">{{ loaiPhong.giaCaNgay.toLocaleString('vi-VN') }}</span>
                      <span class="text-xs text-gray-500 whitespace-nowrap">VNĐ/đêm</span>
                    </div>
                  </div>

                  <!-- Row 2: All Tags on one horizontal row -->
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="inline-flex items-center gap-1 bg-gray-100 px-2 py-1 rounded text-xs whitespace-nowrap">
                      <nova-icon icon="carbon:hotel" :size="12" />
                      {{ loaiPhong.soGiuongDon }}đ-{{ loaiPhong.soGiuongDOi }}đôi
                    </span>
                    <span class="inline-flex items-center gap-1 bg-gray-100 px-2 py-1 rounded text-xs whitespace-nowrap">
                      <nova-icon icon="carbon:user-multiple" :size="12" />
                      Tối đa {{ loaiPhong.soNguoiToiDa }}
                    </span>
                    <span class="inline-flex items-center gap-1 bg-green-50 text-green-700 px-2 py-1 rounded text-xs font-bold border border-green-100 whitespace-nowrap">
                      <nova-icon icon="carbon:building" :size="12" />
                      Còn {{ loaiPhong.soPhongTrong }}
                    </span>
                  </div>



                  <!-- Row 3: Quantity -->
                  <div class="flex items-center gap-2 pt-2 border-t border-gray-100">
                    <span class="text-sm font-medium text-gray-700">Số lượng:</span>
                    <n-input-number
                      :value="selectedLoaiPhong.get(loaiPhong.idLoaiPhong) || 0"
                      :min="0"
                      :max="loaiPhong.soPhongTrong"
                      button-placement="both"
                      size="small"
                      style="flex: 1; pointer-events: auto;"
                      @update:value="(val) => handleQuantityChange(loaiPhong.idLoaiPhong, val)"
                    />
                    <span class="text-xs text-gray-400">
                      (tối đa {{ loaiPhong.soPhongTrong }})
                    </span>
                  </div>
                </div>
              </n-card>
            </template>

            <div v-else class="flex flex-col items-center justify-center py-12 text-gray-300">
               <nova-icon icon="carbon:search" :size="64" class="mb-4 opacity-40" />
               <span v-if="hasSearched" class="text-lg">Không có phòng trống</span>
               <span v-else class="text-lg">Vui lòng tìm kiếm</span>
            </div>
          </div>
        </div>

        <!-- Cột phải: Form tìm kiếm và thông tin đặt phòng -->
        <div class="flex flex-col h-full pr-2 gap-6">
          <!-- Form tìm kiếm -->
          <n-card size="small" title="Tìm kiếm phòng" :bordered="true" class="compact-card shadow-sm">
            <n-form label-placement="top" :model="formData">
              <n-form-item label="Ngày nhận - Ngày trả" path="ngayNhan" required>
                <n-date-picker
                  v-model:value="formData.ngayNhan"
                  type="datetimerange"
                  start-placeholder="Ngày nhận"
                  end-placeholder="Ngày trả"
                  clearable
                  size="medium"
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item label="Số lượng khách" path="soLuongKhach" required>
                <n-input-number
                  v-model:value="formData.soLuongKhach"
                  :min="1"
                  placeholder="Số khách"
                  size="medium"
                  style="width: 100%"
                />
              </n-form-item>

              <n-button type="primary" size="large" block color="#10b981" @click="handleSearch" style="font-weight: bold;">
                <template #icon>
                  <nova-icon icon="carbon:search" />
                </template>
                Tìm phòng trống
              </n-button>
            </n-form>
          </n-card>

          <!-- Thông tin đặt phòng -->
          <n-card
            size="small"
            title="Chi tiết đặt phòng"
            :bordered="true"
            class="compact-card flex-1 shadow-sm"
          >
            <div class="space-y-3 text-sm h-full flex flex-col">
              <div class="space-y-2">
                 <div class="flex justify-between border-b border-gray-100 pb-2">
                  <span class="text-gray-500 font-medium">Nhận:</span>
                  <span class="font-bold text-gray-800">{{ formData.ngayNhan ? formatDate(formData.ngayNhan[0]) : '-- : --' }}</span>
                </div>
                <div class="flex justify-between border-b border-gray-100 pb-2">
                  <span class="text-gray-500 font-medium">Trả:</span>
                  <span class="font-bold text-gray-800">{{ formData.ngayNhan ? formatDate(formData.ngayNhan[1]) : '-- : --' }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-500 font-medium">Lưu trú:</span>
                  <span class="font-bold text-blue-600 text-base">{{ soNgayO }} đêm</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-gray-500 font-medium">Số khách:</span>
                  <span class="font-bold text-gray-800 text-base">{{ formData.soLuongKhach }} người</span>
                </div>
              </div>

              <div class="flex-1 flex items-end">
                 <div class="w-full bg-green-50 p-3 rounded-lg border border-green-200 flex justify-between items-center">
                    <span class="text-gray-700 font-medium">Đã chọn:</span>
                    <span class="font-bold text-lg text-green-700">
                      {{ Array.from(selectedLoaiPhong.values()).reduce((a, b) => a + b, 0) }} phòng
                    </span>
                </div>
              </div>
            </div>
          </n-card>
        </div>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="end" style="width: 100%">
        <n-button size="large" @click="closeModal">
          Hủy
        </n-button>
        <n-button
          type="primary"
          size="large"
          :disabled="selectedLoaiPhong.size === 0"
          @click="handleSubmit"
        >
          <template #icon>
            <nova-icon icon="carbon:arrow-right" />
          </template>
          Tiếp tục đặt phòng {{ selectedLoaiPhong.size > 0 ? `(${Array.from(selectedLoaiPhong.values()).reduce((a, b) => a + b, 0)} phòng)` : '' }}
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
/* FORCE MODAL WIDTH - Override Naive UI default */
.modal-custom-font.compact-modal :deep(.n-modal) {
  width: 90vw !important;
  max-width: 2000px !important;
}

.compact-modal :deep(.n-card) {
  width: 100% !important;
  max-width: 100% !important;
}

/* Khử scrollbar */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* Ép nội dung Modal giãn đều */
.compact-modal :deep(.n-card__content) {
  overflow-y: hidden;
  padding: 24px !important;
}

/* TĂNG KÍCH THƯỚC FONT CHỮ TỔNG THỂ */
.modal-custom-font :deep(.n-form-item-label) {
  font-size: 15px !important;
  font-weight: 700;
  margin-bottom: 8px;
}

.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input-number-input),
.modal-custom-font :deep(.n-button__content) {
  font-size: 15px !important;
}

.modal-custom-font :deep(.n-input),
.modal-custom-font :deep(.n-input-number) {
  min-height: 44px !important;
}

/* Tăng tiêu đề và mô tả phòng */
.modal-custom-font :deep(h4) {
  font-size: 20px !important;
  margin-bottom: 8px;
}

.modal-custom-font :deep(p) {
  font-size: 14px !important;
  line-height: 1.5;
}

/* Tăng kích thước các thẻ Span (Tag thông tin) */
.modal-custom-font :deep(span) {
  font-size: 14px !important;
}

/* Điều chỉnh lại giá tiền */
.text-lg.font-bold.text-blue-600 {
  font-size: 22px !important;
}

/* Card components */
.compact-card :deep(.n-card__content) {
  padding: 12px 16px !important;
}

.compact-card :deep(.n-card-header) {
  padding: 10px 16px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 16px;
  font-weight: 700;
  padding: 12px 16px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 12px;
}

.modal-custom-font :deep(.n-card) {
  padding: 0;
}

.space-y-4 > * + * {
  margin-top: 16px !important;
}

.space-y-3 > * + * {
  margin-top: 12px !important;
}

.grid {
  gap: 16px;
}

.modal-custom-font :deep(.n-date-picker) {
  min-width: 100%;
}

.modal-custom-font :deep(.n-date-picker .n-input__input-el) {
  font-size: 15px;
  padding: 8px 12px;
}

.modal-custom-font :deep(.n-date-picker .n-input) {
  min-height: 44px;
}

.modal-custom-font :deep(.n-button--large) {
  padding: 12px 20px;
  font-size: 16px !important;
  height: 48px;
}

.modal-custom-font :deep(.n-divider) {
  margin: 8px 0;
}

.modal-custom-font :deep(.n-tag) {
  padding: 4px 10px;
  font-size: 14px;
}

.sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}

/* Tăng kích thước nút hành động ở Action bar */
.modal-custom-font :deep(.n-card__action) {
  padding: 20px 32px !important;
}

/* Tăng kích thước icon */
.modal-custom-font :deep(.n-icon) {
  font-size: 18px;
}
</style>

<style>
/* GLOBAL OVERRIDE - Force modal width */
.chon-loai-phong-modal {
  width: 90vw !important;
  max-width: 2000px !important;
}

.modal-custom-font {
  width: 90vw !important;
  max-width: 2000px !important;
}
</style>
