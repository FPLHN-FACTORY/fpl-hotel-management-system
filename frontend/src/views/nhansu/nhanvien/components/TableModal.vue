<script setup lang="ts">
import { computed, ref, watch, onBeforeUnmount, reactive } from 'vue'
import {
  NModal,
  NForm,
  NGrid,
  NInput,
  NSelect,
  NButton,
  NDatePicker,
  NInputGroup,
  NIcon,
  NFormItemGridItem,
  useMessage
} from 'naive-ui'
import { Html5QrcodeScanner, Html5Qrcode } from 'html5-qrcode'
import { Camera as CameraIcon } from '@vicons/fa'
import { modifyNhanVien } from '@/service/api/nhansu/nhanvien'
import { getWards, convertFullAddress } from '@/service/api/address'
import { parseCCCDQrContent, normalizeString, type CCCDData } from '@/utils/cccd'

const props = defineProps<{
  visible: boolean
  type: 'add' | 'edit'
  modalData: any
  vaiTroOptions: any[]
  tinhOptions: any[]
  xaOptions: any[]
}>()

const emit = defineEmits(['update:visible', 'refresh', 'province-change'])

const message = useMessage()

const formModel = ref<any>({
  ma: '',
  ten: '',
  email: '',
  sdt: '',
  gioiTinh: null,
  ngaySinh: null,
  cccd: '',
  vaitro: null,
  status: 'ACTIVE',
  diaChi: '',
  tinh: null,
  xa: null
})

const loadingWards = ref(false)
const localXaOptions = ref<any[]>([])
const showScanModal = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
let scanner: Html5QrcodeScanner | null = null
const pendingWardName = ref('')

const title = computed(() => props.type === 'add' ? 'Thêm mới nhân viên' : 'Cập nhật nhân viên')

const modalVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const gioiTinhOptions = [
  { label: 'Nam', value: 1 },
  { label: 'Nữ', value: 0 },
  { label: 'Khác', value: 2 }
]

const trangThaiOptions = [
  { label: 'Hoạt động', value: 'ACTIVE' },
  { label: 'Ngưng hoạt động', value: 'INACTIVE' }
]

// Sync localXaOptions with props
watch(() => props.xaOptions, (val) => {
  localXaOptions.value = val
}, { immediate: true })

// Initialize form data
watch(() => props.modalData, (newVal) => {
  if (newVal) {
    formModel.value = { ...newVal }

    // Ensure address codes are strings
    if (newVal.tinh) formModel.value.tinh = String(newVal.tinh)
    if (newVal.xa) formModel.value.xa = String(newVal.xa)

    // Ensure boolean for gioiTinh if it comes as string 'MALE'/'FEMALE'
    if (newVal.gioiTinh === 'MALE' || newVal.gioiTinh === true) formModel.value.gioiTinh = 1
    else if (newVal.gioiTinh === 'FEMALE' || newVal.gioiTinh === false) formModel.value.gioiTinh = 0
    else formModel.value.gioiTinh = 2

    // Ensure date is timestamp
    if (newVal.ngaySinh && typeof newVal.ngaySinh !== 'number') {
      formModel.value.ngaySinh = new Date(newVal.ngaySinh).getTime()
    }
  } else {
    formModel.value = {
      ma: '',
      ten: '',
      email: '',
      sdt: '',
      gioiTinh: null,
      ngaySinh: null,
      cccd: '',
      vaitro: null,
      status: 'ACTIVE',
      diaChi: '',
      tinh: null,
      xa: null
    }
  }
}, { immediate: true })

function handleClose() {
  modalVisible.value = false
}

function handleEmailBlur() {
  const val = formModel.value.email
  if (val && !val.trim().includes('@')) {
    formModel.value.email = val.trim() + '@gmail.com'
  }
}

