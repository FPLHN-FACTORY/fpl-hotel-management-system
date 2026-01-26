import { API_LE_TAN_BOOKING } from '@/constants/url'
import request from '@/service/request'
import type { AxiosResponse } from 'axios'
import type { DefaultResponse } from '@/typings/api/api.common'
import { phieuDatTamStorage } from './phieuDatTamStorage'
import type { PhieuDatTamLocal } from './phieuDatTamStorage'

export interface CheckPhongTrongRequest {
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
}

export interface LoaiPhongAvailableResponse {
  idLoaiPhong: string
  tenLoaiPhong: string
  moTa: string
  soGiuongDon: number
  soGiuongDOi: number
  soNguoiToiDa: number
  giaCaNgay: number
  soPhongTrong: number
}

export interface ChonLoaiPhong {
  idLoaiPhong: string
  soLuong: number
  tenLoaiPhong?: string
  gia?: number
}

export interface DatPhongTheoLoaiRequest {
  ngayNhan: number
  ngayTra: number
  soLuongKhach: number
  danhSachLoaiPhong: ChonLoaiPhong[]
}

export interface TagInfo {
  id: string
  ma: string
  ten: string
  mau: string
}

export interface PhongDatResponse {
  idPhong: string
  maPhong: string
  tenPhong: string
  tenLoaiPhong: string
  tang: number
  gia: number
  sucChua: number
  tags: TagInfo[]
}

export interface SavePhieuDatTamRequest {
  sessionId?: string
  checkInDate: number
  checkOutDate: number
  soLuongKhach: number
  idKhachHang: string | null
  ghiChu: string | null
  nhanNgay: boolean
  tienKhachTra: number | null
  danhSachIdPhong: string[]
  isFromRoomClick: boolean
  currentStep?: 'SELECT_ROOM' | 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'READY_TO_CONFIRM'

  roomDetails?: Array<{
    idPhong: string
    maPhong: string
    tenPhong: string
    tenLoaiPhong: string
    tang: number
    gia: number
    soNgay: number
  }>
}

export interface PhieuDatTamResponse {
  id: string
  sessionId: string
  checkInDate: number
  checkOutDate: number
  soLuongKhach: number
  idKhachHang: string | null
  tenKhachHang: string | null
  ghiChu: string | null
  nhanNgay: boolean
  tienKhachTra: number | null
  tongTien: number
  tienThua: number
  congNo: number
  danhSachPhong: PhongTamResponse[]
  isFromRoomClick: boolean
  currentStep: 'SELECT_ROOM' | 'CUSTOMER_INFO' | 'PAYMENT_INFO' | 'READY_TO_CONFIRM'
}

export interface PhongTamResponse {
  idPhong: string
  maPhong: string
  tenPhong: string
  tenLoaiPhong: string
  tang: number
  gia: number
  soNgay: number
  thanhTien: number
}

export interface ConfirmBookingRequest {
  sessionId?: string
  idKhachHang: string
  idChiTietDoan?: string | null
  tenDoan?: string | null
  checkInDate: number
  checkOutDate: number
  soLuongKhach: number
  ghiChu?: string | null
  nhanNgay: boolean
  tienKhachTra?: number | null
  danhSachIdPhong: string[]
  danhSachLoaiPhong?: ChonLoaiPhong[]
}

export interface TimKhachHangResponse {
  id: string
  maNguoiDung: string
  ngaySinh: number
  gioiTinh: number
  loaiGiayTo:number
  hoTen: string
  email: string
  soGiayTo: string
  soDienThoai: string
  diaChi: string
  quocTich: string
}
export interface TruongDoanResponse {
  id: string
  idChiTietDoan: string
  ngaySinh: number
  gioiTinh: number
  loaiGiayTo:number
  hoTen: string
  tenDoan?: string
  soGiayTo: string
  soDienThoai: string

}

export async function checkPhongTrong(data: CheckPhongTrongRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_BOOKING}/check-phong-trong`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<LoaiPhongAvailableResponse[]>>
    return res.data.data || []
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể kiểm tra phòng trống')
  }
}

export async function getPhongTheoLoai(data: DatPhongTheoLoaiRequest) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_BOOKING}/phong-theo-loai`,
      method: 'POST',
      data,
    })) as AxiosResponse<DefaultResponse<PhongDatResponse[]>>
    return res.data.data || []
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể lấy danh sách phòng')
  }
}

