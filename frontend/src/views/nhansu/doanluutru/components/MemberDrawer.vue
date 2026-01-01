<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { NDrawer, NDrawerContent, NDataTable, NButton, NDivider, NForm, NInput, NSelect, NGrid, NFormItemGi, NIcon } from 'naive-ui'
import { getGroupMembers, addMember, type ChiTietDoan } from '@/service/api/nhansu/doanluutru'
import { QrCode } from '@vicons/carbon'
import ScanQrModal from '@/components/common/ScanQrModal.vue'

const props = defineProps<{ show: boolean, groupId: string }>()
const emit = defineEmits(['update:show'])

const members = ref<ChiTietDoan[]>([])
const loading = ref(false)

const form = reactive({
    hoTen: '',
    soDienThoai: '',
    email: '',
    soCccd: '',
    vaiTro: 'Thành viên'
})

const roleOptions = [
    { label: 'Trưởng đoàn', value: 'Trưởng đoàn' },
    { label: 'Thành viên', value: 'Thành viên' }
]

async function fetchMembers() {
    if (!props.groupId) return
    loading.value = true
    try {
        const res: any = await getGroupMembers(props.groupId)
        if (res && res.data && Array.isArray(res.data)) {
            members.value = res.data
        } else if (Array.isArray(res)) {
            members.value = res
        } else {
            members.value = []
        }
    } finally {
        loading.value = false
    }
}

const onlyAllowNumber = (value: string) => !value || /^\d+$/.test(value)

async function handleAddMember() {
    if (!form.hoTen) {
        window.$message.error('Vui lòng nhập họ tên')
        return;
    }

    // Validate Phone
    if (form.soDienThoai) {
        if (!/^\d{10}$/.test(form.soDienThoai)) {
            window.$message.error('Số điện thoại phải là 10 chữ số')
            return
        }
    }

    // Validate CCCD
    if (form.soCccd) {
        if (!/^\d{9,12}$/.test(form.soCccd)) {
            window.$message.error('Số CCCD phải từ 9-12 chữ số')
            return
        }
    }

    try {
        await addMember({
            idDoanLuuTru: props.groupId,
            ...form
        })
        window.$message.success('Thêm thành viên thành công')
        fetchMembers()
        form.hoTen = ''
        form.soDienThoai = ''
        form.email = ''
        form.soCccd = ''
    } catch (e: any) {
        window.$message.error(e.message || 'Lỗi thêm thành viên')
    }
}

const showScan = ref(false)

function handleScanSuccess(data: any) {
    if (data.name) form.hoTen = data.name
    if (data.cccd) form.soCccd = data.cccd
    window.$message.success('Đã quét thành công: ' + data.name)
}

watch(() => props.show, (val) => {
    if (val) fetchMembers()
})

const columns = [
    { title: 'Tên', key: 'khachHang.hoTen' },
    { title: 'SĐT', key: 'khachHang.soDienThoai' },
    { title: 'CCCD', key: 'khachHang.soCCD', render: (row: any) => row.khachHang?.soCCD || row.khachHang?.soCccd || row.khachHang?.soCCD || '-' },
    { title: 'Vai trò', key: 'vaiTro' }
]
</script>

<template>
    <NDrawer :show="show" @update:show="$emit('update:show', $event)" width="800">
        <NDrawerContent title="Quản lý thành viên đoàn" closable>
            <div class="mb-4">
                <h3 class="text-lg font-medium mb-2">Danh sách thành viên</h3>
                <NDataTable :columns="columns" :data="members" :loading="loading" />
            </div>

            <NDivider />

            <div>
                <h3 class="text-lg font-medium mb-2">Thêm thành viên mới</h3>
                <NForm :model="form" label-placement="left" label-width="120">
                    <NGrid :cols="2" :x-gap="12">
                        <NFormItemGi label="Họ tên">
                            <NInput v-model:value="form.hoTen" />
                        </NFormItemGi>
                        <NFormItemGi label="Số ĐT">
                            <NInput v-model:value="form.soDienThoai" :allow-input="onlyAllowNumber" :maxlength="10"
                                show-count />
                        </NFormItemGi>
                        <NFormItemGi label="CCCD">
                            <div class="flex gap-2 w-full">
                                <NInput v-model:value="form.soCccd" :allow-input="onlyAllowNumber" :maxlength="12"
                                    show-count />
                                <NButton @click="showScan = true" secondary>
                                    <template #icon>
                                        <NIcon :component="QrCode" />
                                    </template>
                                </NButton>
                            </div>
                        </NFormItemGi>
                        <NFormItemGi label="Email">
                            <NInput v-model:value="form.email" />
                        </NFormItemGi>
                        <NFormItemGi label="Vai trò">
                            <NSelect v-model:value="form.vaiTro" :options="roleOptions" />
                        </NFormItemGi>
                    </NGrid>
                    <div class="flex justify-end mt-2">
                        <NButton type="primary" @click="handleAddMember">Thêm thành viên</NButton>
                    </div>
                </NForm>
            </div>

            <template #footer>
                <div class="flex justify-end">
                    <NButton @click="$emit('update:show', false)">Đóng</NButton>
                </div>
            </template>
        </NDrawerContent>
    </NDrawer>
    <ScanQrModal v-model:show="showScan" @success="handleScanSuccess" />
</template>
