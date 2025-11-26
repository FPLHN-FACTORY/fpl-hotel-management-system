<template>
  <n-modal v-model:show="modalVisible" :mask-closable="false" preset="card" :title="title" class="w-700px"
    :segmented="{ content: true, action: true }">
    <n-spin :show="isLoading">
      <n-form label-placement="left" :model="formModel" label-align="left" :label-width="80">
        <n-grid :cols="24" :x-gap="18">
          <n-form-item-grid-item :span="12" label="Họ tên" path="ten">
            <n-input v-model:value="formModel.ten" placeholder="Nhập họ tên" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Email" path="email" :rule="{
            trigger: ['input', 'blur'],
            validator(_, value: string) {
              if (!value) return new Error('Email không được để trống')
              return true
            }
          }">
            <n-input v-model:value="formModel.email" placeholder="Nhập email" @blur="handleEmailBlur" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Số điện thoại" path="sdt" :rule="{
            trigger: ['input', 'blur'],
            validator(_, value: string) {
              if (!value) return new Error('Số điện thoại không được để trống')
              if (!/^\d{9,10}$/.test(value)) return new Error('Số điện thoại phải có 9-10 chữ số')
              return true
            }
          }">
            <n-input v-model:value="formModel.sdt" placeholder="Nhập số điện thoại" maxlength="10" show-count />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="CCCD" path="cccd">
            <n-space vertical style="width: 100%">
              <n-input-group>
                <n-input v-model:value="formModel.cccd" placeholder="Nhập số CCCD" :disabled="isScanning"
                  style="flex: 1" />
                <n-button type="primary" @click="startCamera" :loading="isScanning" :disabled="cameraActive">
                  <template #icon>
                    <n-icon>
                      <ScanIcon />
                    </n-icon>
                  </template>
                  Quét
                </n-button>
              </n-input-group>

              <!-- Hidden elements for camera functionality -->
              <canvas ref="canvasRef" style="display: none;"></canvas>
              <video ref="videoRef" autoplay playsinline style="display: none;"></video>
            </n-space>
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Ngày sinh" path="ngaySinh">
            <n-date-picker v-model:value="dateValue" type="date" placeholder="Chọn ngày sinh" style="width: 100%" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Giới tính" path="gioiTinh">
            <n-select v-model:value="formModel.gioiTinh" :options="genderOptions" label-field="label"
              value-field="value" placeholder="Chọn giới tính" style="width: 100%" clearable
              :value="formModel.gioiTinh || 'MALE'" @update:value="(val) => { formModel.gioiTinh = val }">
              <template #empty>
                <n-empty description="Không có dữ liệu" />
              </template>
            </n-select>
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Vai trò" path="vaitro">
            <n-select v-model:value="formModel.vaitro" :options="props.vaiTroOptions" label-field="label"
              value-field="value" placeholder="Chọn vai trò" style="width: 100%" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Chức vụ" path="chucVu">
            <n-select v-model:value="formModel.chucVu" :options="props.chucVuOptions" label-field="label"
              value-field="value" placeholder="Chọn chức vụ" style="width: 100%" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Mật khẩu" path="matKhau">
            <n-input v-model:value="formModel.matKhau" type="password" placeholder="Nhập mật khẩu" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Địa chỉ" path="diaChi">
            <n-input v-model:value="formModel.diaChi" placeholder="Nhập địa chỉ chi tiết" />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Tỉnh" path="tinh">
            <template v-if="isEditing">
              <n-select v-model:value="formModel.tinh" :options="localTinhOptions" label-field="label"
                value-field="value" placeholder="Chọn tỉnh" style="width: 100%" :loading="isLoading" filterable
                clearable @update:value="handleTinhChange">
                <template #empty>
                  <n-empty description="Không có dữ liệu" />
                </template>
              </n-select>
            </template>
            <n-input v-else :value="provinceName" readonly />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="12" label="Xã/Phường" path="xa">
            <template v-if="isEditing">
              <n-select v-model:value="formModel.xa" :options="localXaOptions" label-field="label" value-field="value"
                :disabled="!formModel.tinh" placeholder="Chọn xã/phường" style="width: 100%" filterable clearable
                :loading="isLoading">
                <template #empty>
                  <n-empty description="Vui lòng chọn tỉnh trước" v-if="!formModel.tinh" />
                  <n-empty description="Không có dữ liệu" v-else />
                </template>
              </n-select>
            </template>
            <n-input v-else :value="wardName" readonly />
          </n-form-item-grid-item>

          <n-form-item-grid-item :span="24" label="Avatar" path="avatar">
            <n-upload v-model:file-list="fileList" :max="1" list-type="image-card" :default-upload="false">
              <n-button>Upload</n-button>
            </n-upload>
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

