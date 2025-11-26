<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="top" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="12">
          <n-form-item-gi :span="6" label="Từ khóa" path="tuKhoa">
            <NInput v-model:value="model.tuKhoa" placeholder="Nhập tên, email hoặc CCCD" clearable />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Vai trò" path="vaitro">
            <NSelect v-model:value="model.vaitro" :options="vaiTroOptions" placeholder="Chọn vai trò" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Chức vụ" path="chucVu">
            <NSelect v-model:value="model.chucVu" :options="chucVuOptions" placeholder="Chọn chức vụ" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Giới tính" path="gioiTinh">
            <NSelect v-model:value="model.gioiTinh" :options="gioiTinhOptions" placeholder="Chọn giới tính" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Tỉnh" path="tinh">
            <NSelect v-model:value="model.tinh" :options="tinhOptions" placeholder="Chọn tỉnh" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Huyện" path="huyen">
            <NSelect v-model:value="model.huyen" :options="huyenOptions" placeholder="Chọn huyện" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Xã" path="xa">
            <NSelect v-model:value="model.xa" :options="xaOptions" placeholder="Chọn xã" clearable
              style="width: 100%" />
          </n-form-item-gi>

          <n-gi :span="24" class="flex justify-end gap-3">
            <NButton type="primary" @click="fetchNhanVien(1)">
              Tìm kiếm
            </NButton>
            <NButton strong secondary @click="handleResetSearch">
              Reset
            </NButton>
          </n-gi>
        </n-grid>
      </n-form>
    </n-card>

    <!-- Camera Modal -->
    <n-modal v-model:show="cameraVisible" :mask-closable="false" :closable="false" style="width: 90%; max-width: 800px">
      <n-card style="width: 100%" :title="isScanning ? 'Đang xử lý ảnh...' : 'Quét CCCD bằng Camera'" :bordered="false"
        size="huge" role="dialog" aria-modal="true">

        <div class="camera-container">
          <video ref="videoRef" autoplay playsinline class="camera-preview"
            style="width: 100%; max-height: 60vh; background: #000;"></video>
          <canvas ref="canvasRef" style="display: none;"></canvas>

          <div class="camera-controls"
            style="margin-top: 16px; display: flex; flex-direction: column; align-items: center; gap: 16px;">
            <div style="text-align: center; margin-bottom: 16px;">
              <p style="color: #666; margin-bottom: 12px;">Đặt CCCD vào khung hình để quét tự động</p>
            </div>

            <div style="display: flex; gap: 16px;">
              <n-button type="primary" size="large" @click="capturePhoto" :loading="isScanning">
                <template #icon>
                  <n-icon>
                    <CameraIcon />
                  </n-icon>
                </template>
                Chụp ảnh
              </n-button>

              <n-button type="default" size="large" @click="closeCamera">
                Hủy bỏ
              </n-button>
            </div>
          </div>
        </div>
      </n-card>
    </n-modal>

    <!-- Bảng danh sách -->
    <n-card>
      <NSpace vertical size="large">
        <div class="flex gap-4">
          <NButton type="primary" @click="handleAddTable">
            Thêm nhân viên
          </NButton>
          <NButton type="info" @click="handleScanIdCard" :loading="isScanning">
            <template #icon>
              <n-icon>
                <CameraIcon />
              </n-icon>
            </template>
            {{ isScanning ? 'Đang mở camera...' : 'Quét CCCD bằng camera' }}
          </NButton>
        </div>

        <div class="table-container">
          <div class="table-wrapper">
            <n-data-table class="borderless-table" :columns="columns" :data="listData" :loading="loading"
              :row-key="(row: NhanVien) => row.id" :scroll-x="2500" :bordered="false" :single-line="false"
              :single-column="false" :row-props="(row) => ({ class: 'borderless-row' })" />
          </div>
        </div>

        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
          @update:page-size="(size: number) => { pageSize = size; fetchNhanVien(1) }">
          <template #prefix>
            Tổng {{ totalItems }} nhân viên
          </template>
        </n-pagination>

        <NhanVienModal v-model:visible="visible" :type="modalType" :modal-data="modalData"
          :vai-tro-options="vaiTroOptions" :chuc-vu-options="chucVuOptions" :gioi-tinh-options="gioiTinhOptions"
          :tinh-options="tinhOptions" :huyen-options="huyenOptions" :xa-options="xaOptions"
          @refresh="fetchNhanVien(currentPage)" @province-change="handleProvinceChange"
          @district-change="handleDistrictChange" />
      </NSpace>
    </n-card>
  </NSpace>
