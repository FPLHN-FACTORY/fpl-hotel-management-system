<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { PhieuDatTamResponse, SavePhieuDatTamRequest, TimKhachHangResponse } from '@/service/api/letan/booking'
import { savePhieuDatTam, searchKhachHang, getPhieuDatTam } from '@/service/api/letan/booking'
import { useDebounceFn } from '@vueuse/core'
import { CameraOutline, QrCodeOutline, SearchOutline, PersonAddOutline, PersonOutline, CallOutline } from '@vicons/ionicons5'
interface Props {
  visible: boolean
  sessionId: string | null
  initialStep: 'CUSTOMER_INFO' | 'PAYMENT_INFO'
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'continue', sessionId: string): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const formData = ref({
  ghiChu: '',
  nhanNgay: false,
  tienKhachTra: null as number | null,
})

const keywordKhachHang = ref('')
const khachHangOptions = ref<TimKhachHangResponse[]>([])
const selectedKhachHang = ref<string | null>(null)
const isSearchingKH = ref(false)
const isLoading = ref(false)
const phieuData = ref<PhieuDatTamResponse | null>(null)

const currentStep = ref<'CUSTOMER_INFO' | 'PAYMENT_INFO'>('CUSTOMER_INFO')

const selectedKhachHangInfo = computed(() => {
  return khachHangOptions.value.find(kh => kh.id === selectedKhachHang.value)
})

const tongTien = computed(() => phieuData.value?.tongTien || 0)

const tienThua = computed(() => {
  if (!formData.value.tienKhachTra || formData.value.tienKhachTra <= 0) return 0
  const thua = formData.value.tienKhachTra - tongTien.value
  return thua > 0 ? thua : 0
})

const congNo = computed(() => {
  if (!formData.value.tienKhachTra || formData.value.tienKhachTra <= 0) return tongTien.value
  const no = tongTien.value - formData.value.tienKhachTra
  return no > 0 ? no : 0
})
const formModelKhachHang = reactive({
  tenDoan: '',
  ghiChu: '',
  hoTenTruongDoan: '',
  soDienThoaiTruongDoan: '',
  ngaySinhTruongDoan: null,
  gioiTinhTruongDoan: null,
  loaiGiayToTruongDoan: null,
  soGiayToTruongDoan: '',
  idDatPhong: null

})
const loaiNguoi = computed(() =>
  phieuData.value?.soLuongKhach > 1 ? "trưởng đoàn" : "khách hàng"
);
const isSearchMode = ref(false);
const resetFormKhachHang = () => {
  Object.assign(formModelKhachHang, {
    tenDoan: '',
    ghiChu: '',
    hoTenTruongDoan: '',
    soDienThoaiTruongDoan: '',
    ngaySinhTruongDoan: null,
    gioiTinhTruongDoan: null,
    loaiGiayToTruongDoan: null,
    soGiayToTruongDoan: '',
    idDatPhong: null
  })
}
function closeModal() {
  modalVisible.value = false
  resetForm()
}

function resetForm() {
  formData.value = {
    ghiChu: '',
    nhanNgay: false,
    tienKhachTra: null,
  }
  keywordKhachHang.value = ''
  khachHangOptions.value = []
  selectedKhachHang.value = null
  phieuData.value = null
  currentStep.value = 'CUSTOMER_INFO'
  resetFormKhachHang()
}

const debouncedSearchKH = useDebounceFn(async () => {
  if (!keywordKhachHang.value || keywordKhachHang.value.length < 2) {
    khachHangOptions.value = []
    return
  }

  try {
    isSearchingKH.value = true
    const data = await searchKhachHang(keywordKhachHang.value)
    khachHangOptions.value = data
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tìm kiếm khách hàng')
  }
  finally {
    isSearchingKH.value = false
  }
}, 500)

watch(() => keywordKhachHang.value, () => {
  debouncedSearchKH()
})

function canNhanNgay() {
  if (!phieuData.value) return false
  const now = Date.now()
  const oneHourBeforeCheckIn = phieuData.value.checkInDate - (60 * 60 * 1000)
  return now >= oneHourBeforeCheckIn
}

