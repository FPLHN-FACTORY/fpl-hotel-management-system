import { API_LE_TAN_PHONG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

export interface ParamsGetRoom extends PaginationParams {
  tuKhoa?: string
  tang?: number
  loaiPhong?: string
  trangThaiHoatDong?: 'DANG_HOAT_DONG' | 'NGUNG_HOAT_DONG' | 'DANG_SUA'
  giaMin?: number
  giaMax?: number
  sucChuaMin?: number
  sucChuaMax?: number
  tagIds?: string[]
}

export interface PhongResponse extends ResponseList {
  id: string
  ma: string
  ten: string
  price: number
  tang: number
  loaiPhong: string
  sucChua: number
  soNguoiQuyDinh: number
  soGiuongDon: number
  soGiuongDoi: number
  trangThaiHoatDong?: 'DANG_HOAT_DONG' | 'NGUNG_HOAT_DONG' | 'DANG_SUA'
  tags?: Array<{
    id: string
    ma: string
    ten: string
    mau?: string
  }>
}

export interface AddPhongRequest {
  ma: string
  ten: string
  idLoaiPhong: string
  tang: number
  trangThaiPhong: 'DANG_HOAT_DONG' | 'NGUNG_HOAT_DONG' | 'DANG_SUA'
  tagIds?: string[]
}

export interface UpdatePhongRequest {
  ma: string
  ten: string
  idLoaiPhong: string
  tang: number
  trangThaiPhong: 'DANG_HOAT_DONG' | 'NGUNG_HOAT_DONG' | 'DANG_SUA'
  tagIds?: string[]
}

export interface PhongDetailResponse {
  phong: {
    id: string
    ma: string
    ten: string
    tang: number
    idLoaiPhong: string
    tenLoaiPhong: string
    soNguoiToiDa: number
    soNguoiQuyDinh: number
    soGiuongDon: number
    soGiuongDoi: number
    giaCaNgay: number
    trangThaiHoatDong: 'DANG_HOAT_DONG' | 'NGUNG_HOAT_DONG' | 'DANG_SUA'
  }
  tagIds: string[]
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

export interface TagResponse {
  id: string
  ma: string
  ten: string
  mau?: string
  moTa?: string
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

export async function deleteRoom(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_PHONG}/${id}`,
      method: 'DELETE',
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể xóa phòng')
  }
}

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

export async function updatePhong(id: string, data: UpdatePhongRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_PHONG}/updatePhong/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể cập nhật phòng')
  }
}

export async function getPhongById(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_PHONG}/${id}`,
      method: 'GET',
    })) as AxiosResponse<DefaultResponse<PhongDetailResponse>>

    return res.data.data || null
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải thông tin phòng')
  }
}

export async function getRoomTypes() {
  try {
    const res = await request({
      url: `${API_LE_TAN_PHONG}/loai-phong`,
      method: 'GET',
    }) as AxiosResponse<DefaultResponse<RoomTypeResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách loại phòng')
  }
}

export async function getAllTags() {
  try {
    const res = await request({
      url: `${API_LE_TAN_PHONG}/tags`,
      method: 'GET',
    }) as AxiosResponse<DefaultResponse<TagResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách tags')
  }
}

export async function getTagsByPhongId(phongId: string) {
  try {
    const res = await request({
      url: `${API_LE_TAN_PHONG}/${phongId}/tags`,
      method: 'GET',
    }) as AxiosResponse<DefaultResponse<TagResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải tags của phòng')
  }
}
