<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useMessage, NModal, NSpace, NButton, NSpin, NTag, NTabs, NTabPane, NEmpty, NCheckbox, NGrid, NGridItem, NScrollbar } from 'naive-ui'
import { apiGetPhongKhaDung, apiGanPhong, apiXacNhanPhieuDat, apiGetChiTietPhieuDat } from '@/service/api/letan/phieudat'

interface Props {
  visible: boolean
  phieuData: any
}

const props = defineProps<Props>()
const emit = defineEmits(['update:visible', 'success'])

const message = useMessage()
const isLoading = ref(false)
const fullPhieuData = ref<any>(null)
const roomTypesData = ref<any[]>([]) // Rooms available grouped by type
const selectedRoomsByType = ref<Record<string, string[]>>({}) // { idLoaiPhong: [idPhong1, idPhong2] }

const modalVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const needsAssignment = computed(() => {
  if (!fullPhieuData.value?.danhSachLoaiPhong) return []
  return fullPhieuData.value.danhSachLoaiPhong.filter((lp: any) => lp.soLuong > lp.soLuongDaGan)
})

const assignmentStatus = computed(() => {
  if (!fullPhieuData.value?.danhSachLoaiPhong) return []
  return fullPhieuData.value.danhSachLoaiPhong.map((lp: any) => {
    const selectedCount = selectedRoomsByType.value[lp.idLoaiPhong]?.length || 0
    const totalNeeded = lp.soLuong - lp.soLuongDaGan
    return {
      ...lp,
      selectedCount,
      totalNeeded,
      isComplete: selectedCount === totalNeeded
    }
  })
})

const isAllComplete = computed(() => {
  return assignmentStatus.value.every((status: any) => status.isComplete)
})

async function loadAvailableRooms() {
  if (!props.phieuData?.id) return
  
  isLoading.value = true
  roomTypesData.value = []
  selectedRoomsByType.value = {}
  
  try {
    // 1. Fetch full details if needed
    const detailRes = await apiGetChiTietPhieuDat(props.phieuData.id)
    fullPhieuData.value = detailRes.data.data || detailRes.data

    // 2. Fetch available rooms for each type
    const fetchPromises = needsAssignment.value.map(async (lp: any) => {
      const res = await apiGetPhongKhaDung(props.phieuData.id, lp.idLoaiPhong)
      return {
        idLoaiPhong: lp.idLoaiPhong,
        tenLoaiPhong: lp.tenLoaiPhong,
        rooms: res.data.data || []
      }
    })
    
    roomTypesData.value = await Promise.all(fetchPromises)
    
    // Initialize selection object
    roomTypesData.value.forEach(type => {
      selectedRoomsByType.value[type.idLoaiPhong] = []
    })
  } catch (error: any) {
    message.error(error.message || 'Lỗi khi tải danh sách phòng trống')
  } finally {
    isLoading.value = false
  }
}

function handleToggleRoom(idLoaiPhong: string, idPhong: string, totalNeeded: number) {
  const current = selectedRoomsByType.value[idLoaiPhong] || []
  if (current.includes(idPhong)) {
    selectedRoomsByType.value[idLoaiPhong] = current.filter(id => id !== idPhong)
  } else {
    if (current.length >= totalNeeded) {
      message.warning(`Đã chọn đủ ${totalNeeded} phòng cho loại này`)
      return
    }
    selectedRoomsByType.value[idLoaiPhong] = [...current, idPhong]
  }
}

async function handleConfirmAssignment() {
  if (!isAllComplete.value) {
    message.warning('Vui lòng chọn đủ số lượng phòng cho tất cả các loại')
    return
  }

  const allSelectedIds = Object.values(selectedRoomsByType.value).flat()
  
  try {
    isLoading.value = true
    
    // 1. Assign Rooms
    await apiGanPhong({
      idPhieuDat: props.phieuData.id,
      danhSachIdPhong: allSelectedIds
    })
    
    // 2. Confirm Booking (moves to CONFIRMED)
    await apiXacNhanPhieuDat(props.phieuData.id)
    
    message.success('Gán phòng và xác nhận phiếu đặt thành công!')
    emit('success')
    modalVisible.value = false
  } catch (error: any) {
    message.error(error.message || 'Lỗi khi gán phòng')
  } finally {
    isLoading.value = false
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadAvailableRooms()
  }
})

