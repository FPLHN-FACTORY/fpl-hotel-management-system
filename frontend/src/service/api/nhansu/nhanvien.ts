import { API_LE_TAN_NHAN_VIEN } from "@/constants/url";
import type { AxiosResponse } from "axios";
import type { DefaultResponse } from "@/typings/api/api.common";
import request from "@/service/request";

export interface NhanVienListParams {
  page?: number;
  size?: number;
  q?: string;
  orderBy?: string;
  sortBy?: string;
  vaiTro?: string;
  gioiTinh?: string;
}

export enum EntityRole {
  ADMIN = "ADMIN",
  STAFF = "STAFF",
}

export enum EntityVaiTro {
  NHAN_VIEN = "NHAN_VIEN",
  QUAN_LY = "QUAN_LY",
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
  xa: string;
  diaChi: string;
  chucVu: EntityRole | null;
  vaitro: EntityVaiTro | null;
  matKhau?: string;
  avatar?: string;
  status?: string;
}

export interface PageableObject<T> {
  data: T[];
  totalPages: number;
  currentPage: number;
  totalElements: number;
}

export async function getAllNhanVien(params: NhanVienListParams) {
  const res = (await request({
    url: API_LE_TAN_NHAN_VIEN,
    method: "GET",
    params,
  })) as AxiosResponse<DefaultResponse<PageableObject<NhanVien>>>;
  return res.data;
}

export async function getNhanVienById(id: string) {
  const res = (await request({
    url: `${API_LE_TAN_NHAN_VIEN}/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<NhanVien>>;
  return res.data.data;
}

export async function modifyNhanVien(data: FormData) {
  const res = (await request({
    url: API_LE_TAN_NHAN_VIEN,
    method: "POST",
    data,
  })) as AxiosResponse<DefaultResponse<any>>;
  return res.data;
}

export async function changeNhanVienStatus(id: string) {
  const res = (await request({
    url: `${API_LE_TAN_NHAN_VIEN}/${id}/change-status`,
    method: "PUT",
  })) as AxiosResponse<DefaultResponse<any>>;
  return res.data;
}

export async function changeNhanVienRole(id: string) {
  const res = (await request({
    url: `${API_LE_TAN_NHAN_VIEN}/${id}/change-role`,
    method: "PUT",
  })) as AxiosResponse<DefaultResponse<any>>;
  return res.data;
}

export async function checkDuplicate(
  field: "cccd" | "sdt" | "email",
  value: string,
  excludeId?: string
) {
  const res = (await request({
    url: `${API_LE_TAN_NHAN_VIEN}/check-duplicate`,
    method: "POST",
    data: { field, value, excludeId },
  })) as AxiosResponse<DefaultResponse<{ exists: boolean }>>;
  return (res.data as any)?.exists ?? false;
}

export async function deleteNhanVien(id: string) {
  const res = (await request({
    url: `${API_LE_TAN_NHAN_VIEN}/${id}`,
    method: "DELETE",
  })) as AxiosResponse<DefaultResponse<any>>;
  return res.data;
}
