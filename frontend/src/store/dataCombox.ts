import { fetchLoaiPhong } from "@/service/api/letan/lichdatphong/sodophong"
import { DataCombobox } from "@/typings/api/api.common"

export const useDataCombobox = defineStore('dataCombobox', () => {
    const dataCombobox = reactive({
        loaiPhong: undefined as DataCombobox
    })

    const fetchDataLoaiPhong = async () => {
        const res = await fetchLoaiPhong()

        dataCombobox.loaiPhong = res.data
    }

    return {
        dataCombobox,
        fetchDataLoaiPhong
    }
})