</template>

<style scoped>
/* Table styles */
:deep(.n-data-table) {
  --n-border-color: #e5e7eb !important;
  --n-merged-border-color: #e5e7eb !important;
  --n-td-color: #ffffff !important;
  --n-th-color: #f9fafb !important;
  --n-th-text-color: #1f2937 !important;
  --n-td-text-color: #1f2937 !important;
  --n-border: 1px solid #e5e7eb !important;
  --n-td-border-color: #e5e7eb !important;
  --n-th-border-color: #e5e7eb !important;
  --n-td-padding: 12px 16px !important;
  --n-th-padding: 12px 16px !important;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

/* Fixed columns background */
:deep(.n-data-table-th--fixed-left),
:deep(.n-data-table-td--fixed-left) {
  background-color: #ffffff !important;
}

:deep(.n-data-table-th--fixed-right),
:deep(.n-data-table-td--fixed-right) {
  background-color: #ffffff !important;
}

/* Remove row borders */
:deep(.n-data-table-tbody .n-data-table-tr) {
  border-bottom: 1px solid #e5e7eb !important;
}

/* Hover effect for rows */
:deep(.n-data-table-tbody .n-data-table-tr:hover) {
  background-color: #f9fafb !important;
  transition: background-color 0.2s;
}

/* Style for the table container */
.table-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}

/* Ensure fixed columns have proper z-index */
:deep(.n-data-table-th--fixed-left),
:deep(.n-data-table-td--fixed-left) {
  z-index: 1;
}

:deep(.n-data-table-th--fixed-right),
:deep(.n-data-table-td--fixed-right) {
  z-index: 1;
}

/* Remove header bottom border */
:deep(.n-data-table .n-data-table-thead) {
  border-bottom: none !important;
}

/* Remove row borders */
:deep(.n-data-table-tbody .n-data-table-tr) {
  border-bottom: none !important;
}

/* Hover effect for rows */
:deep(.n-data-table-tbody .n-data-table-tr:hover) {
  background-color: #f9fafb;
  transition: background-color 0.2s;
}

/* Remove cell borders */
:deep(.n-data-table-td),
:deep(.n-data-table-th) {
  border: none !important;
}

/* Remove last row bottom border */
:deep(.n-data-table-tbody .n-data-table-tr:last-child) {
  border-bottom: none !important;
}

/* Style for the table container */
.table-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
}
</style>

<script setup lang="ts">
import { h, onMounted, onBeforeUnmount, reactive, ref, watch, nextTick } from 'vue'
import axios from 'axios'
import type { DataTableColumns, FormInst } from 'naive-ui'
import { useBoolean } from '@/hooks'
import {
  NButton,
  NInput,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  NTooltip,
  NIcon,
} from 'naive-ui'
import { User, Email, Phone, Calendar, GenderMale, GenderFemale } from '@vicons/carbon'
import NhanVienModal from './components/TableModal.vue'
import { getAllNhanVien, changeNhanVienStatus, changeNhanVienRole } from '@/service/api/nhansu/nhanvien'
import type { NhanVien } from '@/service/api/nhansu/nhanvien'
import { formatFullAddress, fetchProvinces } from '@/utils/addressUtils'
import { Camera as CameraIcon } from '@vicons/fa'
import { Close as CloseIcon } from '@vicons/ionicons5'

const { bool: loading, setTrue: startLoading, setFalse: endLoading } = useBoolean(false)
const { bool: visible, setTrue: openModal, setFalse: closeModal } = useBoolean(false)
const { bool: cameraVisible, setTrue: openCameraModal, setFalse: closeCameraModal } = useBoolean(false)

// Sorting state
const sortBy = ref<string>('')
const sortOrder = ref<'asc' | 'desc'>('asc')

// Form options
const vaiTroOptions = [
  { label: 'Nhân viên', value: 'STAFF' },
  { label: 'Quản lý', value: 'MANAGER' },
]

