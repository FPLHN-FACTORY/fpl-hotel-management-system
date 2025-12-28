<script setup lang="ts">
import { computed, defineEmits, defineProps, onMounted, ref, reactive, watch } from 'vue'
import { addPhong, getAllTags, getPhongById, getRoomTypes, updatePhong } from '@/service/api/letan/phong'
import { addKhachHang, updateKhachHang, fetchLoaiKhachHang, fetchTinhThanhPho, TinhReponse, fetchPhuongXaQuanHuyen, fetchSearch, SearchDiaChiCuReponse } from '@/service/api/nhansu/khachhang'
import { normalize, normalizeTinh } from '@/utils/addressconvert'
import { QrCodeOutline } from '@vicons/ionicons5'
import CccdScanner from '@/components/custom/CccdScanner.vue';
import dayjs from 'dayjs'


onMounted(() => {
  loadLoaiKhachHang();
  loadTinhThanhPho();
});
interface Customer {
  id?: string
  hoTen?: string
  soDienThoai?: string
  ngaySinh?: number
  gioiTinh?: string
  email?: string
  loaiGiayTo?: string
  soGiayTo?: string
  diaChi?: string
  idLoaiKhachHang?: string
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

const title = computed(() => (props.type === 'edit' ? 'Sửa thông tin khách hàng' : 'Thêm khách hàng'))

const defaultCustomer: Customer = {
  // hoTen: '',
  // soDienThoai: '',
  // email: '',
  // soCCCD: '',
  // soHoChieu: '',
  // diaChi: '',
  // idLoaiKhachHang: null,
  hoTen: '',
  soDienThoai: '',
  ngaySinh: null,
  gioiTinh: null,
  email: '',
  loaiGiayTo: null,
  soGiayTo: '',
  diaChi: '',
  idLoaiKhachHang: null,
}

const formModelDiaChi = reactive({
  tinhId: null,
  phuongId: null
})
const formModel = ref<Customer>({ ...defaultCustomer })
const isLoading = ref(false)
const regexPhone = /^(0[3|5|7|8|9])[0-9]{8}$/
const regexEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const regexCCCD = /^[0-9]{12}$/
const regexPassport = /^[A-Z][0-9]{7}$/
const loaiKhachHangOptions = ref([]);
const tinhThanhPhoOptions = ref([])
const phuongOptions = ref([]);
const showScanner = ref(false)
async function loadLoaiKhachHang() {
  const res = await fetchLoaiKhachHang();

  loaiKhachHangOptions.value = [

    ...res
  ];
}
const loaiGiayToOptions = ref([
  { label: 'CCCD', value: 0 },
  { label: 'Hộ chiếu', value: 1 }
])

const gioiTinhToOptions = ref([
  { label: 'Nam', value: 0 },
  { label: 'Nữ', value: 1 },
  { label: 'Khác', value: 2 }
])
async function loadTinhThanhPho() {
  const res = await fetchTinhThanhPho()

  // Map dữ liệu API sang format NSelect
  tinhThanhPhoOptions.value = res.map(p => ({
    label: p.name.includes("Thành phố") ? p.name : `Tỉnh ${p.name}`,
    value: p.province_code
  }))
  console.log("tinhThanhPhoOptions ", tinhThanhPhoOptions.value)
}



function validateForm() {

  if (!formModel.value.hoTen?.trim()) {
    window.$message.warning('Vui lòng nhập họ tên khách hàng!')
    return false
  }


  if (!formModel.value.soDienThoai?.trim()) {
    window.$message.warning('Vui lòng nhập số điện thoại!')
    return false
  }
  if (!regexPhone.test(formModel.value.soDienThoai.trim())) {
    window.$message.warning('Số điện thoại không hợp lệ! (Ví dụ: 0901234567)')
    return false
  }


  if (!formModel.value.email?.trim()) {
    window.$message.warning('Vui lòng nhập email!')
    return false
  }
  if (!regexEmail.test(formModel.value.email.trim())) {
    window.$message.warning('Email không hợp lệ!')
    return false
  }


  const loaiGiayTo = formModel.value.loaiGiayTo
  const soGiayTo = formModel.value.soGiayTo?.trim()

  if (loaiGiayTo==null) {
    window.$message.warning('Vui lòng chọn loại giấy tờ !')
    return false
  }

  if (!soGiayTo) {
    window.$message.warning('Vui lòng nhập số giấy tờ!')
    return false
  }
  if (loaiGiayTo === "CCCD" && !regexCCCD.test(soGiayTo)) {
    window.$message.warning('CCCD phải gồm 12 số!')
    return false
  }


  if (loaiGiayTo === "HO_CHIEU" && !regexPassport.test(soGiayTo)) {
    window.$message.warning('Số hộ chiếu phải có dạng: 1 chữ cái + 7 số (Ví dụ: B1234567)')
    return false
  }
  if (!formModelDiaChi.tinhId && !formModelDiaChi.phuongId) {
    window.$message.warning('Vui lòng chọn địa chỉ')
    return false
  }
  if (!formModelDiaChi.tinhId) {
    window.$message.warning('Vui lòng chọn 1 tỉnh/thành phố')
    return false
  }
  if (!formModelDiaChi.phuongId) {
    window.$message.warning('Vui lòng chọn 1 phường/xã')
    return false
  }
  return true
}


async function handleSubmit() {
  if (!validateForm()) return

  try {
    if (props.type === 'edit' && formModel.value.id) {

      const payload = {
        hoTen: formModel.value.hoTen.trim(),
        gioiTinh: formModel.value.gioiTinh,
        ngaySinh: formModel.value.ngaySinh
          ? dayjs(formModel.value.ngaySinh).format('YYYY-MM-DD')
          : null,
        loaiGiayTo: formModel.value.loaiGiayTo,
        soGiayTo: formModel.value.soGiayTo?.trim(),
        soDienThoai: formModel.value.soDienThoai.trim(),
        email: formModel.value.email.trim(),
        idLoaiKhachHang: formModel.value.idLoaiKhachHang ?? null,
        diaChi: JSON.stringify({
          province: {
            code: formModelDiaChi.tinhId,
            name: tinhThanhPhoOptions.value.find(t => t.value === formModelDiaChi.tinhId)?.label || ''
          },
          ward: {
            code: formModelDiaChi.phuongId,
            name: phuongOptions.value.find(p => p.value === formModelDiaChi.phuongId)?.label || ''
          }
        })
      }
      const res = await updateKhachHang(formModel.value.id, payload)
      window.$message.success(res?.message || 'Cập nhật khách hàng thành công!')
    }
    else {
      const payload = {
        hoTen: formModel.value.hoTen.trim(),
        gioiTinh: formModel.value.gioiTinh,
        ngaySinh: formModel.value.ngaySinh
          ? dayjs(formModel.value.ngaySinh).format('YYYY-MM-DD')
          : null,
        loaiGiayTo: formModel.value.loaiGiayTo,
        soGiayTo: formModel.value.soGiayTo?.trim(),
        soDienThoai: formModel.value.soDienThoai.trim(),
        email: formModel.value.email.trim(),
        idLoaiKhachHang: formModel.value.idLoaiKhachHang ?? null,
        diaChi: JSON.stringify({
          province: {
            code: formModelDiaChi.tinhId,
            name: tinhThanhPhoOptions.value.find(t => t.value === formModelDiaChi.tinhId)?.label || ''
          },
          ward: {
            code: formModelDiaChi.phuongId,
            name: phuongOptions.value.find(p => p.value === formModelDiaChi.phuongId)?.label || ''
          }
        })
      }
      const res = await addKhachHang(payload)
      window.$message.success(res?.message || 'Thêm khách hàng thành công!')
    }

    emit('refresh')
    closeModal()
    formModel.value = { ...defaultCustomer }
  }
  catch (error: any) {
    window.$message.error(error.message || (props.type === 'edit' ? 'Không thể cập nhật khách hàng' : 'Không thể thêm khách hàng'))
  }
}
watch(
  () => props.visible,
  async (isVisible) => {
    if (!isVisible) {
      // Khi modal đóng → reset form
      resetForm()
      return
    }

    // Khi modal mở
    if (props.type === 'edit' && props.modalData) {
      const val = props.modalData
      formModel.value = { ...val, idLoaiKhachHang: val.idLoaiKhachHang ?? '' }

      if (val.diaChi) {
        try {
          const diaChi = JSON.parse(val.diaChi)
          formModelDiaChi.tinhId = diaChi.province.code

          const phuongRes = await fetchPhuongXaQuanHuyen(formModelDiaChi.tinhId)
          phuongOptions.value = phuongRes.map(p => ({
            label: p.ward_name,
            value: p.ward_code
          }))

          formModelDiaChi.phuongId = diaChi.ward.code
        } catch { }
      }
    // } else if (props.type === 'add' && props.modalData) {
    //   const val = props.modalData
    //   resetForm() // reset trước khi điền dữ liệu QR
    //   const str = val.ngaySinh; // ddMMyyyy
    //   const ngay = parseInt(str.slice(0, 2));
    //   const thang = parseInt(str.slice(2, 4));
    //   const nam = parseInt(str.slice(4, 8));
    //   formModel.value.ngaySinh = new Date(nam, thang - 1, ngay);

    //   formModel.value.hoTen = val.hoTen || ''
    //   formModel.value.soGiayTo = val.soGiayTo || ''
    //   formModel.value.diaChi = val.diaChi || ''

    //   formModel.value.gioiTinh = val.gioiTinh?.toUpperCase() || '';


    //   if (val.diaChi) {
    //     await doiDiaChiCuSangMoi(val.diaChi)
    //   }
    }
  },
  { immediate: true }
)

function resetForm() {
  formModel.value = { ...defaultCustomer }
  formModelDiaChi.tinhId = null
  formModelDiaChi.phuongId = null
  phuongOptions.value = []
}




async function doiDiaChiCuSangMoi(params: string) {
  const parts = params.split(',').map(p => p.trim())
  const len = parts.length
  console.log("params", params)
  // Giả sử CCCD luôn có 4 phần: Xã, Thị trấn/Huyện, Thị xã/Huyện, Tỉnh
  // Cách mapping phổ biến:
  let xaPhuong = '';
  const tinh = parts[parts.length - 1] || '';
  console.log("tinhs", tinh)
  let data: any[] = [];
  let match: SearchDiaChiCuReponse | undefined;
  if (len > 3) {
    xaPhuong = parts[parts.length - 3] || '' // có thể bỏ hoặc dùng nếu cần
    console.log("xa", xaPhuong)


    data = await fetchSearch(xaPhuong);
    console.log("data", data)
    // Lọc kết quả hợp lệ
    match = data.filter(r =>
      r.is_merger_match && normalize(r.matched_old_unit) === (normalize(xaPhuong))
    )
      .map(r => ({
        ward_name: r.name,
        ward_code: r.ward_code,
        province_code: r.province_code,
        province_name: r.province_name,
        matched_old_unit: r.matched_old_unit,
        province_merged_with: r.province_merged_with || [],
        merger_details: r.merger_details
      }))
      .find(r => {
        console.log("length", r.province_merged_with.length)
        console.log("tinh tu api", r.province_merged_with)
        console.log("length", normalizeTinh(tinh))
        return !r.province_merged_with.length || r.province_merged_with.map(normalizeTinh).includes(normalizeTinh(tinh))
      });

  }
  else {
    xaPhuong = parts[parts.length - 2] || ''
    console.log("xa", xaPhuong)
    data = await fetchSearch(xaPhuong);
    console.log("data", data)
    // Lọc kết quả hợp lệ
    match = data.filter(r =>
      normalize(r.name) === (normalize(xaPhuong))
    )
      .map(r => ({
        ward_name: r.name,
        ward_code: r.ward_code,
        province_code: r.province_code,
        province_name: r.province_name,
        matched_old_unit: r.matched_old_unit,
        province_merged_with: r.province_merged_with || [],
        merger_details: r.merger_details
      }))
      .find(r => {

        console.log("tinh dia chi moi", normalizeTinh(tinh))
        return normalizeTinh(r.province_name).includes(normalizeTinh(tinh))
      });
  }


  if (match) {
    formModelDiaChi.tinhId = match.province_code;

    // Load phường theo tỉnh mới
    const phuongRes = await fetchPhuongXaQuanHuyen(match.province_code);
    phuongOptions.value = phuongRes.map(p => ({
      label: p.ward_name,
      value: p.ward_code
    }));

    // Set phường
    formModelDiaChi.phuongId = match.ward_code;

    console.log('Địa chỉ mới:', match.ward_name, match.province_name);
  } else {
    console.warn('Không tìm thấy địa chỉ mới phù hợp cho:', params);
  }

}

watch(
  () => formModelDiaChi.tinhId,
  async (newTinhId) => {
    // Mỗi lần chọn tỉnh mới → reset phường đã chọn
    formModelDiaChi.phuongId = null
    phuongOptions.value = []

    if (!newTinhId) return

    const res = await fetchPhuongXaQuanHuyen(newTinhId)

    phuongOptions.value = res.map(p => ({
      label: p.ward_name,
      value: p.ward_code
    }))
  }
)


function handleQuetCCCD() {

  if(formModel.value.loaiGiayTo != 0){
    window.$message.warning('Vui lòng chọn loại giấy tờ là CCCD để quét!')
    return
  }
  showScanner.value = true
}
async function onScanResult (data: any) {
  // data sẽ có các trường: cccd, hoTen, ngaySinh, gioiTinh, diaChi, ngayCap
  console.log('CCCD data:', data)


    const val = data
    // reset trước khi điền dữ liệu QR
      const str = val.ngaySinh; // ddMMyyyy
      const ngay = parseInt(str.slice(0, 2));
      const thang = parseInt(str.slice(2, 4));
      const nam = parseInt(str.slice(4, 8));
      formModel.value.ngaySinh = new Date(nam, thang - 1, ngay);

      formModel.value.hoTen = val.hoTen || ''
      formModel.value.soGiayTo = val.soGiayTo || ''
      formModel.value.diaChi = val.diaChi || ''

      formModel.value.gioiTinh = val.gioiTinh==='Nam'?0:(val.gioiTinh==='Nữ'?1:2);


      if (val.diaChi) {
        await doiDiaChiCuSangMoi(val.diaChi)
      }

  showScanner.value = false // đóng scanner
}
</script>

<template>
  <CccdScanner v-model="showScanner" @scan-result="onScanResult" />
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card" :title="title" class="w-900px"
    :segmented="{ content: true, action: true }">
    <n-spin :show="isLoading">
      <n-form label-placement="left" :model="formModel" label-align="left" :label-width="125">
        <n-grid :cols="24" :x-gap="18">
          <n-form-item-grid-item :span="12" label="Họ tên" path="hoTen">
            <n-input v-model:value="formModel.hoTen" placeholder="Nhập họ tên" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Ngày sinh" path="ngaySinh">
            <n-date-picker v-model:value="formModel.ngaySinh" type="date" placeholder="Chọn ngày sinh"
              style="width: 100%" clearable />
          </n-form-item-grid-item>
          <n-form-item-grid-item :span="18" label="Loại giấy tờ" path="loaiGiayTo">
            <n-select v-model:value="formModel.loaiGiayTo" :options="loaiGiayToOptions" placeholder="Chọn loại giấy tờ"
              clearable />
          </n-form-item-grid-item>
            <n-form-item-grid-item :span="3">
          <n-button type="primary" style="border-radius: 6px" @click="handleQuetCCCD">
            <template #icon>
              <n-icon>
                <QrCodeOutline />
              </n-icon>
            </template>
          </n-button>
</n-form-item-grid-item> 
          <n-form-item-grid-item :span="12" label="Số giấy tờ" path="soGiayTo">
            <NInput v-model:value="formModel.soGiayTo" placeholder="Nhập số giấy tờ" clearable />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Giới tính" path="gioiTinh">
            <n-radio-group v-model:value="formModel.gioiTinh">
              <n-radio v-for="item in gioiTinhToOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </n-radio>
            </n-radio-group>
          </n-form-item-grid-item>
          <n-form-item-grid-item :span="12" label="Số điện thoại" path="soDienThoai">
            <NInput v-model:value="formModel.soDienThoai" placeholder="Nhập số điện thoại " clearable />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Email" path="email">
            <NInput v-model:value="formModel.email" placeholder="Nhập email" clearable />
          </n-form-item-grid-item>
          <n-form-item-grid-item :span="24" label="Loại khách hàng" path="idLoaiKhachHang">
            <NSelect v-model:value="formModel.idLoaiKhachHang" :options="loaiKhachHangOptions"
              placeholder="Chọn loại khách hàng" clearable />
          </n-form-item-grid-item>
          <n-form-item-grid-item :span="24" label="Địa chỉ" path="diaChi">
            <div class="address-select-wrapper">
              <n-select v-model:value="formModelDiaChi.tinhId" :options="tinhThanhPhoOptions"
                placeholder="Chọn tỉnh/thành phố" clearable class="address-select" />
              <n-select v-model:value="formModelDiaChi.phuongId" :options="phuongOptions" placeholder="Chọn phường/xã"
                clearable :disabled="!formModelDiaChi.tinhId" class="address-select" />
            </div>
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
.w-900px {
  width: 900px;
}

.address-select-wrapper {
  display: flex;
  gap: 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 4px;
  background-color: #fff;
}

.address-select {
  flex: 1;
  /* luôn chiếm bằng nhau */
  min-width: 355px;
  /* tránh quá hẹp khi chưa có data */
}
</style>
