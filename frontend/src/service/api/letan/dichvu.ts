import request from '@/service/request'
import type { AxiosResponse } from 'axios'
import type { DefaultResponse, PagedResponse } from '@/typings/api/api.common'

export interface DichVuRequest {
  id?: string
  maDichVu: string
  tenDichVu: string
  donViTinh: string
  donGia: number
  moTa?: string
  trangThai?: number
}

export interface DichVuResponse {
  id: string
  maDichVu: string
  tenDichVu: string
  donViTinh: string
  donGia: number
  moTa?: string
  trangThai: number
  createdDate: number
}

export interface DichVuSearchParams {
  q?: string
  trangThai?: number
  page?: number
  size?: number
}

const API_DICH_VU = '/api/v1/leTan/dich-vu'

export async function searchDichVu(params: DichVuSearchParams) {
  const res = (await request({
    url: API_DICH_VU,
    method: 'GET',
    params,
  })) as AxiosResponse<PagedResponse<DichVuResponse>>
  return res.data
}

export async function getAllActiveDichVu() {
  const res = (await request({
    url: `${API_DICH_VU}/active`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<DichVuResponse[]>>
  return res.data
}

export async function getDichVuById(id: string) {
  const res = (await request({
    url: `${API_DICH_VU}/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<DichVuResponse>>
  return res.data
}

export async function createDichVu(data: DichVuRequest) {
  const res = await request({
    url: API_DICH_VU,
    method: 'POST',
    data,
  })
  return res.data
}

export async function updateDichVu(id: string, data: DichVuRequest) {
  const res = await request({
    url: `${API_DICH_VU}/${id}`,
    method: 'PUT',
    data,
  })
  return res.data
}

export async function deleteDichVu(id: string) {
  const res = await request({
    url: `${API_DICH_VU}/${id}`,
    method: 'DELETE',
  })
  return res.data
}