const chucVuOptions = [
  { label: 'Nhân viên lễ tân', value: 'RECEPTIONIST' },
  { label: 'Quản lý', value: 'MANAGER' },
]

const gioiTinhOptions = [
  { label: 'Nam', value: 'MALE' },
  { label: 'Nữ', value: 'FEMALE' },
  { label: 'Khác', value: 'OTHER' },
]

// Address options
const tinhOptions = ref<Array<{ label: string; value: string }>>([])
const huyenOptions = ref<Array<{ label: string; value: string }>>([])
const xaOptions = ref<Array<{ label: string; value: string }>>([])
const addressData = ref<Array<any>>([])

// Modal state
const modalType = ref<'add' | 'edit'>('add')
const modalData = ref<{ id: string } | null>(null)

const initialModel = {
  tuKhoa: '',
  vaitro: null,
  chucVu: null,
  gioiTinh: null,
  tinh: null,
  huyen: null,
  xa: null,
  trangThai: null,
  ngaySinh: null,
  diaChi: '',
  email: '',
  sdt: '',
  cccd: ''
}

const model = reactive({ ...initialModel })
const formRef = ref<FormInst | null>(null)

const listData = ref<NhanVien[]>([])
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const errorMessage = ref('')
const isMounted = ref(true)

// Cleanup on unmount
onBeforeUnmount(() => {
  isMounted.value = false
})

// Function to get address object by code
async function getAddressObject(code: string, type: 'tinh' | 'huyen' | 'xa') {
  if (!code) return null;

  try {
    // First, get all provinces to find the correct one
    const provincesResponse = await fetch('https://provinces.open-api.vn/api/p/');
    const provinces = await provincesResponse.json();

    // If we're looking for a province, return it directly
    if (type === 'tinh') {
      return provinces.find((p: any) => p.code == code) || null;
    }

    // For district or ward, find the parent first
    const province = provinces.find((p: any) => {
      if (type === 'huyen') {
        return p.districts?.some((d: any) => d.code == code);
      } else if (type === 'xa') {
        return p.districts?.some((d: any) =>
          d.wards?.some((w: any) => w.code == code)
        );
      }
      return false;
    });

    if (!province) return null;

    if (type === 'huyen') {
      // Find the district in the province
      const district = province.districts?.find((d: any) => d.code == code);
      if (district) {
        // If we need to get wards for this district, make sure they're loaded
        if (!district.wards) {
          const districtResponse = await fetch(`https://provinces.open-api.vn/api/d/${code}?depth=2`);
          const districtData = await districtResponse.json();
          return districtData;
        }
        return district;
      }
    } else if (type === 'xa') {
      // Find the ward in the province's districts
      for (const district of province.districts || []) {
        const ward = district.wards?.find((w: any) => w.code == code);
        if (ward) return ward;
      }
    }

    return null;
  } catch (error) {
    console.error(`Error fetching ${type} data:`, error);
    return null;
  }
}

// Function to update address names for a single row
async function updateAddressNames(row: any) {
  try {
    const [tinhName, huyenName, xaName, formattedAddress] = await Promise.all([
      formatFullAddress('', '', '', row.tinh),
      formatFullAddress('', '', row.huyen, row.tinh),
      formatFullAddress('', row.xa, row.huyen, row.tinh),
      formatFullAddress(row.diaChi, row.xa, row.huyen, row.tinh)
    ]);

    // Extract just the name part (remove the full address part)
    const extractName = (full: string) => {
      if (!full) return '';
      const parts = full.split(',').map(p => p.trim());
      return parts[0] || '';
    };

    return {
      ...row,
      tinhName: extractName(tinhName) || row.tinh,
      huyenName: extractName(huyenName) || row.huyen,
      xaName: extractName(xaName) || row.xa,
      formattedAddress
    };
  } catch (error) {
    console.error('Error updating address names:', error);
    return {
      ...row,
      tinhName: row.tinh,
      huyenName: row.huyen,
      xaName: row.xa,
      formattedAddress: [row.diaChi, row.xa, row.huyen, row.tinh].filter(Boolean).join(', ')
    };
  }
}