async function handleLuuTam() {
  if (!phieuData.value) return

  try {
    isLoading.value = true

    const updateData: SavePhieuDatTamRequest = {
      sessionId: phieuData.value.sessionId,
      checkInDate: phieuData.value.checkInDate,
      checkOutDate: phieuData.value.checkOutDate,
      soLuongKhach: phieuData.value.soLuongKhach,
      idKhachHang: selectedKhachHang.value || phieuData.value.idKhachHang,
      ghiChu: formData.value.ghiChu || phieuData.value.ghiChu,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra !== null ? formData.value.tienKhachTra : phieuData.value.tienKhachTra,
      danhSachIdPhong: phieuData.value.danhSachPhong.map(p => p.idPhong),
      isFromRoomClick: phieuData.value.isFromRoomClick,
      currentStep: selectedKhachHang.value ?
        (formData.value.tienKhachTra !== null ? 'READY_TO_CONFIRM' : 'PAYMENT_INFO') :
        'CUSTOMER_INFO',
      roomDetails: phieuData.value.danhSachPhong.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: p.soNgay,
      })),
    }

    await savePhieuDatTam(updateData)
    window.$message.success('Đã lưu phiếu đặt tạm!')
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể lưu phiếu đặt tạm')
  }
  finally {
    isLoading.value = false
  }
}

async function handleTiepTuc() {
  if (!phieuData.value) return

  if (currentStep.value === 'CUSTOMER_INFO') {
    if (isSearchMode.value) {
      if (!selectedKhachHang.value) {
        window.$message.warning('Vui lòng chọn khách hàng')
        return
      }
    }

    // ✍️ MODE: NHẬP TAY
    else {
      if (
        !formModelKhachHang.hoTenTruongDoan ||
        !formModelKhachHang.soDienThoaiTruongDoan ||
        formModelKhachHang.loaiGiayToTruongDoan === null ||
        !formModelKhachHang.soGiayToTruongDoan ||
        !formModelKhachHang.ngaySinhTruongDoan ||
        formModelKhachHang.gioiTinhTruongDoan === null
      ) {
        window.$message.warning('Vui lòng nhập đầy đủ thông tin khách hàng')
        return
      }
    }

    try {
      isLoading.value = true

      const updateData: SavePhieuDatTamRequest = {
        sessionId: phieuData.value.sessionId,
        checkInDate: phieuData.value.checkInDate,
        checkOutDate: phieuData.value.checkOutDate,
        soLuongKhach: phieuData.value.soLuongKhach,
        idKhachHang: selectedKhachHang.value,
        ghiChu: formData.value.ghiChu || phieuData.value.ghiChu,
        nhanNgay: formData.value.nhanNgay,
        tienKhachTra: phieuData.value.tienKhachTra,
        danhSachIdPhong: phieuData.value.danhSachPhong.map(p => p.idPhong),
        isFromRoomClick: phieuData.value.isFromRoomClick,
        currentStep: 'PAYMENT_INFO',
        roomDetails: phieuData.value.danhSachPhong.map(p => ({
          idPhong: p.idPhong,
          maPhong: p.maPhong,
          tenPhong: p.tenPhong,
          tenLoaiPhong: p.tenLoaiPhong,
          tang: p.tang,
          gia: p.gia,
          soNgay: p.soNgay,
        })),
      }

      await savePhieuDatTam(updateData)

      currentStep.value = 'PAYMENT_INFO'

      const updatedPhieu = await getPhieuDatTam(phieuData.value.sessionId)
      phieuData.value = updatedPhieu
    }
    catch (error: any) {
      window.$message.error(error.message || 'Không thể lưu thông tin khách hàng')
    }
    finally {
      isLoading.value = false
    }
    return
  }

  if (currentStep.value === 'PAYMENT_INFO') {
    if (formData.value.tienKhachTra === null || formData.value.tienKhachTra === undefined) {
      window.$message.warning('Vui lòng nhập số tiền khách trả')
      return
    }
  }

  try {
    isLoading.value = true

    const updateData: SavePhieuDatTamRequest = {
      sessionId: phieuData.value.sessionId,
      checkInDate: phieuData.value.checkInDate,
      checkOutDate: phieuData.value.checkOutDate,
      soLuongKhach: phieuData.value.soLuongKhach,
      idKhachHang: selectedKhachHang.value || phieuData.value.idKhachHang,
      ghiChu: formData.value.ghiChu || phieuData.value.ghiChu,
      nhanNgay: formData.value.nhanNgay,
      tienKhachTra: formData.value.tienKhachTra !== null ? formData.value.tienKhachTra : phieuData.value.tienKhachTra,
      danhSachIdPhong: phieuData.value.danhSachPhong.map(p => p.idPhong),
      isFromRoomClick: phieuData.value.isFromRoomClick,
      currentStep: 'READY_TO_CONFIRM',
      roomDetails: phieuData.value.danhSachPhong.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: p.soNgay,
      })),
    }

    await savePhieuDatTam(updateData)
    emit('continue', phieuData.value.sessionId)
    closeModal()
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tiếp tục')
  }
  finally {
    isLoading.value = false
  }
}

