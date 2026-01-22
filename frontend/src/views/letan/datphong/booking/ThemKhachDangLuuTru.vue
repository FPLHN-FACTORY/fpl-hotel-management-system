<script setup lang="ts">
import type { FormInst, FormRules } from 'naive-ui'
import { addGuestDuringStay, getRoomsByDoan } from '@/service/api/letan/doanluutru'
import type { AddGuestDuringStayRequest, RoomCapacityInfo } from '@/service/api/letan/doanluutru'

interface Props {
  visible: boolean
  idDoan: string
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const rooms = ref<RoomCapacityInfo[]>([])
const showConfirmDialog = ref(false)
const existingCustomerData = ref<any>(null)

const formData = ref<AddGuestDuringStayRequest>({
  idPhong: '',
  hoTen: '',
  gioiTinh: 0,
  ngaySinh: '',
  loaiGiayTo: 0,
  soGiayTo: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  quocTich: 'Việt Nam',
  ghiChu: '',
  confirmUseOld: false,
})

const genderOptions = [
  { label: 'Nam', value: 0 },
  { label: 'Nữ', value: 1 },
  { label: 'Khác', value: 2 },
]

const idTypeOptions = [
  { label: 'CCCD', value: 0 },
  { label: 'Hộ chiếu', value: 1 },
]

const rules: FormRules = {
  hoTen: { required: true, message: 'Vui lòng nhập họ tên', trigger: 'blur' },
  gioiTinh: { required: true, message: 'Vui lòng chọn giới tính', type: 'number', trigger: 'change' },
  ngaySinh: { required: true, message: 'Vui lòng chọn ngày sinh', trigger: 'change' },
  loaiGiayTo: { required: true, message: 'Vui lòng chọn loại giấy tờ', type: 'number', trigger: 'change' },
  soGiayTo: { required: true, message: 'Vui lòng nhập số giấy tờ', trigger: 'blur' },
  soDienThoai: { required: true, message: 'Vui lòng nhập số điện thoại', trigger: 'blur' },
  idPhong: { required: true, message: 'Vui lòng chọn phòng', trigger: 'change' },
}

const roomOptions = computed(() => {
  return rooms.value.map(room => ({
    label: `${room.tenPhong} (${room.soNguoiHienTai}/${room.soNguoiToiDa} người)`,
    value: room.idPhong,
    disabled: room.soNguoiHienTai >= room.soNguoiToiDa,
  }))
})

const selectedRoomInfo = computed(() => {
  if (!formData.value.idPhong)
    return null
  return rooms.value.find(r => r.idPhong === formData.value.idPhong)
})

watch(() => props.visible, async (val) => {
  if (val) {
    await fetchRooms()
    resetForm()
  }
})

async function fetchRooms() {
  try {
    const res = await getRoomsByDoan(props.idDoan)
    rooms.value = res.data || []
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tải danh sách phòng')
  }
}

function resetForm() {
  formData.value = {
    idPhong: '',
    hoTen: '',
    gioiTinh: 0,
    ngaySinh: '',
    loaiGiayTo: 0,
    soGiayTo: '',
    soDienThoai: '',
    email: '',
    diaChi: '',
    quocTich: 'Việt Nam',
    ghiChu: '',
    confirmUseOld: false,
  }
  existingCustomerData.value = null
  showConfirmDialog.value = false
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true

    const res = await addGuestDuringStay(props.idDoan, formData.value)

    // If customer exists and needs confirmation
    if (res.code === 409) {
      existingCustomerData.value = res.data
      showConfirmDialog.value = true
      loading.value = false
      return
    }

    window.$message.success(res.message || 'Thêm khách thành công')
    emit('success')
    emit('update:visible', false)
  }
  catch (error: any) {
    window.$message.error(error.message || 'Thêm khách thất bại')
  }
  finally {
    loading.value = false
  }
}

async function handleConfirmUseExisting() {
  formData.value.confirmUseOld = true
  showConfirmDialog.value = false
  await handleSubmit()
}

function handleClose() {
  emit('update:visible', false)
}
</script>

