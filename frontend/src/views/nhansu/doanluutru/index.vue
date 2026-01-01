<script setup lang="ts">
import { h, onMounted, ref, reactive } from 'vue'
import { NButton, NSpace, NTag, NIcon, NDataTable, NCard, NForm, NGrid, NFormItemGi, NInput } from 'naive-ui'
import { Add, Rotate } from '@vicons/carbon'
import { getAllGroups, type DoanLuuTru } from '@/service/api/nhansu/doanluutru'
import MemberDrawer from './components/MemberDrawer.vue'
import CreateGroupModal from './components/CreateGroupModal.vue'

const data = ref<DoanLuuTru[]>([])
const loading = ref(false)
const showMemberDrawer = ref(false)
const showCreateModal = ref(false)
const selectedGroupId = ref('')

const searchModel = reactive({
  tuKhoa: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res: any = await getAllGroups({ tuKhoa: searchModel.tuKhoa })
    if (res && res.data && Array.isArray(res.data)) {
      data.value = res.data
    } else if (Array.isArray(res)) {
      data.value = res
    } else {
      data.value = []
    }
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchModel.tuKhoa = ''
  fetchData()
}

function handleManageMembers(row: DoanLuuTru) {
  selectedGroupId.value = row.id
  showMemberDrawer.value = true
}

const columns: any = [
  { title: 'Mã đoàn', key: 'maDoan', align: 'center' },
  { title: 'Tên đoàn', key: 'tenDoan', align: 'center' },
  { title: 'Trưởng đoàn', key: 'truongDoan', align: 'center', render: (row: any) => row.truongDoan?.hoTen || '-' },
  { title: 'Booking', key: 'datPhong', align: 'center', render: (row: any) => row.datPhong?.id ? 'Booking #' + row.datPhong.id.substring(0, 8) : '-' },
  { title: 'Ghi chú', key: 'ghiChu', align: 'center' },
  {
    title: 'Trạng thái', key: 'trangThai', align: 'center',
    render(row: any) {
      const map: any = {
        0: { label: 'Chưa check-in', type: 'info' },
        1: { label: 'Đang ở', type: 'success' },
        2: { label: 'Đã check-out', type: 'default' }
      }
      const st = map[row.trangThai ?? 0]
      return h(NTag, { type: st.type }, { default: () => st.label })
    }
  },
  {
    title: 'Thời gian lưu trú',
    align: 'center',
    render(row: any) {
      if (!row.thoiGianCheckIn || !row.thoiGianCheckOut) return '-'
      const start = new Date(row.thoiGianCheckIn).toLocaleDateString()
      const end = new Date(row.thoiGianCheckOut).toLocaleDateString()
      return `${start} - ${end}`
    }
  },
  {
    title: 'Thao tác',
    key: 'actions',
    align: 'center',
    render(row: DoanLuuTru) {
      return h(NButton, {
        size: 'small',
        type: 'info',
        onClick: () => handleManageMembers(row)
      }, { default: () => 'Chi tiết thành viên' })
    }
  }
]

onMounted(fetchData)
</script>

<template>
  <NSpace vertical size="large" class="p-4">
    <NCard>
      <NForm :model="searchModel" label-placement="left">
        <NGrid :cols="24" :x-gap="24">
          <NFormItemGi :span="8" label="Tên">
            <NInput v-model:value="searchModel.tuKhoa" placeholder="Tên đoàn, Mã đoàn, Trưởng đoàn..."
              @keyup.enter="fetchData" />
          </NFormItemGi>
          <NFormItemGi :span="6">
            <NButton strong secondary @click="handleReset">
              Làm mới
            </NButton>
          </NFormItemGi>
        </NGrid>
      </NForm>
    </NCard>

    <NCard :bordered="false">
      <div class="flex gap-4 mb-3">
        <NButton type="primary" @click="showCreateModal = true">Thêm đoàn lưu trú</NButton>
      </div>

      <NDataTable :columns="columns" :data="data" :loading="loading" />
    </NCard>

    <MemberDrawer v-model:show="showMemberDrawer" :group-id="selectedGroupId" />
    <CreateGroupModal v-model:show="showCreateModal" @success="fetchData" />
  </NSpace>
</template>

<style scoped>
/* Tăng font size cho toàn bộ trang đoàn lưu trú */
:deep(.n-card-header__main) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.n-form-item-label) {
  font-size: 17px;
}

:deep(.n-input__input-el),
:deep(.n-input__textarea-el),
:deep(.n-base-selection-label),
:deep(.n-base-selection-input),
:deep(.n-input-number-input),
:deep(.n-button__content) {
  font-size: 17px;
}

:deep(.n-data-table-th),
:deep(.n-data-table-td) {
  font-size: 17px;
}

:deep(.n-data-table-th__title) {
  font-size: 17px;
  font-weight: 600;
}

:deep(.n-tag) {
  font-size: 17px;
}

:deep(.n-pagination) {
  font-size: 17px;
}

:deep(.n-base-select-option__content) {
  font-size: 17px;
}

:deep(.n-alert) {
  font-size: 17px;
}
</style>