async function loadPhieuDatTam() {
  if (!props.sessionId) return

  try {
    isLoading.value = true
    const data = await getPhieuDatTam(props.sessionId)
    phieuData.value = data

    formData.value = {
      ghiChu: data.ghiChu || '',
      nhanNgay: data.nhanNgay || false,
      tienKhachTra: data.tienKhachTra || null,
    }

    selectedKhachHang.value = data.idKhachHang

    if (data.tenKhachHang && data.idKhachHang) {
      khachHangOptions.value = [{
        id: data.idKhachHang,
        hoTen: data.tenKhachHang,
        email: '',
        soCCCD: '',
        soDienThoai: '',
        diaChi: '',
        maNguoiDung: '',
        quocTich: '',
      } as TimKhachHangResponse]
    }

    currentStep.value = props.initialStep

    if (currentStep.value === 'PAYMENT_INFO' && !data.idKhachHang) {
      currentStep.value = 'CUSTOMER_INFO'
      window.$message.warning('Vui lòng nhập thông tin khách hàng trước')
    }
  }
  catch (error: any) {
    window.$message.error('Không thể tải phiếu đặt tạm')
    closeModal()
  }
  finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.sessionId) {
    loadPhieuDatTam()
  } else if (!val) {
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

const loaiGiayToOptions = ref([
  { label: 'CCCD', value: 0 },
  { label: 'Hộ chiếu', value: 1 }
])

const gioiTinhToOptions = ref([
  { label: 'Nam', value: 0 },
  { label: 'Nữ', value: 1 },
  { label: 'Khác', value: 2 }
])
const showScanner = ref(false)
const showOCRModal = ref(false)
function handleQuetCCCD() {

  if (formModelKhachHang.loaiGiayToTruongDoan !== 0) {
    window.$message.warning('Vui lòng chọn loại giấy tờ là CCCD để quét!')
    return
  }
  showScanner.value = true
}
function handleOCRCCCD() {

  showOCRModal.value = true
}
const onlyAllowNumber = (value: string) => !value || /^\d+$/.test(value)


watch(isSearchMode, (val) => {
  if (val) {
    resetFormKhachHang()
  } else {
    selectedKhachHang.value = null
  }
})

const isValidKhachHang = computed(() => {
  // Search mode
  if (isSearchMode.value) {
    return !!selectedKhachHang.value
  }

  // Nhập tay
  return (
    !!formModelKhachHang.hoTenTruongDoan &&
    !!formModelKhachHang.soDienThoaiTruongDoan &&
    formModelKhachHang.loaiGiayToTruongDoan !== null &&
    formModelKhachHang.loaiGiayToTruongDoan !== undefined &&
    !!formModelKhachHang.soGiayToTruongDoan &&
    !!formModelKhachHang.ngaySinhTruongDoan &&
    formModelKhachHang.gioiTinhTruongDoan !== null
  )
})

const isDisableButton = computed(() => {
  if (currentStep.value === 'CUSTOMER_INFO') {
    return !isValidKhachHang.value
  }

  if (currentStep.value === 'PAYMENT_INFO') {
    return formData.value.tienKhachTra == null
  }

  return false
})


</script>

<template>
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card"
    :title="currentStep === 'CUSTOMER_INFO' ? 'Thông tin phiếu đặt' : 'Thông tin thanh toán'"
    class="w-800px modal-custom-font" :segmented="{ content: true, action: true }">
    <n-spin :show="isLoading">
      <div v-if="phieuData" class="space-y-4">

        <n-card size="small" title="Thông tin đặt phòng" :bordered="false" class="bg-blue-50">
          <div class="grid grid-cols-3 gap-3 text-sm">
            <div>
              <div class="text-gray-600 mb-1">Số phòng</div>
              <div class="font-semibold">{{ phieuData.danhSachPhong.length }} phòng</div>
            </div>
            <div>
              <div class="text-gray-600 mb-1">Thời gian lưu trú</div>
              <div class="font-semibold">{{ Math.ceil((phieuData.checkOutDate - phieuData.checkInDate) / (1000 * 60 * 60
                * 24)) }} đêm</div>
            </div>
            <div>
              <div class="text-gray-600 mb-1">Tổng tiền</div>
              <div class="font-semibold text-blue-600">{{ phieuData.tongTien.toLocaleString('vi-VN') }} VNĐ</div>
            </div>
          </div>
        </n-card>

        <n-card v-if="currentStep === 'CUSTOMER_INFO'" size="small" :title="`Thông tin ${loaiNguoi}`" :bordered="false">
          <div class="flex justify-end mb-3">
            <n-button type="primary" style="border-radius: 6px" @click="isSearchMode = !isSearchMode">
              <template #icon>
                <n-icon v-if="isSearchMode">
                  <PersonAddOutline />
                </n-icon>
                <n-icon v-else>
                  <SearchOutline />
                </n-icon>
              </template>

              {{ isSearchMode ? 'Nhập khách mới' : 'Tìm khách có sẵn' }}
            </n-button>

          </div>

          <!-- 🔍 SEARCH MODE -->
          <n-form v-if="isSearchMode" label-placement="top">
            <n-form-item label="Tìm khách hàng">
              <n-select v-model:value="selectedKhachHang" filterable remote clearable
                placeholder="Nhập tên, SĐT, CCCD hoặc Email..." :options="khachHangOptions.map(kh => ({
                  label: `${kh.hoTen} - ${kh.soDienThoai || kh.email}`,
                  value: kh.id,
                }))" :loading="isSearchingKH" :clear-filter-after-select="false"
                @search="(val) => keywordKhachHang = val" />
            </n-form-item>

            <n-card v-if="selectedKhachHangInfo" size="small">
              <div class="mt-3 rounded-lg bg-gray-50 text-sm border border-gray-200 px-4 py-3shadow-sm">
                <div class="grid grid-cols-2 gap-4">
                  <!-- Họ tên -->
                  <div class="flex items-start gap-3">
                    <n-icon size="15" class="text-blue-500 my-1.5">
                      <PersonOutline />
                    </n-icon>

                    <div>
                      <div class="text-gray-500 text-xs my-1">Họ tên</div>
                      <div class="font-semibold text-gray-800 mb-2">
                        {{ selectedKhachHangInfo.hoTen }}
                      </div>
                    </div>
                  </div>

                  <!-- Số điện thoại -->
                  <div class="flex items-start gap-3">
                    <n-icon size="15" class="text-green-500 my-1.5">
                      <CallOutline />
                    </n-icon>

                    <div>
                      <div class="text-gray-500 text-xs my-1">Số điện thoại</div>
                      <div class="font-semibold text-gray-800 mb-2">
                        {{ selectedKhachHangInfo.soDienThoai }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </n-card>

          </n-form>

          <!-- ✍️ INPUT MODE -->
          <n-form v-else label-placement="top">
            <NGrid :cols="2" :x-gap="12">
              <NFormItemGi v-if="phieuData?.soLuongKhach > 1" :span="2" label="Tên đoàn (Tùy chọn)">
                <NInput v-model:value="formModelKhachHang.tenDoan"
                  placeholder="Để trống sẽ tự sinh theo tên trưởng đoàn..." />
              </NFormItemGi>





              <NFormItemGi label="Họ tên">
                <NInput v-model:value="formModelKhachHang.hoTenTruongDoan" placeholder="Họ và tên..." />
              </NFormItemGi>

              <n-form-item-grid-item label="Ngày sinh" path="ngaySinh">
                <n-date-picker v-model:value="formModelKhachHang.ngaySinhTruongDoan" type="date"
                  placeholder="Chọn ngày sinh" style="width: 100%;" clearable />
              </n-form-item-grid-item>
              <n-form-item-grid-item label="Giới tính" path="gioiTinh">
                <n-radio-group v-model:value="formModelKhachHang.gioiTinhTruongDoan">
                  <n-radio v-for="item in gioiTinhToOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </n-radio>
                </n-radio-group>
              </n-form-item-grid-item>

              <NFormItemGi label="Loại giấy tờ" path="loaiGiayTo">
                <div class="flex gap-2 w-full">
                  <n-select v-model:value="formModelKhachHang.loaiGiayToTruongDoan" :options="loaiGiayToOptions"
                    placeholder="Chọn loại giấy tờ" clearable />

                  <NButton type="primary" style="border-radius: 6px" @click="handleQuetCCCD">
                    <template #icon>
                      <NIcon>
                        <QrCodeOutline />
                      </NIcon>
                    </template>
                  </NButton>
                  <NButton type="primary" style="border-radius: 6px" @click="handleOCRCCCD">
                    <template #icon>
                      <NIcon>
                        <CameraOutline />
                      </NIcon>
                    </template>
                  </NButton>

                </div>
              </NFormItemGi>
              <NFormItemGi label="Số giấy tờ">
                <NInput v-model:value="formModelKhachHang.soGiayToTruongDoan" placeholder="Số giấy tờ ..." clearable />
              </NFormItemGi>
              <NFormItemGi label="Số điện thoại">
                <NInput v-model:value="formModelKhachHang.soDienThoaiTruongDoan" placeholder="Số điện thoại..."
                  :allow-input="onlyAllowNumber" :maxlength="10" show-count />
              </NFormItemGi>
            </NGrid>
          </n-form>
        </n-card>

        <n-card v-if="currentStep === 'PAYMENT_INFO'" size="small" title="Thông tin khách hàng" :bordered="false"
          class="bg-blue-50">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <div class="text-gray-600 mb-1">Họ tên</div>
              <div class="font-semibold">{{ phieuData?.tenKhachHang || 'Chưa có' }}</div>
            </div>
            <div>
              <div class="text-gray-600 mb-1">Số khách</div>
              <div class="font-semibold">{{ phieuData?.soLuongKhach }} người</div>
            </div>
          </div>
        </n-card>

        <n-card v-if="currentStep === 'PAYMENT_INFO'" size="small" title="Thanh toán" :bordered="false"
          class="bg-gradient-to-r from-green-50 to-emerald-50">
          <div class="space-y-3">
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span>Tổng tiền phòng:</span>
                <span class="font-semibold">{{ tongTien.toLocaleString('vi-VN') }} VNĐ</span>
              </div>

              <n-form label-placement="top">
                <n-form-item label="Tiền khách trả">
                  <n-input-number v-model:value="formData.tienKhachTra" :min="0" placeholder="Nhập số tiền"
                    style="width: 100%" :format-value="(value: number) => value?.toLocaleString('vi-VN')" />
                </n-form-item>
              </n-form>

              <n-divider class="my-2" />

              <div v-if="formData.tienKhachTra !== null && formData.tienKhachTra !== undefined" class="space-y-1">
                <div v-if="tienThua > 0" class="flex justify-between text-green-600 font-semibold">
                  <span>Tiền thừa trả khách:</span>
                  <span>{{ tienThua.toLocaleString('vi-VN') }} VNĐ</span>
                </div>
                <div v-if="congNo > 0" class="flex justify-between text-red-600 font-semibold">
                  <span>Công nợ:</span>
                  <span>{{ congNo.toLocaleString('vi-VN') }} VNĐ</span>
                </div>
              </div>

              <div class="flex justify-between items-center pt-2 border-t-2 border-green-300">
                <span class="text-base font-semibold">Tổng tiền:</span>
                <span class="text-2xl font-bold text-green-600">
                  {{ tongTien.toLocaleString('vi-VN') }} VNĐ
                </span>
              </div>
            </div>
          </div>
        </n-card>

        <n-card size="small" title="Thông tin bổ sung" :bordered="false" style="margin-top: 0px;">
          <n-form label-placement="top">
            <n-form-item label="Ghi chú">
              <n-input v-model:value="formData.ghiChu" type="textarea" placeholder="Nhập ghi chú (tùy chọn)..."
                :rows="1" :maxlength="500" show-count />
            </n-form-item>

            <n-form-item>
              <n-checkbox v-model:checked="formData.nhanNgay" :disabled="!canNhanNgay()">
                <span class="text-sm">
                  <nova-icon icon="carbon:license-draft" class="mr-1" />
                  Nhận phòng ngay (Check-in)
                </span>
              </n-checkbox>
              <template #feedback>
                <n-text v-if="!canNhanNgay()" type="warning" class="text-xs">
                  Chỉ áp dụng khi còn tối đa 1 giờ trước check-in
                </n-text>
              </template>
            </n-form-item>
          </n-form>
        </n-card>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-button size="large" @click="closeModal">Hủy</n-button>
        <n-space>
          <n-button size="large" @click="handleLuuTam">
            <template #icon>
              <nova-icon icon="carbon:save" />
            </template>
            Lưu tạm
          </n-button>
          <n-button type="primary" size="large" :disabled="isDisableButton" @click="handleTiepTuc">
            <template #icon>
              <nova-icon :icon="currentStep === 'CUSTOMER_INFO' ? 'carbon:arrow-right' : 'carbon:checkmark'" />
            </template>

            {{ currentStep === 'CUSTOMER_INFO'
              ? 'Tiếp tục nhập thanh toán'
              : 'Xác nhận' }}
          </n-button>

        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-800px {
  width: 800px;
  max-width: 95vw;
  max-height: 90vh;
}

.modal-custom-font :deep(.n-card-header) {
  font-size: 16px;
  font-weight: 600;
  padding: 10px 16px;
}

.modal-custom-font :deep(.n-form-item-label),
.modal-custom-font :deep(.n-input__input-el),
.modal-custom-font :deep(.n-input__textarea-el),
.modal-custom-font :deep(.n-button__content) {
  font-size: 14px;
}

.modal-custom-font :deep(.n-form-item) {
  margin-bottom: 12px;
}
</style>
