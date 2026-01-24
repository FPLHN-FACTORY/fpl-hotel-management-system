<script setup lang="ts">
import { ref, watch, reactive, h } from 'vue'
import { NDrawer, NDrawerContent, NDataTable, NButton, NDivider, NForm, NInput, NSelect, NGrid, NFormItemGi, NIcon, NTooltip, NRadioGroup, NRadio } from 'naive-ui'
import { getGroupMembers, addMember, type ChiTietDoan, type ParamsGetMembers, checkSoLuongToiDa } from '@/service/api/nhansu/doanluutru'
import ScanQrModal from '@/components/common/ScanQrModal.vue'
import { CameraOutline, QrCodeOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import CccdOCR from '@/components/custom/CccdOCR.vue'
import CccdScanner from '@/components/custom/CccdScanner.vue'
import { useDialog } from 'naive-ui'
import AssignRoomModal from './AssignRoomModal.vue'
import { getCustomerByGiayTo, type GiayToRequest, updateKhachHangLuuTru } from '@/service/api/nhansu/khachhang'
const dialog = useDialog()

const props = defineProps<{ show: boolean, groupId: string }>()
const emit = defineEmits(['update:show'])

const members = ref<ChiTietDoan[]>([])
const loading = ref(false)
const totalItems = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const showScanner = ref(false)
const showOCRModal = ref(false)
const showAssignRoomModal = ref(false)
const selectedMember = ref<any>(null)
const giayToRequest = ref<GiayToRequest>({
    loaiGiayTo: null,
    soGiayTo: ''
})


const form = reactive({
    hoTen: '',
    gioiTinh: null as number | null,
    ngaySinh: null as Date | null,
    loaiGiayTo: null as number | null,
    soGiayTo: '',
    vaiTro: 1 as number
})
const formSearch = reactive({
    hoTen: '',

    loaiGiayTo: null,
    soGiayTo: '',

})
const roleOptions = [
    { label: 'Trưởng đoàn', value: 'Trưởng đoàn' },
    { label: 'Thành viên', value: 'Thành viên' }
]
const loaiGiayToOptions = [
    { label: 'CCCD', value: 0 },
    { label: 'Hộ chiếu', value: 1 }
]

const gioiTinhToOptions = [
    { label: 'Nam', value: 0 },
    { label: 'Nữ', value: 1 },
    { label: 'Khác', value: 2 }
]
async function fetchMembers(page = 1) {
    if (!props.groupId) return
    loading.value = true
    try {
        const params: ParamsGetMembers = {
            page,
            size: pageSize.value,

        }
        if (formSearch.hoTen)
            params.hoTen = formSearch.hoTen

        if (formSearch.soGiayTo)
            params.soGiayTo = formSearch.soGiayTo
        if (formSearch.loaiGiayTo !== null)
            params.loaiGiayTo = formSearch.loaiGiayTo

        const res: any = await getGroupMembers(params, props.groupId)

        members.value = res.items


        totalItems.value = res.totalItems
        currentPage.value = res.currentPage


    } finally {
        loading.value = false
    }
}

async function openUpdateCustomerModal() {
    giayToRequest.value.loaiGiayTo = form.loaiGiayTo
    giayToRequest.value.soGiayTo = form.soGiayTo

    // 2. Truyền VALUE
    const res = await getCustomerByGiayTo(giayToRequest.value)
    console.log("timmm", res.data)
    const payload = {
        ...form,
        ngaySinh: form.ngaySinh
            ? dayjs(form.ngaySinh).format('YYYY-MM-DD')
            : ''
    }

    const res1 = await updateKhachHangLuuTru(res.data.id, payload) // ✅
    window.$message.success(res1?.message || 'Cập nhật khách hàng thành công!')
    handleAddMember(true)
    // 4. Reload & reset
    await fetchMembers(1)
    resetForm()
}


function showConfirmModal() {
    dialog.warning({
        title: 'Xác nhận sử dụng lại khách hàng',
        content: 'Khách hàng đã tồn tại trong hệ thống. Bạn có muốn sử dụng lại thông tin hay cập nhật lại thông tin khách hàng không?',
        action: () =>
            h('div', { style: 'display:flex; gap:12px; justify-content:flex-end' }, [

                // ❌ HỦY
                h(NButton, {
                    onClick: () => dialog.destroyAll()
                }, { default: () => 'Hủy' }),

                // 🟠 CẬP NHẬT
                h(NButton, {
                    type: 'warning', // cam
                    onClick: () => {
                        dialog.destroyAll()
                        openUpdateCustomerModal()
                    }
                }, { default: () => 'Cập nhật thông tin' }),

                // ✅ ĐỒNG Ý
                h(NButton, {
                    type: 'success', // xanh lá
                    onClick: () => {
                        dialog.destroyAll()
                        handleAddMember(true)
                        resetForm()
                    }
                }, { default: () => 'Đồng ý' })
            ])
    })
}
function validateGuest(form: any) {
  const errors: Record<string, string> = {}

  // Họ tên
  if (!form.hoTen || form.hoTen.trim().length < 2) {
    errors.hoTen = 'Họ tên phải có ít nhất 2 ký tự'
  }

  // Giới tính (0 | 1 | 2)
  if (form.gioiTinh === null || form.gioiTinh === undefined) {
    errors.gioiTinh = 'Vui lòng chọn giới tính'
  }

// Ngày sinh (BẮT BUỘC)
if (!form.ngaySinh) {
  errors.ngaySinh = 'Vui lòng chọn ngày sinh'
} else {
  const birthDate = dayjs(form.ngaySinh)
  if (!birthDate.isValid()) {
    errors.ngaySinh = 'Ngày sinh không hợp lệ'
  } else if (birthDate.isAfter(dayjs(), 'day')) {
    errors.ngaySinh = 'Ngày sinh không được lớn hơn ngày hiện tại'
  }
}


  // Loại giấy tờ
  if (form.loaiGiayTo === null || form.loaiGiayTo === undefined) {
    errors.loaiGiayTo = 'Vui lòng chọn loại giấy tờ'
  }

  // Số giấy tờ
  if (!form.soGiayTo || !form.soGiayTo.trim()) {
    errors.soGiayTo = 'Số giấy tờ không được để trống'
  } else {
    // CCCD
    if (form.loaiGiayTo === 0 && !/^\d{12}$/.test(form.soGiayTo)) {
      errors.soGiayTo = 'CCCD phải gồm 12 chữ số'
    }

    // Hộ chiếu
    if (form.loaiGiayTo === 1 && !/^[A-Z0-9]{6,9}$/i.test(form.soGiayTo)) {
      errors.soGiayTo = 'Hộ chiếu không hợp lệ'
    }
  }

  return errors
}

async function handleAddMember(confirm: boolean = false) {
      const errors = validateGuest(form)

  
    const payload = {
        idDoanLuuTru: props.groupId,
        hoTen: form.hoTen,
        gioiTinh: form.gioiTinh,
        ngaySinh: form.ngaySinh
            ? dayjs(form.ngaySinh).format('YYYY-MM-DD')
            : '',
        loaiGiayTo: form.loaiGiayTo ?? 0,
        soGiayTo: form.soGiayTo,
        vaiTro: form.vaiTro,
        confirmUseOld: confirm // ✅ boolean thật
    }

    console.log('SEND PAYLOAD:', JSON.stringify(payload))

    try {
            await checkSoLuongToiDa(props.groupId);
            if (Object.keys(errors).length > 0) {
    // Hiển thị lỗi đầu tiên
    window.$message.error(Object.values(errors)[0])
    return
  }
             console.log('id doan',props.groupId)
        const res = await addMember(payload)
        window.$message.success(res?.message || 'Thêm khách hàng thành công!')
        console.log("resMember", res)
            await fetchMembers(1)
        resetForm()
        
    } catch (e: any) {
        if (e.response?.status === 409) {
            showConfirmModal()
        } else if (e.response?.data != null) {
            const msg =
                e?.response?.data?.message

            window.$message.error(msg)
        }
    }


}

function resetForm() {
    form.hoTen = ''
    form.gioiTinh = null
    form.ngaySinh = null
    form.loaiGiayTo = null
    form.soGiayTo = ''

}

const showScan = ref(false)
// --- Watch tự động lọc ---
watch(
    formSearch,
    () => {
        fetchMembers(1)
    },
    { deep: true },
)

function handleResetSearch() {
    formSearch.hoTen = ''
    formSearch.loaiGiayTo = null
    formSearch.soGiayTo = ''
    fetchMembers(1, props.groupId)
}

async function changePage(page: number) {
    const res = await fetchMembers(page)
    console.log("changePage", res)
}

function handleScanSuccess(data: any) {
    // if (data.name) form.hoTen = data.name
    // if (data.cccd) form.soCccd = data.cccd
    // window.$message.success('Đã quét thành công: ' + data.name)
}

watch(() => props.show, (val) => {
    if (val) fetchMembers(1)
})


async function onScanResult(data: any) {
    // data sẽ có các trường: cccd, hoTen, ngaySinh, gioiTinh, diaChi, ngayCap
    console.log('CCCD data:', data)


    const val = data
    // reset trước khi điền dữ liệu QR
    const str = val.ngaySinh; // ddMMyyyy
    const ngay = parseInt(str.slice(0, 2));
    const thang = parseInt(str.slice(2, 4));
    const nam = parseInt(str.slice(4, 8));
    form.ngaySinh = new Date(nam, thang - 1, ngay);

    form.hoTen = val.hoTen || ''
    form.soGiayTo = val.soGiayTo || ''

    form.gioiTinh = val.gioiTinh === 'Nam' ? 0 : (val.gioiTinh === 'Nữ' ? 1 : 2);



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
        form.ngaySinh = new Date(nam, thang - 1, ngay)
    } else {
        form.ngaySinh = null
    }

    form.loaiGiayTo = val.loaiGiayTo ?? null
    form.hoTen = val.hoTen || ''
    form.soGiayTo = val.soGiayTo || ''


    form.gioiTinh = val.gioiTinh === 'Nam' ? 0 : (val.gioiTinh === 'Nữ' ? 1 : val.gioiTinh === 'X' ? 2 : null);




    showOCRModal.value = false

}
function handleQuetCCCD() {

    if (form.loaiGiayTo !== 0) {
        window.$message.warning('Vui lòng chọn loại giấy tờ là CCCD để quét!')
        return
    }
    showScanner.value = true
}
function handleOCRCCCD() {

    showOCRModal.value = true
}

