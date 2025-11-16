// src/service/api/letan/phong.ts
import { API_LE_TAN_TAG } from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse } from '@/typings/api/api.common'
import request from '@/service/request'

// Tham số truy vấn lọc tag
export interface ParamsGetTag extends PaginationParams {
  maOrTen?: string
  status?:number |null
}

// Kiểu dữ liệu tag trả về 
export interface TagResponse  {
  id: string
   rowNumber: number
  ma: string
  ten: string
  moTa: string
  status:string
}

export interface AddAndUpdateRequest {
  ten: string
    moTa: string
 }



export async function getAllTags(params: ParamsGetTag) {
  try {
    const res = (await request({
      url: API_LE_TAN_TAG,
      method: 'GET',
      params,
    })) as AxiosResponse<
      DefaultResponse<{
        data: TagResponse[]
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
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách Tag')
  }
}

// Xóa Tag (thay đổi trạng thái)
export async function changeStatusTag(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_TAG}/change-status/${id}`,
      method: 'PUT',
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể xóa Tag')
  }
}

// add Tag
export async function addTag(data: AddAndUpdateRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_TAG}/add-tag`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể thêm tag')
  }
}

// update Tag
export async function updateTag(data: AddAndUpdateRequest, id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_TAG}/update-tag/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể update tag')
  }
}
