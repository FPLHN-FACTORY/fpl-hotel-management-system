<script setup lang="ts">
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { addPhong, getAllTags, getPhongById, getRoomTypes, updatePhong } from '@/service/api/letan/phong'

interface Room {
  id?: string
  maPhong: string
  tenPhong: string
  tang?: number
  loaiPhong?: string
  gia?: number
  sucChua?: number
  soNguoiQuyDinh?: number
  soGiuongDon?: number
  soGiuongDoi?: number
  trangThaiHoatDong: string
  tags?: string[]
}

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

const title = computed(() => (props.type === 'edit' ? 'Sửa phòng' : 'Thêm phòng'))

const defaultRoom: Room = {
  maPhong: '',
  tenPhong: '',
  tang: undefined,
  loaiPhong: undefined,
  gia: undefined,
  sucChua: undefined,
  soNguoiQuyDinh: undefined,
  soGiuongDon: undefined,
  soGiuongDoi: undefined,
  trangThaiHoatDong: 'DANG_HOAT_DONG',
  tags: [],
}

const formModel = ref<Room>({ ...defaultRoom })
const isLoading = ref(false)

const loaiPhongOptions = ref<{ label: string; value: string; soLuongNguoiToiDa: number; soNguoiQuyDinh: number; soGiuongDon: number; soGiuongDoi: number; giaCaNgay: number }[]>([])
const isSucChuaLocked = ref(false)
const isGiaLocked = ref(false)
const isBedInfoLocked = ref(false)
const isSoNguoiQuyDinhLocked = ref(false)

const tagOptions = ref<{ label: string; value: string }[]>([])

const trangThaiOptions = ref([
  { label: 'Hoạt động', value: 'DANG_HOAT_DONG' },
  { label: 'Bảo trì', value: 'DANG_SUA' },
  { label: 'Ngưng hoạt động', value: 'NGUNG_HOAT_DONG' },
])

// Computed để disable tên phòng khi edit
const isTenPhongDisabled = computed(() => props.type === 'edit')

async function fetchLoaiPhong() {
  try {
    const data = await getRoomTypes()
    loaiPhongOptions.value = data.map(lp => ({
      label: lp.ten,
      value: String(lp.id),
      soLuongNguoiToiDa: lp.soNguoiToiDa ?? 1,
      soNguoiQuyDinh: lp.soNguoiQuyDinh ?? 1,
      soGiuongDon: lp.soGiuongDon ?? 0,
      soGiuongDoi: lp.soGiuongDoi ?? 0,
      giaCaNgay: lp.giaCaNgay ?? 0,
    }))
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải loại phòng')
  }
}

async function fetchTags() {
  try {
    const data = await getAllTags()
    tagOptions.value = data.map(tag => ({
      label: tag.ma,
      value: tag.id,
    }))
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải danh sách tags')
  }
}

async function fetchPhongDetail(id: string) {
  try {
    isLoading.value = true
    const data = await getPhongById(id)

    formModel.value = {
      id: data.phong.id,
      maPhong: data.phong.ma,
      tenPhong: data.phong.ten,
      tang: data.phong.tang,
      loaiPhong: data.phong.idLoaiPhong,
      gia: data.phong.giaCaNgay,
      sucChua: data.phong.soNguoiToiDa,
      soNguoiQuyDinh: data.phong.soNguoiQuyDinh,
      soGiuongDon: data.phong.soGiuongDon,
      soGiuongDoi: data.phong.soGiuongDoi,
      trangThaiHoatDong: data.phong.trangThaiHoatDong,
      tags: data.tagIds || [],
    }

    isGiaLocked.value = true
    isSucChuaLocked.value = true
    isBedInfoLocked.value = true
    isSoNguoiQuyDinhLocked.value = true
  }
  catch (err: any) {
    window.$message.error(err.message || 'Không thể tải thông tin phòng')
  }
  finally {
    isLoading.value = false
  }
}

watch(
  () => formModel.value.loaiPhong,
  (newLoaiPhong) => {
    if (!newLoaiPhong) {
      formModel.value.gia = undefined
      formModel.value.sucChua = undefined
      formModel.value.soNguoiQuyDinh = undefined
      formModel.value.soGiuongDon = undefined
      formModel.value.soGiuongDoi = undefined
      isGiaLocked.value = false
      isSucChuaLocked.value = false
      isBedInfoLocked.value = false
      isSoNguoiQuyDinhLocked.value = false
      return
    }

    const selected = loaiPhongOptions.value.find(lp => lp.value === newLoaiPhong)

    if (selected) {
      if (props.type === 'add') {
        formModel.value.gia = selected.giaCaNgay
        formModel.value.sucChua = selected.soLuongNguoiToiDa
        formModel.value.soNguoiQuyDinh = selected.soNguoiQuyDinh
        formModel.value.soGiuongDon = selected.soGiuongDon
        formModel.value.soGiuongDoi = selected.soGiuongDoi

        isGiaLocked.value = true
        isSucChuaLocked.value = true
        isBedInfoLocked.value = true
        isSoNguoiQuyDinhLocked.value = true
      }
    }
  },
)

