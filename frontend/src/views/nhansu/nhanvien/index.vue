<script setup lang="ts">
import { h, ref, reactive, onMounted, watch } from "vue";
import type { DataTableColumns, FormInst, DataTableRowKey } from "naive-ui";
import {
  NButton,
  NInput,
  NSelect,
  NSpace,
  NTag,
  NDataTable,
  NPagination,
  NCard,
  NAlert,
  NForm,
  NGrid,
  NPopconfirm,
  useMessage,
  NIcon,
  NFormItemGi,
  NGi
} from "naive-ui";
import {

} from "@vicons/fa";
import {
  Email,
  Location,
  User,
  Calendar,
  Identification,
  UserRole,
  Phone,
  Barcode
} from "@vicons/carbon";
import TableModal from "./components/TableModal.vue";
import {
  getAllNhanVien,
  deleteNhanVien,
  changeNhanVienStatus,
  type NhanVien,
} from "@/service/api/nhansu/nhanvien";
import { Html5Qrcode } from "html5-qrcode";
import { parseCCCDQrContent } from "@/utils/cccd";
import { getProvinces, getWards, convertFullAddress } from '@/service/api/address';

// ---------------------- Types ----------------------

interface SearchParams {
  tuKhoa?: string;
  vaiTro?: string;
  chucVu?: string;
  gioiTinh?: string;
  page?: number;
  size?: number;
  orderBy?: string;
  sortBy?: string;
  sort?: string;
  order?: "asc" | "desc";
  q?: string;
  tinh?: string;
  xa?: string;
  trangThai?: string;
}

// ---------------------- State ----------------------
const loading = ref(false);
const listData = ref<NhanVien[]>([]);
const totalItems = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const errorMessage = ref("");
const message = useMessage();

// Form model for search
const initialModel = {
  tuKhoa: "",
  vaiTro: "",
  gioiTinh: "",
  chucVu: "",
  tinh: "",
  xa: "",
  trangThai: ""
};

const model = reactive({ ...initialModel });
const formRef = ref<FormInst | null>(null);

// Modal state
const modalVisible = ref(false);
const modalType = ref<"add" | "edit">("add");
const modalData = ref<NhanVien | null>(null);

// Address options
const tinhOptions = ref<Array<{ label: string; value: string }>>([]);
const xaOptions = ref<Array<{ label: string; value: string }>>([]);

// Expanded rows
const expandedRowKeys = ref<DataTableRowKey[]>([]);

// Options
const vaiTroOptions = [
  { label: 'Nhân viên', value: 'NHAN_VIEN' },
  { label: 'Quản lý', value: 'QUAN_LY' }
]

const gioiTinhOptions = [
  { label: 'Nam', value: 'true' },
  { label: 'Nữ', value: 'false' }
]

// ---------------------- Functions ----------------------

function formatDate(date: any) {
  if (!date) return ''
  return new Date(date).toLocaleDateString('vi-VN')
}

async function handleModalProvinceChange(provinceCode: string) {
  if (!provinceCode) {
    xaOptions.value = []
    return
  }
  try {
    const wards = await getWards(provinceCode)
    xaOptions.value = wards.map((w: any) => ({
      label: w.ward_name,
      value: w.ward_code
    }))
  } catch (e) {
    console.error(e)
    message.error('Không thể tải danh sách xã/phường')
  }
}

