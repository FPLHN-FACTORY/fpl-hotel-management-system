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
  ghiChu: string;
  hoTen:string;
  ngaySinh:string;
  gioiTinh:number;
  loaiGiayTo:number;
  soGiayTo:string;
  soDienThoai:string;
  maDatPhong:string;
  thoiGianCheckIn?: number;
  thoiGianCheckOut?: number;
  trangThai?: number;
}

export interface ChiTietDoan {
  orderNumber:number,
  id: string;
  vaiTro: number;
  khachHang: {
    id: string;
    hoTen: string;
    gioiTinh:number;
    ngaySinh:string;
    loaiGiayTo:number;
    soGiayTo: string;
  };
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

export async function getAllBooked() {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/booked`,
    method: "GET",
  })as AxiosResponse<DefaultResponse<DataCombobox>>
  return res.data;
}