function openAssignRoomModal(member: any) {
    if (member.bookingStatus === 'CHECKED_IN') {
        window.$message.warning('Không thể thay đổi phòng sau khi đã check-in')
        return
    }
console.log("member", member)
    selectedMember.value = member
    showAssignRoomModal.value = true
}


const columns = [
    { title: 'STT', key: 'orderNumber' },
    { title: 'Họ và tên', key: 'hoTen' },
    { title: 'Giới tính', key: 'gioiTinh', render: (row: any) => { const gt = row.gioiTinh; return gt === 0 ? 'Nam' : gt === 1 ? 'Nữ' : gt === 2 ? 'X' : '-' } },
    { title: 'Ngày sinh', key: 'ngaySinh' },
    {
        title: 'Loại giấy tờ', key: 'loaiGiayTo', render: (row: any) =>
            row.loaiGiayTo === 0
                ? 'CCCD'
                : row.loaiGiayTo === 1
                    ? 'Hộ chiếu'
                    : '-'
    },
    { title: 'Số giấy tờ', key: 'soGiayTo', render: (row: any) => row.soGiayTo || '-' },
    { title: 'Vai trò', key: 'vaiTro', render: (row: any) => row.vaiTro === 0 ? "Trưởng đoàn" : "Thành viên" },
    {
        title: 'Phòng',
        key: 'phong',
        render: (row: any) => {
            if (!row.phongId) {
                return h(
                    'span',
                    { style: 'color:#f56c6c' },
                    'Chưa gán'
                )
            }
            return `Phòng ${row.tenPhong}`
        }
    }, {
        title: 'Hành động',
        key: 'action',
        render: (row: any) => {
            const isCheckedIn = row.trangThaiChiTietDoan === 'CHECKED_IN'
            const label = row.phongId ? 'Đổi phòng' : 'Gán phòng'

            return h(
                NButton,
                {
                    size: 'small',
                    type: row.phongId ? 'warning' : 'primary',
                    disabled: isCheckedIn, // ✅ chặn đổi sau check-in
                    onClick: () => openAssignRoomModal(row)
                },
                { default: () => label }
            )
        }
    }
]

