<script setup lang="ts">
import { getPaymentStatus, processPayment } from '@/service/api/letan/doanluutru'
import type { PaymentStatusResponse, PaymentRequest } from '@/service/api/letan/doanluutru'

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

const loading = ref(false)
const paymentStatus = ref<PaymentStatusResponse | null>(null)
const paymentAmount = ref<number>(0)
const paymentMethod = ref<string>('TIEN_MAT')
const paymentNote = ref('')

const paymentMethods = [
  { label: 'Tiền mặt', value: 'TIEN_MAT' },
  { label: 'Chuyển khoản', value: 'CHUYEN_KHOAN' },
  { label: 'Thẻ', value: 'THE' },
  { label: 'VNPay', value: 'VNPAY' },
  { label: 'Momo', value: 'MOMO' },
]

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const paymentHistoryColumns = [
  { title: 'Mã TT', key: 'maThanhToan', width: 150 },
  { title: 'Số tiền', key: 'soTien', render: (row: any) => formatCurrency(row.soTien) + ' VNĐ' },
  { title: 'Phương thức', key: 'phuongThuc' },
  {
    title: 'Thời gian',
    key: 'thoiGianThanhToan',
    render: (row: any) => formatDate(row.thoiGianThanhToan),
  },
  { title: 'Nhân viên', key: 'tenNhanVien', render: (row: any) => row.tenNhanVien || '-' },
  { title: 'Ghi chú', key: 'ghiChu', render: (row: any) => row.ghiChu || '-' },
]

watch(() => props.visible, async (val) => {
  if (val) {
    await loadPaymentStatus()
  }
})

async function loadPaymentStatus() {
  loading.value = true
  try {
    const res = await getPaymentStatus(props.idDoan)
    paymentStatus.value = res.data
    // Set default payment amount to remaining debt
    paymentAmount.value = res.data.congNo
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tải trạng thái thanh toán')
  }
  finally {
    loading.value = false
  }
}

async function handlePayment() {
  if (!paymentStatus.value)
    return

  if (paymentAmount.value <= 0) {
    window.$message.warning('Số tiền thanh toán phải lớn hơn 0')
    return
  }

  if (paymentAmount.value > paymentStatus.value.congNo) {
    window.$message.warning('Số tiền thanh toán vượt quá số tiền còn nợ')
    return
  }

  try {
    loading.value = true
    const request: PaymentRequest = {
      soTien: paymentAmount.value,
      phuongThuc: paymentMethod.value,
      ghiChu: paymentNote.value,
    }

    const res = await processPayment(props.idDoan, request)
    window.$message.success(res.message || 'Thanh toán thành công')

    // Reload payment status
    await loadPaymentStatus()

    // Reset form
    paymentNote.value = ''

    // If fully paid, emit success and close
    if (paymentStatus.value && paymentStatus.value.congNo === 0) {
      emit('success')
      emit('update:visible', false)
    }
  }
  catch (error: any) {
    window.$message.error(error.message || 'Thanh toán thất bại')
  }
  finally {
    loading.value = false
  }
}

function handlePayFullAmount() {
  if (paymentStatus.value) {
    paymentAmount.value = paymentStatus.value.congNo
  }
}

const formatCurrency = (amount: number) => amount?.toLocaleString('vi-VN') || '0'
const formatDate = (timestamp: number | null) => {
  if (!timestamp)
    return '-'
  return new Date(timestamp).toLocaleString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    preset="card"
    title="Thu tiền và quản lý công nợ"
    style="width: 900px; max-width: 95vw"
    :mask-closable="false"
  >
    <n-spin :show="loading">
      <div v-if="paymentStatus">
        <!-- Thông tin đoàn -->
        <n-card size="small" class="mb-4">
          <n-grid :cols="2" :x-gap="16">
            <n-grid-item>
              <p class="text-sm text-gray-600">
                Mã đoàn
              </p>
              <p class="font-bold">
                {{ paymentStatus.maDoan }}
              </p>
            </n-grid-item>
            <n-grid-item>
              <p class="text-sm text-gray-600">
                Tên đoàn
              </p>
              <p class="font-bold">
                {{ paymentStatus.tenDoan }}
              </p>
            </n-grid-item>
          </n-grid>
        </n-card>

        <!-- Tổng quan thanh toán -->
        <n-card title="Tổng quan" size="small" class="mb-4">
          <n-descriptions :column="1" bordered size="small">
            <n-descriptions-item label="Tổng chi phí">
              <n-tag type="info" size="large">
                {{ formatCurrency(paymentStatus.tongChiPhi) }} VNĐ
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Đã thanh toán">
              <n-tag type="success" size="large">
                {{ formatCurrency(paymentStatus.tongDaThanhToan) }} VNĐ
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Còn nợ">
              <n-tag :type="paymentStatus.congNo > 0 ? 'error' : 'success'" size="large">
                {{ formatCurrency(paymentStatus.congNo) }} VNĐ
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="Trạng thái">
              <n-tag :type="paymentStatus.trangThai === 'DA_THANH_TOAN' ? 'success' : 'warning'">
                {{ paymentStatus.trangThai === 'DA_THANH_TOAN' ? 'Đã thanh toán' : 'Còn nợ' }}
              </n-tag>
            </n-descriptions-item>
          </n-descriptions>
        </n-card>

        <!-- Form thanh toán -->
        <n-card v-if="paymentStatus.congNo > 0" title="Thanh toán" size="small" class="mb-4">
          <n-form label-placement="left" label-width="140">
            <n-form-item label="Phương thức">
              <n-select
                v-model:value="paymentMethod"
                :options="paymentMethods"
                placeholder="Chọn phương thức thanh toán"
              />
            </n-form-item>

            <n-form-item label="Số tiền">
              <n-input-number
                v-model:value="paymentAmount"
                :min="0"
                :max="paymentStatus.congNo"
                :step="10000"
                style="width: 100%"
                :format="(value: number | null) => value ? formatCurrency(value) + ' VNĐ' : '0 VNĐ'"
                :parse="(value: string) => Number(value.replace(/[^0-9]/g, '')) || 0"
              >
                <template #suffix>
                  <n-button text type="primary" @click="handlePayFullAmount">
                    Trả hết
                  </n-button>
                </template>
              </n-input-number>
            </n-form-item>

            <n-form-item label="Ghi chú">
              <n-input
                v-model:value="paymentNote"
                type="textarea"
                placeholder="Ghi chú (không bắt buộc)"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </n-form-item>

            <n-form-item>
              <n-button type="primary" :loading="loading" @click="handlePayment" block>
                <template #icon>
                  <nova-icon icon="carbon:currency" />
                </template>
                Thanh toán {{ formatCurrency(paymentAmount) }} VNĐ
              </n-button>
            </n-form-item>
          </n-form>
        </n-card>

        <!-- Lịch sử thanh toán -->
        <n-card v-if="paymentStatus.payments.length > 0" title="Lịch sử thanh toán" size="small">
          <n-data-table
            :columns="paymentHistoryColumns"
            :data="paymentStatus.payments"
            :pagination="false"
            size="small"
          />
        </n-card>

        <n-empty v-else description="Chưa có lịch sử thanh toán" class="my-4" />
      </div>
    </n-spin>

    <template #footer>
      <div class="flex justify-end gap-2">
        <n-button @click="modalVisible = false">
          Đóng
        </n-button>
      </div>
    </template>
  </n-modal>
</template>
