<script setup lang="ts">
import { getCostBreakdown, getTemporaryInvoice } from '@/service/api/letan/doanluutru'
import type { CostBreakdownResponse, InvoiceResponse } from '@/service/api/letan/doanluutru'

interface Props {
  visible: boolean
  idDoan: string
}

interface Emits {
  (e: 'update:visible', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const costData = ref<CostBreakdownResponse | null>(null)
const activeTab = ref('breakdown')

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

watch(() => props.visible, async (val) => {
  if (val) {
    await loadCostBreakdown()
  }
})

async function loadCostBreakdown() {
  loading.value = true
  try {
    const res = await getCostBreakdown(props.idDoan)
    costData.value = res.data
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tải chi phí')
  }
  finally {
    loading.value = false
  }
}

async function handlePrintInvoice() {
  try {
    loading.value = true
    const res = await getTemporaryInvoice(props.idDoan)
    const invoice = res.data
    
    // Tạo nội dung in
    const printWindow = window.open('', '_blank')
    if (!printWindow)
      return

    printWindow.document.write(generateInvoiceHTML(invoice))
    printWindow.document.close()
    
    // Đợi load xong rồi in
    printWindow.onload = () => {
      printWindow.print()
    }
  }
  catch (error: any) {
    window.$message.error(error.message || 'Không thể tạo hóa đơn')
  }
  finally {
    loading.value = false
  }
}

function generateInvoiceHTML(invoice: InvoiceResponse): string {
  const formatDate = (timestamp: number) => {
    return new Date(timestamp).toLocaleDateString('vi-VN')
  }

  const formatCurrency = (amount: number) => {
    return amount.toLocaleString('vi-VN') + ' VNĐ'
  }

  return `
    <!DOCTYPE html>
    <html>
    <head>
      <title>Hóa đơn ${invoice.maHoaDon}</title>
      <meta charset="UTF-8">
      <style>
        body { font-family: 'Times New Roman', serif; padding: 20px; }
        h1 { text-align: center; color: #333; }
        h2 { color: #666; border-bottom: 2px solid #333; padding-bottom: 10px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f5f5f5; font-weight: bold; }
        .text-right { text-align: right; }
        .total-row { font-weight: bold; font-size: 16px; background-color: #f9f9f9; }
        .header { margin-bottom: 30px; }
        .info-section { margin: 15px 0; }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>${invoice.tenKhachSan || 'KHÁCH SẠN'}</h1>
        <p style="text-align: center;">
          ${invoice.diaChiKhachSan || ''}<br>
          SĐT: ${invoice.soDienThoaiKhachSan || ''}
        </p>
        <h1>HÓA ĐƠN TẠM TÍNH</h1>
        <p style="text-align: center;">Mã hóa đơn: ${invoice.maHoaDon}</p>
        <p style="text-align: center;">Ngày tạo: ${formatDate(invoice.thoiGianTao)}</p>
      </div>

      <div class="info-section">
        <p><strong>Mã đoàn:</strong> ${invoice.maDoan}</p>
        <p><strong>Tên đoàn:</strong> ${invoice.tenDoan}</p>
        <p><strong>Khách hàng:</strong> ${invoice.tenKhachHang || ''}</p>
        <p><strong>Số điện thoại:</strong> ${invoice.soDienThoai || ''}</p>
        <p><strong>Thời gian lưu trú:</strong> ${formatDate(invoice.ngayCheckIn)} - ${formatDate(invoice.ngayCheckOut)} (${invoice.soNgayLuuTru} đêm)</p>
      </div>

      <h2>CHI TIẾT PHÒNG</h2>
      <table>
        <thead>
          <tr>
            <th>Phòng</th>
            <th>Loại phòng</th>
            <th class="text-right">Giá/đêm</th>
            <th class="text-right">Số đêm</th>
            <th class="text-right">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          ${invoice.roomCosts.map(room => `
            <tr>
              <td>${room.tenPhong}</td>
              <td>${room.tenLoaiPhong}</td>
              <td class="text-right">${formatCurrency(room.giaPhong)}</td>
              <td class="text-right">${room.soNgay}</td>
              <td class="text-right">${formatCurrency(room.thanhTien)}</td>
            </tr>
          `).join('')}
          <tr class="total-row">
            <td colspan="4">Tổng tiền phòng</td>
            <td class="text-right">${formatCurrency(invoice.tongTienPhong)}</td>
          </tr>
        </tbody>
      </table>

      ${invoice.serviceCosts.length > 0 ? `
        <h2>DỊCH VỤ PHÁT SINH</h2>
        <table>
          <thead>
            <tr>
              <th>Dịch vụ</th>
              <th>Phòng</th>
              <th class="text-right">Đơn giá</th>
              <th class="text-right">Số lượng</th>
              <th class="text-right">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${invoice.serviceCosts.map(service => `
              <tr>
                <td>${service.tenDichVu}</td>
                <td>${service.phongApDung || '-'}</td>
                <td class="text-right">${formatCurrency(service.donGia)}</td>
                <td class="text-right">${service.soLuong}</td>
                <td class="text-right">${formatCurrency(service.thanhTien)}</td>
              </tr>
            `).join('')}
            <tr class="total-row">
              <td colspan="4">Tổng tiền dịch vụ</td>
              <td class="text-right">${formatCurrency(invoice.tongTienDichVu)}</td>
            </tr>
          </tbody>
        </table>
      ` : ''}

      <h2>TỔNG CỘNG</h2>
      <table>
        <tbody>
          <tr class="total-row" style="font-size: 18px; color: #d32f2f;">
            <td>TỔNG THANH TOÁN</td>
            <td class="text-right">${formatCurrency(invoice.tongCong)}</td>
          </tr>
        </tbody>
      </table>

      <div style="margin-top: 50px; display: flex; justify-content: space-between;">
        <div style="text-align: center;">
          <p><strong>Người lập</strong></p>
          <p style="margin-top: 60px;">(Ký, ghi rõ họ tên)</p>
        </div>
        <div style="text-align: center;">
          <p><strong>Khách hàng</strong></p>
          <p style="margin-top: 60px;">(Ký, ghi rõ họ tên)</p>
        </div>
      </div>
    </body>
    </html>
  `
}

const formatCurrency = (amount: number) => amount?.toLocaleString('vi-VN') || '0'
const formatDate = (timestamp: number | null) => {
  if (!timestamp)
    return '-'
  return new Date(timestamp).toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    preset="card"
    title="Tổng hợp chi phí lưu trú"
    style="width: 900px; max-width: 95vw"
    :mask-closable="false"
  >
    <n-spin :show="loading">
      <div v-if="costData">
        <!-- Thông tin đoàn -->
        <n-descriptions :column="2" bordered size="small" class="mb-4">
          <n-descriptions-item label="Mã đoàn">
            {{ costData.maDoan }}
          </n-descriptions-item>
          <n-descriptions-item label="Tên đoàn">
            {{ costData.tenDoan }}
          </n-descriptions-item>
          <n-descriptions-item label="Check-in">
            {{ formatDate(costData.ngayCheckIn) }}
          </n-descriptions-item>
          <n-descriptions-item label="Check-out">
            {{ formatDate(costData.ngayCheckOut) }}
          </n-descriptions-item>
          <n-descriptions-item label="Số đêm">
            {{ costData.soNgayLuuTru }}
          </n-descriptions-item>
        </n-descriptions>

        <!-- Chi tiết phòng -->
        <n-card title="Chi phí phòng" class="mb-4" size="small">
          <n-data-table
            :columns="[
              { title: 'Phòng', key: 'tenPhong' },
              { title: 'Loại phòng', key: 'tenLoaiPhong' },
              { title: 'Giá/đêm', key: 'giaPhong', render: (row: any) => formatCurrency(row.giaPhong) + ' VNĐ' },
              { title: 'Số đêm', key: 'soNgay' },
              { title: 'Thành tiền', key: 'thanhTien', render: (row: any) => formatCurrency(row.thanhTien) + ' VNĐ' },
            ]"
            :data="costData.roomCosts"
            :pagination="false"
            size="small"
          />
          <div :style="{ textAlign: 'right' }" class="mt-3">
            <n-tag type="info" size="large">
              Tổng tiền phòng: {{ formatCurrency(costData.tongTienPhong) }} VNĐ
            </n-tag>
          </div>
        </n-card>

        <!-- Chi tiết dịch vụ -->
        <n-card v-if="costData.serviceCosts.length > 0" title="Dịch vụ phát sinh" class="mb-4" size="small">
          <n-data-table
            :columns="[
              { title: 'Dịch vụ', key: 'tenDichVu' },
              { title: 'Phòng', key: 'phongApDung', render: (row: any) => row.phongApDung || '-' },
              { title: 'Đơn giá', key: 'donGia', render: (row: any) => formatCurrency(row.donGia) + ' VNĐ' },
              { title: 'SL', key: 'soLuong' },
              { title: 'Thành tiền', key: 'thanhTien', render: (row: any) => formatCurrency(row.thanhTien) + ' VNĐ' },
            ]"
            :data="costData.serviceCosts"
            :pagination="false"
            size="small"
          />
          <div :style="{ textAlign: 'right' }" class="mt-3">
            <n-tag type="warning" size="large">
              Tổng tiền dịch vụ: {{ formatCurrency(costData.tongTienDichVu) }} VNĐ
            </n-tag>
          </div>
        </n-card>

        <!-- Tổng cộng -->
        <n-card size="small">
          <div class="flex justify-between items-center">
            <h3 class="text-xl font-bold">
              TỔNG CỘNG
            </h3>
            <n-tag type="error" size="large" style="font-size: 18px; padding: 10px 20px">
              {{ formatCurrency(costData.tongCong) }} VNĐ
            </n-tag>
          </div>
        </n-card>
      </div>
    </n-spin>

    <template #footer>
      <div class="flex justify-end gap-2">
        <n-button @click="modalVisible = false">
          Đóng
        </n-button>
        <n-button type="primary" :loading="loading" @click="handlePrintInvoice">
          <template #icon>
            <nova-icon icon="carbon:printer" />
          </template>
          In hóa đơn
        </n-button>
      </div>
    </template>
  </n-modal>
</template>
