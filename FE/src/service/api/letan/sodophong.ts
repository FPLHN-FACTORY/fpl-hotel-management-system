// src/service/api/letan/sodophong.ts
import { API_LE_TAN_SO_DO_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'
import { DataCombobox } from '../dataCombobox.api'

// ============================
// Types
// ============================

// Trạng thái phòng (từ backend)
export type TrangThaiPhongDat = 'TRONG' | 'DANG_SU_DUNG' | 'SAP_NHAN' | 'SAP_TRA' | 'QUA_GIO_TRA'

// Dữ liệu phòng trả về từ API sơ đồ
export interface SoDoPhongResponse {
  id: string
  ma: string
  ten: string
  loaiPhong: string
  tang: number
  trangThaiHoatDong: '0' | '1' | '2' // map sang HOAT_DONG/BAO_TRI/NGUNG_HOAT_DONG nếu cần
  sucChua: number
  price: number | null
  trangThaiPhong: TrangThaiPhongDat
  trangThaiVeSinh: string
}

// Tham số lọc khi lấy sơ đồ phòng
export interface ParamsGetSoDoPhong {
  ma?: string
  ten?: string
  loaiPhong?: string
  tang?: number
}

// ============================
// API calls
// ============================

/**
 * Lấy danh sách sơ đồ phòng (không phân trang)
 */
export async function getSoDoPhong(params: ParamsGetSoDoPhong = {}) {
  try {
    const res = (await request({
      url: API_LE_TAN_SO_DO_PHONG,
      method: 'GET',
      params,
    })) as AxiosResponse<DefaultResponse<SoDoPhongResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải sơ đồ phòng')
  }
}

export const fetchLoaiPhong = async () => {
    const res = (await request({
        url: `${API_LE_TAN_SO_DO_PHONG}/loai-phong`,
        method: 'GET'
    })) as AxiosResponse<DefaultResponse<DataCombobox>>

    return res.data
}