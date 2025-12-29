<template>
  <n-card
    :class="['rounded-xl shadow-sm cursor-pointer flex flex-col justify-between p-4 transition-all border h-40', bgColor]"
    size="small"
    @click="$emit('click', room)"
  >
    <!-- Header: Trạng thái dọn phòng + menu -->
    <div class="flex justify-between items-center mb-2">
      <n-tag size="small" round :style="cleanTagStyle" class="flex items-center gap-1">
        <n-icon-wrapper :size="18" :color="cleanTagStyle.color" :border-radius="999">
          <nova-icon 
            :size="16" 
            :icon="room.cleanStatus === 'clean' ? 'carbon:magic-wand' : 'icon-park-outline:cosmetic-brush'" 
          />
        </n-icon-wrapper>
        {{ room.cleanStatus === 'clean' ? 'Sạch' : 'Chưa dọn' }}
      </n-tag>

      <n-dropdown :options="menuOptions" trigger="click" @select="handleMenuSelect">
        <n-button quaternary circle size="tiny">
          <n-icon-wrapper :size="26"  :border-radius="999" style="background-color: aliceblue;">
            <nova-icon :size="20" icon="icon-park-outline:more-one" color="black"/>
          </n-icon-wrapper>
        </n-button>
      </n-dropdown>
    </div>

    <!-- Info phòng -->
    <div class="flex flex-col gap-1 mb-2">
      <div class="text-base font-semibold text-gray-800 truncate">
        {{ room.code }}
      </div>
      <div class="text-sm text-gray-600" v-if="room.status === 'empty'">
        {{ formatPrice(room.price) }}/đêm
      </div>
    </div>

    <!-- Trạng thái stayDays -->
    <div class="flex justify-end mt-auto">
      <n-tag
        v-if="room.status !== 'empty'"
        size="small"
        round
        :style="{
          backgroundColor: '#ffffff70',
          color: '#333',
          fontWeight: '500',
          fontSize: '0.75rem'
        }"
      >
        {{ room.stayDays || 0 }} / {{ room.totalDays || 0 }} ngày
      </n-tag>
    </div>
  </n-card>
</template>

<script setup>
import { computed } from 'vue'
import { useMessage } from 'naive-ui'

const props = defineProps({ room: Object })
const emit = defineEmits(['click', 'updateCleanStatus'])
const message = useMessage()

const menuOptions = [
  { label: 'Chuyển trạng thái dọn phòng', key: 'toggleClean' }
]

const handleMenuSelect = (key) => {
  if (key === 'toggleClean') {
    const newStatus = props.room.cleanStatus === 'clean' ? 'notClean' : 'clean'
    emit('updateCleanStatus', newStatus)
    message.success(
      `Phòng ${props.room.code} → ${newStatus === 'clean' ? 'Đã dọn' : 'Chưa dọn'}`
    )
  }
}

// màu nền theo trạng thái phòng
const bgColor = computed(() => {
  switch (props.room.status) {
    case 'empty':
      return 'bg-white border-gray-200 text-gray-900'
    case 'soonCheckin':
      return 'bg-blue-100 border-blue-200 text-gray-900'
    case 'inUse':
      return 'bg-green-100 border-green-200 text-gray-900'
    case 'soonCheckout':
      return 'bg-yellow-100 border-yellow-200 text-gray-900'
    case 'overdue':
      return 'bg-red-100 border-red-200 text-gray-900'
    default:
      return 'bg-gray-100 border-gray-200 text-gray-900'
  }
})

// style cho tag trạng thái dọn phòng
const cleanTagStyle = computed(() => ({
  backgroundColor: props.room.cleanStatus === 'clean' ? '#e8f5e9' : '#fdecea',
  color: props.room.cleanStatus === 'clean' ? '#2e7d32' : '#c62828',
  fontWeight: '600',
  padding: '0.15rem 0.45rem',
  fontSize: '0.75rem',
  display: 'flex',
  alignItems: 'center',
  gap: '0.25rem'
}))

const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
</script>

<style scoped>
.n-card {
  transition: all 0.2s ease;
}

.n-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}
</style>
