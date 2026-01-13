<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ChonLoaiPhong, PhongDatResponse } from '@/service/api/letan/booking'
import { getPhongTheoLoai, savePhieuDatTam } from '@/service/api/letan/booking'

interface Props {
  visible: boolean
  bookingData: {
    ngayNhan: number
    ngayTra: number
    soLuongKhach: number
    danhSachLoaiPhong: ChonLoaiPhong[]
  } | null
  sessionId?: string
}

interface Emits {
  (e: 'update:visible', visible: boolean): void
  (e: 'next', data: {
    sessionId: string
    danhSachIdPhong: string[]
    tongTien: number
  }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

const danhSachPhong = ref<PhongDatResponse[]>([])
const selectedPhongIds = ref<string[]>([])
const isLoading = ref(false)

const soNgayO = computed(() => {
  if (!props.bookingData) return 0
  return Math.ceil((props.bookingData.ngayTra - props.bookingData.ngayNhan) / (1000 * 60 * 60 * 24))
})

const selectedPhongList = computed(() => {
  return danhSachPhong.value.filter(p => selectedPhongIds.value.includes(p.idPhong))
})

const tongTienPhong = computed(() => {
  return selectedPhongList.value.reduce((sum, p) => sum + p.gia, 0)
})

const tongTien = computed(() => {
  return tongTienPhong.value * soNgayO.value
})

const sessionId = ref(props.sessionId || generateSessionId())

function generateSessionId() {
  return `booking_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

function closeModal() {
  modalVisible.value = false
}

async function loadDanhSachPhong() {
  if (!props.bookingData) return
  try {
    isLoading.value = true
    const data = await getPhongTheoLoai({
      ngayNhan: props.bookingData.ngayNhan,
      ngayTra: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachLoaiPhong: props.bookingData.danhSachLoaiPhong,
    })
    danhSachPhong.value = data
    selectedPhongIds.value = data.map(p => p.idPhong)
  } catch (error: any) {
    window.$message.error(error.message || 'Không thể tải danh sách phòng')
  } finally {
    isLoading.value = false
  }
}

function togglePhong(idPhong: string) {
  const index = selectedPhongIds.value.indexOf(idPhong)
  if (index > -1) {
    selectedPhongIds.value.splice(index, 1)
  } else {
    selectedPhongIds.value.push(idPhong)
  }
}

async function handleLuuTam() {
  if (!props.bookingData) {
    window.$message.warning('Dữ liệu đặt phòng không hợp lệ')
    return
  }
  if (selectedPhongIds.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  try {
    isLoading.value = true

    await savePhieuDatTam({
      sessionId: sessionId.value,
      checkInDate: props.bookingData.ngayNhan,
      checkOutDate: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachIdPhong: selectedPhongIds.value,
      idKhachHang: null,
      ghiChu: null,
      nhanNgay: false,
      tienKhachTra: null,
      isFromRoomClick: false,
      currentStep: 'CUSTOMER_INFO',
      roomDetails: selectedPhongList.value.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: soNgayO.value,
      })),
    })

    window.$message.success('Đã lưu phiếu đặt tạm!')
    closeModal()
  } catch (error: any) {
    window.$message.error(error.message || 'Không thể lưu phiếu đặt tạm')
  } finally {
    isLoading.value = false
  }
}

async function handleNext() {
  if (!props.bookingData) {
    window.$message.warning('Dữ liệu đặt phòng không hợp lệ')
    return
  }
  if (selectedPhongIds.value.length === 0) {
    window.$message.warning('Vui lòng chọn ít nhất một phòng')
    return
  }

  try {
    isLoading.value = true

    const result = await savePhieuDatTam({
      sessionId: sessionId.value,
      checkInDate: props.bookingData.ngayNhan,
      checkOutDate: props.bookingData.ngayTra,
      soLuongKhach: props.bookingData.soLuongKhach,
      danhSachIdPhong: selectedPhongIds.value,
      idKhachHang: null,
      ghiChu: null,
      nhanNgay: false,
      tienKhachTra: null,
      isFromRoomClick: false,
      currentStep: 'CUSTOMER_INFO',
      roomDetails: selectedPhongList.value.map(p => ({
        idPhong: p.idPhong,
        maPhong: p.maPhong,
        tenPhong: p.tenPhong,
        tenLoaiPhong: p.tenLoaiPhong,
        tang: p.tang,
        gia: p.gia,
        soNgay: soNgayO.value,
      })),
    })

    emit('next', {
      sessionId: result.sessionId,
      danhSachIdPhong: selectedPhongIds.value,
      tongTien: tongTien.value,
    })
  } catch (error: any) {
    window.$message.error(error.message || 'Không thể lưu phiếu đặt tạm')
  } finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.bookingData) {
    loadDanhSachPhong()
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

function getTagColor(tag: any): string {
  return tag.mau || '#667eea'
}
</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    title="Chọn phòng"
    class="w-1300px"
    :segmented="{ content: true, action: true }"
  >
    <n-spin :show="isLoading">
      <div class="grid grid-cols-12 gap-5">
        <div class="col-span-8 space-y-4">
          <n-card size="small" :bordered="false">
            <template #header>
              <div class="flex justify-between items-center">
                <span class="text-lg font-semibold">Danh sách phòng</span>
                <n-tag type="info" size="large">
                  Đã chọn: {{ selectedPhongIds.length }}/{{ danhSachPhong.length }} phòng
                </n-tag>
              </div>
            </template>
            <div class="space-y-3 max-h-[500px] overflow-y-auto pr-2">
              <div
                v-for="phong in danhSachPhong"
                :key="phong.idPhong"
                class="border rounded-lg p-4 cursor-pointer transition-all"
                :class="[
                  selectedPhongIds.includes(phong.idPhong)
                    ? 'border-blue-500 bg-blue-50 shadow-md'
                    : 'border-gray-200 hover:border-blue-300',
                ]"
                @click="togglePhong(phong.idPhong)"
              >
                <div class="flex justify-between items-start">
                  <div class="flex items-start gap-3 flex-1">
                    <n-checkbox
                      :checked="selectedPhongIds.includes(phong.idPhong)"
                      @click.stop
                      @update:checked="() => togglePhong(phong.idPhong)"
                    />
                    <div class="flex-1">
                      <div class="flex items-center gap-2 mb-2">
                        <h4 class="font-bold text-base">{{ phong.maPhong }} - {{ phong.tenPhong }}</h4>
                        <n-tag size="small" type="success">{{ phong.tenLoaiPhong }}</n-tag>
                      </div>
                      <div class="text-gray-600 space-y-1">
                        <div class="flex items-center gap-4 text-sm">
                          <span class="flex items-center gap-1">
                            <nova-icon icon="carbon:building" :size="14" />
                            Tầng {{ phong.tang }}
                          </span>
                          <span class="flex items-center gap-1">
                            <nova-icon icon="carbon:user-multiple" :size="14" />
                            {{ phong.sucChua }} người
                          </span>
                        </div>
                        <div v-if="phong.tags && phong.tags.length > 0" class="flex gap-2 flex-wrap mt-2">
                          <n-tag
                            v-for="tag in phong.tags"
                            :key="tag.id"
                            size="small"
                            round
                            :color="{ color: getTagColor(tag), textColor: '#fff', borderColor: getTagColor(tag) }"
                          >
                            {{ tag.ten }}
                          </n-tag>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="text-right ml-4">
                    <div class="text-lg font-bold text-blue-600">
                      {{ phong.gia.toLocaleString('vi-VN') }}
                    </div>
                    <div class="text-xs text-gray-500">VNĐ / đêm</div>
                  </div>
                </div>
              </div>
            </div>
          </n-card>
        </div>

        <div class="col-span-4 space-y-4">
          <n-card v-if="bookingData" size="small" title="Thông tin đặt phòng" class="bg-blue-50">
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <div class="text-gray-600 mb-1">
                  <nova-icon icon="carbon:calendar" class="mr-1" />Nhận phòng
                </div>
                <div class="font-semibold">{{ formatDate(bookingData.ngayNhan) }}</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">
                  <nova-icon icon="carbon:calendar" class="mr-1" />Trả phòng
                </div>
                <div class="font-semibold">{{ formatDate(bookingData.ngayTra) }}</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">
                  <nova-icon icon="carbon:user-multiple" class="mr-1" />Số khách
                </div>
                <div class="font-semibold">{{ bookingData.soLuongKhach }} người</div>
              </div>
              <div>
                <div class="text-gray-600 mb-1">
                  <nova-icon icon="carbon:time" class="mr-1" />Lưu trú
                </div>
                <div class="font-semibold text-blue-600">{{ soNgayO }} đêm</div>
              </div>
            </div>
          </n-card>

          <div class="bg-gradient-to-r from-green-50 to-emerald-50 p-4 rounded-lg border border-green-200">
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span>Tiền phòng ({{ selectedPhongIds.length }} phòng × 1 đêm):</span>
                <span class="font-semibold">{{ tongTienPhong.toLocaleString('vi-VN') }} VNĐ</span>
              </div>
              <div class="flex justify-between">
                <span>Số đêm:</span>
                <span class="font-semibold">{{ soNgayO }} đêm</span>
              </div>
              <n-divider class="my-2" />
              <div class="flex justify-between items-center">
                <span class="text-base font-semibold">Tổng cộng:</span>
                <span class="text-2xl font-bold text-green-600">
                  {{ tongTien.toLocaleString('vi-VN') }} VNĐ
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="space-between" style="width: 100%">
        <n-text class="text-base">
          <strong>{{ selectedPhongIds.length }}</strong> phòng đã chọn
        </n-text>
        <n-space>
          <n-button size="large" @click="closeModal">Hủy</n-button>
          <n-button
            size="large"
            :disabled="selectedPhongIds.length === 0"
            @click="handleLuuTam"
          >
            <template #icon><nova-icon icon="carbon:save" /></template>
            Lưu tạm
          </n-button>
          <n-button
            type="primary"
            size="large"
            :disabled="selectedPhongIds.length === 0"
            @click="handleNext"
          >
            <template #icon><nova-icon icon="carbon:arrow-right" /></template>
            Tiếp tục
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-1300px {
  width: 1300px;
  max-width: 95vw;
}
</style>
