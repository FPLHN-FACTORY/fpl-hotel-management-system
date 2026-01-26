<script setup lang="ts">
import { ref, watch, h } from 'vue'
import { NModal, NCard, NSelect, NButton } from 'naive-ui'
import type { SelectOption } from 'naive-ui'
import { getAllBookedTheoDoan, type DSPhongDaDatTheoDoanCombox, assignRoom } from '@/service/api/nhansu/doanluutru'

const props = defineProps<{
  show: boolean
  member: any
  bookingId: string
}>()

const emit = defineEmits(['update:show', 'success'])

const roomOptions = ref<SelectOption[]>([])
const roomsRaw = ref<DSPhongDaDatTheoDoanCombox[]>([])
const selectedRoomId = ref<string | null>(null)
const loading = ref(false)

watch(
  () => props.show,
  async (val) => {
    if (!val) return
    loading.value = true
    roomsRaw.value = await getAllBookedTheoDoan(props.bookingId)
    console.log(roomsRaw.value)

    roomOptions.value = roomsRaw.value.map((r) => {
      const isFull = r.soNguoiHienTai >= r.soNguoiToiDa
      const baseLabel = `Phòng ${r.ten} - Tầng ${r.tang} - Giường ${r.soGiuongDon} đơn, ${r.soGiuongDoi} đôi - Số người quy định : ${r.soNguoiQuyDinh} người - Hiện tại ${r.soNguoiHienTai}/${r.soNguoiToiDa} người`

      return {
        value: r.id,
        label: baseLabel + (isFull ? ' (Đã đủ)' : ''),

      }
    })

    selectedRoomId.value = props.member?.phongId ?? null
    loading.value = false
  }
)
const loaiAction = computed(() => {
  return props.member?.phongId ? 'Đổi phòng' : 'Gán phòng'
})
async function handleSubmit() {
  if (!selectedRoomId.value) {
    window.$message.warning('Vui lòng chọn phòng')
    return
  }

  const selectedRoom = roomsRaw.value.find(
    (r) => r.id === selectedRoomId.value
  )

  if (!selectedRoom) return

  if (selectedRoom.soNguoiHienTai >= selectedRoom.soNguoiToiDa) {
    window.$message.warning('Phòng đã đủ người, vui lòng chọn phòng khác')
    return
  }

  if (props.member?.phong?.id === selectedRoomId.value) {
    window.$message.info('Khách đã ở phòng này')
    return
  }


  try {
    await assignRoom(props.member.id, { idPhong: selectedRoomId.value })
    window.$message.success(`${loaiAction.value} thành công`)
    emit('success')
    emit('update:show', false)
  } catch (error: any) {
  console.error('Assign room error FULL:', error)

  const msg =
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Có lỗi xảy ra'

  window.$message.error(msg)
}


}

</script>

<template>
  <NModal :show="show" @update:show="$emit('update:show', $event)">
    <NCard style="width: 800px" :title="`${loaiAction} cho khách hàng`" :bordered="false" role="dialog"
      aria-modal="true">
      <div class="mb-3">
        <div><b>Khách hàng:</b> {{ member?.hoTen }}</div>
        <div class="mt-1">
          <b>Phòng hiện tại:</b>
          <span v-if="member?.phongId">
            Phòng {{ member.tenPhong }}
          </span>
          <span v-else class="text-red-500">
            Chưa gán
          </span>
        </div>
      </div>

      <NSelect v-model:value="selectedRoomId" :options="roomOptions" placeholder="Chọn phòng" clearable
        :loading="loading" />

      <template #footer>
        <div class="flex justify-end gap-2">
          <NButton @click="$emit('update:show', false)">Hủy</NButton>
          <NButton type="primary" @click="handleSubmit">
            Lưu
          </NButton>
        </div>
      </template>
    </NCard>
  </NModal>
</template>