<script setup lang="ts">
import { computed, defineEmits, defineProps, onMounted, ref, watch } from 'vue'
import { addTag, updateTag,changeStatusTag } from '@/service/api/letan/tag'

interface Tag {
  id:string
  tenTag: string
  moTaTag?: string
}

interface Props {
  visible: boolean
  type?: 'add' | 'edit'
  modalData?: Tag | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', visible: boolean): void
  (e: 'refresh'): void
}>()

const modalVisible = computed({
  get: () => props.visible,
  set: (val: boolean) => emit('update:visible', val),
})

function closeModal() {
  modalVisible.value = false
}

const title = computed(() => (props.type === 'edit' ? 'Sửa tag' : 'Thêm tag'))

const defaultTag: Tag = {
  id:'',
  tenTag: '',
  moTaTag: ''
  
}

const formModel = ref<Tag>({ ...defaultTag})



const isSucChuaLocked = ref(false)
const isGiaLocked = ref(false)

// --- Trạng thái phòng ---
const trangThaiOptions = ref([
  { label: 'Hoạt động', value: 'HOAT_DONG' },
  { label: 'Bảo trì', value: 'BAO_TRI' },
  { label: 'Ngưng hoạt động', value: 'NGUNG_HOAT_DONG' },
])



watch(
  () => props.modalData,
  (val) => {
    if (props.type === "edit" && val) {
      formModel.value = {
        id: val.id,              // 🔥 Quan trọng
        tenTag: val.tenTag || "",
        moTaTag: val.moTaTag || ""
      }
    } else {
      formModel.value = { ...defaultTag }
    }
  },
  { immediate: true }
)



async function handleSubmit() {
  try {
    if (!formModel.value.tenTag?.trim()) {
      window.$message.warning('Vui lòng nhập tên tag!')
      return
    }if (!formModel.value.moTaTag?.trim()) {
      window.$message.warning('Vui lòng nhập mô tả tag!')
      return
    }
     if (formModel.value.tenTag.length <= 1) {
    window.$message.warning('Tên tag phải dài hơn 1 ký tự!')
    return
  }

  // 3. Không được chứa ký tự đặc biệt
const validNameRegex = /^[\p{L}0-9_ ]+$/u
  if (!validNameRegex.test(formModel.value.tenTag)) {
    window.$message.warning('Tên tag không được chứa ký tự đặc biệt!')
    return
  }

    const payload = {
      ten: formModel.value.tenTag.trim(),
      moTa: formModel.value.moTaTag?.trim() || ''
    }

    let res

    if (props.type === 'edit' && props.modalData) {
     res = await updateTag(payload, formModel.value.id)
      window.$message.success(res?.message || 'Cập nhật tag thành công!')
    } else {
      res = await addTag(payload)
      window.$message.success(res?.message || 'Thêm tag thành công!')
    }

    emit('refresh')
    closeModal()
    formModel.value = { ...defaultTag }
  } catch (error: any) {
    window.$message.error(error.message || 'Lỗi thao tác tag')
  }
}



</script>

<template>
  <n-modal
    v-model:show="modalVisible"
    :mask-closable="false"
    preset="card"
    :title="title"
    class="w-700px"
    :segmented="{ content: true, action: true }"
  >
  <n-form label-placement="left" :model="formModel" label-align="left" :label-width="120">
  <n-grid :cols="24" :x-gap="18">
    
    <!-- Dòng 1 -->
    <n-form-item-grid-item :span="24" label="Tên tag" path="tenTag">
      <n-input v-model:value="formModel.tenTag" placeholder="Nhập tên tag" />
    </n-form-item-grid-item>

    <!-- Dòng 2 -->
    <n-form-item-grid-item :span="24" label="Mô tả" path="moTaTag">
      <n-input
        v-model:value="formModel.moTaTag"
        type="textarea"
        placeholder="Nhập mô tả tag"
        :autosize="{ minRows: 2, maxRows: 4 }"
      />
    </n-form-item-grid-item>

  </n-grid>
</n-form>



    <template #action>
      <n-space justify="center">
        <n-button @click="closeModal">Hủy</n-button>
        <n-button type="primary" @click="handleSubmit">Lưu</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.w-700px {
  width: 700px;
}
</style>
