<script setup lang="ts">
import { reactive, watch, ref } from 'vue'
import { NModal, NCard, NForm, NInput, NButton, NInputNumber, NGrid, NFormItemGi, NIcon } from 'naive-ui'
import { QrCode } from '@vicons/carbon'
import { createGroup, getAllBooked } from '@/service/api/nhansu/doanluutru'
import ScanQrModal from '@/components/common/ScanQrModal.vue'
import { CameraOutline, QrCodeOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import CccdOCR from '@/components/custom/CccdOCR.vue'
import CccdScannner from '@/components/custom/CccdScanner.vue'
import type { DataCombobox } from '@/typings/api/api.common'
const props = defineProps<{ show: boolean }>()
const emit = defineEmits(['update:show', 'success'])



const onlyAllowNumber = (value: string) => !value || /^\d+$/.test(value)
const showScanner = ref(false)
const showOCRModal = ref(false)


const dataDaDatPhong = ref<DataCombobox>([])
const formModel = reactive({
    tenDoan: '',
    ghiChu: '',
    hoTenTruongDoan: '',
    soDienThoaiTruongDoan: '',
    ngaySinhTruongDoan: null,
    gioiTinhTruongDoan: null,
    loaiGiayToTruongDoan: null,
    soGiayToTruongDoan: '',
    idDatPhong:null

})
const loaiGiayToOptions = ref([
    { label: 'CCCD', value: 0 },
    { label: 'Hộ chiếu', value: 1 }
])

const gioiTinhToOptions = ref([
    { label: 'Nam', value: 0 },
    { label: 'Nữ', value: 1 },
    { label: 'Khác', value: 2 }
])
onMounted(() => {
    loadDataDatPhongCombobox()
});

async function loadDataDatPhongCombobox() {
    const res = await getAllBooked();
    dataDaDatPhong.value = [
        ...res.data
    ];
}

watch(() => props.show, (val) => {
    if (val) {
        formModel.tenDoan = ''
        formModel.ghiChu = ''
        formModel.hoTenTruongDoan = ''
        formModel.ngaySinhTruongDoan = null
 formModel.idDatPhong=null
        formModel.loaiGiayToTruongDoan = null
        formModel.gioiTinhTruongDoan = null
        formModel.soDienThoaiTruongDoan = ''
        formModel.soGiayToTruongDoan = ''

    }
})


async function handleSubmit() {
    if (!formModel.hoTenTruongDoan || !formModel.soDienThoaiTruongDoan || !formModel.soGiayToTruongDoan || formModel.loaiGiayToTruongDoan == null || formModel.gioiTinhTruongDoan == null || formModel.ngaySinhTruongDoan == null) {
        window.$message.warning('Vui lòng nhập đầy đủ thông tin trưởng đoàn')
        return
    }

    // Validate Phone
    const phoneRegex = /^\d{10}$/
    if (!phoneRegex.test(formModel.soDienThoaiTruongDoan)) {
        window.$message.error('Số điện thoại phải là 10 chữ số')
        return
    }


    try {
        const payload = {
            ...formModel,
            ngaySinhTruongDoan: dayjs(formModel.ngaySinhTruongDoan).format('YYYY-MM-DD')
        }

        await createGroup(payload)

        window.$message.success('Tạo đoàn thành công')
        emit('update:show', false)
        emit('success')
    } catch (e: any) {
        window.$message.error(  e?.response?.data?.message || 'Lỗi')
       
    }
}
async function onScanResult(data: any) {
    // data sẽ có các trường: cccd, hoTen, ngaySinh, gioiTinh, diaChi, ngayCap
    console.log('CCCD data:', data)


    const val = data
    // reset trước khi điền dữ liệu QR
    const str = val.ngaySinh; // ddMMyyyy
    const ngay = parseInt(str.slice(0, 2));
    const thang = parseInt(str.slice(2, 4));
    const nam = parseInt(str.slice(4, 8));
    formModel.ngaySinhTruongDoan = new Date(nam, thang - 1, ngay);

    formModel.hoTenTruongDoan = val.hoTen || ''
    formModel.soGiayToTruongDoan = val.soGiayTo || ''

    formModel.gioiTinhTruongDoan = val.gioiTinh === 'Nam' ? 0 : (val.gioiTinh === 'Nữ' ? 1 : 2);



    showScanner.value = false // đóng scanner
}


async function onOCRResult(data: any) {
    if (!data || Object.keys(data).length === 0) {
        window.$message.error(
            "Đọc dữ liệu ảnh không thành công (Ảnh mờ nhòe hoặc loại giấy tờ chưa được hỗ trợ,...)"
        )

        return
    }
    console.log('OCR:', data)


    const val = data
    if (val.ngaySinh) {
        const [ngay, thang, nam] = val.ngaySinh.split('/').map(Number)
        formModel.ngaySinhTruongDoan = new Date(nam, thang - 1, ngay)
    } else {
        formModel.ngaySinhTruongDoan = null
    }

    formModel.loaiGiayToTruongDoan = val.loaiGiayTo ?? null
    formModel.hoTenTruongDoan = val.hoTen || ''
    formModel.soGiayToTruongDoan = val.soGiayTo || ''
    formModel.gioiTinhTruongDoan = val.gioiTinh === 'Nam' ? 0 : (val.gioiTinh === 'Nữ' ? 1 : val.gioiTinh === 'X' ? 2 : null);
    showOCRModal.value = false

}
function handleQuetCCCD() {

    if (formModel.loaiGiayToTruongDoan !== 0) {
        window.$message.warning('Vui lòng chọn loại giấy tờ là CCCD để quét!')
        return
    }
    showScanner.value = true
}
function handleOCRCCCD() {

    showOCRModal.value = true
}


</script>

<template>
    <CccdOCR v-model="showOCRModal" @result="onOCRResult" @close="showOCRModal = false" />

    <CccdScanner v-model="showScanner" @scan-result="onScanResult" />
    <NModal :show="show" @update:show="$emit('update:show', $event)">
        <NCard style="width: 900px" title="Tạo đoàn mới" :bordered="false" size="huge" role="dialog" aria-modal="true">
            <NForm :model="formModel" label-placement="left" label-align="left" label-width="140">
                <NGrid :cols="2" :x-gap="12">
                    <NFormItemGi :span="2" label="Tên đoàn (Tùy chọn)">
                        <NInput v-model:value="formModel.tenDoan"
                            placeholder="Để trống sẽ tự sinh theo tên trưởng đoàn..." />
                    </NFormItemGi>


                    <NFormItemGi :span="2" label="Các phòng đã đặt">
                        <n-select v-model:value="formModel.idDatPhong" :options="dataDaDatPhong"
                            placeholder="Chọn phòng đã đặt" clearable />
                    </NFormItemGi>


                    <NFormItemGi :span="2">
                        <div class="text-base font-bold mb-2">Thông tin trưởng đoàn</div>
                    </NFormItemGi>

                    <NFormItemGi label="Họ tên">
                        <NInput v-model:value="formModel.hoTenTruongDoan" placeholder="Họ và tên..." />
                    </NFormItemGi>

                    <n-form-item-grid-item label="Ngày sinh" path="ngaySinh">
                        <n-date-picker v-model:value="formModel.ngaySinhTruongDoan" type="date"
                            placeholder="Chọn ngày sinh" style="width: 100%;" clearable />
                    </n-form-item-grid-item>
                    <n-form-item-grid-item label="Giới tính" path="gioiTinh">
                        <n-radio-group v-model:value="formModel.gioiTinhTruongDoan">
                            <n-radio v-for="item in gioiTinhToOptions" :key="item.value" :value="item.value">
                                {{ item.label }}
                            </n-radio>
                        </n-radio-group>
                    </n-form-item-grid-item>

                    <NFormItemGi label="Loại giấy tờ" path="loaiGiayTo">
                        <div class="flex gap-2 w-full">
                            <n-select v-model:value="formModel.loaiGiayToTruongDoan" :options="loaiGiayToOptions"
                                placeholder="Chọn loại giấy tờ" clearable />

                            <NButton type="primary" style="border-radius: 6px" @click="handleQuetCCCD">
                                <template #icon>
                                    <NIcon>
                                        <QrCodeOutline />
                                    </NIcon>
                                </template>
                            </NButton>
                            <NButton type="primary" style="border-radius: 6px" @click="handleOCRCCCD">
                                <template #icon>
                                    <NIcon>
                                        <CameraOutline />
                                    </NIcon>
                                </template>
                            </NButton>

                        </div>
                    </NFormItemGi>
                    <NFormItemGi label="Số giấy tờ">
                        <NInput v-model:value="formModel.soGiayToTruongDoan" placeholder="Số giấy tờ ..." clearable />
                    </NFormItemGi>
                    <NFormItemGi label="Số điện thoại">
                        <NInput v-model:value="formModel.soDienThoaiTruongDoan" placeholder="Số điện thoại..."
                            :allow-input="onlyAllowNumber" :maxlength="10" show-count />
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

</template>