export async function savePhieuDatTam(data: SavePhieuDatTamRequest): Promise<PhieuDatTamResponse> {
  try {
    let tenKhachHang: string | null = null
    if (data.idKhachHang) {
      try {
        const khachHangList = await searchKhachHang('')
        const khach = khachHangList.find(k => k.id === data.idKhachHang)
        tenKhachHang = khach?.hoTen || null
      } catch (error) {
        console.error('Error fetching khach hang:', error)
      }
    }

    let tongTien = 0
    const danhSachPhong: PhieuDatTamLocal['danhSachPhong'] = []

    if (data.roomDetails && data.roomDetails.length > 0) {
      for (const room of data.roomDetails) {
        const thanhTien = room.gia * room.soNgay
        tongTien += thanhTien

        danhSachPhong.push({
          idPhong: room.idPhong,
          maPhong: room.maPhong,
          tenPhong: room.tenPhong,
          tenLoaiPhong: room.tenLoaiPhong,
          tang: room.tang,
          gia: room.gia,
          soNgay: room.soNgay,
          thanhTien,
        })
      }
    } else {
      const soNgay = Math.ceil((data.checkOutDate - data.checkInDate) / (1000 * 60 * 60 * 24))
      for (const idPhong of data.danhSachIdPhong) {
        const giaPhong = 1000000
        const thanhTien = giaPhong * soNgay
        tongTien += thanhTien

        danhSachPhong.push({
          idPhong,
          maPhong: `P${idPhong.slice(-3)}`,
          tenPhong: `Phòng ${idPhong.slice(-3)}`,
          tenLoaiPhong: 'Standard',
          tang: 1,
          gia: giaPhong,
          soNgay,
          thanhTien,
        })
      }
    }

    let tienThua = 0
    let congNo = 0
    if (data.tienKhachTra !== null && data.tienKhachTra !== undefined) {
      const hieuSo = data.tienKhachTra - tongTien
      if (hieuSo >= 0) {
        tienThua = hieuSo
      } else {
        congNo = Math.abs(hieuSo)
      }
    } else {
      congNo = tongTien
    }

    let currentStep: PhieuDatTamLocal['currentStep'] = data.currentStep || 'SELECT_ROOM'

    if (!data.currentStep) {
      if (!data.idKhachHang) {
        currentStep = 'CUSTOMER_INFO'
      } else if (data.tienKhachTra === null || data.tienKhachTra === undefined) {
        currentStep = 'PAYMENT_INFO'
      } else {
        currentStep = 'READY_TO_CONFIRM'
      }
    }

    const saved = phieuDatTamStorage.savePhieuDatTam({
      sessionId: data.sessionId,
      checkInDate: data.checkInDate,
      checkOutDate: data.checkOutDate,
      soLuongKhach: data.soLuongKhach,
      idKhachHang: data.idKhachHang,
      tenKhachHang,
      ghiChu: data.ghiChu,
      nhanNgay: data.nhanNgay,
      tienKhachTra: data.tienKhachTra,
      tongTien,
      tienThua,
      congNo,
      danhSachPhong,
      isFromRoomClick: data.isFromRoomClick,
      currentStep,
    })



    return {
      id: saved.id,
      sessionId: saved.sessionId,
      checkInDate: saved.checkInDate,
      checkOutDate: saved.checkOutDate,
      soLuongKhach: saved.soLuongKhach,
      idKhachHang: saved.idKhachHang,
      tenKhachHang: saved.tenKhachHang,
      ghiChu: saved.ghiChu,
      nhanNgay: saved.nhanNgay,
      tienKhachTra: saved.tienKhachTra,
      tongTien: saved.tongTien,
      tienThua: saved.tienThua,
      congNo: saved.congNo,
      danhSachPhong: saved.danhSachPhong,
      isFromRoomClick: saved.isFromRoomClick,
      currentStep: saved.currentStep,
    }
  } catch (error: any) {
    throw new Error(error.message || 'Không thể lưu phiếu đặt tạm')
  }
}

export async function getPhieuDatTam(sessionId: string): Promise<PhieuDatTamResponse> {
  try {
    const phieu = phieuDatTamStorage.getPhieuDatTam(sessionId)
    if (!phieu) {
      throw new Error('Không tìm thấy phiếu đặt tạm')
    }

    return {
      id: phieu.id,
      sessionId: phieu.sessionId,
      checkInDate: phieu.checkInDate,
      checkOutDate: phieu.checkOutDate,
      soLuongKhach: phieu.soLuongKhach,
      idKhachHang: phieu.idKhachHang,
      tenKhachHang: phieu.tenKhachHang,
      ghiChu: phieu.ghiChu,
      nhanNgay: phieu.nhanNgay,
      tienKhachTra: phieu.tienKhachTra,
      tongTien: phieu.tongTien,
      tienThua: phieu.tienThua,
      congNo: phieu.congNo,
      danhSachPhong: phieu.danhSachPhong,
      isFromRoomClick: phieu.isFromRoomClick,
      currentStep: phieu.currentStep,
    }
  } catch (error: any) {
    throw new Error(error.message || 'Không thể tải phiếu đặt tạm')
  }
}