async function processIdCardImage(file: File) {
  try {
    loading.value = true;
    // Ensure element exists
    if (!document.getElementById("reader-hidden-index")) {
      const div = document.createElement("div");
      div.id = "reader-hidden-index";
      div.style.display = "none";
      document.body.appendChild(div);
    }

    const html5QrCode = new Html5Qrcode("reader-hidden-index");
    const decodedText = await html5QrCode.scanFile(file, true);
    html5QrCode.clear();

    const parsed = parseCCCDQrContent(decodedText);

    if (parsed) {
      // Map to NhanVien format
      const nvData: any = {
        ten: parsed.ten,
        cccd: parsed.cccd,
        ngaySinh: parsed.ngaySinh ? new Date(parsed.ngaySinh) : undefined,
        gioiTinh: parsed.gioiTinh, // 'MALE' or 'FEMALE'
        diaChi: parsed.diaChi,
        tinh: '',
        xa: ''
      };

      // Try to match province using API
      const scannedAddress = parsed.diaChi;
      console.log('Converting address:', scannedAddress);

      try {
        const converted = await convertFullAddress(scannedAddress);

        if (converted) {
          console.log('Converted Address:', converted);

          nvData.tinh = converted.tinh_code;
          nvData.diaChi = converted.dia_chi_duong;

          // Fetch wards so they are available when modal opens
          await handleModalProvinceChange(converted.tinh_code);

          // Set ward if available
          if (converted.xa_code) {
            nvData.xa = converted.xa_code;
          }

        } else {
          message.warning(`Không thể tự động phân tích địa chỉ: ${scannedAddress}`);
        }
      } catch (err) {
        console.error('Error converting address:', err);
      }

      modalType.value = "add";
      modalData.value = nvData;
      modalVisible.value = true;
      message.success("Đã quét thông tin từ ảnh");
    } else {
      message.warning("Không tìm thấy mã QR hợp lệ");
    }
  } catch (e) {
    console.error(e);
    message.error("Không thể đọc mã QR từ ảnh");
  } finally {
    loading.value = false;
  }
}

function handleAddTable() {
  modalType.value = 'add'
  modalData.value = null
  modalVisible.value = true
}

function handleFileUpload(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processIdCardImage(file)
  }
  target.value = ''
}

function handleResetSearch() {
  model.tuKhoa = ''
  model.vaiTro = ''
  model.gioiTinh = ''
  model.tinh = ''
  model.xa = ''
  fetchNhanVien(1)
}

function changePage(page: number) {
  currentPage.value = page
  fetchNhanVien(page)
}

async function openEditModal(row: NhanVien) {
  modalType.value = 'edit'
  modalData.value = { ...row }
  if (row.tinh) {
    await handleModalProvinceChange(row.tinh)
  }
  modalVisible.value = true
}

async function handleChangeStatusAction(row: NhanVien) {
  if (!row.id) return
  try {
    await changeNhanVienStatus(row.id)
    message.success('Đổi trạng thái thành công')
    fetchNhanVien(currentPage.value)
  } catch (e: any) {
    message.error(e.message || 'Lỗi khi đổi trạng thái')
  }
}

function handleRowClick(row: NhanVien) {
  if (!row.id) return
  const index = expandedRowKeys.value.indexOf(row.id)
  if (index > -1) {
    expandedRowKeys.value.splice(index, 1)
  } else {
    expandedRowKeys.value.push(row.id)
  }
}

// ---------------------- Fetch ----------------------
async function fetchNhanVien(page = 1) {
  try {
    loading.value = true;
    errorMessage.value = "";

    const params: SearchParams = {
      page: page,
      size: pageSize.value,
      q: model.tuKhoa || undefined,
      tuKhoa: model.tuKhoa || undefined,
      vaiTro: model.vaiTro || undefined,
      chucVu: model.chucVu || undefined,
      gioiTinh: model.gioiTinh || undefined,
      tinh: model.tinh || undefined,
      xa: model.xa || undefined,
      trangThai: model.trangThai || undefined,
      orderBy: "desc",
      sortBy: "createdDate",
    };

    const response = await getAllNhanVien(params);

    if (response && response.data) {
      listData.value = (response.data.data as NhanVien[]) || [];
      totalItems.value = response.data.totalElements || 0;
      currentPage.value = response.data.currentPage + 1;
    } else {
      listData.value = [];
      totalItems.value = 0;
    }
  } catch (error: any) {
    console.error("Error fetching nhân viên:", error);
    errorMessage.value =
      error.message || "Có lỗi xảy ra khi tải dữ liệu nhân viên";
    message.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
}

// ---------------------- Watchers ----------------------
watch(
  () => model.tinh,
  (newVal) => {
    if (newVal) {
      model.xa = "";
    } else {
      xaOptions.value = [];
    }
  }
);

let searchTimeout: number | null = null;
watch(
  [
    () => model.tuKhoa,
    () => model.vaiTro,
    () => model.chucVu,
    () => model.gioiTinh,
    () => model.tinh,
    () => model.xa,
    () => model.trangThai,
  ],
  () => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }

    searchTimeout = window.setTimeout(() => {
      fetchNhanVien(1);
    }, 500);
  },
  { deep: true }
);

