import request from "../../request";
import type { DefaultResponse } from '@/typings/api/api.common'
import type { AxiosResponse } from 'axios'

const PREFIX_API = "/api/v1/leTan/dich-vu-phat-sinh";

export interface DichVuPhatSinh {
  id: string;
  tenDichVu: string;
  soLuong: number;
  donGia: number;
  thanhTien: number;
  nhanVien?: any;
}

export interface SaveDichVuRequest {
  tenDichVu: string;
  soLuong: number;
  donGia: number;
  idPhieuDatPhong?: string;
  idChiTietDatPhong?: string;
}

export async function saveDichVu(data: SaveDichVuRequest) {
  const res = await request.post<DefaultResponse<DichVuPhatSinh>>(`${PREFIX_API}`, data);
  return res.data.data;
}

export async function deleteDichVu(id: string) {
  const res = await request.delete<DefaultResponse<any>>(`${PREFIX_API}/${id}`);
  return res.data.data;
}

export async function getDichVuByBooking(bookingId: string) {
  const res = await request.get<DefaultResponse<DichVuPhatSinh[]>>(`${PREFIX_API}/booking/${bookingId}`);
  return res.data.data || [];
}

export async function getDichVuByRoomBooking(roomBookingId: string) {
  const res = await request.get<DefaultResponse<DichVuPhatSinh[]>>(
    `${PREFIX_API}/room-booking/${roomBookingId}`,
  );
  return res.data.data || [];
}

export async function getActiveBookingByRoom(roomId: string) {
  // Return type depends on what backend sends. Usually DefaultResponse<ChiTietDatPhong>
  const res = await request.get<DefaultResponse<any>>(`${PREFIX_API}/active-booking/${roomId}`);
  return res.data; // Note: getActiveBookingByRoom caller in index.vue expects { data: ... } structure?
  // Let's check index.vue usage: const res = await getActiveBookingByRoom(room.id); if (res.data) ...
  // So index.vue expects the AxiosResponse structure or at least an object with .data property.
  // Wait, if I change this function to return data directly, I break index.vue.
}

