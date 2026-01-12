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
    ([idLoaiPhong, soLuong]) => ({ idLoaiPhong, soLuong }),
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
    class="w-1300px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="grid grid-cols-12 gap-6">
        <!-- Cột trái: Danh sách loại phòng -->
        <div class="col-span-7">
          <div v-if="hasSearched && loaiPhongList.length > 0" class="space-y-3">
            <n-card
              v-for="loaiPhong in loaiPhongList"
              :key="loaiPhong.idLoaiPhong"
              :bordered="true"
              class="hover:shadow-md transition-shadow"
              size="small"
            >
              <div class="space-y-2">
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h4 class="font-bold text-base mb-1">
                      {{ loaiPhong.tenLoaiPhong }}
                    </h4>
                    <p class="text-gray-600 text-sm">
                      {{ loaiPhong.moTa }}
                    </p>
                  </div>
                  <div class="text-right ml-4">
                    <div class="text-xl font-bold text-gray-800">
                      {{ loaiPhong.giaCaNgay.toLocaleString('vi-VN') }}
                    </div>
                    <div class="text-xs text-gray-500">
                      VNĐ / đêm
                    </div>
                  </div>
                </div>

                <div class="flex flex-wrap gap-2 text-xs text-gray-600">
                  <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                    <nova-icon icon="carbon:hotel" :size="14" />
                    {{ loaiPhong.soGiuongDon }} đơn · {{ loaiPhong.soGiuongDOi }} đôi
                  </span>
                  <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                    <nova-icon icon="carbon:user-multiple" :size="14" />
                    Tối đa {{ loaiPhong.soNguoiToiDa }} người
                  </span>
                  <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                    <nova-icon icon="carbon:building" :size="14" />
                    Còn {{ loaiPhong.soPhongTrong }} phòng
                  </span>
                </div>

                <div class="flex items-center gap-2 pt-2 border-t">
                  <span class="font-semibold text-sm">Số lượng:</span>
                  <n-input-number
                    :value="selectedLoaiPhong.get(loaiPhong.idLoaiPhong) || 0"
                    :min="0"
                    :max="loaiPhong.soPhongTrong"
                    style="width: 110px"
                    size="small"
                    @update:value="(val) => handleQuantityChange(loaiPhong.idLoaiPhong, val)"
                  />
                  <span class="text-xs text-gray-500">
                    (tối đa {{ loaiPhong.soPhongTrong }})
                  </span>
                </div>
              </div>
            </n-card>
          </div>

          <n-empty
            v-if="hasSearched && loaiPhongList.length === 0"
            description="Không có phòng trống trong khoảng thời gian này"
            class="my-8"
          />

          <div v-if="!hasSearched" class="flex items-center justify-center h-64">
            <n-empty description="Vui lòng chọn ngày và số khách để tìm phòng">
              <template #icon>
                <nova-icon icon="carbon:search" :size="48" class="text-gray-400" />
              </template>
            </n-empty>
          </div>
        </div>

        <!-- Cột phải: Form tìm kiếm và thông tin đặt phòng -->
        <div class="col-span-5 space-y-4">
          <!-- Form tìm kiếm -->
          <n-card size="small" title="Tìm kiếm phòng" :bordered="true" class="sticky top-0">
            <n-form label-placement="top" :model="formData">
              <n-form-item label="Ngày nhận - Ngày trả" path="ngayNhan" required>
                <n-date-picker
                  v-model:value="formData.ngayNhan"
                  type="datetimerange"
                  start-placeholder="Ngày nhận"
                  end-placeholder="Ngày trả"
                  clearable
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item label="Số lượng khách" path="soLuongKhach" required>
                <n-input-number
                  v-model:value="formData.soLuongKhach"
                  :min="1"
                  placeholder="Số khách"
                  style="width: 100%"
                />
              </n-form-item>

              <n-button type="primary" size="large" block @click="handleSearch">
                <template #icon>
                  <nova-icon icon="carbon:search" />
                </template>
                Tìm phòng trống
              </n-button>
            </n-form>
          </n-card>

          <!-- Thông tin đặt phòng -->
          <n-card
            v-if="hasSearched && formData.ngayNhan"
            size="small"
            title="Chi tiết đặt phòng"
            :bordered="true"
            class="bg-blue-50"
          >
            <div class="space-y-2">
              <div>
                <div class="text-xs text-gray-600 mb-1">
                  Nhận phòng
                </div>
                <div class="font-semibold text-sm">
                  {{ formatDate(formData.ngayNhan[0]) }}
                </div>
              </div>
              <n-divider class="!my-1" />
              <div>
                <div class="text-xs text-gray-600 mb-1">
                  Trả phòng
                </div>
                <div class="font-semibold text-sm">
                  {{ formatDate(formData.ngayNhan[1]) }}
                </div>
              </div>
              <n-divider class="!my-1" />
              <div>
                <div class="text-xs text-gray-600 mb-1">
                  Thời gian lưu trú
                </div>
                <div class="font-semibold text-base text-blue-600">
                  {{ soNgayO }} đêm
                </div>
              </div>
              <n-divider class="!my-1" />
              <div>
                <div class="text-xs text-gray-600 mb-1">
                  Số khách
                </div>
                <div class="font-semibold text-sm">
                  {{ formData.soLuongKhach }} người
                </div>
              </div>
              <n-divider class="!my-1" />
              <div v-if="selectedLoaiPhong.size > 0" class="bg-white p-2 rounded-lg">
                <div class="text-xs text-gray-600 mb-1">
                  Đã chọn
                </div>
                <div class="font-bold text-lg text-green-600">
                  {{ Array.from(selectedLoaiPhong.values()).reduce((a, b) => a + b, 0) }} phòng
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
          Tiếp tục đặt phòng ({{ Array.from(selectedLoaiPhong.values()).reduce((a, b) => a + b, 0) }} phòng)
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1300px {
  width: 1300px;
  max-width: 95vw;
  max-height: 85vh;
}

.w-1300px :deep(.n-card__content) {
  max-height: calc(85vh - 140px);
  overflow-y: auto;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 17px;
  font-weight: 600;
  padding: 12px 20px;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input-number-input),
.modal-custom-font :deep(.n-button__content),
.modal-custom-font :deep(.n-divider__title),
.modal-custom-font :deep(h3),
.modal-custom-font :deep(h4),
.modal-custom-font :deep(p),
.modal-custom-font :deep(span),
.modal-custom-font :deep(.n-text) {
  font-size: 14px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 12px;
}

.modal-custom-font :deep(.n-card) {
  padding: 12px;
}

.modal-custom-font :deep(.n-card-header) {
  padding: 10px 16px;
}

.space-y-4 > * + * {
  margin-top: 12px !important;
}

.space-y-3 > * + * {
  margin-top: 10px !important;
}

.grid {
  gap: 16px;
}

.modal-custom-font :deep(.n-date-picker) {
  min-width: 100%;
}

.modal-custom-font :deep(.n-date-picker .n-input__input-el) {
  font-size: 13px;
  padding: 6px 10px;
}

.modal-custom-font :deep(.n-date-picker .n-input) {
  min-height: 36px;
}

.modal-custom-font :deep(.n-button--large) {
  padding: 8px 16px;
  font-size: 14px;
}

.modal-custom-font :deep(.n-divider) {
  margin: 8px 0;
}

.modal-custom-font :deep(.n-tag) {
  padding: 2px 8px;
  font-size: 13px;
}

.sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}

.modal-custom-font :deep(.n-card__action) {
  padding: 12px 20px;
}
</style>