// async function handleAssignRoom(roomId: string) {
//   if (selectedMember.value.phong?.id === roomId) {
//     window.$message.info('Khách đã ở phòng này')
//     return
//   }

//   try {
//     await assignRoom({
//       memberId: selectedMember.value.id,
//       roomId
//     })

//     window.$message.success('Gán phòng thành công')
//     showAssignRoomModal.value = false
//     fetchMembers(currentPage.value)
//   } catch (e: any) {
//     window.$message.error(e?.response?.data?.message)
//   }
// }

</script>

<template>
    <CccdOCR v-model="showOCRModal" @result="onOCRResult" @close="showOCRModal = false" />
    <CccdScanner v-model="showScanner" @scan-result="onScanResult" />
    <AssignRoomModal
  v-model:show="showAssignRoomModal"
  :member="selectedMember"
  :booking-id="props.groupId"
  @success="fetchMembers(currentPage)"
/>

    <NDrawer :show="show" @update:show="$emit('update:show', $event)" width="1200">
        <NDrawerContent title="Quản lý thành viên đoàn" closable>
            <n-card>
                <NForm ref="formRef" label-placement="top" label-align="left" :show-feedback="false">
                    <NGrid :cols="24" :x-gap="12" :y-gap="12">
                        <NFormItemGi :span="7" label="Họ và tên " path="ten">
                            <NInput v-model:value="formSearch.hoTen" placeholder="Nhập họ và tên khách hàng"
                                clearable />
                        </NFormItemGi>

                        <NFormItemGi :span="7" label="Loại giấy tờ" path="loaiGiayTo">
                            <NSelect v-model:value="formSearch.loaiGiayTo" placeholder="Chọn loại giấy tờ" clearable
                                :options="loaiGiayToOptions" />
                        </NFormItemGi>


                        <NFormItemGi :span="7" label="Số giấy tờ" path="soGiayTo">
                            <NInput v-model:value="formSearch.soGiayTo" placeholder="Nhập số giấy tờ khách hàng"
                                clearable />
                        </NFormItemGi>

                        <NFormItemGi :span="3">
                            <n-tooltip trigger="hover">
                                <template #trigger>
                                    <n-button quaternary circle @click="handleResetSearch">
                                        <template #icon>
                                            <nova-icon icon="carbon:reset" />
                                        </template>
                                    </n-button>
                                </template>
                                Làm mới
                            </n-tooltip>
                        </NFormItemGi>
                    </NGrid>
                </NForm>
            </n-card>

            <NDivider style="margin: 12px 0" />

            <div class="mb-2">
                <h3 class="text-lg font-medium mb-1">Danh sách thành viên</h3>
                <NDataTable :columns="columns" :data="members" :loading="loading" />
                <div class="mt-4">
                    <n-pagination v-model:page="currentPage" :page-count="Math.ceil(totalItems / pageSize)"
                        :page-size="pageSize" show-size-picker :page-sizes="[10, 20, 30, 50]" @update:page="changePage"
                        @update:page-size="(size: number) => { pageSize = size; fetchMembers(1) }">
                        <template #prefix>
                            Tổng {{ totalItems }} khách hàng
                        </template>
                    </n-pagination>
                </div>
            </div>

            <NDivider style="margin: 12px 0" />


            <div>
                <h3 class="text-lg font-medium mb-1">Thêm thành viên mới</h3>
                <NForm :model="form" label-placement="left" label-width="120">
                    <NGrid :cols="24" :x-gap="12" :y-gap="12">
                        <NFormItemGi :span="12" label="Họ và tên">
                            <NInput v-model:value="form.hoTen" placeholder="Họ và tên..." />
                        </NFormItemGi>
                        <n-form-item-grid-item :span="12" label="Ngày sinh" path="ngaySinh">
                            <n-date-picker v-model:value="form.ngaySinh" type="date" placeholder="Chọn ngày sinh"
                                style="width: 100%;" clearable />
                        </n-form-item-grid-item>


                        <NFormItemGi :span="24" label="Loại giấy tờ" path="loaiGiayTo">
                            <div class="flex gap-2 w-full">
                                <NSelect v-model:value="form.loaiGiayTo" :options="loaiGiayToOptions"
                                    placeholder="Loại giấy tờ ..." clearable />

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
                        <NFormItemGi :span="12" label="Số giấy tờ">
                            <NInput v-model:value="form.soGiayTo" placeholder="Số giấy tờ ..." clearable />
                        </NFormItemGi>
                        <n-form-item-grid-item :span="12" label="Giới tính" path="gioiTinh">
                            <n-radio-group v-model:value="form.gioiTinh">
                                <n-radio v-for="item in gioiTinhToOptions" :key="item.value" :value="item.value">
                                    {{ item.label }}
                                </n-radio>
                            </n-radio-group>
                        </n-form-item-grid-item>
                    </NGrid>
                    <div class="flex justify-end mt-2">
                        <NButton type="primary" @click="handleAddMember()">Thêm thành viên</NButton>
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
