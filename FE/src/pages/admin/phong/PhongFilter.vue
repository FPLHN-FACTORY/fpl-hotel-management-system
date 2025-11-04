<template>
  <DivCustom label="Bộ lọc">
    <template #icon>
      <FilterOutlined />
    </template>

    <div class="flex flex-col gap-3 w-full">

      <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
        <a-input
          v-model:value="filterParams.q"
          placeholder="Tìm kiếm theo mã hoặc tên phòng..."
          allow-clear
          @change="handleFilterChange"
          class="h-[40px]"
        >
          <template #prefix>
            <SearchOutlined class="text-gray-400" />
          </template>
        </a-input>


        <a-select
          v-model:value="filterParams.loaiPhong"
          placeholder="Tất cả loại phòng"
          allow-clear
          class="h-[40px]"
          @change="handleFilterChange"
        >
          <a-select-option value="Phòng 3 giường">Phòng 3 giường</a-select-option>
          <a-select-option value="Phòng 2 giường đơn">Phòng 2 giường đơn</a-select-option>
          <a-select-option value="Phòng 1 giường đôi 1 giường đơn">Phòng 1 giường đôi 1 giường đơn</a-select-option>
        </a-select>

        <a-select
          v-model:value="filterParams.trangThaiPhong"
          placeholder="Tất cả trạng thái"
          allow-clear
          class="h-[40px]"
          @change="handleFilterChange"
        >
          <a-select-option value="TRONG">Trống</a-select-option>
          <a-select-option value="DA_DAT">Đã đặt</a-select-option>
          <a-select-option value="DANG_SU_DUNG">Đang sử dụng</a-select-option>
        </a-select>

        <div class="flex gap-2">
          <a-select
            v-model:value="sortParams.sortBy"
            placeholder="Sắp xếp theo"
            class="flex-1 h-[40px]"
            @change="handleSortChange"
          >
            <a-select-option value="maPhong">Mã phòng</a-select-option>
            <a-select-option value="tenPhong">Tên phòng</a-select-option>
            <a-select-option value="giaHienTai">Giá</a-select-option>
            <a-select-option value="sucChua">Sức chứa</a-select-option>
            <a-select-option value="trangThaiPhong">Trạng thái</a-select-option>
          </a-select>

          <a-button
            type="default"
            class="!flex !items-center !justify-center h-[40px] w-[40px] p-0"
            @click="toggleSortDirection"
          >
            <ArrowUpOutlined v-if="sortParams.orderBy === 'ASC'" />
            <ArrowDownOutlined v-else />
          </a-button>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <a-button
          type="link"
          size="small"
          @click="activeKey = activeKey.length > 0 ? [] : ['1']"
          class="px-0 text-[#1890ff]"
        >
          <template #icon>
            <DownOutlined v-if="activeKey.length === 0" />
            <UpOutlined v-else />
          </template>
          Bộ lọc nâng cao
        </a-button>

        <a-button
          type="link"
          size="small"
          @click="handleReset"
          class="ml-auto text-gray-500 hover:text-[#1890ff]"
        >
          <ReloadOutlined />
          <span class="ml-1">Đặt lại</span>
        </a-button>
      </div>

      <a-collapse v-model:activeKey="activeKey" :bordered="false">
        <a-collapse-panel key="1" :show-arrow="false">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 pt-1">
            <div>
              <label class="block text-sm text-gray-600 mb-2 font-medium">Khoảng giá (VNĐ)</label>
              <div class="flex gap-2 items-center">
                <a-input-number
                  v-model:value="filterParams.giaMin"
                  placeholder="Từ"
                  :min="0"
                  :formatter="v => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="v => v!.replace(/,/g, '')"
                  class="flex-1"
                  @change="handleFilterChange"
                />
                <span class="text-gray-500">-</span>
                <a-input-number
                  v-model:value="filterParams.giaMax"
                  placeholder="Đến"
                  :min="0"
                  :formatter="v => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                  :parser="v => v!.replace(/,/g, '')"
                  class="flex-1"
                  @change="handleFilterChange"
                />
              </div>
            </div>

            <div>
              <label class="block text-sm text-gray-600 mb-2 font-medium">Sức chứa (người)</label>
              <div class="flex gap-2 items-center">
                <a-input-number
                  v-model:value="filterParams.sucChuaMin"
                  placeholder="Từ"
                  :min="0"
                  class="flex-1"
                  @change="handleFilterChange"
                />
                <span class="text-gray-500">-</span>
                <a-input-number
                  v-model:value="filterParams.sucChuaMax"
                  placeholder="Đến"
                  :min="0"
                  class="flex-1"
                  @change="handleFilterChange"
                />
              </div>
            </div>
          </div>
        </a-collapse-panel>
      </a-collapse>
    </div>
  </DivCustom>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import DivCustom from '@/components/custom/Div/DivCustom.vue'
import {
  FilterOutlined,
  SearchOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  ReloadOutlined,
  DownOutlined,
  UpOutlined
} from '@ant-design/icons-vue'

const emit = defineEmits(['filter-change', 'sort-change'])

const activeKey = ref<string[]>([])

const filterParams = reactive({
  q: undefined as string | undefined,
  loaiPhong: undefined as string | undefined,
  trangThaiPhong: undefined as string | undefined,
  giaMin: undefined as number | undefined,
  giaMax: undefined as number | undefined,
  sucChuaMin: undefined as number | undefined,
  sucChuaMax: undefined as number | undefined
})

const sortParams = reactive({
  sortBy: 'maPhong',
  orderBy: 'ASC'
})

const handleFilterChange = () => {
  emit('filter-change', { ...filterParams })
}

const handleSortChange = () => {
  emit('sort-change', { ...sortParams })
}

const toggleSortDirection = () => {
  sortParams.orderBy = sortParams.orderBy === 'ASC' ? 'DESC' : 'ASC'
  handleSortChange()
}

const handleReset = () => {
  Object.keys(filterParams).forEach(k => (filterParams[k as keyof typeof filterParams] = undefined))
  sortParams.sortBy = 'maPhong'
  sortParams.orderBy = 'ASC'
  activeKey.value = []
  emit('filter-change', { ...filterParams })
  emit('sort-change', { ...sortParams })
}
</script>

<style scoped>
:deep(.ant-collapse) {
  background: transparent;
}

:deep(.ant-collapse-item) {
  border: none !important;
}

:deep(.ant-collapse-content) {
  border-top: 1px solid #f0f0f0;
  background: transparent;
}

:deep(.ant-input),
:deep(.ant-select-selector),
:deep(.ant-input-number) {
  border-radius: 8px;
  transition: all 0.2s ease-in-out;
}

:deep(.ant-input:hover),
:deep(.ant-select-selector:hover),
:deep(.ant-input-number:hover) {
  border-color: #1890ff !important;
}

:deep(.ant-input:focus),
:deep(.ant-select-focused .ant-select-selector),
:deep(.ant-input-number-focused) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}
</style>