<script setup lang="ts">
import { computed, defineEmits, defineProps, onBeforeUnmount, ref, watch } from 'vue'
import { Scan as ScanIcon, Camera as CameraIcon, InformationCircleOutline as InfoIcon } from '@vicons/ionicons5'
import type { UploadFileInfo } from 'naive-ui'
import type { SelectOption } from 'naive-ui'
import { getAddressName } from '@/utils/addressUtils'
import { modifyNhanVien } from '@/service/api/nhansu/nhanvien'
import axios from 'axios'

interface NhanVien {
  id?: string
  ma: string
  ten: string
  hoTen?: string
  email: string
  sdt: string
  diaChi: string
  gioiTinh: string | null
  ngaySinh: string | null
  xa: string
  huyen?: string
  tinh: string
  cccd: string
  chucVu: 'ADMIN' | 'STAFF' | string
  vaitro?: string
  avatar?: string
  matKhau?: string
  trangThai?: string
}

interface OptionType extends SelectOption {
  label: string
  value: string
  parentCode?: string
  type?: string
  [key: string]: any
}

interface Props {
  type: 'add' | 'edit' | 'view'
  visible: boolean
  modalData: NhanVien | null
  vaiTroOptions: OptionType[]
  chucVuOptions: OptionType[]
  tinhOptions: OptionType[]
  huyenOptions: OptionType[]
  xaOptions: OptionType[]
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'add',
  visible: false,
  modalData: null,
  title: (props: Props) => props.type === 'add' ? 'Thêm nhân viên mới' : 'Chỉnh sửa thông tin nhân viên',
  vaiTroOptions: () => [],
  chucVuOptions: () => [],
  tinhOptions: () => [],
  huyenOptions: () => [],
  xaOptions: () => []
})
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void
  (e: 'province-change', value: string): void
}>()

const isEditing = computed(() => props.type === 'add' || props.type === 'edit')

const modalVisible = computed<boolean>({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// Function to close the modal
function closeModal() {
  modalVisible.value = false
}

// Default employee data
const defaultNhanVien: NhanVien = {
  id: undefined,
  ma: '',
  ten: '',
  hoTen: '',
  email: '',
  sdt: '',
  diaChi: '',
  gioiTinh: '',
  ngaySinh: null,
  xa: '',
  huyen: '',
  tinh: '',
  cccd: '',
  chucVu: 'STAFF',
  vaitro: '',
  matKhau: '',
  trangThai: 'ACTIVE',
  avatar: undefined
}

// Gender options with proper string values
const genderOptions = ref([
  { label: 'Nam', value: 'MALE' },
  { label: 'Nữ', value: 'FEMALE' }
]);

// Function to normalize gender value
const normalizeGender = (value: any): string => {
  if (value === true || value === 'true' || value === 'MALE' || value === 1) {
    return 'MALE';
  } else if (value === false || value === 'false' || value === 'FEMALE' || value === 0) {
    return 'FEMALE';
  }
  return 'MALE'; // Default value
};

// Refs for camera functionality
const videoRef = ref<HTMLVideoElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const cameraActive = ref(false)
const isScanning = ref(false)
const scanProgress = ref(0)
const stream = ref<MediaStream | null>(null)

// Form and data
const formModel = ref<NhanVien>({ ...defaultNhanVien, gioiTinh: 'MALE' })
const fileList = ref<UploadFileInfo[]>([])
const isLoading = ref(false)
const message = useMessage()

// Address related refs
const localTinhOptions = ref<OptionType[]>([])
const localXaOptions = ref<OptionType[]>([])
const provinceName = ref('')
const wardName = ref('')

// Date handling
const dateValue = computed({
  get: () => formModel.value.ngaySinh ? new Date(formModel.value.ngaySinh).getTime() : null,
  set: (val: number | null) => {
    formModel.value.ngaySinh = val ? new Date(val).toISOString().split('T')[0] : null
  }
})

// Handle email blur to append @gmail.com if needed
const handleEmailBlur = () => {
  if (formModel.value.email && !formModel.value.email.includes('@')) {
    formModel.value.email += '@gmail.com'
  } else if (formModel.value.email && !formModel.value.email.endsWith('@gmail.com')) {
    formModel.value.email = formModel.value.email.split('@')[0] + '@gmail.com'
  }
}

// Camera functions
const startCamera = async () => {
  try {
    cameraActive.value = true
    isScanning.value = true

    // Stop any existing stream
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop())
    }

    // Request camera access
    const mediaStream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 1920 },
        height: { ideal: 1080 },
        facingMode: 'environment',
        aspectRatio: 1.6 // Closer to ID card aspect ratio
      }
    })

    stream.value = mediaStream

    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
      await videoRef.value.play()

      // Add a small delay to ensure video is playing
      setTimeout(() => {
        if (videoRef.value) {
          // Adjust video to maintain aspect ratio
          videoRef.value.style.objectFit = 'cover'
        }
      }, 100)
    }

    isScanning.value = false
  } catch (error) {
    console.error('Error accessing camera:', error)
    window.$message.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập camera của bạn.')
    cameraActive.value = false
    isScanning.value = false
  }
}

