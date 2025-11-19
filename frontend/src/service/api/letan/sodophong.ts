// src/service/api/letan/sodophong.ts
import { API_LE_TAN_SO_DO_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { DataCombobox, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

// ============================
// Types
// ============================

// Trạng thái phòng (từ backend)
export type TrangThaiPhongDat = 'TRONG' | 'DANG_SU_DUNG' | 'SAP_NHAN' | 'SAP_TRA' | 'QUA_GIO_TRA'

export type TrangThaiVeSinh = 'SACH' | 'DANG_DON' | 'CHUA_DON'

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
  trangThaiVeSinh: TrangThaiVeSinh
}

// Tham số lọc khi lấy sơ đồ phòng
export interface ParamsGetSoDoPhong {
  q?: string | null
  idLoaiPhong?: string | null
  minPrice?: number | null;
  maxPrice?: number | null;
  ngayDen?: number | null | undefined;
  ngayDi?: number | null | undefined;
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
// Thay đổi trạng thái vệ sinh của phòng
export async function updateTrangThaiVeSinh(
  roomId: string,
  status: 'SACH' | 'DANG_DON' | 'CHUA_DON'
) {
  try {
    const res = await request.put(
      `${API_LE_TAN_SO_DO_PHONG}/vesinh/${roomId}`,
      null,
      {
        params: { status: statusToOrdinal(status) },
      }
    )
    return res.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể cập nhật trạng thái vệ sinh')
  }
}

/**
 * Chuyển trạng thái vệ sinh sang số ordinal tương ứng với backend
 */
function statusToOrdinal(status: 'SACH' | 'DANG_DON' | 'CHUA_DON'): number {
  switch (status) {
    case 'SACH': return 0
    case 'DANG_DON': return 1
    case 'CHUA_DON': return 2
    default: return 2
  }
}
