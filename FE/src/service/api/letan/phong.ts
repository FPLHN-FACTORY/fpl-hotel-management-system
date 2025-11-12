// src/typings/api/room.ts
import { API_LE_TAN_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

// Tham số truy vấn lọc phòng
export interface ParamsGetRoom extends PaginationParams {
  q?: string          // Tìm kiếm theo tên hoặc mã
  tang?: number       // Tầng
  loaiPhongId?: string
  trangThaiPhong?: 'TRONG' | 'DA_DAT' | 'DANG_SU_DUNG' | 'DANG_DON' | 'BAO_TRI' | 'TAM_KHOA'
  giaMin?: number
  giaMax?: number
}

// Kiểu dữ liệu phòng trả về (projection từ backend)
export interface PhongResponse extends ResponseList {
  id: string
  ma: string
  ten: string
  price: number
  loaiPhong: string
  sucChua: string
  trangThai: string
}

export async function getAllRooms(params: ParamsGetRoom) {
  const queryParams = { ...params }

  const res = (await request({
    url: API_LE_TAN_PHONG,
    method: 'GET',
    params: queryParams,
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
    currentPage: res.data.data.currentPage || 1,
  }
}


// Xóa phòng (thay đổi trạng thái)
export async function deleteRoom(id: string) {
  const res = (await request({
    url: `${API_LE_TAN_PHONG}/changeStatus/${id}`,
    method: 'PUT',
  })) as AxiosResponse<DefaultResponse<PhongResponse>>

  return res.data
}