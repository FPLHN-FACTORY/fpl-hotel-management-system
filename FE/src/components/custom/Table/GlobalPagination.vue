<template>
  <div class="flex flex-col md:flex-row items-center justify-between gap-3 px-4 py-3 bg-white rounded-xl shadow-sm">
    <!-- Thông tin hiển thị -->
    <div class="text-sm text-gray-500">
      Hiển thị
      <span class="font-semibold text-gray-900">{{ displayStart }}–{{ displayEnd }}</span>
      /
      <span class="font-semibold text-gray-900">{{ total }}</span>
      kết quả
    </div>

    <!-- Phân trang -->
    <div class="flex items-center gap-3">
      <a-select
        :value="pageSize"
        @change="onChangePageSize"
        size="middle"
        class="!w-[120px]"
      >
        <a-select-option
          v-for="n in pageSizeOptions"
          :key="n"
          :value="n"
        >
          {{ n }} / trang
        </a-select-option>
      </a-select>

      <a-pagination
        :current="current"
        :total="total"
        :pageSize="pageSize"
        :showSizeChanger="false"
        :showLessItems="true"
        @change="onChangePage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  current: number
  total: number
  pageSize: number
  pageSizeOptions?: number[]
}

const props = withDefaults(defineProps<Props>(), {
  current: 1,
  total: 0,
  pageSize: 10,
  pageSizeOptions: () => [10, 20, 30, 50]
})

const emit = defineEmits<{
  change: [page: number, pageSize: number]
  'update:current': [value: number]
  'update:pageSize': [value: number]
}>()

const displayStart = computed(() =>
  props.total === 0 ? 0 : (props.current - 1) * props.pageSize + 1
)
const displayEnd = computed(() =>
  Math.min(props.current * props.pageSize, props.total)
)

const onChangePage = (page: number) => {
  emit('update:current', page)
  emit('change', page, props.pageSize)
}

const onChangePageSize = (size: number) => {
  emit('update:pageSize', size)
  emit('update:current', 1)
  emit('change', 1, size)
}
</script>

<style scoped>
:deep(.ant-select-selector) {
  border-radius: 10px !important;
  background-color: #f9fafb !important;
  border: 1px solid #e5e7eb !important;
}

:deep(.ant-select-selector:hover) {
  border-color: #1677ff !important;
}

:deep(.ant-pagination-item) {
  border-radius: 8px;
}

:deep(.ant-pagination-item-active) {
  font-weight: 600;
  border-color: #1677ff;
  background-color: #e6f4ff;
}

:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  border-radius: 8px;
}
</style>