async function handleSubmit() {
  try {
    // Basic validation
    if (!formModel.value.ten || !formModel.value.email) {
      message.warning('Vui lòng điền đầy đủ thông tin bắt buộc')
      return
    }

    // Phone validation
    if (!/^\d{9,10}$/.test(formModel.value.sdt)) {
      message.warning('Số điện thoại phải có từ 9 đến 10 chữ số')
      return
    }

    const formData = new FormData()

    // Map fields
    if (formModel.value.id) formData.append('id', formModel.value.id)
    formData.append('code', formModel.value.ma || '')
    formData.append('ten', formModel.value.ten)
    formData.append('email', formModel.value.email)
    formData.append('sdt', formModel.value.sdt)
    formData.append('diaChi', formModel.value.diaChi || '')
    formData.append('cccd', formModel.value.cccd || '')

    // Gioi tinh
    if (formModel.value.gioiTinh === 1) formData.append('gioiTinh', 'true')
    else if (formModel.value.gioiTinh === 0) formData.append('gioiTinh', 'false')

    // Date
    if (formModel.value.ngaySinh) {
      const date = new Date(formModel.value.ngaySinh)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const dateStr = `${year}-${month}-${day}`
      formData.append('ngaySinh', dateStr)
    }

    // Role
    if (formModel.value.vaitro) formData.append('vaiTro', formModel.value.vaitro)

    // Address codes
    if (formModel.value.tinh) formData.append('tinh', formModel.value.tinh)
    if (formModel.value.xa) {
      formData.append('xa', formModel.value.xa)
      // Find district (huyen) from localXaOptions
      const selectedWard = localXaOptions.value.find(w => w.value === formModel.value.xa)
      if (selectedWard && selectedWard.district_code) {
        formData.append('huyen', selectedWard.district_code)
      }
    }

    await modifyNhanVien(formData)
    message.success(props.type === 'add' ? 'Thêm mới thành công' : 'Cập nhật thành công')

    if (props.type === 'add') {
      formModel.value = {
        ma: '',
        ten: '',
        email: '',
        sdt: '',
        gioiTinh: null,
        ngaySinh: null,
        cccd: '',
        vaitro: null,
        status: 'ACTIVE',
        diaChi: '',
        tinh: null,
        xa: null
      }
    }

    emit('refresh')
    handleClose()
  } catch (error: any) {
    console.error(error)
    message.error(error.message || 'Có lỗi xảy ra')
  }
}

function handleProvinceChange(val: string) {
  formModel.value.tinh = val
  formModel.value.xa = null
  emit('province-change', val)
}

function openScanModal() {
  showScanModal.value = true
}

// Helper to apply parsed data
async function applyParsedData(data: CCCDData) {
  formModel.value.cccd = data.cccd
  formModel.value.ten = data.ten
  formModel.value.ngaySinh = data.ngaySinh
  formModel.value.gioiTinh = data.gioiTinh === 'MALE' ? 1 : 0

  // Use the new API to convert the full address string
  console.log('Converting address:', data.diaChi);

  try {
    const converted = await convertFullAddress(data.diaChi);

    if (converted) {
      console.log('Converted Address:', converted);

      // 1. Set Province
      formModel.value.tinh = converted.tinh_code;
      emit('province-change', converted.tinh_code);

      // 2. Set Full Address to the input field as requested
      // Format: Street, Ward, Province
      formModel.value.diaChi = `${converted.dia_chi_duong}, ${converted.xa_name}, ${converted.tinh_name}`;

      // 3. Fetch Wards and Set Ward
      loadingWards.value = true;
      try {
        const wards = await getWards(converted.tinh_code);

        // Map wards for local options
        const mappedWards = wards.map((ward: any) => ({
          label: ward.ward_name,
          value: ward.ward_code,
          name: ward.ward_name
        }));

        // Deduplicate
        const uniqueMap = new Map();
        mappedWards.forEach(item => {
          if (!uniqueMap.has(item.value)) {
            uniqueMap.set(item.value, item);
          }
        });
        localXaOptions.value = Array.from(uniqueMap.values());

        // Set the ward code
        // Try to find the ward in the loaded options to ensure we use the correct code that matches the dropdown
        // This fixes the issue where the API might return a different code than the Open API
        const foundWard = localXaOptions.value.find(w =>
          w.value === converted.xa_code ||
          normalizeString(w.label) === normalizeString(converted.xa_name)
        );

        if (foundWard) {
          formModel.value.xa = foundWard.value;
        } else {
          // Fallback: If not found (API mismatch), add manual option so it displays the name correctly
          console.warn(`Ward mismatch: Code ${converted.xa_code}, Name ${converted.xa_name}. Adding manual option.`);
          const manualOption = {
            label: converted.xa_name,
            value: converted.xa_code,
            name: converted.xa_name
          };
          localXaOptions.value.push(manualOption);
          formModel.value.xa = converted.xa_code;
        }

      } catch (err) {
        console.error('Error fetching wards:', err);
        message.error('Không thể tải danh sách xã/phường');
      } finally {
        loadingWards.value = false;
      }

    } else {
      // Fallback if conversion fails: keep original full address
      throw new Error('API returned null');
    }
  } catch (err) {
    console.error('Error converting address:', err);

    // Fallback: Use original address
    formModel.value.diaChi = data.diaChi;

    // Try to match province locally using data.provinceName
    if (data.provinceName) {
      const normalizedProv = normalizeString(data.provinceName);
      const foundProv = props.tinhOptions.find(p => {
        const pName = normalizeString(p.label);
        return pName.includes(normalizedProv) || normalizedProv.includes(pName);
      });

      if (foundProv) {
        formModel.value.tinh = foundProv.value;
        emit('province-change', foundProv.value);
        message.success(`Đã tự động chọn tỉnh: ${foundProv.label}`);
      }
    }

    message.warning('Không thể phân tích chi tiết địa chỉ (API lỗi). Vui lòng kiểm tra lại.');
  }
}

