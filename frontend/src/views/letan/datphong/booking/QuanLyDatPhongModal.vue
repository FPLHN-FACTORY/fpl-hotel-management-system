<script setup lang="ts">
import { ref, computed, watch, h, onMounted } from 'vue'
import {
  saveDichVu,
  deleteDichVu,
  getDichVuByRoomBooking,
  getDichVuByBooking,
  type DichVuPhatSinh
} from '@/service/api/letan/incurredService'
import { getAllActiveDichVu, type DichVuResponse } from '@/service/api/letan/dichvu'
import { useMessage, useDialog, NButton, NTag, NRadioGroup, NRadioButton } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { checkout } from '@/service/api/letan/booking'
import ThemKhachDangLuuTru from './ThemKhachDangLuuTru.vue'
import ChiPhiLuuTruModal from './ChiPhiLuuTruModal.vue'
import ThanhToanModal from './ThanhToanModal.vue'
import { getDoanByBooking } from '@/service/api/letan/doanluutru'

interface Props {
  visible: boolean
  bookingDetails: {
    id: string // idChiTietDatPhong (Room Booking ID)
    roomId: string
    roomName: string
    customerName?: string
    status: string
    phieuDatPhongId?: string
  } | null
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'success'): void // Refresh parent if needed
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const message = useMessage()
const dialog = useDialog()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const activeTab = ref('services')
const serviceScope = ref<'ROOM' | 'GROUP'>('ROOM')
const servicesList = ref<DichVuPhatSinh[]>([])
const loading = ref(false)

// Danh sách dịch vụ có sẵn
const availableServices = ref<DichVuResponse[]>([])
const serviceOptions = computed(() => {
  return availableServices.value.map(dv => ({
    label: `${dv.tenDichVu} - ${dv.donGia.toLocaleString('vi-VN')} VNĐ/${dv.donViTinh}`,
    value: dv.id,
    donGia: dv.donGia,
    tenDichVu: dv.tenDichVu,
  }))
})

// Form Add Service
const showAddServiceModal = ref(false)
const newService = ref({
  idDichVu: null as string | null,
  tenDichVu: '',
  soLuong: 1,
  donGia: 0,
})

// Guest addition during stay
const showAddGuestModal = ref(false)
const showCostModal = ref(false)
const showPaymentModal = ref(false)
const idDoan = ref<string>('')
const isActivelystaying = computed(() => {
  return props.bookingDetails?.status === 'DANG_SU_DUNG' || 
         props.bookingDetails?.status === 'SAP_TRA' ||
         props.bookingDetails?.status === 'QUA_GIO_TRA'
})

const totalServiceCost = computed(() => {
  return servicesList.value.reduce((sum, item) => sum + item.thanhTien, 0)
})

const isPaid = computed(() => {
  return props.bookingDetails?.status === 'CHECKOUT'
})

const columns: DataTableColumns<DichVuPhatSinh> = [
  { title: 'Tên dịch vụ', key: 'tenDichVu' },
  { title: 'Số lượng', key: 'soLuong' },
  {
    title: 'Đơn giá',
    key: 'donGia',
    render: (row) => row.donGia.toLocaleString('vi-VN') + ' VNĐ'
  },
  {
    title: 'Thành tiền',
    key: 'thanhTien',
    render: (row) => row.thanhTien.toLocaleString('vi-VN') + ' VNĐ'
  },
  {
    title: 'Thao tác',
    key: 'action',
    render: (row) => {
      return h(NButton, {
        size: 'small',
        type: 'error',
        disabled: isPaid.value,
        onClick: () => handleDeleteService(row)
      }, { default: () => 'Xóa' })
    }
  }
]

const summary = (pageData: DichVuPhatSinh[]) => {
  return {
    tenDichVu: {
      value: h(
        'span',
        { style: { fontWeight: 'bold' } },
        'Tổng cộng'
      ),
      colSpan: 3
    },
    thanhTien: {
      value: h(
        'span',
        { style: { fontWeight: 'bold', color: 'red' } },
        totalServiceCost.value.toLocaleString('vi-VN') + ' VNĐ'
      ),
      colSpan: 1
    }
  }
}

async function loadServices() {
  if (!props.bookingDetails?.id) return
  loading.value = true
  try {
    let res: DichVuPhatSinh[] = []
    if (serviceScope.value === 'ROOM') {
      res = await getDichVuByRoomBooking(props.bookingDetails.id)
    } else if (serviceScope.value === 'GROUP' && props.bookingDetails.phieuDatPhongId) {
      res = await getDichVuByBooking(props.bookingDetails.phieuDatPhongId)
    }
    servicesList.value = res
  } catch (error: any) {
    message.error(error.message || 'Lỗi tải dịch vụ')
  } finally {
    loading.value = false
  }
}

async function loadAvailableServices() {
  try {
    const res = await getAllActiveDichVu()
    availableServices.value = res.data || []
  } catch (error: any) {
    console.error('Failed to load services:', error)
    message.warning('Không thể tải danh sách dịch vụ')
  }
}

