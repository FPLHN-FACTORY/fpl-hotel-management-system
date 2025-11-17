<script setup lang="ts">
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { addPhong, getRoomTypes } from '@/service/api/letan/phong'

interface Room {
  maPhong: string
  tenPhong: string
  tang?: number
  loaiPhong?: string
  gia?: number
  sucChua?: number
  trangThaiHoatDong: string
}

interface Props {
  visible: boolean
  type?: 'add' | 'edit'
  modalData?: Room | null
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

const title = computed(() => (props.type === 'edit' ? 'Sửa phòng' : 'Thêm phòng'))

const defaultRoom: Room = {
  maPhong: '',
  tenPhong: '',
  tang: undefined,
  loaiPhong: undefined,
  gia: undefined,
  sucChua: undefined,
  trangThaiHoatDong: 'Hoạt động',
}

const formModel = ref<Room>({ ...defaultRoom })

// --- Loại phòng ---
const loaiPhongOptions = ref<{ label: string, value: string, soLuongNguoiToiDa: number, giaCaNgay: number }[]>([])
const isSucChuaLocked = ref(false)
const isGiaLocked = ref(false)

// --- Trạng thái phòng ---
const trangThaiOptions = ref([
  { label: 'Hoạt động', value: 'HOAT_DONG' },
  { label: 'Bảo trì', value: 'BAO_TRI' },
  { label: 'Ngưng hoạt động', value: 'NGUNG_HOAT_DONG' },
])

function mapTrangThaiPhong(trangThai: string): string {
  switch (trangThai) {
    case 'Hoạt động': return 'HOAT_DONG'
    case 'Bảo trì': return 'BAO_TRI'
    case 'Ngưng hoạt động': return 'NGUNG_HOAT_DONG'
    default: return 'HOAT_DONG'
  }
}

function reverseMapTrangThaiPhong(enumValue: string): string {
  switch (enumValue) {
    case 'HOAT_DONG': return 'Hoạt động'
    case 'BAO_TRI': return 'Bảo trì'
    case 'NGUNG_HOAT_DONG': return 'Ngưng hoạt động'
    default: return 'Hoạt động'
  }
}

// --- Fetch loại phòng ---
async function fetchLoaiPhong() {
  try {
    const data = await getRoomTypes()
    loaiPhongOptions.value = data.map(lp => ({
      label: lp.ten,
      value: String(lp.id),
      soLuongNguoiToiDa: lp.soLuongNguoiToiDa ?? 1,
      giaCaNgay: lp.giaCaNgay ?? 0
    }))
  } catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}

// --- Watch loại phòng để tự set giá và sức chứa ---
watch(
  () => formModel.value.loaiPhong,
  (newLoaiPhong) => {
    if (!newLoaiPhong) {
      formModel.value.gia = undefined
      formModel.value.sucChua = undefined
      isGiaLocked.value = false
      isSucChuaLocked.value = false
      return
    }
    const selected = loaiPhongOptions.value.find(lp => lp.value === newLoaiPhong)
    if (selected) {
      formModel.value.gia = selected.giaCaNgay
      formModel.value.sucChua = selected.soLuongNguoiToiDa
      isGiaLocked.value = true
      isSucChuaLocked.value = true
    }
  },
)

// --- Watch khi modal mở ---
watch(
  () => props.visible,
  (val) => {
    if (val) {
      fetchLoaiPhong()
      if (props.type === 'edit' && props.modalData) {
        formModel.value = {
          ...props.modalData,
          trangThaiHoatDong: reverseMapTrangThaiPhong(props.modalData.trangThaiHoatDong),
        }
        isGiaLocked.value = true
        isSucChuaLocked.value = true
      } else {
        formModel.value = { ...defaultRoom }
        isGiaLocked.value = false
        isSucChuaLocked.value = false
      }
    }
  },
)

async function handleSubmit() {
  try {
    if ( !formModel.value.tenPhong?.trim()) {
      window.$message.warning('Vui lòng nhập đầy đủ tên phòng!')
      return
    }
    if (!formModel.value.loaiPhong) {
      window.$message.warning('Vui lòng chọn loại phòng!')
      return
    }
    if (!formModel.value.sucChua || formModel.value.sucChua <= 0) {
      window.$message.warning('Sức chứa phải lớn hơn 0!')
      return
    }
    if (!formModel.value.tang || formModel.value.tang <= 0) {
      window.$message.warning('Tầng phải lớn hơn 0!')
      return
    }

    const payload = {
      ma: formModel.value.maPhong.trim(),
      ten: formModel.value.tenPhong.trim(),
      idLoaiPhong: formModel.value.loaiPhong!,
      sucChua: formModel.value.sucChua!,
      tang: formModel.value.tang!,
      trangThaiPhongHoatDong: mapTrangThaiPhong(formModel.value.trangThaiHoatDong),
    }

    const res = await addPhong(payload)

    if (res?.message?.includes('Tên phòng này đã tồn tại')) {
      window.$message.warning('Tên phòng này đã tồn tại!')
      return
    }

    window.$message.success(res?.message || 'Thêm phòng thành công!')
    emit('refresh')
    closeModal()
    formModel.value = { ...defaultRoom }
  } catch (error: any) {
    window.$message.error(error.message || 'Không thể thêm phòng')
  }
}

onMounted(fetchLoaiPhong)
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
          <n-input v-model:value="formModel.maPhong" placeholder="Mã phòng theo tên phòng" disabled />
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
          <n-input-number
            v-model:value="formModel.gia"
            :min="0"
            placeholder="Tự động từ loại phòng"
            :disabled="isGiaLocked"
          />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Sức chứa" path="sucChua">
          <n-input-number
            v-model:value="formModel.sucChua"
            :min="1"
            placeholder="Tự động từ loại phòng"
            :disabled="isSucChuaLocked"
          />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Trạng thái hoạt động" path="trangThaiHoatDong">
          <n-select
            v-model:value="formModel.trangThaiHoatDong"
            :options="trangThaiOptions"
            label-field="label"
            value-field="value"
            placeholder="Chọn trạng thái"
          />
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
