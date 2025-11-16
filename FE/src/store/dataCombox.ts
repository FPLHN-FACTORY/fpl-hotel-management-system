import { DataCombobox,  } from "@/service/api/dataCombobox.api"
import { fetchLoaiPhong } from "@/service/api/letan/sodophong"

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