<script setup lang="ts">
import { computed, defineEmits, defineProps, ref, watch } from 'vue'
import { createDichVu, getDichVuById, updateDichVu } from '@/service/api/letan/dichvu'
import type { DichVuRequest } from '@/service/api/letan/dichvu'

interface Props {
  visible: boolean
  type?: 'add' | 'edit'
  modalData?: { id: string } | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void
}>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

function closeModal() {
  modalVisible.value = false
}

const title = computed(() => (props.type === 'edit' ? 'Sửa dịch vụ' : 'Thêm dịch vụ'))

const defaultDichVu: DichVuRequest = {
  maDichVu: '',
  tenDichVu: '',
  donViTinh: '',
  donGia: 0,
  moTa: '',
  trangThai: 0,
}

const formModel = ref<DichVuRequest>({ ...defaultDichVu })
const isLoading = ref(false)

const trangThaiOptions = ref([
  { label: 'Hoạt động', value: 0 },
  { label: 'Ngưng hoạt động', value: 1 },
])

const donViTinhOptions = [
  { label: 'Chai', value: 'Chai' },
  { label: 'Lon', value: 'Lon' },
  { label: 'Kg', value: 'Kg' },
  { label: 'Suất', value: 'Suất' },
  { label: 'Giờ', value: 'Giờ' },
  { label: 'Lần', value: 'Lần' },
  { label: 'Bộ', value: 'Bộ' },
  { label: 'Cái', value: 'Cái' },
]

async function fetchDichVuDetail(id: string) {
  try {
    isLoading.value = true
    const res = await getDichVuById(id)
    const data = res.data

    formModel.value = {
      id: data.id,
      maDichVu: data.maDichVu,
      tenDichVu: data.tenDichVu,
      donViTinh: data.donViTinh,
      donGia: data.donGia,
      moTa: data.moTa || '',
      trangThai: data.trangThai,
    }
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải thông tin dịch vụ')
  }
  finally {
    isLoading.value = false
  }
}

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.type === 'edit' && props.modalData?.id) {
        fetchDichVuDetail(props.modalData.id)
      }
      else {
        // Auto-generate random service code for new services
        const randomCode = `DV${Math.floor(100000 + Math.random() * 900000)}`
        formModel.value = { ...defaultDichVu, maDichVu: randomCode }
      }
    }
  },
)

function validateForm() {
  if (!formModel.value.maDichVu?.trim()) {
    window.$message.warning('Vui lòng nhập mã dịch vụ!')
    return false
  }
  if (!formModel.value.tenDichVu?.trim()) {
    window.$message.warning('Vui lòng nhập tên dịch vụ!')
    return false
  }
  if (!formModel.value.donViTinh?.trim()) {
    window.$message.warning('Vui lòng nhập đơn vị tính!')
    return false
  }
  if (!formModel.value.donGia || formModel.value.donGia <= 0) {
    window.$message.warning('Đơn giá phải lớn hơn 0!')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validateForm())
    return

  try {
    const payload: DichVuRequest = {
      maDichVu: formModel.value.maDichVu.trim(),
      tenDichVu: formModel.value.tenDichVu.trim(),
      donViTinh: formModel.value.donViTinh.trim(),
      donGia: formModel.value.donGia,
      moTa: formModel.value.moTa?.trim() || '',
      trangThai: formModel.value.trangThai,
    }

    if (props.type === 'edit' && formModel.value.id) {
      const res = await updateDichVu(formModel.value.id, payload)
      window.$message.success(res?.message || 'Cập nhật dịch vụ thành công!')
    }
    else {
      const res = await createDichVu(payload)
      window.$message.success(res?.message || 'Thêm dịch vụ thành công!')
    }

    emit('refresh')
    closeModal()
    formModel.value = { ...defaultDichVu }
  }
  catch (error: any) {
    window.$message.error(error.message || (props.type === 'edit' ? 'Không thể cập nhật dịch vụ' : 'Không thể thêm dịch vụ'))
  }
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="w-700px modal-custom-font"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <n-form label-placement="left" :model="formModel" label-align="left" :label-width="150">
        <n-grid :cols="24" :x-gap="18">
          <n-form-item-grid-item :span="12" label="Mã dịch vụ" path="maDichVu">
            <n-input
              v-model:value="formModel.maDichVu"
              placeholder="Mã dịch vụ tự động tạo"
              disabled
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Tên dịch vụ" path="tenDichVu">
            <n-input
              v-model:value="formModel.tenDichVu"
              placeholder="Nhập tên dịch vụ"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Đơn vị tính" path="donViTinh">
            <n-select
              v-model:value="formModel.donViTinh"
              :options="donViTinhOptions"
              label-field="label"
              value-field="value"
              placeholder="Chọn đơn vị tính"
              tag
              filterable
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Đơn giá (VNĐ)" path="donGia">
            <n-input-number
              v-model:value="formModel.donGia"
              :min="0"
              :step="1000"
              placeholder="Nhập đơn giá"
              style="width: 100%"
              :format="(value: number) => value.toLocaleString('vi-VN')"
              :parse="(input: string) => Number(input.replace(/[^\d]/g, ''))"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Trạng thái" path="trangThai">
            <n-select
              v-model:value="formModel.trangThai"
              :options="trangThaiOptions"
              label-field="label"
              value-field="value"
              placeholder="Chọn trạng thái"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="24" label="Mô tả" path="moTa">
            <n-input
              v-model:value="formModel.moTa"
              type="textarea"
              placeholder="Nhập mô tả (không bắt buộc)"
              :rows="3"
            />
          </n-form-item-grid-item>
        </n-grid>
      </n-form>
    </n-spin>

    <template #action>
      <n-space justify="center">
        <n-button @click="closeModal">
          Hủy
        </n-button>
        <n-button type="primary" @click="handleSubmit">
          Lưu
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-700px {
  width: 700px;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
}

.modal-custom-font :deep(.n-form-item-label) {
  font-size: 17px;
}

.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-base-selection-label),
.modal-custom-font :deep(.n-base-selection-input),
.modal-custom-font :deep(.n-input-number-input),
.modal-custom-font :deep(.n-button__content) {
  font-size: 17px;
}

.modal-custom-font :deep(.n-base-select-option__content) {
  font-size: 17px;
}
</style>