<template>
  <n-modal
    :show="visible"
    preset="card"
    title="Thêm khách trong quá trình lưu trú"
    :style="{ width: '700px' }"
    :mask-closable="false"
    @update:show="handleClose"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="140"
      require-mark-placement="left"
    >
      <n-form-item label="Phòng" path="idPhong">
        <n-select
          v-model:value="formData.idPhong"
          :options="roomOptions"
          placeholder="Chọn phòng"
          :loading="loading"
        />
      </n-form-item>

      <n-alert v-if="selectedRoomInfo && selectedRoomInfo.soNguoiHienTai >= selectedRoomInfo.soNguoiToiDa" type="warning" class="mb-4">
        Phòng đã đầy ({{ selectedRoomInfo.soNguoiHienTai }}/{{ selectedRoomInfo.soNguoiToiDa }})
      </n-alert>

      <n-divider>Thông tin khách</n-divider>

      <n-form-item label="Họ tên" path="hoTen">
        <n-input v-model:value="formData.hoTen" placeholder="Nhập họ tên" />
      </n-form-item>

      <n-form-item label="Giới tính" path="gioiTinh">
        <n-radio-group v-model:value="formData.gioiTinh">
          <n-radio v-for="opt in genderOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </n-radio>
        </n-radio-group>
      </n-form-item>

      <n-form-item label="Ngày sinh" path="ngaySinh">
        <n-date-picker
          v-model:formatted-value="formData.ngaySinh"
          type="date"
          format="dd/MM/yyyy"
          value-format="yyyy-MM-dd"
          placeholder="Chọn ngày sinh"
          style="width: 100%"
        />
      </n-form-item>

      <n-form-item label="Loại giấy tờ" path="loaiGiayTo">
        <n-select
          v-model:value="formData.loaiGiayTo"
          :options="idTypeOptions"
          placeholder="Chọn loại giấy tờ"
        />
      </n-form-item>

      <n-form-item label="Số giấy tờ" path="soGiayTo">
        <n-input v-model:value="formData.soGiayTo" placeholder="Nhập số CCCD/Hộ chiếu" />
      </n-form-item>

      <n-form-item label="Số điện thoại" path="soDienThoai">
        <n-input v-model:value="formData.soDienThoai" placeholder="Nhập số điện thoại" />
      </n-form-item>

      <n-form-item label="Email">
        <n-input v-model:value="formData.email" placeholder="Nhập email (không bắt buộc)" />
      </n-form-item>

      <n-form-item label="Địa chỉ">
        <n-input v-model:value="formData.diaChi" placeholder="Nhập địa chỉ (không bắt buộc)" />
      </n-form-item>

      <n-form-item label="Quốc tịch">
        <n-input v-model:value="formData.quocTich" placeholder="Nhập quốc tịch" />
      </n-form-item>

      <n-form-item label="Ghi chú">
        <n-input
          v-model:value="formData.ghiChu"
          type="textarea"
          placeholder="Nhập ghi chú (không bắt buộc)"
          :autosize="{ minRows: 2, maxRows: 4 }"
        />
      </n-form-item>
    </n-form>

    <template #footer>
      <div class="flex justify-end gap-2">
        <n-button @click="handleClose">
          Hủy
        </n-button>
        <n-button type="primary" :loading="loading" @click="handleSubmit">
          Thêm khách
        </n-button>
      </div>
    </template>

    <!-- Confirm dialog for existing customer -->
    <n-modal
      v-model:show="showConfirmDialog"
      preset="dialog"
      title="Xác nhận"
      content="Khách hàng đã tồn tại trong hệ thống. Bạn có muốn sử dụng thông tin cũ không?"
      positive-text="Sử dụng"
      negative-text="Hủy"
      @positive-click="handleConfirmUseExisting"
      @negative-click="showConfirmDialog = false"
    >
      <n-descriptions v-if="existingCustomerData" :column="1" size="small" bordered class="mt-4">
        <n-descriptions-item label="Họ tên">
          {{ existingCustomerData.hoTen }}
        </n-descriptions-item>
        <n-descriptions-item label="Số điện thoại">
          {{ existingCustomerData.soDienThoai }}
        </n-descriptions-item>
        <n-descriptions-item label="Email">
          {{ existingCustomerData.email || 'N/A' }}
        </n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </n-modal>
</template>

<style scoped>
:deep(.n-form-item-label) {
  font-weight: 500;
}
</style>
