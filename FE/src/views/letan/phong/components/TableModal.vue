<script setup lang="ts">
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { addPhong, getAllLoaiPhong } from '@/service/api/letan/phong'

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
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void // 👈 emit để load lại danh sách sau khi thêm
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
  trangThai: 'Trống',
}

const formModel = ref<Room>({ ...defaultRoom })

// --- Loại phòng ---
const loaiPhongOptions = ref<{ label: string, value: string, soLuongNguoiToiDa: number, giaHienTai: number }[]>([])
const isSucChuaLocked = ref(false)
const isGiaLocked = ref(false)

const trangThaiOptions = ref([
  { label: 'Trống', value: 'Trống' },
  { label: 'Đã đặt', value: 'Đã đặt' },
  { label: 'Đang sử dụng', value: 'Đang sử dụng' },
  { label: 'Đang dọn', value: 'Đang dọn' },
  { label: 'Bảo trì', value: 'Bảo trì' },
  { label: 'Tạm khóa', value: 'Tạm khóa' },
])

function mapTrangThaiPhong(trangThai: string): string {
  switch (trangThai) {
    case 'Trống': return 'TRONG'
    case 'Đã đặt': return 'DA_DAT'
    case 'Đang sử dụng': return 'DANG_SU_DUNG'
    case 'Đang dọn': return 'DANG_DON'
    case 'Bảo trì': return 'BAO_TRI'
    case 'Tạm khóa': return 'TAM_KHOA'
    default: return 'TRONG'
  }
}

function reverseMapTrangThaiPhong(enumValue: string): string {
  switch (enumValue) {
    case 'TRONG': return 'Trống'
    case 'DA_DAT': return 'Đã đặt'
    case 'DANG_SU_DUNG': return 'Đang sử dụng'
    case 'DANG_DON': return 'Đang dọn'
    case 'BAO_TRI': return 'Bảo trì'
    case 'TAM_KHOA': return 'Tạm khóa'
    default: return 'Trống'
  }
}

async function fetchLoaiPhong() {
  try {
    const data = await getAllLoaiPhong()
    loaiPhongOptions.value = data.map(lp => ({
      label: lp.ten,
      value: String(lp.id),
      soLuongNguoiToiDa: lp.soLuongNguoiToiDa || 0,
      giaHienTai: lp.giaHienTai || 0,
    }))
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}

watch(
  () => formModel.value.loaiPhong,
  (newLoaiPhong) => {
    if (!newLoaiPhong) {
      formModel.value.sucChua = undefined
      formModel.value.gia = undefined
      isSucChuaLocked.value = false
      isGiaLocked.value = false
      return
    }
    const selected = loaiPhongOptions.value.find(lp => lp.value === newLoaiPhong)
    if (selected) {
      formModel.value.sucChua = selected.soLuongNguoiToiDa
      formModel.value.gia = selected.giaHienTai
      isSucChuaLocked.value = true
      isGiaLocked.value = true
    }
  },
)

watch(
  () => props.visible,
  (val) => {
    if (val) {
      fetchLoaiPhong()
      if (props.type === 'edit' && props.modalData) {
        formModel.value = {
          ...props.modalData,
          trangThai: reverseMapTrangThaiPhong(props.modalData.trangThai),
        }
        isSucChuaLocked.value = true
        isGiaLocked.value = true
      }
      else {
        formModel.value = { ...defaultRoom }
        isSucChuaLocked.value = false
        isGiaLocked.value = false
      }
    }
  },
)

async function handleSubmit() {
  try {
    // ⚠️ Kiểm tra dữ liệu trước khi gửi
    if (!formModel.value.maPhong?.trim() || !formModel.value.tenPhong?.trim()) {
      window.$message.warning('Vui lòng nhập đầy đủ mã và tên phòng!')
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
      trangThaiPhong: mapTrangThaiPhong(formModel.value.trangThai),
    }

    const res = await addPhong(payload)

    if (res?.message?.includes('Mã phòng này đã tồn tại')) {
      window.$message.warning('Mã phòng này đã tồn tại!')
      return
    }
    if (res?.message?.includes('Tên phòng này đã tồn tại')) {
      window.$message.warning('Tên phòng này đã tồn tại!')
      return
    }

    window.$message.success(res?.message || 'Thêm phòng thành công!')
    emit('refresh') // reload danh sách phòng
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

        <n-form-item-grid-item :span="12" label="Trạng thái" path="trangThai">
          <n-select
            v-model:value="formModel.trangThai"
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
</style>