// ---------------------- Table columns ----------------------
const columns: DataTableColumns<NhanVien> = [
  {
    type: 'expand',
    renderExpand: (row) => {
      return h(
        'div',
        { style: 'padding: 20px 32px; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-radius: 8px; margin: 8px 0;' },
        [
          h('div', { style: 'display: grid; grid-template-columns: repeat(2, minmax(0, max-content)); gap: 32px; justify-content: start;' }, [
            // Column 1
            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#8b5cf6' }, { default: () => h(Email) }),
                  h('span', 'Email:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.email || '--')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#ec4899' }, { default: () => h(Location) }),
                  h('span', 'Địa chỉ:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.diaChi || '--')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#10b981' }, { default: () => h(User) }),
                  h('span', 'Giới tính:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.gioiTinh === true ? 'Nam' : row.gioiTinh === false ? 'Nữ' : 'Khác')
              ]),
            ]),

            // Column 2
            h('div', { style: 'display: flex; flex-direction: column; gap: 12px;' }, [
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#f59e0b' }, { default: () => h(Calendar) }),
                  h('span', 'Ngày sinh:')
                ]),
                h('span', { style: 'color: #6b7280;' }, formatDate(row.ngaySinh) || '--')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#06b6d4' }, { default: () => h(Identification) }),
                  h('span', 'CCCD:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.cccd || '--')
              ]),
              h('div', { style: 'display: flex; align-items: center; gap: 12px;' }, [
                h('div', { style: 'display: flex; align-items: center; gap: 8px; font-weight: 600; color: #374151; min-width: 150px;' }, [
                  h(NIcon, { size: 18, color: '#3b82f6' }, { default: () => h(UserRole) }),
                  h('span', 'Vai trò:')
                ]),
                h('span', { style: 'color: #6b7280;' }, row.vaitro === 'NHAN_VIEN' ? 'Nhân viên' : row.vaitro === 'QUAN_LY' ? 'Quản lý' : row.vaitro || '--')
              ]),
            ])
          ])
        ]
      )
    }
  },
  {
    title: "Mã NV",
    key: "ma",
    align: "center",
    width: 120,
    render: (row) => {
      return h(
        'div',
        { style: 'display: flex; align-items: center; gap: 8px; justify-content: center;' },
        [
          h(NIcon, { size: 16, color: '#667eea' }, { default: () => h(Barcode) }),
          h('span', { style: 'font-weight: 500;' }, row.ma || "--")
        ]
      )
    }
  },
  {
    title: "Tên",
    key: "ten",
    align: "center",
    render: (row) => {
      return h('span', { style: 'font-weight: 600; color: #374151;' }, row.ten || "--")
    }
  },
  {
    title: "Số điện thoại",
    key: "sdt",
    align: "center",
    width: 150,
    render: (row) => {
      return h(
        'div',
        { style: 'display: flex; align-items: center; gap: 8px; justify-content: center;' },
        [
          h(NIcon, { size: 14, color: '#10b981' }, { default: () => h(Phone) }),
          h('span', row.sdt || "--")
        ]
      )
    }
  },
  {
    title: 'Trạng thái',
    align: "center",
    key: 'status',
    width: 150,
    render: row => {
      const map: Record<string, { label: string; type: 'success' | 'warning' | 'error' | 'info' }> = {
        ACTIVE: { label: 'Hoạt động', type: 'success' },
        INACTIVE: { label: 'Ngưng hoạt động', type: 'error' }
      }
      const st = map[row.status as string] || { label: row.status || '-', type: 'info' }
      return h(NTag, { type: st.type as any, bordered: false, round: true }, { default: () => st.label })
    },
  },
  {
    title: "Thao tác",
    key: "actions",
    align: "center",
    width: 180,
    fixed: "right",
    render: (row) =>
      h(
        NSpace,
        { justify: "center" },
        {
          default: () => [
            h(
              NButton,
              {
                size: "small",
                type: "primary",
                onClick: (e) => {
                  e.stopPropagation();
                  openEditModal(row);
                },
              },
              { default: () => "Sửa" }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleChangeStatusAction(row),
                positiveText: "Xác nhận",
                negativeText: "Hủy",
              },
              {
                default: () => `Bạn có chắc muốn ${row.status === 'ACTIVE' ? 'ngưng hoạt động' : 'kích hoạt'} nhân viên này?`,
                trigger: () =>
                  h(
                    NButton,
                    {
                      size: "small",
                      type: row.status === 'ACTIVE' ? "warning" : "success",
                      onClick: (e) => e.stopPropagation()
                    },
                    { default: () => "Đổi trạng thái" }
                  ),
              }
            ),
          ],
        }
      ),
  },
];

