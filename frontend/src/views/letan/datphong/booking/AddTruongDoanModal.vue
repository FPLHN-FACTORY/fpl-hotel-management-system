<script setup lang="ts">
import { reactive, watch, ref,computed } from 'vue'
import {
  NModal,
  NCard,
  NForm,
  NInput,
  NButton,
  NGrid,
  NFormItemGi,
  NIcon
} from 'naive-ui'
import dayjs from 'dayjs'
import { CameraOutline, QrCodeOutline } from '@vicons/ionicons5'
import CccdOCR from '@/components/custom/CccdOCR.vue'
import CccdScanner from '@/components/custom/CccdScanner.vue'
import { type TruongDoan, addTruongDoan } from '@/service/api/letan/booking'
/* ================= PROPS & EMIT ================= */
const props = defineProps<{
  show: boolean
  isDoan: boolean   // 👈 thêm dòng này
}>()


const emit = defineEmits<{
  (e: 'update:show', val: boolean): void
  (e: 'submit', payload: any): void
}>()

/* ================= STATE ================= */
const onlyAllowNumber = (value: string) => !value || /^\d+$/.test(value)
const showScanner = ref(false)
const showOCRModal = ref(false)

const formModel = reactive({
  tenDoan: '',
  hoTen: '',
  soDienThoai: '',
  ngaySinh: null as Date | null,
  gioiTinh: null as number | null,
  loaiGiayTo: null as number | null,
  soGiayTo: '',

})
const modalTitle = computed(() => {
  return props.isDoan ? 'Thêm trưởng đoàn' : 'Thêm khách hàng'
})
const loaiGiayToOptions = [
  { label: 'CCCD', value: 0 },
  { label: 'Hộ chiếu', value: 1 }
]

const gioiTinhToOptions = [
  { label: 'Nam', value: 0 },
  { label: 'Nữ', value: 1 },
  { label: 'Khác', value: 2 }
]

/* ================= WATCH ================= */
watch(() => props.show, (val) => {
  if (val) {
    Object.assign(formModel, {
      tenDoan: '',
      hoTen: '',
      soDienThoai: '',
      ngaySinh: null,
      gioiTinh: null,
      loaiGiayTo: null,
      soGiayTo: '',

    })
  }
})

/* ================= SUBMIT ================= */
async function handleSubmit() {
  if (
    !formModel.hoTen ||
    !formModel.soDienThoai ||
    !formModel.soGiayTo ||
    formModel.loaiGiayTo == null ||
    formModel.gioiTinh == null ||
    formModel.ngaySinh == null
  ) {
    window.$message.warning('Vui lòng nhập đầy đủ thông tin trưởng đoàn')
    return
  }

  const phoneRegex = /^\d{10}$/
  if (!phoneRegex.test(formModel.soDienThoai)) {
    window.$message.error('Số điện thoại phải là 10 chữ số')
    return
  }

  const payload = {
    ...formModel,
      isDoan: props.isDoan,
    ngaySinh: dayjs(formModel.ngaySinh).format('YYYY-MM-DD')
  }
  console.log('payload', payload)
  try {
    // 🔥 ĐỢI API
    const res = await addTruongDoan(payload)

    window.$message.success(modalTitle.value + ' thành công')

    emit('submit', {
      id: res.id,
      hoTen: res.hoTen,
      soDienThoai: res.soDienThoai,
      ngaySinh: dayjs(res.ngaySinh).format('YYYY-MM-DD'),
      gioiTinh: res.gioiTinh,
      loaiGiayTo: res.loaiGiayTo,
      soGiayTo: res.soGiayTo,
      tenDoan: res.tenDoan || null,
      idChiTietDoan: res.idChiTietDoan
    })


    emit('update:show', false)
  } catch (e: any) {
    window.$message.error(e?.response?.data?.message || 'Lỗi khi thêm trưởng đoàn')
  }
}

/* ================= QR / OCR ================= */
function handleQuetCCCD() {
  if (formModel.loaiGiayTo !== 0) {
    window.$message.warning('Vui lòng chọn loại giấy tờ là CCCD để quét!')
    return
  }
  showScanner.value = true
}

