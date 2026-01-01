import request from "@/service/request";
import { API_URL } from "@/constants/url";

const API_DOAN_LUU_TRU = `${API_URL}/leTan/doan-luu-tru`;

export interface DoanLuuTru {
  id: string;
  maDoan: string;
  tenDoan: string;
  ghiChu: string;
  datPhong: {
    id: string;
    maPhong?: string;
    // other fields
  };
  truongDoan?: {
    id: string;
    hoTen: string;
  };
  createdTime?: number;
  thoiGianCheckIn?: number;
  thoiGianCheckOut?: number;
  trangThai?: number;
}

export interface ChiTietDoan {
  id: string;
  vaiTro: string;
  khachHang: {
    id: string;
    hoTen: string;
    soDienThoai: string;
    soCCD: string;
    email: string;
  };
}

export async function getAllGroups(params?: { tuKhoa?: string }) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/list`,
    method: "GET",
    params,
  });
  return res.data;
}

export async function createGroup(data: {
  tenDoan?: string;
  ghiChu: string;
  hoTenTruongDoan: string;
  soDienThoaiTruongDoan: string;
  emailTruongDoan: string;
  soCccdTruongDoan: string;
  soNguoi: number;
}) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/create`,
    method: "POST",
    data,
  });
  return res.data;
}

export async function getGroupMembers(id: string) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/${id}/members`,
    method: "GET",
  });
  return res.data;
}

export async function addMember(data: {
  idDoanLuuTru: string;
  idKhachHang?: string;
  hoTen?: string;
  email?: string;
  soDienThoai?: string;
  soCccd?: string;
  diaChi?: string;
  quocTich?: string;
  vaiTro: string;
}) {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/add-member`,
    method: "POST",
    data,
  });
  return res.data;
}

export async function getActiveBookings() {
  const res = await request({
    url: `${API_DOAN_LUU_TRU}/active-bookings`,
    method: "GET",
  });
  return res.data;
}