watch(
  () => formModel.value.tenPhong,
  (newTen) => {
    formModel.value.maPhong = newTen
  },
)

watch(
  () => props.visible,
  (val) => {
    if (val) {
      fetchLoaiPhong()
      fetchTags()
      if (props.type === 'edit' && props.modalData?.id) {
        fetchPhongDetail(props.modalData.id)
      }
      else {
        formModel.value = { ...defaultRoom }
        isGiaLocked.value = false
        isSucChuaLocked.value = false
        isBedInfoLocked.value = false
        isSoNguoiQuyDinhLocked.value = false
      }
    }
  },
)

function validateForm() {
  if (!formModel.value.tenPhong?.trim()) {
    window.$message.warning('Vui lòng nhập đầy đủ tên phòng!')
    return false
  }
  if (!formModel.value.loaiPhong) {
    window.$message.warning('Vui lòng chọn loại phòng!')
    return false
  }
  if (!formModel.value.sucChua || formModel.value.sucChua <= 0) {
    window.$message.warning('Sức chứa phải lớn hơn 0!')
    return false
  }
  if (!formModel.value.tang || formModel.value.tang <= 0) {
    window.$message.warning('Tầng phải lớn hơn 0!')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!validateForm()) return

  try {
    if (props.type === 'edit' && formModel.value.id) {
      const payload = {
        ma: formModel.value.maPhong.trim(),
        ten: formModel.value.tenPhong.trim(),
        idLoaiPhong: formModel.value.loaiPhong!,
        tang: formModel.value.tang!,
        trangThaiPhong: formModel.value.trangThaiHoatDong,
        tagIds: formModel.value.tags || [],
      }

      const res = await updatePhong(formModel.value.id, payload)
      window.$message.success(res?.message || 'Cập nhật phòng thành công!')
    }
    else {
      const payload = {
        ma: formModel.value.maPhong.trim(),
        ten: formModel.value.tenPhong.trim(),
        idLoaiPhong: formModel.value.loaiPhong!,
        sucChua: formModel.value.sucChua!,
        tang: formModel.value.tang!,
        trangThaiHoatDong: formModel.value.trangThaiHoatDong,
        tagIds: formModel.value.tags || [],
      }

      const res = await addPhong(payload)

      if (res?.message?.includes('Tên phòng này đã tồn tại')) {
        window.$message.warning('Tên phòng này đã tồn tại!')
        return
      }

      window.$message.success(res?.message || 'Thêm phòng thành công!')
    }

    emit('refresh')
    closeModal()
    formModel.value = { ...defaultRoom }
  }
  catch (error: any) {
    window.$message.error(error.message || (props.type === 'edit' ? 'Không thể cập nhật phòng' : 'Không thể thêm phòng'))
  }
}

onMounted(() => {
  fetchLoaiPhong()
  fetchTags()
})
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
    <n-spin :show="isLoading">
      <n-form label-placement="left" :model="formModel" label-align="left" :label-width="150">
        <n-grid :cols="24" :x-gap="18">
          <n-form-item-grid-item :span="24" label="Tên phòng" path="tenPhong">
            <n-input
              v-model:value="formModel.tenPhong"
              placeholder="Nhập tên phòng"
              :disabled="isTenPhongDisabled"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Tầng" path="tang">
            <n-input-number v-model:value="formModel.tang" :min="1" :max="3" placeholder="Nhập tầng" style="width: 100%" />
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
              style="width: 100%"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Số người quy định" path="soNguoiQuyDinh">
            <n-input-number
              v-model:value="formModel.soNguoiQuyDinh"
              :min="1"
              placeholder="Tự động từ loại phòng"
              :disabled="isSoNguoiQuyDinhLocked"
              style="width: 100%"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Sức chứa tối đa" path="sucChua">
            <n-input-number
              v-model:value="formModel.sucChua"
              :min="1"
              placeholder="Tự động từ loại phòng"
              :disabled="isSucChuaLocked"
              style="width: 100%"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Số giường đơn" path="soGiuongDon">
            <n-input-number
              v-model:value="formModel.soGiuongDon"
              :min="0"
              placeholder="Tự động từ loại phòng"
              :disabled="isBedInfoLocked"
              style="width: 100%"
            />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Số giường đôi" path="soGiuongDoi">
            <n-input-number
              v-model:value="formModel.soGiuongDoi"
              :min="0"
              placeholder="Tự động từ loại phòng"
              :disabled="isBedInfoLocked"
              style="width: 100%"
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

          <n-form-item-grid-item :span="24" label="Tags" path="tags">
            <n-select
              v-model:value="formModel.tags"
              :options="tagOptions"
              label-field="label"
              value-field="value"
              placeholder="Chọn tags"
              multiple
              filterable
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
</style>