async function fetchNhanVien(page = 1) {
  if (!isMounted.value) return

  startLoading()
  errorMessage.value = ''
  try {
    const params: any = {
      page,
      size: pageSize.value,
      q: model.tuKhoa,
      sort: sortBy.value ? `${sortBy.value},${sortOrder.value}` : undefined
    }

    const res = await getAllNhanVien(params)

    if (!isMounted.value) return

    // Process the data to include address names
    const processedData = await Promise.all(
      res.items.map((item: any) => updateAddressNames(item))
    );

    listData.value = processedData;
    totalItems.value = res.totalItems || 0
    currentPage.value = res.currentPage || 1
  } catch (error: any) {
    if (!isMounted.value) return

    errorMessage.value = error.message || 'Không thể tải danh sách nhân viên'
    window.$message.error(errorMessage.value)
    listData.value = []
  } finally {
    if (isMounted.value) {
      endLoading()
    }
  }
}

// Debounce search to prevent too many requests
let searchTimeout: number | null = null
const isScanning = ref(false)
watch(
  () => ({ ...model }),
  () => {
    if (searchTimeout) {
      clearTimeout(searchTimeout)
    }
    searchTimeout = window.setTimeout(() => {
      if (isMounted.value) {
        fetchNhanVien(1)
      }
    }, 300)
  },
  { deep: true }
)

function handleEditTable(row: NhanVien) {
  console.log('Edit row data:', row);
  if (!row || !row.id) {
    window.$message.error('Không tìm thấy thông tin nhân viên');
    return;
  }
  modalType.value = 'edit';
  //modalData.value = { ...row }; // Pass the entire row data
  openModal();
}

function handleAddTable() {
  modalType.value = 'add'
  modalData.value = null
  openModal()
}

async function handleChangeStatus(id: string) {
  if (!isMounted.value) return

  try {
    const res = await changeNhanVienStatus(id)
    if (!isMounted.value) return

    window.$message.success(res?.message || 'Thay đổi trạng thái thành công!')
    fetchNhanVien(currentPage.value)
  } catch (error: any) {
    if (!isMounted.value) return
    window.$message.error(error.message || 'Lỗi khi thay đổi trạng thái')
  }
}

async function handleChangeRole(id: string) {
  if (!isMounted.value) return

  try {
    const res = await changeNhanVienRole(id)
    if (!isMounted.value) return

    window.$message.success(res?.message || 'Thay đổi vai trò thành công!')
    fetchNhanVien(currentPage.value)
  } catch (error: any) {
    if (!isMounted.value) return
    window.$message.error(error.message || 'Lỗi khi thay đổi vai trò')
  }
}


function handleResetSearch() {
  Object.assign(model, initialModel)
  fetchNhanVien(1)
}

function changePage(page: number) {
  fetchNhanVien(page)
}

// Format date helper
const formatDate = (dateString: string | Date): string => {
  if (!dateString) return '--';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  });
};

function handleSort(key: string) {
  if (sortBy.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = key;
    sortOrder.value = 'asc';
  }
  fetchNhanVien(currentPage.value);
}

const columns: DataTableColumns<NhanVien> = [
  {
    align: 'center',
    key: 'status',
    width: 100,
    render: (row) => {
      return h(NTag, { type: row.trangThai === 'ACTIVE' ? 'success' : 'error' }, { default: () => row.trangThai === 'ACTIVE' ? 'Hoạt động' : 'Ngừng hoạt động' })
    },
  },
  {
    title: 'Thao tác',
    align: 'center',
    key: 'actions',
    fixed: 'right',
    width: 180,
    render: (row) => {
      return h(NSpace, { justify: 'center', wrap: false }, {
        default: () => [
          h(NButton, {
            text: true,
            type: 'primary',
            onClick: () => handleEditTable(row)
          }, { default: () => 'Sửa' }),
          h(NButton, {
            text: true,
            type: 'error',
            onClick: () => handleChangeStatus(row.id)
          }, { default: () => row.trangThai === 'ACTIVE' ? 'Khóa' : 'Mở khóa' }),
          h(NButton, {
            text: true,
            type: 'warning',
            onClick: () => handleChangeRole(row.id)
          }, { default: () => 'Đổi vai trò' })
        ]
      })
    }
  }
]

