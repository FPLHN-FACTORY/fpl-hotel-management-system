import { API_LE_TAN_KHACH_HANG} from '@/constants/url'
import type { AxiosResponse } from 'axios'
import type { ResponseList, PaginationParams, DefaultResponse,DataCombobox } from '@/typings/api/api.common'
import request from '@/service/request'

export interface ParamsGetCustomers extends PaginationParams {
  ten?: string

  sdtEmail?: string
loaiGiayTo?: number
soGiayTo?: string
  soCCCDSoHoChieu?: string
  status?: number


 idLoaiKhachHang?:string
}

export interface KhachHangResponse extends ResponseList {
orderNumber: number
  id: string
  ma: string
  hoTen: string
  ngaySinh: string
  gioiTinh:number
  loaiGiayTo: number
  soGiayTo: string
  soDienThoai: string
    email: string
  
    diaChi: string
    status: number
    tenLoaiKhachHang: string
idLoaiKhachHang: string

    
}

export interface AddAndUpdateKhachHangRequest {
  hoTen: string
  soDienThoai: string
  email: string
  ngaySinh: string
  gioiTinh:number
  loaiGiayTo: number
  soGiayTo: string
  diaChi: string
  idLoaiKhachHang: string
}
export interface UpdateKhachHangLuuTruRequest {
  hoTen: string
  ngaySinh: string
  gioiTinh:number
  loaiGiayTo: number
  soGiayTo: string
  
}
export interface TinhReponse {
  province_code: string
  name: string
 
}
export interface SearchDiaChiCuReponse {
  ward_name: string
  ward_code: string
 province_code:string
 province_name:string
 matched_old_unit:string
 merger_details:string
 province_merged_with:string[]
}
export interface QuanHuyenPhuongXaReponse {
  ward_name: string
  ward_code: string
 province_code:string
 province_name:string

}
export interface GiayToRequest  {
loaiGiayTo: number|null
soGiayTo: string
}

export async function getAllCustomers(params: ParamsGetCustomers){
  try {
    const res = (await request({
      url: API_LE_TAN_KHACH_HANG,
      method: 'GET',
      params,
    })) as AxiosResponse<
      DefaultResponse<{
        data: KhachHangResponse[]
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
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách khách hàng')
  }
}

export async function changeStatusKhachHang(id: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_KHACH_HANG}/changeStatus/${id}`,
      method: 'PUT',
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể thay đổi trạng thái khách hàng')
  }
}

export async function addKhachHang(data: AddAndUpdateKhachHangRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_KHACH_HANG}/add-khach-hang`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể thêm khách hàng')
  }
}

export async function updateKhachHang(id: string, data: AddAndUpdateKhachHangRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_KHACH_HANG}/update-khach-hang/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể cập nhật khách hàng')
  }
}
export async function updateKhachHangLuuTru(id: string, data: UpdateKhachHangLuuTruRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_KHACH_HANG}/update-khach-hang-luu-tru/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<any>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể cập nhật khách hàng luu tru')
  }
}


export const fetchLoaiKhachHang = async () => {
  const res = (await request({
    url: `${API_LE_TAN_KHACH_HANG}/loai-khach-hang`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<DataCombobox>>

  return res.data.data
}

export async function fetchTinhThanhPho(){
  try {
    const res = (await request({
      url: `https://34tinhthanh.com/api/provinces`,
      method: 'GET',
   
    })) as AxiosResponse<DefaultResponse<TinhReponse>>
console.log(res.data)
    return  res.data|| []
  
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách tinh thanh pho')
  }
}

export async function fetchPhuongXaQuanHuyen(params:string){
  try {
    const res = (await request({
      url: `https://34tinhthanh.com/api/wards?province_code=${params}`,
      method: 'GET',
   
    })) as AxiosResponse<DefaultResponse<QuanHuyenPhuongXaReponse[]>>
console.log(res.data)
    return  res.data|| []
  
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tải danh sách phuong xa')
  }
}
export async function fetchSearch(params:string){
  try {
    const res = (await request({
      url: `https://34tinhthanh.com/api/search?q=${params}`,
      method: 'GET',
   
    })) as AxiosResponse<DefaultResponse<SearchDiaChiCuReponse[]>>
console.log(res.data)
    return  res.data|| []
  
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tim thay')
  }
}


export async function getCustomerByGiayTo(params:GiayToRequest){
  try {
    const res = (await request({
      url: `${API_LE_TAN_KHACH_HANG}/khach-hang-giay-to`,
      method: 'GET',
      params,
    })) as AxiosResponse<DefaultResponse<KhachHangResponse>>
console.log("By giay to ",res.data)
   return res.data;
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tìm thấy khách hàng khách hàng')
  }
}