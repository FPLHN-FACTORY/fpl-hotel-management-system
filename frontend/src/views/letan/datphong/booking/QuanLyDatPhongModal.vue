<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import {
  saveDichVu,
  deleteDichVu,
  getDichVuByRoomBooking,
  getDichVuByBooking,
  type DichVuPhatSinh
} from '@/service/api/letan/incurredService'
import { useMessage, useDialog, NButton, NTag, NRadioGroup, NRadioButton } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { checkout } from '@/service/api/letan/booking'

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

// Form Add Service
const showAddServiceModal = ref(false)
const newService = ref({
  tenDichVu: '',
  soLuong: 1,
  donGia: 0,
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

async function handleAddService() {
  if (!newService.value.tenDichVu) {
    message.warning('Vui lòng nhập tên dịch vụ')
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
      ...newService.value
    }

    if (serviceScope.value === 'ROOM') {
      payload.idChiTietDatPhong = props.bookingDetails?.id
    } else {
      payload.idPhieuDatPhong = props.bookingDetails?.phieuDatPhongId
    }

    await saveDichVu(payload)
    message.success('Thêm dịch vụ thành công')
    showAddServiceModal.value = false
    newService.value = { tenDichVu: '', soLuong: 1, donGia: 0 }
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

watch(() => props.visible, (val) => {
  if (val) {
    loadServices()
  }
})

watch(serviceScope, () => {
  loadServices()
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
    </n-tabs>

    <template #footer>
      <div class="flex justify-end gap-2">
        <n-button @click="modalVisible = false">Đóng</n-button>
        <n-button type="error" @click="handleCheckout" :loading="loading" :disabled="isPaid" v-if="!isPaid">
          Trả phòng & Thanh toán
        </n-button>
      </div>
    </template>


    <!-- Modal Add Service -->
    <n-modal v-model:show="showAddServiceModal" preset="dialog" title="Thêm dịch vụ mới">
      <div class="space-y-4 pt-4">
        <n-form-item label="Tên dịch vụ">
          <n-input v-model:value="newService.tenDichVu" placeholder="Ví dụ: Nước ngọt, Giặt là..." />
        </n-form-item>
        <div class="flex gap-4">
          <n-form-item label="Số lượng" class="flex-1">
            <n-input-number v-model:value="newService.soLuong" :min="1" />
          </n-form-item>
          <n-form-item label="Đơn giá (VNĐ)" class="flex-1">
            <n-input-number v-model:value="newService.donGia" :min="0" :step="1000"
              :format="(value) => value.toLocaleString('vi-VN')" :parse="(value) => Number(value.replace(/,/g, ''))" />
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

  </n-modal>
</template>

<style scoped>
.w-800px {
  width: 800px;
  max-width: 95vw;
}
</style>