function handleOCRCCCD() {
  showOCRModal.value = true
}

function onScanResult(data: any) {
  const str = data.ngaySinh
  const ngay = parseInt(str.slice(0, 2))
  const thang = parseInt(str.slice(2, 4))
  const nam = parseInt(str.slice(4, 8))

  formModel.ngaySinh = new Date(nam, thang - 1, ngay)
  formModel.hoTen = data.hoTen || ''
  formModel.soGiayTo = data.soGiayTo || ''
  formModel.gioiTinh =
    data.gioiTinh === 'Nam' ? 0 : data.gioiTinh === 'Nữ' ? 1 : 2

  showScanner.value = false
}

function onOCRResult(data: any) {
  if (!data || Object.keys(data).length === 0) {
    window.$message.error('Đọc dữ liệu ảnh không thành công')
    return
  }

  if (data.ngaySinh) {
    const [d, m, y] = data.ngaySinh.split('/').map(Number)
    formModel.ngaySinh = new Date(y, m - 1, d)
  }

  formModel.loaiGiayTo = data.loaiGiayTo ?? null
  formModel.hoTen = data.hoTen || ''
  formModel.soGiayTo = data.soGiayTo || ''
  formModel.gioiTinh =
    data.gioiTinh === 'Nam' ? 0 : data.gioiTinh === 'Nữ' ? 1 : 2

  showOCRModal.value = false
}
watch(
  () => props.isDoan,
  (val) => {
    if (!val) {
      formModel.tenDoan = ''
    }
  },
  { immediate: true }
)



</script>

<template>
  <CccdOCR v-model="showOCRModal" @result="onOCRResult" />
  <CccdScanner v-model="showScanner" @scan-result="onScanResult" />

  <NModal :show="show" @update:show="emit('update:show', $event)">
          <NCard :title="modalTitle" style="width: 900px" :bordered="false">
      <NForm :model="formModel" label-width="140">
        <NGrid :cols="2" :x-gap="12">
          <NFormItemGi v-if="props.isDoan" :span="2" label="Tên đoàn">
            <NInput v-model:value="formModel.tenDoan" placeholder="Nhập tên đoàn" />
          </NFormItemGi>


          <NFormItemGi label="Họ tên">
            <NInput v-model:value="formModel.hoTen" placeholder="Nhập họ và tên..." />
          </NFormItemGi>

          <n-form-item-grid-item label="Ngày sinh">
            <n-date-picker v-model:value="formModel.ngaySinh" type="date" placeholder="Chọn ngày sinh"
              style="width: 100%;" clearable />
          </n-form-item-grid-item>

          <NFormItemGi label="Giới tính">
            <n-radio-group v-model:value="formModel.gioiTinh">
              <n-radio v-for="i in gioiTinhToOptions" :key="i.value" :value="i.value">
                {{ i.label }}
              </n-radio>
            </n-radio-group>
          </NFormItemGi>

          <NFormItemGi label="Loại giấy tờ">
            <div class="flex gap-2 w-full">
              <n-select v-model:value="formModel.loaiGiayTo" :options="loaiGiayToOptions"
                placeholder="Chọn loại giấy tờ..." />
              <NButton type="primary" @click="handleQuetCCCD">
                <NIcon>
                  <QrCodeOutline />
                </NIcon>
              </NButton>
              <NButton type="primary" @click="handleOCRCCCD">
                <NIcon>
                  <CameraOutline />
                </NIcon>
              </NButton>
            </div>
          </NFormItemGi>

          <NFormItemGi label="Số giấy tờ">
            <NInput v-model:value="formModel.soGiayTo" placeholder="Nhập số giấy tờ..." />
          </NFormItemGi>

          <NFormItemGi label="Số điện thoại">
            <NInput v-model:value="formModel.soDienThoai" :allow-input="onlyAllowNumber" maxlength="10"
              placeholder="Nhập số điện thoại..." />
          </NFormItemGi>

        </NGrid>
      </NForm>

      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton @click="emit('update:show', false)">Hủy</NButton>
          <NButton type="primary" @click="handleSubmit">Lưu</NButton>
        </div>
      </template>
    </NCard>
  </NModal>
</template>
