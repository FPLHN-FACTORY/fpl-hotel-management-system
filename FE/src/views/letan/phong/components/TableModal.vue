<script setup lang="ts">
import { ref, computed, watch, defineProps, defineEmits } from 'vue'
import type { FormInst } from 'naive-ui'

interface Room {
  maPhong: string
  tenPhong: string
  tang?: number
  loaiPhong?: string
  gia?: number
  sucChua?: number
  trangThai: string
}

interface Props {
  visible: boolean
  type?: 'add' | 'edit'
  modalData?: Room | null
}

const props = defineProps<Props>()
const emit = defineEmits<{ (e: 'update:visible', visible: boolean): void }>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

function closeModal() {
  modalVisible.value = false
}

// --- Modal title ---
const title = computed(() => (props.type === 'edit' ? 'Sửa phòng' : 'Thêm phòng'))

// --- Form model ---
const defaultRoom: Room = {
  maPhong: '',
  tenPhong: '',
  tang: undefined,
  loaiPhong: undefined,
  gia: undefined,
  sucChua: undefined,
  trangThai: 'Trống',
}
const formModel = ref<Room>({ ...defaultRoom })

// --- Options ---
const loaiPhongOptions = [
  { label: 'Phòng 3 giường', value: 'Phòng 3 giường' },
  { label: 'Phòng 2 giường đơn', value: 'Phòng 2 giường đơn' },
  { label: 'Phòng 1 giường đôi 1 giường đơn', value: 'Phòng 1 giường đôi 1 giường đơn' },
]

const trangThaiOptions = [
  { label: 'Trống', value: 'Trống' },
  { label: 'Đã đặt', value: 'Đã đặt' },
  { label: 'Đang sử dụng', value: 'Đang sử dụng' },
]

// --- Update form when modal opens ---
watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.type === 'edit' && props.modalData) {
        formModel.value = { ...props.modalData }
      } else {
        formModel.value = { ...defaultRoom }
      }
    }
  }
)

// --- Submit form ---
function handleSubmit() {
  if (props.type === 'edit') {
    window.$message.success(`Đã cập nhật phòng: ${formModel.value.tenPhong}`)
  } else {
    window.$message.success(`Đã thêm phòng: ${formModel.value.tenPhong}`)
  }
  closeModal()
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="w-700px"
    :segmented="{ content: true, action: true }"
  >
    <n-form label-placement="left" :model="formModel" label-align="left" :label-width="120">
      <n-grid :cols="24" :x-gap="18">
        <n-form-item-grid-item :span="12" label="Mã phòng" path="maPhong">
          <n-input v-model:value="formModel.maPhong" placeholder="Nhập mã phòng" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Tên phòng" path="tenPhong">
          <n-input v-model:value="formModel.tenPhong" placeholder="Nhập tên phòng" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Tầng" path="tang">
          <n-input-number v-model:value="formModel.tang" :min="1" placeholder="Nhập tầng" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Loại phòng" path="loaiPhong">
          <n-select
            v-model:value="formModel.loaiPhong"
            :options="loaiPhongOptions"
            label-field="label"
            value-field="value"
            placeholder="Chọn loại phòng"
          />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Giá (VNĐ)" path="gia">
          <n-input-number v-model:value="formModel.gia" :min="0" placeholder="Nhập giá" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Sức chứa" path="sucChua">
          <n-input-number v-model:value="formModel.sucChua" :min="1" placeholder="Nhập sức chứa" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Trạng thái" path="trangThai">
          <n-select v-model:value="formModel.trangThai" :options="trangThaiOptions" placeholder="Chọn trạng thái" />
        </n-form-item-grid-item>
      </n-grid>
    </n-form>

    <template #action>
      <n-space justify="center">
        <n-button @click="closeModal">Hủy</n-button>
        <n-button type="primary" @click="handleSubmit">Lưu</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-700px {
  width: 700px;
}
</style>