// Watch để tự động điền giá khi chọn dịch vụ
watch(() => newService.value.idDichVu, (selectedId) => {
  if (selectedId) {
    const selected = serviceOptions.value.find(opt => opt.value === selectedId)
    if (selected) {
      newService.value.tenDichVu = selected.tenDichVu
      newService.value.donGia = selected.donGia
    }
  } else {
    newService.value.tenDichVu = ''
    newService.value.donGia = 0
  }
})

async function handleAddService() {
  if (!newService.value.idDichVu && !newService.value.tenDichVu) {
    message.warning('Vui lòng chọn hoặc nhập tên dịch vụ')
    return
  }
  if (newService.value.soLuong <= 0) {
    message.warning('Số lượng phải lớn hơn 0')
    return
  }
  if (newService.value.donGia < 0) {
    message.warning('Đơn giá không được âm')
    return
  }

  try {
    loading.value = true
    const payload: any = {
      tenDichVu: newService.value.tenDichVu,
      soLuong: newService.value.soLuong,
      donGia: newService.value.donGia,
    }

    if (serviceScope.value === 'ROOM') {
      payload.idChiTietDatPhong = props.bookingDetails?.id
    } else {
      payload.idPhieuDatPhong = props.bookingDetails?.phieuDatPhongId
    }

    await saveDichVu(payload)
    message.success('Thêm dịch vụ thành công')
    showAddServiceModal.value = false
    newService.value = { idDichVu: null, tenDichVu: '', soLuong: 1, donGia: 0 }
    loadServices()
  } catch (error: any) {
    message.error(error.message || 'Lỗi thêm dịch vụ')
  } finally {
    loading.value = false
  }
}

function handleDeleteService(row: DichVuPhatSinh) {
  dialog.warning({
    title: 'Xác nhận xóa',
    content: `Bạn có chắc chắn muốn xóa dịch vụ "${row.tenDichVu}"?`,
    positiveText: 'Xóa',
    negativeText: 'Hủy',
    onPositiveClick: async () => {
      try {
        await deleteDichVu(row.id)
        message.success('Đã xóa dịch vụ')
        loadServices()
      } catch (error: any) {
        message.error(error.message || 'Lỗi xóa dịch vụ')
      }
    }
  })
}


// ... previous code ...

async function handleCheckout() {
  dialog.info({
    title: 'Xác nhận trả phòng',
    content: `Bạn có chắc chắn muốn trả phòng và thanh toán cho phòng ${props.bookingDetails?.roomName || ''} không?`,
    positiveText: 'Đồng ý',
    negativeText: 'Hủy',
    onPositiveClick: async () => {
      try {
        loading.value = true
        const res = await checkout(props.bookingDetails!.id)
        message.success('Trả phòng thành công!')

        const info = () => h('div', [
          h('p', `Tổng ngày: ${res.days}`),
          h('p', `Tiền phòng: ${res.roomCost.toLocaleString('vi-VN')} VNĐ`),
          h('p', `Tiền dịch vụ: ${res.serviceCost.toLocaleString('vi-VN')} VNĐ`),
          h('p', { style: 'font-weight: bold; margin-top: 8px' }, `Tổng cộng: ${res.total.toLocaleString('vi-VN')} VNĐ`)
        ])

        dialog.success({
          title: 'Hóa đơn thanh toán',
          content: info,
          positiveText: 'Hoàn tất',
          onPositiveClick: () => {
            emit('success')
            modalVisible.value = false
          }
        })

      } catch (e: any) {
        message.error(e.message)
      } finally {
        loading.value = false
      }
    }
  })
}

watch(() => props.bookingDetails, async (details) => {
  if (details) {
    await loadServices()
    
    // Extract idDoan from booking details
    // The backend response includes phieuDatPhongId which links to the DoanLuuTru
    if (details.phieuDatPhongId) {
      try {
        // Fetch the DoanLuuTru info using the phieuDatPhongId
        const doanInfo = await getDoanByBooking(details.phieuDatPhongId)
        idDoan.value = doanInfo?.id || ''
      }
      catch (error) {
        console.warn('Could not fetch doan info:', error)
        idDoan.value = ''
      }
    }
  }
})

watch(serviceScope, () => {
  loadServices()
})

function handleAddGuestSuccess() {
  loadServices() // Refresh services list
  emit('success') // Refresh parent  })
}

function handlePaymentSuccess() {
  emit('success') // Refresh parent to update payment status
  modalVisible.value = false
}

onMounted(() => {
  loadAvailableServices()
})
</script>

