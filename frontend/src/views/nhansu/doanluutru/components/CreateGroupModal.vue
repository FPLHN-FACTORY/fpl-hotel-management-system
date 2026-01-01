<script setup lang="ts">
import { reactive, watch, ref } from 'vue'
import { NModal, NCard, NForm, NInput, NButton, NInputNumber, NGrid, NFormItemGi, NIcon } from 'naive-ui'
import { QrCode } from '@vicons/carbon'
import { createGroup } from '@/service/api/nhansu/doanluutru'
import ScanQrModal from '@/components/common/ScanQrModal.vue'

const props = defineProps<{ show: boolean }>()
const emit = defineEmits(['update:show', 'success'])

const showScan = ref(false)

const onlyAllowNumber = (value: string) => !value || /^\d+$/.test(value)

const formModel = reactive({
    tenDoan: '',
    ghiChu: '',
    hoTenTruongDoan: '',
    soDienThoaiTruongDoan: '',
    emailTruongDoan: '',
    soCccdTruongDoan: '',
    soNguoi: 1
})

watch(() => props.show, (val) => {
    if (val) {
        formModel.tenDoan = ''
        formModel.ghiChu = ''
        formModel.hoTenTruongDoan = ''
        formModel.soDienThoaiTruongDoan = ''
        formModel.emailTruongDoan = ''
        formModel.soCccdTruongDoan = ''
        formModel.soNguoi = 1
    }
})

function handleScanSuccess(data: any) {
    if (data.name) formModel.hoTenTruongDoan = data.name
    if (data.cccd) formModel.soCccdTruongDoan = data.cccd
    window.$message.success('Đã quét thành công CCCD: ' + data.name)
}

async function handleSubmit() {
    if (!formModel.hoTenTruongDoan || !formModel.soDienThoaiTruongDoan || !formModel.soCccdTruongDoan) {
        window.$message.warning('Vui lòng nhập đầy đủ thông tin trưởng đoàn')
        return
    }

    // Validate Phone
    const phoneRegex = /^\d{10}$/
    if (!phoneRegex.test(formModel.soDienThoaiTruongDoan)) {
        window.$message.error('Số điện thoại phải là 10 chữ số')
        return
    }

    // Validate CCCD
    const cccdRegex = /^\d{9,12}$/
    if (!cccdRegex.test(formModel.soCccdTruongDoan)) {
        window.$message.error('Số CCCD phải từ 9-12 chữ số')
        return
    }

    try {
        await createGroup(formModel)
        window.$message.success('Tạo đoàn thành công')
        emit('update:show', false)
        emit('success')
    } catch (e: any) {
        window.$message.error(e.message || 'Lỗi')
    }
}
</script>

<template>
    <NModal :show="show" @update:show="$emit('update:show', $event)">
        <NCard style="width: 900px" title="Tạo đoàn mới" :bordered="false" size="huge" role="dialog" aria-modal="true">
            <NForm :model="formModel" label-placement="left" label-width="140">
                <NGrid :cols="2" :x-gap="12">
                    <NFormItemGi :span="2" label="Tên đoàn (Tùy chọn)">
                        <NInput v-model:value="formModel.tenDoan"
                            placeholder="Để trống sẽ tự sinh theo tên trưởng đoàn..." />
                    </NFormItemGi>

                    <NFormItemGi :span="2">
                        <div class="text-base font-bold mb-2">Thông tin trưởng đoàn</div>
                    </NFormItemGi>

                    <NFormItemGi label="Họ tên">
                        <NInput v-model:value="formModel.hoTenTruongDoan" placeholder="Họ và tên..." />
                    </NFormItemGi>
                    <NFormItemGi label="Số CCCD">
                        <div class="flex gap-2 w-full">
                            <NInput v-model:value="formModel.soCccdTruongDoan" placeholder="Số CCCD..."
                                :allow-input="onlyAllowNumber" :maxlength="12" show-count />
                            <NButton @click="showScan = true" secondary>
                                <template #icon>
                                    <NIcon :component="QrCode" />
                                </template>
                            </NButton>
                        </div>
                    </NFormItemGi>
                    <NFormItemGi label="Số điện thoại">
                        <NInput v-model:value="formModel.soDienThoaiTruongDoan" placeholder="Số điện thoại..."
                            :allow-input="onlyAllowNumber" :maxlength="10" show-count />
                    </NFormItemGi>
                    <NFormItemGi label="Email">
                        <NInput v-model:value="formModel.emailTruongDoan" placeholder="Email..." />
                    </NFormItemGi>

                    <NFormItemGi label="Số người">
                        <NInputNumber v-model:value="formModel.soNguoi" :min="1" style="width: 100%" />
                    </NFormItemGi>

                    <NFormItemGi :span="2" label="Ghi chú">
                        <NInput type="textarea" v-model:value="formModel.ghiChu" />
                    </NFormItemGi>
                </NGrid>
            </NForm>
            <template #footer>
                <div class="flex justify-end gap-2">
                    <NButton @click="$emit('update:show', false)">Hủy</NButton>
                    <NButton type="primary" @click="handleSubmit">Lưu</NButton>
                </div>
            </template>
        </NCard>
    </NModal>
    <ScanQrModal v-model:show="showScan" @success="handleScanSuccess" />
</template>
