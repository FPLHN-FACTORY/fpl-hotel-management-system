import request from "@/service/request";
import { API_URL } from "@/constants/url";
import type { ResponseList, PaginationParams, DefaultResponse,DataCombobox } from '@/typings/api/api.common'
const API_DOAN_LUU_TRU = `${API_URL}/leTan/doan-luu-tru`;
import type { AxiosResponse } from 'axios'
export interface DoanLuuTru {
  orderNumber:number,
  id: string;
  maDoan: string;
  tenDoan: string;

  hoTen:string;
  ngaySinh:string;
  gioiTinh:number;
  loaiGiayTo:number;
  soGiayTo:string;
  soDienThoai:string;
  maDatPhong:string;
  ngayCheckIn?: number;
  ngayCheckOut?: number;
  thoiGianCheckIn?: number;
  thoiGianCheckOut?: number;
  trangThai?: number;
}

export interface ChiTietDoan {
  orderNumber:number,
  id: string;
  vaiTro: number;
  khachHangId: string;
phongId:string;
tenPhong:string;
    hoTen: string;
    gioiTinh:number;
    ngaySinh:string;
    loaiGiayTo:number;
    soGiayTo: string;
  trangThaiChiTietDoan: string;
}
export interface ParamsGetGroups extends PaginationParams {
tuKhoa?: string
thoiGianCheckIn?: number
thoiGianCheckOut?: number
}
export async function getAllGroups(params:ParamsGetGroups) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/list`,
    method: "GET",
    params,
  })as  AxiosResponse<
      DefaultResponse<{
        data: DoanLuuTru[]
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

export async function createGroup(data: {
  tenDoan?: string;
  idDatPhong:string;
  ghiChu: string;
  hoTenTruongDoan: string;
  soDienThoaiTruongDoan: string;
  ngaySinhTruongDoan:string;
  loaiGiayToTruongDoan:number;
  soGiayToTruongDoan: string;
 
}) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/create`,
    method: "POST",
    data,
  });
  return res.data;
}


export interface ParamsGetMembers extends PaginationParams {
  hoTen?: string
loaiGiayTo?: number
soGiayTo?: string
}

export async function getGroupMembers(params: ParamsGetMembers,id: string) {
  const res = (await request({
    url: `${API_DOAN_LUU_TRU}/${id}/members`,
    method: "GET",
    params
  })) as  AxiosResponse<
      DefaultResponse<{
        data: ChiTietDoan[]
        totalPages: number
        currentPage: number
        totalElements: number
      }>
    >
    console.log("items",res.data.data.data )
       console.log("totalitems", res.data.data.totalElements )
          console.log("totalPages", res.data.data.totalPages )
             console.log("currentPage",params.page )
        return {
      items: res.data.data.data || [],
      totalItems: res.data.data.totalElements || 0,
      totalPages: res.data.data.totalPages || 0,
      currentPage: params.page || 1,
    }
}

export async function addMember(data: {
  idDoanLuuTru: string;
  idKhachHang?: string;
  hoTen?: string;
  email?: string;
  ngaySinh:string;
  soDienThoai?: string;
  loaiGiayTo:number;
  gioiTinh:number;
  soGiayTo?: string;
  diaChi?: string;
  quocTich?: string;
 confirmUseOld:boolean;
}) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/add-member`,
    method: "POST",
    data,
  });
  return res.data;
}

export interface DSPhongDaDatTheoDoanCombox extends ResponseList {
id:string
ten:string
tang:number
soGiuongDon:number
soGiuongDoi:number
soNguoiQuyDinh:number
soNguoiToiDa:number
soNguoiHienTai:number 
}
export async function getAllBookedTheoDoan(id:string) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/${id}/booked`,
    method: "GET"
  
  })as AxiosResponse<DefaultResponse<DSPhongDaDatTheoDoanCombox[]>>
  return res.data.data;
}

export interface AssignRoomRequest {
  idPhong: string;
}
export async function assignRoom(id: string, data: AssignRoomRequest) {
  try {
    const res = (await request({
      url: `${API_DOAN_LUU_TRU}/assign-room/${id}`,
      method: 'PUT',
      data,
    })) as AxiosResponse<DefaultResponse<ChiTietDoan>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể gán phòng')
  }
}

export interface CheckSoLuongToiDaResponse {
  tongSoNguoiHienTai: number
   tongSoNguoiToiDa: number
}
export async function checkSoLuongToiDa(idDoan:string) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/check-room/${idDoan}`,
    method: "GET",
   
  })as  AxiosResponse<
      DefaultResponse<CheckSoLuongToiDaResponse>>
    return res.data;
}


export async function  checkInDoan(id: string) {
  try {
    const res = (await request({
      url: `${API_DOAN_LUU_TRU}/${id}/check-in`,
      method: 'PUT',
 
    })) as AxiosResponse<DefaultResponse<ChiTietDoan>>

    return res.data
  }
  catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể checkin')
  }
}