// Watch for USB Scanner input
watch(() => formModel.value.cccd, async (newVal) => {
  if (newVal && newVal.includes('|')) {
    const parsed = parseCCCDQrContent(newVal)
    if (parsed) {
      await applyParsedData(parsed)
      message.success('Đã nhận diện dữ liệu từ máy quét')
    }
  }
})

// File upload scan
function triggerFileUpload() {
  fileInputRef.value?.click()
}

async function handleScanFile(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    const html5QrCode = new Html5Qrcode("reader-hidden")
    const decodedText = await html5QrCode.scanFile(file, true)

    // Clean up
    html5QrCode.clear()

    const parsed = parseCCCDQrContent(decodedText)
    if (parsed) {
      await applyParsedData(parsed)
      message.success('Quét ảnh thành công')
      showScanModal.value = false
    } else {
      message.warning('Không tìm thấy mã QR CCCD trong ảnh')
    }
  } catch (err) {
    console.error('Error scanning file:', err)
    message.error('Không thể đọc mã QR từ ảnh này')
  } finally {
    // Reset input
    target.value = ''
  }
}

// Watch xaOptions to set pending ward
watch(() => props.xaOptions, (newOptions) => {
  if (pendingWardName.value && newOptions && newOptions.length > 0) {
    const foundWard = newOptions.find(w => {
      const wName = (w.label || '').toLowerCase().replace(/^(phường|xã|thị trấn)\s+/i, '')
      const targetName = pendingWardName.value.toLowerCase().replace(/^(phường|xã|thị trấn)\s+/i, '')
      return wName === targetName || targetName.includes(wName) || wName.includes(targetName)
    })

    if (foundWard) {
      formModel.value.xa = foundWard.value
      pendingWardName.value = '' // Clear pending
    }
  }
}, { deep: true })

const isProcessingScan = ref(false)

// Watch showScanModal to init/clear scanner
watch(showScanModal, (val) => {
  if (val) {
    isProcessingScan.value = false
    setTimeout(() => {
      if (!scanner) {
        scanner = new Html5QrcodeScanner(
          "reader",
          {
            fps: 30, // Higher FPS for faster scanning
            qrbox: { width: 220, height: 220 }, // Smaller box forces user to move closer, improving resolution
            videoConstraints: {
              facingMode: "environment",
              width: { min: 1280, ideal: 1920, max: 3840 }, // Request high resolution
              height: { min: 720, ideal: 1080, max: 2160 },
              focusMode: "continuous"
            } as any,
            formatsToSupport: [0], // QR_CODE
            experimentalFeatures: {
              useBarCodeDetectorIfSupported: true // Use native barcode detector if available (faster and more robust)
            }
          },
          false
        );
        scanner.render(onScanSuccess, (err) => {
          // console.warn(err)
        });
      }
    }, 100)
  } else {
    if (scanner) {
      scanner.clear().catch(err => console.error(err))
      scanner = null
    }
  }
})

async function onScanSuccess(decodedText: string, decodedResult: any) {
  if (isProcessingScan.value) return

  try {
    const parsed = parseCCCDQrContent(decodedText)
    if (parsed) {
      isProcessingScan.value = true

      // Stop scanning immediately to prevent duplicate reads
      if (scanner) {
        try {
          await scanner.clear()
          scanner = null
        } catch (e) {
          console.error('Error clearing scanner:', e)
        }
      }

      await applyParsedData(parsed)
      message.success('Quét CCCD thành công')
      showScanModal.value = false
    } else {
      message.warning('Mã QR không đúng định dạng CCCD')
    }
  } catch (e) {
    console.error(e)
    message.error('Lỗi khi xử lý dữ liệu QR')
    isProcessingScan.value = false
  }
}

onBeforeUnmount(() => {
  if (scanner) {
    scanner.clear().catch(err => console.error(err))
  }
})
</script>

