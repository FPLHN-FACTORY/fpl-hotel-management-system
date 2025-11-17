// src/service/api/letan/roomType.ts
import { API_LE_TAN_LOAI_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

// --- Tham số truy vấn lọc loại phòng ---
export interface ParamsGetRoomType extends PaginationParams {
  tuKhoa?: string
  trangThai?: 'ACTIVE' | 'INACTIVE'
}

// --- Kiểu dữ liệu loại phòng trả về ---
export interface RoomTypeResponse extends ResponseList {
  id: string
  ma: string
  ten: string
  soGiuongDon: number
  soGiuongDoi: number
  soNguoiQuyDinh: number
  soNguoiToiDa: number
  giaCaNgay: number
  status: 'ACTIVE' | 'INACTIVE'
}

// --- Thêm / Sửa loại phòng ---
export interface AddRoomTypeRequest {
  ma: string
  ten: string
  soGiuongDon: number
  soGiuongDoi: number
  soNguoiQuyDinh: number
  soNguoiToiDa: number
  giaCaNgay: number
  trangThai: 'ACTIVE' | 'INACTIVE'
}

// --- Lấy danh sách loại phòng ---
export async function getAllRoomTypes(params: ParamsGetRoomType) {
  try {
    const res = await request({
      url: `${API_LE_TAN_LOAI_PHONG}`,
      method: 'GET',
      params,
    }) as AxiosResponse<DefaultResponse<RoomTypeResponse[]>>

    return {
      items: res.data.data || [],
      totalItems: res.data.data?.length || 0,
      totalPages: 1,
      currentPage: params.page || 1
    }

  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách loại phòng')
  }
}

// --- Xóa / đổi trạng thái loại phòng ---
export async function deleteRoomType(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_LOAI_PHONG}/${id}`,
      method: 'DELETE',
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể xóa loại phòng')
  }
}

// --- Thêm loại phòng ---
export async function addRoomType(data: AddRoomTypeRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_LOAI_PHONG}`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể thêm loại phòng')
  }
}

// --- Sửa loại phòng ---
export async function updateRoomType(id: string, data: AddRoomTypeRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_LOAI_PHONG}/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể cập nhật loại phòng')
  }
}