// Fetch districts based on selected province
const fetchHuyen = (provinceCode: string) => {
  const province = addressData.value.find((p: any) => p.code === provinceCode)
  if (province && province.districts) {
    huyenOptions.value = province.districts.map((d: any) => ({
      label: d.name,
      value: d.code
    }))
  } else {
    huyenOptions.value = []
  }
  // Clear xa options when province changes
  xaOptions.value = []
}

// Fetch wards based on selected district
const fetchXa = async (districtCode: string, provinceCode: string) => {
  if (!districtCode || !provinceCode) {
    xaOptions.value = [];
    return;
  }

  try {
    // Fetch the district details which includes its wards
    const response = await axios.get(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
    
    if (response.data && response.data.wards && response.data.wards.length > 0) {
      xaOptions.value = response.data.wards.map((ward: any) => ({
        label: ward.name,
        value: ward.code.toString()
      }));
    } else {
      xaOptions.value = [];
    }
  } catch (error) {
    console.error('Error fetching wards:', error);
    xaOptions.value = [];
  }
}

// Handle province selection change from modal
const handleProvinceChange = (provinceCode: string) => {
  fetchHuyen(provinceCode);
};

// Handle district selection change from modal
const handleDistrictChange = (districtCode: string) => {
  if (districtCode) {
    // Get the province code from the form model
    const provinceCode = model.tinh || modalData.value?.tinh;
    if (provinceCode) {
      fetchXa(districtCode, provinceCode);
    } else {
      console.error('No province code found');
      xaOptions.value = [];
    }
  } else {
    xaOptions.value = [];
  }
};
// In Vue 3 with <script setup>, all top-level bindings are automatically available in the template

// Watch for province changes in the search form
watch(() => model.tinh, (newVal) => {
  if (newVal) {
    model.huyenName = ''
    model.xaName = ''
    fetchHuyen(newVal)
  } else {
    huyenOptions.value = []
    xaOptions.value = []
  }
})

// Watch for district changes
watch(() => model.huyen, (newVal) => {
  if (newVal) {
    model.xaName = ''
    fetchXa(newVal)
  } else {
    xaOptions.value = []
  }
})

// Fetch address data
const fetchAddressData = async () => {
  try {
    // Use the fetchProvinces function from addressUtils
    const provinces = await fetchProvinces();
    
    // Map the provinces to the format expected by the select component
    tinhOptions.value = provinces.map(province => ({
      label: province.type === 'thanh-pho' ? `Thành phố ${province.name}` : `Tỉnh ${province.name}`,
      value: province.code
    }));
    
    // Cache the data for offline use
    localStorage.setItem('provinces', JSON.stringify(tinhOptions.value));
  } catch (error) {
    console.error('Error fetching provinces:', error);
    
    // If there's an error, try to load from localStorage
    const cached = localStorage.getItem('provinces');
    if (cached) {
      tinhOptions.value = JSON.parse(cached);
    } else {
      // Fallback to hardcoded new administrative divisions if API and cache fail
      tinhOptions.value = [
        // 6 centrally run cities
        { label: 'Thành phố Hà Nội', value: '01' },
        { label: 'Thành phố Hồ Chí Minh', value: '79' },
        { label: 'Thành phố Hải Phòng', value: '31' },
        { label: 'Thành phố Đà Nẵng', value: '48' },
        { label: 'Thành phố Huế', value: '46' },
        { label: 'Thành phố Cần Thơ', value: '92' },
        
        // 28 provinces
        // Unchanged provinces
        { label: 'Tỉnh Lai Châu', value: '12' },
        { label: 'Tỉnh Điện Biên', value: '11' },
        { label: 'Tỉnh Sơn La', value: '14' },
        { label: 'Tỉnh Lạng Sơn', value: '20' },
        { label: 'Tỉnh Quảng Ninh', value: '22' },
        { label: 'Tỉnh Thanh Hóa', value: '38' },
        { label: 'Tỉnh Nghệ An', value: '40' },
        { label: 'Tỉnh Hà Tĩnh', value: '42' },
        { label: 'Tỉnh Cao Bằng', value: '04' },
        
        // Merged provinces
        { label: 'Tỉnh Tuyên Quang', value: '08' },
        { label: 'Tỉnh Lào Cai', value: '10' },
        { label: 'Tỉnh Thái Nguyên', value: '19' },
        { label: 'Tỉnh Phú Thọ', value: '25' },
        { label: 'Tỉnh Bắc Ninh', value: '27' },
        { label: 'Tỉnh Hưng Yên', value: '33' },
        { label: 'Tỉnh Ninh Bình', value: '37' },
        { label: 'Tỉnh Quảng Trị', value: '45' },
        { label: 'Tỉnh Quảng Ngãi', value: '51' },
        { label: 'Tỉnh Gia Lai', value: '64' },
        { label: 'Tỉnh Khánh Hòa', value: '56' },
        { label: 'Tỉnh Lâm Đồng', value: '68' },
        { label: 'Tỉnh Đắk Lắk', value: '66' },
        { label: 'Tỉnh Đồng Nai', value: '75' },
        { label: 'Tỉnh Tây Ninh', value: '72' },
        { label: 'Tỉnh Vĩnh Long', value: '86' },
        { label: 'Tỉnh Đồng Tháp', value: '87' },
        { label: 'Tỉnh Cà Mau', value: '96' },
        { label: 'Tỉnh An Giang', value: '89' }
      ];
    }
  }
}

onMounted(() => {
  fetchNhanVien();
  fetchAddressData();
})
const fileInput = ref<HTMLInputElement | null>(null)

// Handle ID card scan button click
// Refs for camera and auto-capture
const videoRef = ref<HTMLVideoElement | null>(null)
const streamRef = ref<MediaStream | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const analysisInterval = ref<number | null>(null)
const lastCaptureTime = ref<number>(0)
const captureCooldown = 3000 // 3 seconds cooldown between captures

// Analyze video frame for ID card detection
const analyzeFrame = () => {
  if (!videoRef.value || !canvasRef.value) return false

  const video = videoRef.value
  const canvas = canvasRef.value
  // Optimize canvas for frequent read operations
  const context = canvas.getContext('2d', { willReadFrequently: true })
  if (!context) return false

  // Set canvas dimensions to match video
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight

  // Draw current video frame to canvas
  context.drawImage(video, 0, 0, canvas.width, canvas.height)

  // Get image data for analysis
  const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
  const data = imageData.data

  // Simple edge detection (count non-black pixels as potential edges)
  let edgeCount = 0
  for (let i = 0; i < data.length; i += 4) {
    // Check if pixel is not black (R+G+B > 50)
    if (data[i] + data[i + 1] + data[i + 2] > 50) {
      edgeCount++
    }
  }

  // If we have a good amount of edges, consider it a potential ID card
  const edgeThreshold = (canvas.width * canvas.height) * 0.3 // 30% of pixels
  return edgeCount > edgeThreshold
}

// Auto-capture when ID card is detected
const checkAndCapture = () => {
  if (Date.now() - lastCaptureTime.value < captureCooldown) return

  const isCardDetected = analyzeFrame()
  if (isCardDetected) {
    lastCaptureTime.value = Date.now()
    capturePhoto()
  }
}

// Handle ID card scan button click
const handleScanIdCard = async () => {
  try {
    openCameraModal()
    await nextTick() // Wait for the modal to be rendered

    // Request camera access
    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        width: { ideal: 1280 },
        height: { ideal: 720 },
        facingMode: 'environment' // Use back camera by default
      }
    })

    if (videoRef.value) {
      videoRef.value.srcObject = stream
      streamRef.value = stream
      await videoRef.value.play()

      // Start auto-capture analysis
      analysisInterval.value = window.setInterval(checkAndCapture, 500) // Check every 500ms
    }
  } catch (error) {
    console.error('Error accessing camera:', error)
    window.$message.error('Không thể truy cập camera. Vui lòng kiểm tra quyền truy cập.')
    closeCameraModal()
  }
}