<template>
  <n-modal v-model:show="modalVisible" preset="card" title="Quản lý đặt phòng" class="w-800px" :mask-closable="false">
    <div v-if="bookingDetails" class="mb-4">
      <div class="flex gap-4 items-center">
        <h3 class="text-lg font-bold">{{ bookingDetails.roomName }}</h3>
        <n-tag :type="isPaid ? 'success' : 'info'">{{ bookingDetails.status }}</n-tag>
      </div>
      <div>Khách hàng: {{ bookingDetails.customerName || 'Khách lẻ' }}</div>
    </div>

    <n-tabs v-model:value="activeTab" type="line" animated>
      <n-tab-pane name="services" tab="Dịch vụ phát sinh">
        <div class="flex justify-between mb-4 items-center">
          <n-radio-group v-model:value="serviceScope" size="medium">
            <n-radio-button value="ROOM" label="Cho phòng này" />
            <n-radio-button value="GROUP" label="Cho cả đoàn" :disabled="!bookingDetails?.phieuDatPhongId" />
          </n-radio-group>
        </div>

        <div class="flex justify-between mb-2">
          <span>Tổng chi phí dịch vụ ({{ serviceScope === 'ROOM' ? 'Phòng' : 'Đoàn' }}): <strong>{{
            totalServiceCost.toLocaleString('vi-VN') }} VNĐ</strong></span>
          <n-button type="primary" @click="showAddServiceModal = true" :disabled="isPaid">
            <template #icon><nova-icon icon="carbon:add" /></template>
            Thêm dịch vụ
          </n-button>
        </div>

        <n-data-table :columns="columns" :data="servicesList" :loading="loading" :bordered="false" :summary="summary" />
      </n-tab-pane>

      <n-tab-pane name="cost" tab="Tổng hợp chi phí">
        <n-empty v-if="!idDoan" description="Cần có mã đoàn để xem tổng chi phí" />
        <div v-else class="text-center py-8">
          <p class="mb-4">Xem chi tiết tổng hợp chi phí lưu trú bao gồm tiền phòng và dịch vụ</p>
          <n-button type="info" size="large" @click="showCostModal = true">
            <template #icon><nova-icon icon="carbon:document" /></template>
            Xem chi tiết chi phí
          </n-button>
        </div>
      </n-tab-pane>
    </n-tabs>

    <template #footer>
      <div class="flex justify-between">
        <div>
          <n-button v-if="isActivelystaying && idDoan" type="success" @click="showAddGuestModal = true">
            <template #icon><nova-icon icon="carbon:user-add" /></template>
            Thêm khách
          </n-button>
        </div>
        <div class="flex gap-2">
          <n-button @click="modalVisible = false">Đóng</n-button>
          <n-button v-if="idDoan && !isPaid" type="warning" @click="showPaymentModal = true">
            <template #icon><nova-icon icon="carbon:currency" /></template>
            Thu tiền
          </n-button>
          <n-button type="error" @click="handleCheckout" :loading="loading" :disabled="isPaid" v-if="!isPaid">
            Trả phòng & Thanh toán
          </n-button>
        </div>
      </div>
    </template>


    <!-- Modal Add Service -->
    <n-modal v-model:show="showAddServiceModal" preset="dialog" title="Thêm dịch vụ">
      <div class="space-y-4 pt-4">
        <n-form-item label="Chọn dịch vụ có sẵn">
          <n-select
            v-model:value="newService.idDichVu"
            :options="serviceOptions"
            placeholder="Chọn dịch vụ có sẵn (hoặc nhập tay bên dưới)"
            clearable
            filterable
          />
        </n-form-item>
        <n-form-item label="Hoặc nhập tên dịch vụ">
          <n-input
            v-model:value="newService.tenDichVu"
            placeholder="Nhập tên dịch vụ nếu không có trong danh sách"
            :disabled="!!newService.idDichVu"
          />
        </n-form-item>
        <div class="flex gap-4">
          <n-form-item label="Số lượng" class="flex-1">
            <n-input-number v-model:value="newService.soLuong" :min="1" />
          </n-form-item>
          <n-form-item label="Đơn giá (VNĐ)" class="flex-1">
            <n-input-number
              v-model:value="newService.donGia"
              :min="0"
              :step="1000"
              :format="(value: number) => value.toLocaleString('vi-VN')"
              :parse="(value: string) => Number(value.replace(/,/g, ''))"
              :disabled="!!newService.idDichVu"
            />
          </n-form-item>
        </div>
        <div class="text-right font-bold text-lg">
          Thành tiền: {{ (newService.soLuong * newService.donGia).toLocaleString('vi-VN') }} VNĐ
        </div>
      </div>
      <template #action>
        <n-button @click="showAddServiceModal = false">Hủy</n-button>
        <n-button type="primary" @click="handleAddService" :loading="loading">Thêm</n-button>
      </template>
    </n-modal>

    <!-- Modal Add Guest During Stay -->
    <ThemKhachDangLuuTru
      v-model:visible="showAddGuestModal"
      :id-doan="idDoan"
      @success="handleAddGuestSuccess"
    />

    <!-- Modal Chi Phí Lưu Trú -->
    <ChiPhiLuuTruModal
      v-model:visible="showCostModal"
      :id-doan="idDoan"
    />

    <!-- Modal Thanh Toán -->
    <ThanhToanModal
      v-model:visible="showPaymentModal"
      :id-doan="idDoan"
      @success="handlePaymentSuccess"
    />

  </n-modal>
</template>

<style scoped>
.w-800px {
  width: 800px;
  max-width: 95vw;
}
</style>
