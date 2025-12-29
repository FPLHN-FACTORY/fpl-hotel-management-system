import { API_LE_TAN_DAT_PHONG } from '@/constants/url'
import request from '@/service/request'
import type { AxiosResponse } from 'axios'
import type { DefaultResponse } from '@/typings/api/api.common'

export interface CheckPhongTrongRequest {
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
}

export interface LoaiPhongAvailableResponse {
  idLoaiPhong: string
  tenLoaiPhong: string
  moTa: string
  soGiuongDon: number
  soGiuongDOi: number
  soNguoiToiDa: number
  giaCaNgay: number
  soPhongTrong: number
}

export interface ChonLoaiPhong {
  idLoaiPhong: string
  soLuong: number
}

export interface DatPhongTheoLoaiRequest {
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
}

export interface TagInfo {
  id: string
  ma: string
  ten: string
  mau: string
}

export interface PhongDatResponse {
  idPhong: string
  maPhong: string
  tenPhong: string
  tenLoaiPhong: string
  tang: number
  gia: number
  sucChua: number
  tags: TagInfo[]
}

export interface CreateDatPhongRequest {
  ngayNhan: number
  ngayTra: number
  idKhachHang: string
  ghiChu?: string
  nhanNgay: boolean
  danhSachIdPhong: string[]
}

export interface TimKhachHangResponse {
  id: string
  maNguoiDung: string
  hoTen: string
  email: string
  soCCCD: string
  soDienThoai: string
  diaChi: string
  quocTich: string
}

export async function checkPhongTrong(data: CheckPhongTrongRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_DAT_PHONG}/check-phong-trong`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<LoaiPhongAvailableResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể kiểm tra phòng trống')
  }
}

export async function getPhongTheoLoai(data: DatPhongTheoLoaiRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_DAT_PHONG}/phong-theo-loai`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<PhongDatResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể lấy danh sách phòng')
  }
}

export async function createDatPhong(data: CreateDatPhongRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_DAT_PHONG}/create`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<string>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể đặt phòng')
  }
}

export async function searchKhachHang(keyword?: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_DAT_PHONG}/khach-hang/search`,
      method: 'GET',
      params: { keyword },
    })) as AxiosResponse<DefaultResponse<TimKhachHangResponse[]>>

    return res.data.data || []
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tìm kiếm khách hàng')
  }
}
