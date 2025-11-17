// src/service/api/letan/phong.ts
import { API_LE_TAN_PHONG } from '@/constants/url'
import { API_LE_TAN_LOAI_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

// Tham số truy vấn lọc phòng
export interface ParamsGetRoom extends PaginationParams {
  tuKhoa?: string
  tang?: number
  loaiPhong?: string
  trangThaiHoatDong?:  'HOAT_DONG' | 'BAO_TRI' | 'NGUNG_HOAT_DONG'
  giaMin?: number
  giaMax?: number
  sucChuaMin?: number
  sucChuaMax?: number
}

// Kiểu dữ liệu phòng trả về (projection từ backend)
export interface PhongResponse extends ResponseList {
  id: string
  ma: string
  ten: string
  price: number
  tang: number
  loaiPhong: string
  sucChua: string
  trangThaiHoatDong?:  'HOAT_DONG' | 'BAO_TRI' | 'NGUNG_HOAT_DONG'
}

export interface AddPhongRequest {
  ma: string
  ten: string
  idLoaiPhong: string
  tang: number
  trangThaiHoatDong: 'HOAT_DONG' | 'BAO_TRI' | 'NGUNG_HOAT_DONG'
}

export interface RoomTypeResponse extends ResponseList {
  id: string
  ma: string
  ten: string
  soGiuongDon: number
  soGiuongDoi: number
  soNguoiQuyDinh: number
  soLuongNguoiToiDa: number
  giaCaNgay: number
  status: 'ACTIVE' | 'INACTIVE'
}


export async function getAllRooms(params: ParamsGetRoom) {
  try {
    const res = (await request({
      url: API_LE_TAN_PHONG,
      method: 'GET',
      params,
    })) as AxiosResponse<
      DefaultResponse<{
        data: PhongResponse[]
        totalPages: number
        currentPage: number
        totalElements: number
      }>
    >

    return {
      items: res.data.data.data || [],
      totalItems: res.data.data.totalElements || 0,
      totalPages: res.data.data.totalPages || 0,
      currentPage: params.page || 1,
    }
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách phòng')
  }
}

// Xóa phòng (thay đổi trạng thái)
export async function deleteRoom(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_PHONG}/changeStatus/${id}`,
      method: 'PUT',
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể xóa phòng')
  }
}

// add phong
export async function addPhong(data: AddPhongRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_PHONG}/addPhong`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể thêm phòng')
  }
}

// --- Lấy danh sách loại phòng ---
export async function getRoomTypes() {
  try {
    const res = await request({
      url: `${API_LE_TAN_PHONG}/loai-phong`,
      method: 'GET',
    }) as AxiosResponse<DefaultResponse<RoomTypeResponse[]>>

    return  res.data.data || []
    
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách loại phòng')
  }
}