</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    preset="card"
    title="Gán phòng cho phiếu đặt"
    style="width: 1000px; max-width: 95vw"
    :segmented="{ content: true, action: true }"
    :mask-closable="false"
  >
    <n-spin :show="isLoading">
      <div class="space-y-4">
        <!-- Summary Alert -->
        <n-alert v-if="phieuData" type="info" :bordered="false" class="mb-4">
          <template #icon>
            <nova-icon icon="carbon:information" />
          </template>
          <div>
            <span class="font-bold">Mã phiếu:</span> {{ phieuData.maPhieu }} | 
            <span class="font-bold">Khách hàng:</span> {{ phieuData.khachHang?.hoTen || phieuData.tenKhachHang || 'Chưa gán' }}
          </div>
          <div class="mt-1">
            <span class="font-bold">Thời gian:</span> 
            {{ new Date(props.phieuData.ngayCheckIn).toLocaleString('vi-VN') }} 
            - 
            {{ new Date(props.phieuData.ngayCheckOut).toLocaleString('vi-VN') }}
          </div>
        </n-alert>

        <!-- Dynamic Selection Progress -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
          <div 
            v-for="status in assignmentStatus" 
            :key="status.idLoaiPhong"
            class="p-3 rounded-lg border flex flex-col gap-1"
            :class="status.isComplete ? 'bg-green-50 border-green-200' : 'bg-orange-50 border-orange-200'"
          >
            <div class="flex justify-between items-center">
              <span class="font-semibold text-sm">{{ status.tenLoaiPhong }}</span>
              <n-tag :type="status.isComplete ? 'success' : 'warning'" size="small" round>
                {{ status.selectedCount }} / {{ status.totalNeeded }}
              </n-tag>
            </div>
          </div>
        </div>

        <!-- Room Selection Tabs -->
        <n-tabs type="line" animated v-if="roomTypesData.length > 0">
          <n-tab-pane 
            v-for="type in roomTypesData" 
            :key="type.idLoaiPhong" 
            :name="type.idLoaiPhong"
          >
            <template #tab>
              <div class="flex items-center gap-2">
                <span>{{ type.tenLoaiPhong }}</span>
                <n-tag size="tiny" round>{{ type.rooms.length }} trống</n-tag>
              </div>
            </template>

            <n-scrollbar style="max-height: 400px">
              <div v-if="type.rooms.length === 0" class="py-10">
                <n-empty description="Không có phòng trống nào phù hợp cho loại phòng này trong thời gian đã chọn" />
              </div>
              <n-grid v-else :cols="24" :x-gap="12" :y-gap="12">
                <n-grid-item 
                  v-for="room in type.rooms" 
                  :key="room.id" 
                  :span="6"
                >
                  <div 
                    class="room-card p-3 rounded border cursor-pointer transition-all hover:shadow-md"
                    :class="selectedRoomsByType[type.idLoaiPhong]?.includes(room.id) 
                      ? 'border-blue-500 bg-blue-50' 
                      : 'border-gray-200'"
                    @click="handleToggleRoom(type.idLoaiPhong, room.id, assignmentStatus.find((s: any) => s.idLoaiPhong === type.idLoaiPhong)?.totalNeeded || 0)"
                  >
                    <div class="flex justify-between items-start">
                      <div>
                        <div class="font-bold text-base">{{ room.tenPhong }}</div>
                        <div class="text-xs text-gray-500">Tầng {{ room.tang }} | {{ room.maPhong }}</div>
                      </div>
                      <n-checkbox 
                        :checked="selectedRoomsByType[type.idLoaiPhong]?.includes(room.id)"
                        @click.stop
                        @update:checked="() => handleToggleRoom(type.idLoaiPhong, room.id, assignmentStatus.find((s: any) => s.idLoaiPhong === type.idLoaiPhong)?.totalNeeded || 0)"
                      />
                    </div>
                    <div class="mt-2 flex flex-wrap gap-1">
                      <n-tag v-for="tag in room.tags" :key="tag.id" size="tiny" :color="{ color: tag.mau, textColor: '#fff' }">
                        {{ tag.ten }}
                      </n-tag>
                    </div>
                  </div>
                </n-grid-item>
              </n-grid>
            </n-scrollbar>
          </n-tab-pane>
        </n-tabs>

        <div v-else-if="!isLoading" class="py-20 text-center text-gray-400">
          <nova-icon icon="carbon:warning" :size="48" class="mb-2" />
          <p>Không tìm thấy nhu cầu gán phòng cho phiếu đặt này</p>
        </div>
      </div>
    </n-spin>

    <template #action>
      <n-space justify="end">
        <n-button @click="modalVisible = false">Đóng</n-button>
        <n-button 
          type="primary" 
          :disabled="!isAllComplete || isLoading"
          :loading="isLoading"
          @click="handleConfirmAssignment"
        >
          <template #icon>
            <nova-icon icon="carbon:checkmark" />
          </template>
          Xác nhận gán phòng & Chốt phiếu
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.room-card {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.room-card:hover {
  transform: translateY(-2px);
}
</style>