// Capture photo from camera
const capturePhoto = async () => {
  if (!videoRef.value || !canvasRef.value) return

  try {
    const video = videoRef.value
    const canvas = canvasRef.value
    const context = canvas.getContext('2d')

    if (!context) return

    // Set canvas dimensions to match video
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight

    // Draw current video frame to canvas
    context.drawImage(video, 0, 0, canvas.width, canvas.height)

    // Convert canvas to blob
    const blob = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.8)
    })

    if (!blob) {
      throw new Error('Failed to capture image')
    }

    // Create file from blob
    const file = new File([blob], 'id-card.jpg', { type: 'image/jpeg' })

    // Process the captured image
    await processIdCardImage(file)

  } catch (error) {
    console.error('Error capturing photo:', error)
    window.$message.error('Có lỗi khi chụp ảnh. Vui lòng thử lại.')
  }
}

// Process ID card image (either from file or camera)
const processIdCardImage = async (file: File) => {
  try {
    isScanning.value = true

    // Show loading state
    if (window.$loading && typeof window.$loading.start === 'function') {
      window.$loading.start()
    }

    // Send the image to your backend for OCR processing
    const formData = new FormData()
    formData.append('image', file)

    // Use the correct API endpoint - adjust the base URL if needed
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:2345'
    const endpoint = `${baseUrl}/api/v1/scan-id-card`

    console.log('Sending request to:', endpoint)

    try {
      const response = await axios.post(endpoint, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Accept': 'application/json'
        },
        timeout: 30000, // 30 seconds timeout
        withCredentials: true // Include credentials if needed
      })

      // Process the response and fill the form
      if (response.data) {
        const idCardData = response.data

        // Check if we got valid data
        if (!idCardData.fullName && !idCardData.idNumber) {
          window.$message.warning('Không tìm thấy thông tin CCCD. Vui lòng thử lại.')
          isScanning.value = false
          return
        }

        // Map ID card data to form fields
        model.value = {
          ...model.value,
          ten: idCardData.fullName || '',
          cccd: idCardData.idNumber || '',
          ngaySinh: idCardData.dateOfBirth ? new Date(idCardData.dateOfBirth).toISOString().split('T')[0] : '',
          gioiTinh: idCardData.gender === 'Nam' ? 'MALE' :
            idCardData.gender === 'Nữ' ? 'FEMALE' : 'OTHER',
          diaChi: idCardData.address || '',
        }

        // If you have address components, you might need to handle them separately
        if (idCardData.province) {
          const province = tinhOptions.value.find(p =>
            p.label.includes(idCardData.province)
          )
          if (province) {
            model.value.tinh = province.value
            await handleTinhChange(province.value)
          }
        }

        window.$message.success('Đã cập nhật thông tin từ CCCD')
        // Only close the camera after successful scan
        closeCamera()
      }
    } catch (error) {
      console.error('API Request Error:', {
        url: endpoint,
        error: error.response ? error.response.data : error.message,
        status: error.response?.status,
        code: error.code
      })

      // More specific error messages
      if (error.code === 'ERR_NETWORK') {
        window.$message.error(`Không thể kết nối đến máy chủ (${endpoint}). Vui lòng kiểm tra:
          1. Máy chủ backend đã chạy chưa?
          2. Đường dẫn API có chính xác không?
          3. Có vấn đề gì với kết nối mạng không?`)
      } else if (error.response?.status === 404) {
        window.$message.error(`Không tìm thấy API: ${endpoint}. Vui lòng kiểm tra lại đường dẫn.`)
      } else if (error.response?.status === 500) {
        window.$message.error('Lỗi máy chủ. Vui lòng kiểm tra logs của backend.')
      } else {
        window.$message.error(`Lỗi không xác định: ${error.message || 'Vui lòng thử lại sau'}`)
      }

      throw error
    }
  } catch (error) {
    console.error('Error processing ID card:', error)
    window.$message.error('Không thể đọc thông tin từ CCCD. Vui lòng thử lại hoặc nhập thủ công.')
    isScanning.value = false
  } finally {
    // Hide loading
    if (window.$loading?.finish) {
      window.$loading.finish()
    }
  }
}

// Close camera and clean up
const closeCamera = () => {
  // Clear analysis interval
  if (analysisInterval.value) {
    clearInterval(analysisInterval.value)
    analysisInterval.value = null
  }

  // Stop camera stream
  if (streamRef.value) {
    streamRef.value.getTracks().forEach(track => track.stop())
    streamRef.value = null
  }

  closeCameraModal()
}


// Handle file upload
const handleFileUpload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return

  const file = input.files[0]
  if (!file) return

  await processIdCardImage(file)

  // Reset file input
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

</script>