const stopCamera = () => {
  if (stream.value) {
    stream.value.getTracks().forEach(track => track.stop())
    stream.value = null
  }
  cameraActive.value = false
}

const captureImage = async () => {
  if (!videoRef.value || !canvasRef.value || isScanning.value) return;

  isScanning.value = true;

  try {
    const video = videoRef.value;
    const canvas = canvasRef.value;
    const context = canvas.getContext('2d');

    if (!context) {
      throw new Error('Không thể khởi tạo canvas');
    }

    // Set canvas dimensions to match video
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;

    // Draw video frame to canvas
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    // Convert canvas to blob
    const blob = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.9);
    });

    if (!blob) {
      throw new Error('Không thể chuyển đổi ảnh');
    }

    const file = new File([blob], 'cccd.jpg', { type: 'image/jpeg' });

    try {
      await processCapturedImage(file);
      // Only stop camera on successful scan
      stopCamera();
    } catch (error) {
      // Don't show error, just log it
      console.error('Error processing image:', error);
    }
  } catch (error) {
    console.error('Error capturing image:', error);
  } finally {
  }
}

const processCapturedImage = async (file: File) => {
  isScanning.value = true;

  try {
    // Update file list with the captured image
    fileList.value = [{
      id: 'cccd-scan',
      name: 'cccd-scan.jpg',
      status: 'finished',
      url: URL.createObjectURL(file)
    }];

    // Call backend API to process the ID card image
    const formData = new FormData();
    formData.append('image', file);

    const response = await fetch('http://localhost:2345/api/v1/scan-id-card', {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`Error: ${response.status}`);
    }

    const data = await response.json();
    console.log('ID Card Data:', data);

    // Map ID card data to form model
    if (data) {
      // Update CCCD number
      if (data.idNumber) {
        formModel.value.cccd = data.idNumber;
      }

      // Update full name
      if (data.fullName) {
        formModel.value.ten = data.fullName;
      }

      // Update date of birth
      if (data.dateOfBirth) {
        const dob = new Date(data.dateOfBirth);
        dateValue.value = dob.getTime();
      }

      // Update gender (map to Vietnamese values)
      if (data.gender) {
        const genderMap: Record<string, string> = {
          'MALE': 'MALE',
          'FEMALE': 'FEMALE',
          'NAM': 'MALE',
          'NỮ': 'FEMALE',
          'NU': 'FEMALE',
          'Khác': 'OTHER',
          'Other': 'OTHER'
        };

        const genderValue = genderMap[data.gender.toUpperCase()] || 'MALE';
        formModel.value.gioiTinh = genderValue;
      }

      // Update address
      if (data.address) {
        formModel.value.diaChi = data.address;
      }

      window.$message.success('Đã quét thông tin CCCD thành công!');
    }

    return true;
  } catch (error) {
    console.error('Error processing ID card:', error);
    window.$message.error('Có lỗi xảy ra khi xử lý ảnh CCCD: ' + (error as Error).message);
    return false;
  } finally {
    isScanning.value = false;
    stopCamera();
  }
}

