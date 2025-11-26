import { API_ADMIN_NHAN_VIEN } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

export interface NhanVienListParams {
  page?: number
  size?: number
  q?: string
}

export enum EntityRole {
  QUAN_LY = 'QUAN_LY',
  NHAN_VIEN = 'NHAN_VIEN'
}

export enum EntityVaiTro {
  QUAN_LY = 'QUAN_LY',
  LE_TAN = 'LE_TAN',
}

export interface NhanVien {
  id?: string;
  ma?: string;
  ten: string;
  sdt: string;
  email: string;
  gioiTinh: boolean | null;
  ngaySinh: string | null;
  cccd: string;
  tinh: string;
  huyen: string;
  xa: string;
  diaChi: string;
  chucVu: EntityRole | null;
  vaiTro: EntityVaiTro | null;
  matKhau?: string;
  avatar?: string;
  trangThai?: string;
}


export async function getAllNhanVien(params: NhanVienListParams) {
  const res = (await request({
    url: API_ADMIN_NHAN_VIEN,
    method: 'GET',
    params,
  })) as AxiosResponse<
    DefaultResponse<{
      data: NhanVien[]
      totalPages: number
      currentPage: number
      totalElements: number
    }>
  >

  return {
    items: (res.data as any)?.data?.data ?? [],
    totalItems: (res.data as any)?.data?.totalElements ?? 0,
    totalPages: (res.data as any)?.data?.totalPages ?? 0,
    currentPage: params.page || 1,
  }
}

export async function getNhanVienById(id: string) {
  const res = (await request({
    url: `${API_ADMIN_NHAN_VIEN}/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<NhanVien>>
  return res.data.data
}

export async function modifyNhanVien(data: FormData) {
  const res = (await request({
    url: API_ADMIN_NHAN_VIEN,
    method: 'POST',
    data,
  })) as AxiosResponse<DefaultResponse<any>>
  return res.data
}

export async function changeNhanVienStatus(id: string) {
  const res = (await request({
    url: `${API_ADMIN_NHAN_VIEN}/${id}/change-status`,
    method: 'PUT',
  })) as AxiosResponse<DefaultResponse<any>>
  return res.data
}

export async function changeNhanVienRole(id: string) {
  const res = (await request({
    url: `${API_ADMIN_NHAN_VIEN}/${id}/change-role`,
    method: 'PUT',
  })) as AxiosResponse<DefaultResponse<any>>
  return res.data
}

export async function checkDuplicate(field: 'cccd' | 'sdt' | 'email', value: string, excludeId?: string) {
  const res = (await request({
    url: `${API_ADMIN_NHAN_VIEN}/check-duplicate`,
    method: 'POST',
    data: { field, value, excludeId },
  })) as AxiosResponse<DefaultResponse<{ exists: boolean }>>
  return (res.data as any)?.exists ?? false
}