// ---------------------- Mounted ----------------------
onMounted(async () => {
  // Initial data fetch
  await fetchNhanVien();

  // Load provinces
  try {
    const provinces = await getProvinces();
    tinhOptions.value = provinces.map((province) => ({
      label: province.name,
      value: province.province_code
    }));
  } catch (error) {
    console.error("Error fetching provinces:", error);
    message.error("Không thể tải danh sách tỉnh/thành phố");
  }
});
</script>

<template>
  <NSpace vertical size="large">
    <n-card>
      <n-form ref="formRef" :model="model" label-placement="left" :show-feedback="false">
        <n-grid :cols="24" :x-gap="12" :y-gap="8">
          <n-form-item-gi :span="8" label="Tên" path="tuKhoa">
            <NInput v-model:value="model.tuKhoa" placeholder="Nhập tên, email hoặc CCCD" clearable
              @keyup.enter="fetchNhanVien(1)" @update:value="() => fetchNhanVien(1)" />
          </n-form-item-gi>

          <n-form-item-gi :span="6" label="Vai trò" path="vaiTro">
            <NSelect v-model:value="model.vaiTro" :options="vaiTroOptions" placeholder="Chọn vai trò" clearable
              filterable style="width: 100%" @update:value="() => fetchNhanVien(1)" />
          </n-form-item-gi>

          <n-form-item-gi :span="5" label="Giới tính" path="gioiTinh">
            <NSelect v-model:value="model.gioiTinh" :options="gioiTinhOptions" placeholder="Chọn giới tính" clearable
              style="width: 100%" @update:value="() => fetchNhanVien(1)" />
          </n-form-item-gi>

          <n-gi :span="5" class="flex items-center">
            <NButton strong secondary @click="handleResetSearch">
              <template #icon>
              </template>
              Làm mới
            </NButton>
          </n-gi>
        </n-grid>
      </n-form>
    </n-card>

    <n-card>
      <div class="flex gap-4 mb-3">
        <NButton type="primary" @click="handleAddTable">Thêm nhân viên</NButton>
        <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleFileUpload" />
      </div>

      <n-alert v-if="errorMessage && listData.length === 0" type="warning" :title="errorMessage" />

      <n-data-table :columns="columns" :data="listData" :loading="loading" :row-key="(row: NhanVien) => row.id || ''"
        :expanded-row-keys="expandedRowKeys"
        @update:expanded-row-keys="(keys: DataTableRowKey[]) => expandedRowKeys = keys" :row-props="(row: NhanVien) => {
          return {
            style: 'cursor: pointer;',
            onClick: () => handleRowClick(row)
          }
        }" />

      <div class="mt-4 flex justify-end">
        <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)" :page-size="pageSize"
          show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage" @update:page-size="
            (size) => {
              pageSize = size;
              fetchNhanVien(1);
            }
          ">
          <template #prefix>Tổng {{ totalItems }} nhân viên</template>
        </n-pagination>
      </div>
    </n-card>

    <TableModal v-model:visible="modalVisible" :type="modalType" :modal-data="modalData"
      :vai-tro-options="vaiTroOptions" :tinh-options="tinhOptions" :xa-options="xaOptions"
      @refresh="fetchNhanVien(currentPage)" @province-change="handleModalProvinceChange" />
  </NSpace>
</template>


<style scoped>
/* Tăng font size cho toàn bộ trang nhân viên */
:deep(.n-card-header__main) {
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

:deep(.n-data-table-th),
:deep(.n-data-table-td) {
  font-size: 17px;
}

:deep(.n-data-table-th__title) {
  font-size: 17px;
  font-weight: 600;
}

:deep(.n-tag) {
  font-size: 17px;
}

:deep(.n-pagination) {
  font-size: 17px;
}

:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-alert) {
  font-size: 17px;
}

/* Icon và text trong expand row */
:deep(.n-data-table__expand) span {
  font-size: 17px;
}

/* Popconfirm text */
:deep(.n-popconfirm__body) {
  font-size: 17px;
}

:deep(.n-popconfirm__action) .n-button__content {
  font-size: 17px;
}
</style>