export async function getAllPhieuDatTam(): Promise<PhieuDatTamResponse[]> {
  try {
    const allPhieu = phieuDatTamStorage.getAllPhieuDatTam()
    return allPhieu.map(phieu => ({
      id: phieu.id,
      sessionId: phieu.sessionId,
      checkInDate: phieu.checkInDate,
      checkOutDate: phieu.checkOutDate,
      soLuongKhach: phieu.soLuongKhach,
      idKhachHang: phieu.idKhachHang,
      tenKhachHang: phieu.tenKhachHang,
      ghiChu: phieu.ghiChu,
      nhanNgay: phieu.nhanNgay,
      tienKhachTra: phieu.tienKhachTra,
      tongTien: phieu.tongTien,
      tienThua: phieu.tienThua,
      congNo: phieu.congNo,
      danhSachPhong: phieu.danhSachPhong,
      isFromRoomClick: phieu.isFromRoomClick,
      currentStep: phieu.currentStep,
    }))
  } catch (error: any) {
    throw new Error(error.message || 'Không thể tải danh sách phiếu đặt tạm')
  }
}

export async function confirmBookingFromPhieuTam(data: ConfirmBookingRequest) {
  try {
    const payload = {
      idKhachHang: data.idKhachHang,
      idChiTietDoan: data.idChiTietDoan || null,
      tenDoan: data.tenDoan || '',
      ngayNhan: data.checkInDate,
      ngayTra: data.checkOutDate,
      soLuongKhach: data.soLuongKhach,
      ghiChu: data.ghiChu || null,
      nhanNgay: !!data.nhanNgay,
      tienKhachTra: (data.tienKhachTra !== null && data.tienKhachTra !== undefined) ? data.tienKhachTra : null,
      danhSachIdPhong: data.danhSachIdPhong || [],
      danhSachLoaiPhong: data.danhSachLoaiPhong ? data.danhSachLoaiPhong.map(lp => ({
        idLoaiPhong: lp.idLoaiPhong,
        soLuong: lp.soLuong
      })) : []
    }

    const res = (await request({
      url: `${API_LE_TAN_BOOKING}/confirm`,
      method: 'POST',
      data: payload,
    })) as AxiosResponse<DefaultResponse<any>>

    if (data.sessionId) {
      phieuDatTamStorage.deletePhieuDatTam(data.sessionId)
    }

    return res.data.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể xác nhận đặt phòng')
  }
}

export async function deletePhieuDatTam(sessionId: string): Promise<void> {
  try {
    const success = phieuDatTamStorage.deletePhieuDatTam(sessionId)
    if (!success) {
      throw new Error('Không tìm thấy phiếu đặt tạm')
    }
  } catch (error: any) {
    throw new Error(error.message || 'Không thể xóa phiếu đặt tạm')
  }
}

export async function searchKhachHang(keyword?: string) {
  try {
    const res = (await request({
      url: `${API_LE_TAN_BOOKING}/khach-hang/search`,
      method: 'GET',
      params: { keyword },
    })) as AxiosResponse<DefaultResponse<TimKhachHangResponse[]>>
    return res.data.data || []
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Không thể tìm kiếm khách hàng')
  }
}

export async function checkout(idChiTietDatPhong: string) {
  try {
    const res = await request.post(`${API_LE_TAN_BOOKING}/checkout/${idChiTietDatPhong}`)
    return res.data.data
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Lỗi trả phòng')
  }
}


export interface TruongDoan{
    tenDoan?: string;
  hoTenTruongDoan: string;
  soDienThoaiTruongDoan: string;
  ngaySinhTruongDoan:string;
  loaiGiayToTruongDoan:number;
  soGiayToTruongDoan: string;
  gioiTinhTruongDoan: number;
  isDoan: boolean;
}

export async function addTruongDoan(data: TruongDoan) {
  const res = await request({
    url: `${API_LE_TAN_BOOKING}/add-truong-doan`,
    method: "POST",
    data,
  });
  console.log('Response from addTruongDoan:', res);
  console.log('Response from addTruongDoan1:', res.data.data);
  return res.data.data;
}
