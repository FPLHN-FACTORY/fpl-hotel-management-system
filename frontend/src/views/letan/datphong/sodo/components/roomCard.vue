<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMessage } from 'naive-ui'
import type { SoDoPhongResponse, TrangThaiVeSinh } from '@/service/api/letan/sodophong'
import { updateTrangThaiVeSinh } from '@/service/api/letan/sodophong'

const props = defineProps<{ room: SoDoPhongResponse }>()
const emit = defineEmits(['click', 'updateCleanStatus'])

const message = useMessage()
const loading = ref(false)

const menuOptions = computed(() => {
  const current = props.room.trangThaiVeSinh
  const options: { label: string; key: TrangThaiVeSinh }[] = []
  if (current !== 'SACH') options.push({ label: 'Sạch', key: 'SACH' })
  if (current !== 'DANG_DON') options.push({ label: 'Đang dọn', key: 'DANG_DON' })
  if (current !== 'CHUA_DON') options.push({ label: 'Chưa dọn', key: 'CHUA_DON' })
  return options
})

const handleMenuSelect = async (status: TrangThaiVeSinh) => {
  if (loading.value) return
  loading.value = true

  const oldStatus = props.room.trangThaiVeSinh

  try {
    await updateTrangThaiVeSinh(props.room.id, status)
    emit('updateCleanStatus', props.room.id, status)
    message.success(`Cập nhật trạng thái phòng ${props.room.ma} thành công!`)
  } catch (err: any) {
    emit('updateCleanStatus', props.room.id, oldStatus)
    message.error(err.message || 'Cập nhật thất bại')
  } finally {
    loading.value = false
  }
}

const bgColor = computed(() => {
  switch (props.room.trangThaiPhong) {
    case 'TRONG': return 'bg-white border-gray-200 text-gray-900'
    case 'SAP_NHAN': return 'bg-blue-100 border-blue-200 text-gray-900'
    case 'DANG_SU_DUNG': return 'bg-green-100 border-green-200 text-gray-900'
    case 'SAP_TRA': return 'bg-yellow-100 border-yellow-200 text-gray-900'
    case 'QUA_GIO_TRA': return 'bg-red-100 border-red-200 text-gray-900'
    default: return 'bg-gray-100 border-gray-200 text-gray-900'
  }
})

const cleanTagStyle = computed(() => {
  switch (props.room.trangThaiVeSinh) {
    case 'SACH': return { backgroundColor: '#e8f5e9', color: '#2e7d32' }
    case 'DANG_DON': return { backgroundColor: '#FFF3E0', color: '#FB8C00' }
    case 'CHUA_DON':
    default: return { backgroundColor: '#fdecea', color: '#c62828' }
  }
})

const hoverBorderColor = computed(() => {
  switch (props.room.loaiPhong) {
    case 'Phòng Suite': return '#34D399'
    case 'Phòng Đôi': return '#FBBF24'
    case 'Phòng Đơn': return '#F97316'
    default: return '#000000'
  }
})
</script>

<template>
  <n-card
    class="room-card flex flex-col justify-between p-3 rounded-xl cursor-pointer transition-all border"
    :class="bgColor"
    @click="$emit('click', room)"
    size="small"
    style="min-width: 142px; margin-top: 0px;"
    :style="{ '--hover-border': hoverBorderColor, borderColor: hoverBorderColor }"
  >
    <div class="flex justify-between items-center mb-2">
      <n-tag
        size="small"
        round
        :style="{ ...cleanTagStyle, fontWeight: '600', padding: '0.15rem 0.4rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }"
      >
        <n-icon-wrapper :size="16" :color="cleanTagStyle.color" :border-radius="999">
          <nova-icon
            :size="14"
            :icon="props.room.trangThaiVeSinh === 'SACH' ? 'carbon:magic-wand' : 'icon-park-outline:cosmetic-brush'"
          />
        </n-icon-wrapper>
        {{
          props.room.trangThaiVeSinh === 'SACH'
            ? 'Sạch'
            : props.room.trangThaiVeSinh === 'DANG_DON'
            ? 'Đang dọn'
            : 'Chưa dọn'
        }}
      </n-tag>

      <n-dropdown :options="menuOptions" trigger="click" @select="handleMenuSelect">
        <n-button quaternary circle size="tiny" :loading="loading">
          <n-icon-wrapper :size="20">
            <nova-icon icon="icon-park-outline:more-one" color="#ffffff" />
          </n-icon-wrapper>
        </n-button>
      </n-dropdown>
    </div>

    <div class="flex flex-col gap-1 mb-2 truncate">
      <div class="text-base font-semibold text-gray-800 truncate">{{ room.ma }}</div>
      <div class="text-sm text-gray-600" v-if="room.trangThaiPhong === 'TRONG'">
        {{ room.price ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(room.price) : '-' }}/đêm
      </div>
    </div>

    <div class="flex justify-end mt-auto">
      <n-tag
        v-if="room.trangThaiPhong !== 'TRONG'"
        size="small"
        round
        style="background-color: rgba(255,255,255,0.4); color:#333; font-weight:500; font-size:0.75rem;"
      >
        {{ room.stayDays || 0 }} / {{ room.totalDays || 0 }} ngày
      </n-tag>
    </div>
  </n-card>
</template>

<style scoped>
.room-card {
  min-height: 160px;
  transition: all 0.2s ease;
  border-width: 1px;
  transform: translateY(-2px);
  box-shadow: 12px 12px var(--hover-border);
}

.room-card:hover {
  transform: translateY(-4px);
  box-shadow: 16px 16px var(--hover-border);
}
</style>
