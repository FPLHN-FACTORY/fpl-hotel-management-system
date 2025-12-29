<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChonLoaiPhong, LoaiPhongAvailableResponse } from '@/service/api/letan/lichdatphong/datphong'
import { checkPhongTrong } from '@/service/api/letan/lichdatphong/datphong'

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

    // Tự động chọn phòng đầu tiên với số lượng 1
    if (data.length > 0) {
      selectedLoaiPhong.value.clear()
      selectedLoaiPhong.value.set(data[0].idLoaiPhong, 1)
    }

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
    class="w-1000px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <!-- Form tìm kiếm -->
      <n-card size="small" title="Thông tin tìm kiếm" :bordered="false" class="mb-4">
        <n-form label-placement="left" :model="formData" label-align="left">
          <n-grid :cols="24" :x-gap="18">
            <n-form-item-grid-item :span="14" label="Ngày nhận - Ngày trả" path="ngayNhan" required>
              <n-date-picker
                v-model:value="formData.ngayNhan"
                type="datetimerange"
                start-placeholder="Ngày nhận"
                end-placeholder="Ngày trả"
                clearable
                style="width: 100%"
              />
            </n-form-item-grid-item>

            <n-form-item-grid-item :span="10" label="Số lượng khách" path="soLuongKhach" required>
              <n-input-number
                v-model:value="formData.soLuongKhach"
                :min="1"
                placeholder="Số khách"
                style="width: 100%"
              />
            </n-form-item-grid-item>

            <n-gi :span="24" class="flex justify-end">
              <n-button type="primary" size="large" @click="handleSearch">
                <template #icon>
                  <nova-icon icon="carbon:search" />
                </template>
                Tìm phòng trống
              </n-button>
            </n-gi>
          </n-grid>
        </n-form>
      </n-card>

      <!-- Thông tin đặt phòng -->
      <n-card v-if="hasSearched && formData.ngayNhan" size="small" title="Thông tin đặt phòng" :bordered="false" class="mb-4 bg-blue-50">
        <div class="grid grid-cols-3 gap-4">
          <div>
            <div class="text-sm text-gray-600 mb-1">
              Nhận phòng
            </div>
            <div class="font-semibold">
              {{ formatDate(formData.ngayNhan[0]) }}
            </div>
          </div>
          <div>
            <div class="text-sm text-gray-600 mb-1">
              Trả phòng
            </div>
            <div class="font-semibold">
              {{ formatDate(formData.ngayNhan[1]) }}
            </div>
          </div>
          <div>
            <div class="text-sm text-gray-600 mb-1">
              Tổng cộng
            </div>
            <div class="font-semibold text-blue-600">
              {{ soNgayO }} đêm · {{ formData.soLuongKhach }} khách
            </div>
          </div>
        </div>
      </n-card>

      <n-divider v-if="hasSearched" />

      <!-- Gợi ý phòng đầu tiên -->
      <div v-if="hasSearched && loaiPhongList.length > 0" class="space-y-4">
        <div>
          <h3 class="text-lg font-semibold mb-3">
            Gợi ý phòng cho {{ formData.soLuongKhach }} khách
          </h3>

          <n-card
            v-if="loaiPhongList[0]"
            :bordered="true"
            class="bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-300 shadow-md hover:shadow-lg transition-shadow"
          >
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <n-tag type="success" size="small" round>
                    <template #icon>
                      <nova-icon icon="carbon:star-filled" />
                    </template>
                    Gợi ý hàng đầu
                  </n-tag>
                </div>
                <h4 class="text-xl font-bold mb-2">
                  {{ loaiPhongList[0].tenLoaiPhong }}
                </h4>
                <p class="text-gray-600 mb-3 text-base">
                  {{ loaiPhongList[0].moTa }}
                </p>
                <div class="flex flex-wrap gap-4 text-base">
                  <span class="flex items-center gap-1.5">
                    <nova-icon icon="carbon:hotel" :size="18" />
                    {{ loaiPhongList[0].soGiuongDon }} giường đơn, {{ loaiPhongList[0].soGiuongDOi }} giường đôi
                  </span>
                  <span class="flex items-center gap-1.5">
                    <nova-icon icon="carbon:user-multiple" :size="18" />
                    Tối đa {{ loaiPhongList[0].soNguoiToiDa }} người
                  </span>
                  <span class="flex items-center gap-1.5 text-green-600 font-medium">
                    <nova-icon icon="carbon:checkmark-filled" :size="18" />
                    Còn {{ loaiPhongList[0].soPhongTrong }} phòng
                  </span>
                </div>
              </div>
              <div class="text-right ml-6">
                <div class="text-3xl font-bold text-blue-600 mb-1">
                  {{ loaiPhongList[0].giaCaNgay.toLocaleString('vi-VN') }}
                </div>
                <div class="text-sm text-gray-500">
                  VNĐ / đêm
                </div>
              </div>
            </div>
          </n-card>
        </div>

        <n-divider>
          <span class="text-gray-500">Tất cả lựa chọn</span>
        </n-divider>

        <!-- Danh sách tất cả loại phòng -->
        <div class="space-y-3">
          <div
            v-for="loaiPhong in loaiPhongList"
            :key="loaiPhong.idLoaiPhong"
            class="border rounded-lg p-4 hover:shadow-md transition-shadow bg-white"
          >
            <div class="flex justify-between items-start mb-3">
              <div class="flex-1">
                <h4 class="font-bold text-lg mb-2">
                  {{ loaiPhong.tenLoaiPhong }}
                </h4>
                <p class="text-gray-600 text-base mb-2">
                  {{ loaiPhong.moTa }}
                </p>
                <div class="flex flex-wrap gap-3 text-base text-gray-500">
                  <span class="flex items-center gap-1">
                    <nova-icon icon="carbon:hotel" :size="16" />
                    {{ loaiPhong.soGiuongDon }} đơn · {{ loaiPhong.soGiuongDOi }} đôi
                  </span>
                  <span class="flex items-center gap-1">
                    <nova-icon icon="carbon:user-multiple" :size="16" />
                    Tối đa {{ loaiPhong.soNguoiToiDa }} người
                  </span>
                  <span class="flex items-center gap-1">
                    <nova-icon icon="carbon:building" :size="16" />
                    Còn {{ loaiPhong.soPhongTrong }} phòng
                  </span>
                </div>
              </div>
              <div class="text-right ml-4">
                <div class="text-2xl font-bold text-gray-800 mb-1">
                  {{ loaiPhong.giaCaNgay.toLocaleString('vi-VN') }}
                </div>
                <div class="text-sm text-gray-500">
                  VNĐ / đêm
                </div>
              </div>
            </div>

            <div class="flex items-center gap-3 pt-3 border-t">
              <span class="font-semibold">Số lượng phòng:</span>
              <n-input-number
                :value="selectedLoaiPhong.get(loaiPhong.idLoaiPhong) || 0"
                :min="0"
                :max="loaiPhong.soPhongTrong"
                style="width: 120px"
                @update:value="(val) => handleQuantityChange(loaiPhong.idLoaiPhong, val)"
              />
              <span class="text-sm text-gray-500">
                (tối đa {{ loaiPhong.soPhongTrong }})
              </span>
            </div>
          </div>
        </div>
      </div>

      <n-empty
        v-if="hasSearched && loaiPhongList.length === 0"
        description="Không có phòng trống trong khoảng thời gian này"
        class="my-8"
      />
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-text v-if="selectedLoaiPhong.size > 0" class="text-base">
          Đã chọn: <strong>{{ Array.from(selectedLoaiPhong.values()).reduce((a, b) => a + b, 0) }} phòng</strong>
        </n-text>
        <div v-else />
        <n-space>
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
            Tiếp tục đặt phòng
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1000px {
  width: 1000px;
  max-width: 95vw;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
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
  font-size: 17px;
}
</style>
