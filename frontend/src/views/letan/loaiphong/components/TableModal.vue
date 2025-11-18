<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { NModal, NForm, NGrid, NInput, NInputNumber, NSelect, NSpace, NButton } from 'naive-ui'
import { addRoomType, updateRoomType } from '@/service/api/letan/loaiphong'

interface RoomTypeForm {
  id?: string | null
  ma: string
  ten: string
  soGiuongDon: number
  soGiuongDoi: number
  soNguoiQuyDinh: number
  soNguoiToiDa: number
  giaCaNgay?: number
  trangThai: 'ACTIVE' | 'INACTIVE'
}

const props = defineProps<{
  visible: boolean
  type: 'add' | 'edit'
  modalData: any | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void
}>()

const modalVisible = computed({ get: () => props.visible, set: v => emit('update:visible', v) })

const defaultForm: RoomTypeForm = {
  id: null,
  ma: '',
  ten: '',
  soGiuongDon: 0,
  soGiuongDoi: 0,
  soNguoiQuyDinh: 1,
  soNguoiToiDa: 1,
  giaCaNgay: 0,
  trangThai: 'ACTIVE'
}

const formModel = ref<RoomTypeForm>({ ...defaultForm })

const trangThaiOptions = [
  { label: 'Hoạt động', value: 'ACTIVE' },
  { label: 'Ngưng hoạt động', value: 'INACTIVE' }
]

function generateCode() { return `LP-${Math.floor(1000 + Math.random() * 9000)}` }

watch(
  () => props.visible,
  (val) => {
    if (val) {
      if (props.type === 'edit' && props.modalData) {
        formModel.value = {
          id: props.modalData.id ?? null,
          ma: props.modalData.ma ?? '',
          ten: props.modalData.ten ?? '',
          soGiuongDon: props.modalData.soGiuongDon ?? 0,
          soGiuongDoi: props.modalData.soGiuongDoi ?? 0,
          soNguoiQuyDinh: props.modalData.soNguoiQuyDinh ?? 1,
          soNguoiToiDa: props.modalData.soNguoiToiDa ?? 1,
          giaCaNgay: props.modalData.giaCaNgay ?? 0,
          trangThai: (props.modalData.status ?? 'ACTIVE') as 'ACTIVE' | 'INACTIVE'
        }
      } else {
        formModel.value = { ...defaultForm, ma: generateCode() }
      }
    }
  },
  { immediate: true }
)

const title = computed(() => (props.type === 'edit' ? 'Sửa loại phòng' : 'Thêm loại phòng'))

async function handleSubmit() {
  if (!formModel.value.ten?.trim()) { window.$message.warning('Vui lòng nhập tên loại phòng!'); return }
  if (formModel.value.soNguoiQuyDinh <= 0 || formModel.value.soNguoiToiDa <= 0) { window.$message.warning('Số người phải lớn hơn 0!'); return }
  if (formModel.value.soNguoiQuyDinh > formModel.value.soNguoiToiDa) { window.$message.warning('Số người quy định không được lớn hơn số người tối đa!'); return }
  if (formModel.value.giaCaNgay! <= 0) {
    window.$message.warning('Số tiền nhập vào phải lớn hơn 0');
    return
  }

  const payload: any = {
    ma: formModel.value.ma,
    ten: formModel.value.ten,
    soGiuongDon: formModel.value.soGiuongDon,
    soGiuongDoi: formModel.value.soGiuongDoi,
    soNguoiQuyDinh: formModel.value.soNguoiQuyDinh,
    soNguoiToiDa: formModel.value.soNguoiToiDa,
    giaCaNgay: formModel.value.giaCaNgay ?? 0,
    trangThai: formModel.value.trangThai
  }

  try {
    if (props.type === 'edit' && formModel.value.id) {
      await updateRoomType(formModel.value.id, payload)
      window.$message.success('Cập nhật loại phòng thành công!')
    } else {
      await addRoomType(payload)
      window.$message.success('Thêm loại phòng thành công!')
    }
    emit('refresh')
    emit('update:visible', false)
  } catch (error: any) {
    window.$message.error(error.message || 'Đã có lỗi khi lưu loại phòng')
  }
}

function handleClose() { emit('update:visible', false) }
</script>

<template>
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card" :title="title" class="w-700px"
    :segmented="{ content: true, action: true }">
    <n-form label-placement="left" :model="formModel" label-align="left" :label-width="150">
      <n-grid :cols="24" :x-gap="18">
        <n-form-item-grid-item :span="12" label="Mã loại phòng" path="ma">
          <n-input v-model:value="formModel.ma" placeholder="Mã sẽ tự sinh" disabled />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Tên loại phòng" path="ten">
          <n-input v-model:value="formModel.ten" placeholder="Nhập tên loại phòng" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Số giường đơn" path="soGiuongDon">
          <n-input-number v-model:value="formModel.soGiuongDon" :min="0" placeholder="0" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Số giường đôi" path="soGiuongDoi">
          <n-input-number v-model:value="formModel.soGiuongDoi" :min="0" placeholder="0" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Số người quy định" path="soNguoiQuyDinh">
          <n-input-number v-model:value="formModel.soNguoiQuyDinh" :min="1" placeholder="1" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Số người tối đa" path="soNguoiToiDa">
          <n-input-number v-model:value="formModel.soNguoiToiDa" :min="1" placeholder="1" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Giá cả ngày (VNĐ)" path="giaCaNgay">
          <n-input-number v-model:value="formModel.giaCaNgay" :min="0" placeholder="0" :show-button="false" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Trạng thái" path="trangThai">
          <n-select v-model:value="formModel.trangThai" :options="trangThaiOptions" label-field="label"
            value-field="value" placeholder="Chọn trạng thái" />
        </n-form-item-grid-item>
      </n-grid>
    </n-form>

    <template #action>
      <n-space justify="center">
        <n-button @click="handleClose">Hủy</n-button>
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
