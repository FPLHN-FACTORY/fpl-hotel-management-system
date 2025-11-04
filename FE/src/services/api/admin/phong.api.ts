import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_PHONG_ADMIN } from "@/constants/url";
import type { PaginationParams, DefaultResponse, PaginationResponse } from "@/types/api.common";

export interface ParamsGetPhong extends PaginationParams {
  q?: string;
  loaiPhong?: string;
  trangThaiPhong?: string; // Changed from number to string to match backend enum
  giaMin?: number;
  giaMax?: number;
  sucChuaMin?: number;
  sucChuaMax?: number;
  sortBy?: string;
  orderBy?: string;
}

export interface BangGiaResponse {
  id: string;
  maBangGia: string;
  giaNgayThuong: number;
  giaCuoiTuan: number;
  heSoCaoDiem: number;
}

export interface PhongResponse {
  id: string;
  maPhong: string;
  tenPhong: string;
  loaiPhong: string;
  giaHienTai: number;
  sucChua: number;
  trangThaiPhong: string; // "TRONG" | "DA_DAT" | "DANG_SU_DUNG"
  bangGia?: BangGiaResponse;
}

export const getRooms = async (params: ParamsGetPhong) => {
  try {
    const res = await request({
      url: `${PREFIX_API_PHONG_ADMIN}`,
      method: "GET",
      params,
    }) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhongResponse>>>>;

    return res.data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};

export const getRoomById = async (id: string) => {
  try {
    const res = await request({
      url: `${PREFIX_API_PHONG_ADMIN}/${id}`,
      method: "GET",
    }) as AxiosResponse<DefaultResponse<PhongResponse>>;

    return res.data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};
export const deleteRoom = async (id:string) => {
  const res = (await request({
    url: `${PREFIX_API_PHONG_ADMIN}/changeStatus/${id}`,
    method: "PUT",
   
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhongResponse>>>>;
  return res.data;
};


export const getAllRoomUsageHistory = async (params: LsDatPhongRequest) => {
  const res = await request({
    url: `${PREFIX_API_PHONG_ADMIN}/history`,
    method: "GET",
    params: formatDateTimeRange(params),
  }) as AxiosResponse<DefaultResponse<PaginationResponse<Array<LeTanResponse>>>>;

  return res.data;
};