// Clean up camera stream when component is unmounted or modal is closed
onBeforeUnmount(() => {
  stopCamera()
})

// Watch for modal close to clean up camera
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    stopCamera()
  }
})

// Watch for options changes and update local options
watch(() => props.tinhOptions, (newOptions) => {
  localTinhOptions.value = [...(newOptions || [])]
}, { immediate: true })

watch(() => props.xaOptions, (newOptions) => {
  localXaOptions.value = [...(newOptions || [])]
}, { immediate: true })

// Handle province selection change
const handleTinhChange = (value: string) => {
  formModel.value.tinh = value;
  formModel.value.xa = '';
  emit('province-change', value);
};

// Handle form submission
const handleSubmit = async () => {
  try {
    isLoading.value = true;
    // Add your form submission logic here
    // For example:
    // await saveEmployee(formModel);
    // emit('refresh');
    // closeModal();
  } catch (error) {
    console.error('Error submitting form:', error);
  } finally {
    isLoading.value = false;
  }
};

const handleIdCardUpload = async ({ file, onFinish = () => { }, onError = (error: any) => console.error('ID card upload error:', error) }: { file: any, onFinish?: () => void, onError?: (error: any) => void }) => {
  if (isScanning.value) return;

  isScanning.value = true;
  let errorMessage = '';

  try {
    if (!file || !file.file) {
      throw new Error('Không có tệp ảnh được chọn');
    }

    const formData = new FormData();
    formData.append('image', file.file);

    const response = await fetch('http://localhost:2345/scan-id-card', {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      errorMessage = errorData.message || 'Lỗi khi xử lý ảnh CCCD';
      throw new Error(errorMessage);
    }

    const data = await response.json();

    // Auto-fill form fields if data is available
    if (data.idNumber) formModel.value.cccd = data.idNumber;
    if (data.fullName) formModel.value.ten = data.fullName;
    if (data.gender) {
      formModel.value.gioiTinh = data.gender === 'Nam' ? 'MALE' : 'FEMALE';
    }

    // Format date if available
    if (data.dateOfBirth) {
      try {
        const date = new Date(data.dateOfBirth);
        if (!isNaN(date.getTime())) {
          dateValue.value = date.getTime();
        }
      } catch (e) {
        console.warn('Invalid date format from API:', data.dateOfBirth);
      }
    }

    // Set address if available
    if (data.address) {
      formModel.value.diaChi = data.address;
    }

    // Handle province selection
    if (data.province && localTinhOptions.value.length > 0) {
      // Try to find matching province in options (case insensitive)
      const provinceMatch = localTinhOptions.value.find(p =>
        p.label.toLowerCase().includes(data.province.toLowerCase()) ||
        data.province.toLowerCase().includes(p.label.toLowerCase())
      );

      if (provinceMatch) {
        formModel.value.tinh = provinceMatch.value;
        // Only call handleTinhChange if we have a valid province
        await handleTinhChange(provinceMatch.value);
      }
    }

    message.success('Quét CCCD thành công');
    onFinish();
  } catch (error) {
    console.error('Error scanning ID card:', error);
    const displayMessage = error.message || 'Có lỗi xảy ra khi quét CCCD';
    message.error(displayMessage);
    onError(error);
  } finally {
    isScanning.value = false;
  }
  const cameraActive = ref(false)
  const videoRef = ref<HTMLVideoElement | null>(null)
  const canvasRef = ref<HTMLCanvasElement | null>(null)
  let stream: MediaStream | null = null

  const dateValue = computed({
    get: () => formModel.value.ngaySinh ? new Date(formModel.value.ngaySinh).getTime() : null,
    set: (val: number | null) => {
      formModel.value.ngaySinh = val ? new Date(val).toISOString().split('T')[0] : null
    }
  })

  const handleSubmit = async () => {
    try {
      isLoading.value = true
      const formData = new FormData()
      formData.append('id', formModel.value.id || '')
      formData.append('code', formModel.value.ma || '')
      formData.append('ten', formModel.value.ten)
      formData.append('email', formModel.value.email)
      formData.append('sdt', formModel.value.sdt)
      formData.append('diaChi', formModel.value.diaChi)
      if (formModel.value.gioiTinh !== null) {
        formData.append('gioiTinh', formModel.value.gioiTinh)
      }
      if (formModel.value.ngaySinh) {
        const date = new Date(formModel.value.ngaySinh);
        const formattedDate = date.toISOString().split('T')[0];
        formData.append('ngaySinh', formattedDate);
      }
      formData.append('xa', formModel.value.xa)
      formData.append('tinh', formModel.value.tinh)
      formData.append('cccd', formModel.value.cccd)
      formData.append('vaiTro', formModel.value.chucVu)

      if (formModel.value.matKhau) {
        formData.append('matKhau', formModel.value.matKhau)
      }

      // Handle file upload if a new file was selected
      if (fileList.value.length > 0) {
        if (fileList.value[0].file) {
          formData.append('avatarFile', fileList.value[0].file)
        } else if (fileList.value[0].url) {
          formData.append('avatar', fileList.value[0].url)
        }
      }

      const res = await modifyNhanVien(formData)
      window.$message.success(res?.message || (props.type === 'edit' ? 'Cập nhật nhân viên thành công!' : 'Thêm nhân viên thành công!'))
      emit('refresh')
      closeModal()
      formModel.value = { ...defaultNhanVien }
      fileList.value = []
    } catch (error: any) {
      window.$message.error(error.message || (props.type === 'edit' ? 'Không thể cập nhật nhân viên' : 'Không thể thêm nhân viên'))
    }
  }

  // Function to update all address names and set form model values
  const updateAddressNames = async () => {
    if (formModel.value.tinh) {
      // First try to get from localTinhOptions
      let province = localTinhOptions.value.find(p => p.value === formModel.value.tinh);

      if (province) {
        // If found in local options, use it
        provinceName.value = province.label;
      } else if (formModel.value.tinh) {
        // If not found, try to fetch it from the API
        try {
          const name = await getAddressName(formModel.value.tinh, 'tinh');
          provinceName.value = name || formModel.value.tinh;

          // Add to local options for future reference
          if (name && !localTinhOptions.value.some(p => p.value === formModel.value.tinh)) {
            localTinhOptions.value.push({
              label: name,
              value: formModel.value.tinh
            });
          }
        } catch (error) {
          console.error('Error fetching province name:', error);
          provinceName.value = formModel.value.tinh;
        }
      }

      // Update ward options when province changes
      if (formModel.value.tinh) {
        const wards = await fetchWards(formModel.value.tinh);
        localXaOptions.value = wards.map(w => ({
          label: w.name,
          value: w.code
        }));
      }

      if (formModel.value.xa) {
        // Get ward name from localXaOptions
        const ward = localXaOptions.value.find(w => w.value === formModel.value.xa);
        wardName.value = ward ? ward.label : formModel.value.xa;
        if (formModel.value.xa) {
          // Get ward name from localXaOptions
          const ward = localXaOptions.value.find(w => w.value === formModel.value.xa);
          wardName.value = ward ? ward.label : formModel.value.xa;
        } else {
          wardName.value = '';
        }
      } else {
        districtName.value = '';
        wardName.value = '';
        localXaOptions.value = [];
      }
    } else {
      provinceName.value = '';
      districtName.value = '';
      wardName.value = '';
      localHuyenOptions.value = [];
      localXaOptions.value = [];
    }
  };

  watch(() => formModel.value.tinh, async () => {
    if (formModel.value.tinh) {
      provinceName.value = await getAddressName(formModel.value.tinh, 'tinh');

      // Reset dependent fields when province changes
      if (formModel.value.huyen) {
        formModel.value.huyen = '';
        formModel.value.xa = '';
        districtName.value = '';
        wardName.value = '';
      }
    } else {
      provinceName.value = '';
    }
  });

  watch(() => formModel.value.huyen, async () => {
    if (formModel.value.huyen) {
      districtName.value = await getAddressName(formModel.value.huyen, 'huyen', formModel.value.tinh);

      // Reset ward when district changes
      if (formModel.value.xa) {
        formModel.value.xa = '';
        wardName.value = '';
      }
    } else {
      districtName.value = '';
    }
  });

  watch(() => formModel.value.xa, async () => {
    if (formModel.value.xa) {
      wardName.value = await getAddressName(formModel.value.xa, 'xa', formModel.value.huyen);
      wardName.value = await getAddressName(formModel.value.xa, 'xa');
    } else {
      wardName.value = '';
    }
  });

  // Initialize address names when modal data changes
  watch(() => props.modalData, async () => {
    if (props.modalData) {
      await nextTick();
      await updateAddressNames();
    }
  }, { immediate: true, deep: true });

  // Initialize form with data when editing
  // In the watch for modalData
  watch(
    () => props.modalData,
    async (newVal) => {
      if (newVal && (props.type === 'edit' || props.type === 'view')) {
        const data = { ...newVal };

        // Ensure gender is properly typed as string
        if (data.gioiTinh !== undefined && data.gioiTinh !== null) {
          data.gioiTinh = String(normalizeGender(data.gioiTinh));
        } else {
          data.gioiTinh = 'MALE'; // Default value
        }
        // Đảm bảo ten và hoTen có giá trị
        if (!data.ten && data.hoTen) {
          data.ten = data.hoTen;
        } else if (!data.hoTen && data.ten) {
          data.hoTen = data.ten;
        }
        // Set the form data
        formModel.value = { ...defaultNhanVien, ...data };
        // Initialize address data for the form
        if (data.tinh) {
          // Lọc Huyện/Quận dựa trên Tỉnh/Thành phố hiện tại
          localHuyenOptions.value = props.huyenOptions
            .filter(h => h.parentCode === data.tinh);
          if (data.huyen) {
            // Lọc Xã/Phường dựa trên Huyện/Quận hiện tại
            localXaOptions.value = props.xaOptions
              .filter(x => x.parentCode === data.huyen);
          } else {
            localXaOptions.value = [];
          }
        } else {
          localHuyenOptions.value = [];
          localXaOptions.value = [];
        }
      } else {
        formModel.value = { ...defaultNhanVien };
        localHuyenOptions.value = [];
        localXaOptions.value = [];
      }
    },
    { immediate: true, deep: true }
  );

  // Watch for modal open/close
  watch(
    () => props.visible,
    async (newVal) => {
      if (newVal) {
        if (props.type === 'edit' && props.modalData) {
          // Reset form with new data
          formModel.value = {
            ...defaultNhanVien,
            ...props.modalData,
            // Ensure address fields are properly set
            tinh: props.modalData.tinh || '',
            huyen: props.modalData.huyen || '',
            xa: props.modalData.xa || ''
          }

          // Update district options if province is selected
          if (formModel.value.tinh) {
            localHuyenOptions.value = props.huyenOptions
              .filter(h => h.parentCode === formModel.value.tinh);
          }

          // Update ward options if district is selected
          if (formModel.value.huyen) {
            localXaOptions.value = props.xaOptions
              .filter(x => x.parentCode === formModel.value.huyen);
          }

          // Handle file upload preview for edit mode
          if (props.modalData?.avatar) {
            fileList.value = [{
              id: props.modalData.id || 'avatar',
              name: 'avatar',
              url: props.modalData.avatar as string,
              status: 'finished',
              thumbnailUrl: props.modalData.avatar as string
            }]
          } else {
            fileList.value = []
          }
        } else {
          // Reset form when adding new
          formModel.value = { ...defaultNhanVien }
          localHuyenOptions.value = []
          localXaOptions.value = []
          fileList.value = []
        }
      }
    },
    { immediate: true }
  )
}

</script>

<style scoped>
.w-700px {
  width: 700px;
  max-width: 90vw;
}

.camera-placeholder {
  width: 100%;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fafafa;
  margin-bottom: 12px;
}

.camera-placeholder:hover {
  border-color: #1890ff;
  background-color: #f0f7ff;
}

.camera-placeholder p {
  margin-top: 12px;
  color: #8c8c8c;
  font-size: 14px;
}

.camera-container {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
  background-color: #000;
  aspect-ratio: 1.6;
  /* ID card aspect ratio */
}

.camera-view {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
}

.id-card-outline {
  width: 85%;
  height: 60%;
  border: 2px solid #1890ff;
  border-radius: 8px;
  position: relative;
  box-shadow: 0 0 0 100vmax rgba(0, 0, 0, 0.7);
  clip-path: inset(0 -100vmax);
}

.camera-hint {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.camera-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

:deep(.n-upload) {
  width: 100%;
}

:deep(.n-upload-dragger) {
  width: 100%;
  padding: 24px;
}
</style>