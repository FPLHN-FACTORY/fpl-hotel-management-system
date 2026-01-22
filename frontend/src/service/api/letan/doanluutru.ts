import request from '@/service/request'

export interface AddGuestDuringStayRequest {
  idDoanLuuTru?: string
  idPhong: string
  hoTen: string
  gioiTinh: string
  soDienThoai: string
  email?: string
  loaiGiayTo: string
  soGiayTo: string
  ngaySinh?: number
  diaChi?: string
  quocTich?: string
  ghiChu?: string
  confirmUseOld?: boolean
}

export interface RoomCapacityInfo {
  idPhong: string
  tenPhong: string
  currentGuests: number
  maxGuests: number
}

/**
 * Thêm khách mới vào đoàn trong quá trình lưu trú
 * Chỉ cho phép khi trạng thái = DANG_LUU_TRU
 */
export function addGuestDuringStay(idDoan: string, data: AddGuestDuringStayRequest) {
  return request<any>({
    url: `/le-tan/doan-luu-tru/${idDoan}/add-guest-during-stay`,
    method: 'post',
    data,
  })
}

/**
 * Lấy danh sách phòng trong đoàn với thông tin sức chứa
 */
export function getRoomsByDoan(idDoan: string) {
  return request<RoomCapacityInfo[]>({
    url: `/le-tan/doan-luu-tru/${idDoan}/rooms`,
    method: 'get',
  })
}

export interface CostBreakdownResponse {
  idDoan: string
  maDoan: string
  tenDoan: string
  ngayCheckIn: number
  ngayCheckOut: number
  soNgayLuuTru: number
  roomCosts: RoomCostDetail[]
  serviceCosts: ServiceCostDetail[]
  tongTienPhong: number
  tongTienDichVu: number
  tongCong: number
}

export interface RoomCostDetail {
  idPhong: string
  tenPhong: string
  maPhong: string
  tenLoaiPhong: string
  giaPhong: number
  soNgay: number
  thanhTien: number
}

export interface ServiceCostDetail {
  idDichVu: string
  tenDichVu: string
  soLuong: number
  donGia: number
  thanhTien: number
  thoiGianTao: number
  phongApDung?: string
}

export interface InvoiceResponse {
  maHoaDon: string
  thoiGianTao: number
  loaiHoaDon: string
  maDoan: string
  tenDoan: string
  tenKhachHang: string
  soDienThoai: string
  ngayCheckIn: number
  ngayCheckOut: number
  soNgayLuuTru: number
  roomCosts: RoomCostDetail[]
  serviceCosts: ServiceCostDetail[]
  tongTienPhong: number
  tongTienDichVu: number
  tongCong: number
  tenKhachSan: string
  diaChiKhachSan: string
  soDienThoaiKhachSan: string
}

/**
 * Lấy phân tích chi phí chi tiết
 */
export function getCostBreakdown(idDoan: string) {
  return request<CostBreakdownResponse>({
    url: `/le-tan/doan-luu-tru/${idDoan}/cost-breakdown`,
    method: 'get',
  })
}

/**
 * Lấy hóa đơn tạm tính
 */
export function getTemporaryInvoice(idDoan: string) {
  return request<InvoiceResponse>({
    url: `/le-tan/doan-luu-tru/${idDoan}/temporary-invoice`,
    method: 'get',
  })
}

export interface PaymentRequest {
  idDoanLuuTru?: string
  soTien: number
  phuongThuc: string
  ghiChu?: string
}

export interface PaymentStatusResponse {
  idDoan: string
  maDoan: string
  tenDoan: string
  tongChiPhi: number
  tongDaThanhToan: number
  congNo: number
  payments: PaymentDetail[]
  trangThai: string
}

export interface PaymentDetail {
  id: string
  maThanhToan: string
  soTien: number
  phuongThuc: string
  thoiGianThanhToan: number
  tenNhanVien?: string
  ghiChu?: string
}

/**
 * Xử lý thanh toán
 */
export function processPayment(idDoan: string, data: PaymentRequest) {
  return request<any>({
    url: `/le-tan/doan-luu-tru/${idDoan}/payment`,
    method: 'post',
    data,
  })
}

/**
 * Lấy trạng thái thanh toán
 */
export function getPaymentStatus(idDoan: string) {
  return request<PaymentStatusResponse>({
    url: `/le-tan/doan-luu-tru/${idDoan}/payment-status`,
    method: 'get',
  })
}

/**
 * Lấy thông tin đoàn theo mã phiếu đặt phòng
 */
export function getDoanByBooking(idBooking: string) {
  return request<{ id: string, maDoan: string, tenDoan: string }>({
    url: `/le-tan/doan-luu-tru/booking/${idBooking}`,
    method: 'get',
  })
}