<template>
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card" :title="title" class="w-800px"
    :segmented="{ content: true, action: true }">
    <n-form label-placement="left" :model="formModel" label-align="left" :label-width="150">
      <n-grid :cols="24" :x-gap="18">
        <!-- Row 1 -->
        <n-form-item-grid-item :span="12" label="Mã nhân viên" path="ma">
          <n-input v-model:value="formModel.ma" placeholder="Mã tự động" disabled />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Họ và tên" path="ten" required>
          <n-input v-model:value="formModel.ten" placeholder="Nhập họ và tên" />
        </n-form-item-grid-item>

        <!-- Row 2 -->
        <n-form-item-grid-item :span="12" label="Email" path="email" required>
          <n-input v-model:value="formModel.email" placeholder="Nhập email" @blur="handleEmailBlur" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Số điện thoại" path="sdt" required>
          <n-input v-model:value="formModel.sdt" placeholder="Nhập số điện thoại"
            :allow-input="(value) => !value || /^\d+$/.test(value)" :maxlength="10" show-count />
        </n-form-item-grid-item>

        <!-- Row 3 -->
        <n-form-item-grid-item :span="12" label="Giới tính" path="gioiTinh">
          <!-- In TableModal.vue -->
          <n-select v-model:value="formModel.gioiTinh" :options="gioiTinhOptions" placeholder="Chọn giới tính" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Ngày sinh" path="ngaySinh">
          <n-date-picker v-model:value="formModel.ngaySinh as number | null" type="date" style="width: 100%"
            :is-date-disabled="(timestamp: number) => timestamp > Date.now()" />
        </n-form-item-grid-item>

        <!-- Row 4 - CCCD/CMND -->
        <n-form-item-grid-item :span="12" label="Số CCCD/CMND" path="cccd">
          <n-input-group>
            <n-input v-model:value="formModel.cccd" placeholder="Nhập số CCCD/CMND" />
            <n-button type="primary" @click="openScanModal">
              <template #icon>
                <n-icon><camera-icon /></n-icon>
              </template>
            </n-button>
          </n-input-group>
        </n-form-item-grid-item>

        <!-- Row 5 -->
        <n-form-item-grid-item :span="12" label="Vai trò" path="vaitro">
          <n-select v-model:value="formModel.vaitro as any" :options="vaiTroOptions" placeholder="Chọn vai trò" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Trạng thái" path="status">
          <n-select v-model:value="formModel.status" :options="trangThaiOptions" placeholder="Chọn trạng thái" />
        </n-form-item-grid-item>

        <!-- Address Row -->
        <n-form-item-grid-item :span="24" label="Địa chỉ" path="diaChi">
          <n-input v-model:value="formModel.diaChi" placeholder="Số nhà, tên đường, thôn/xóm..." />
        </n-form-item-grid-item>

        <!-- Address Selection -->
        <n-form-item-grid-item :span="12" label="Tỉnh/Thành phố" path="tinh">
          <n-select v-model:value="formModel.tinh" :options="tinhOptions" placeholder="Chọn tỉnh/thành phố"
            @update:value="(value) => {
              handleProvinceChange(value);
            }" @clear="handleProvinceChange('')" filterable clearable :loading="loadingWards" label-field="label"
            value-field="value" style="width: 100%" :disabled="!!formModel.diaChi" />
        </n-form-item-grid-item>

        <n-form-item-grid-item :span="12" label="Phường/Xã" path="xa">
          <n-select v-model:value="formModel.xa" :options="localXaOptions" :loading="loadingWards"
            placeholder="Chọn phường/xã" filterable clearable label-field="label" value-field="value"
            style="width: 100%" :disabled="!!formModel.diaChi" />
        </n-form-item-grid-item>
      </n-grid>
    </n-form>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 12px">
        <n-button @click="handleClose">Hủy bỏ</n-button>
        <n-button type="primary" @click="handleSubmit">
          {{ type === 'edit' ? 'Cập nhật' : 'Thêm mới' }}
        </n-button>
      </div>
    </template>
  </n-modal>

  <!-- Scan Modal -->
  <n-modal v-model:show="showScanModal" preset="card" title="Quét mã QR CCCD" class="w-600px">
    <div class="mb-4 text-center text-gray-500" style="font-style: italic; color: #666;">
      Đưa mã QR vào giữa khung. Di chuyển lại gần để mã QR lấp đầy khung và rõ nét.
    </div>
    <div id="reader"></div>
    <div id="reader-hidden" style="display: none;"></div>
    <div class="mt-4 flex justify-center gap-4">
      <input type="file" accept="image/*" @change="handleScanFile" ref="fileInputRef" style="display: none" />
      <n-button @click="triggerFileUpload">
        <template #icon>
          <n-icon><camera-icon /></n-icon>
        </template>
        Chọn ảnh từ thư viện
      </n-button>
    </div>
  </n-modal>
</template>

<style scoped>
.w-800px {
  width: 800px;
  max-width: 90vw;
}

.w-600px {
  width: 600px;
  max-width: 90vw;
}

:deep(.n-upload-trigger) {
  width: 100%;
}

/* Tăng font size cho modal */
:deep(.n-card-header) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.n-form-item-label) {
  font-size: 17px;
}

:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-input-number-input),
:deep(.n-button__content) {
  font-size: 17px;
}

:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-date-picker-panel-month__month-item) {
  font-size: 17px;
}

:deep(.n-date-panel-date__date) {
  font-size: 17px;
}

/* Scanner modal text */
:deep(.text-gray-500) {
  font-size: 17px;
}
</style>
