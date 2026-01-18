import request from "@/service/request";

/**
 * API Service cho quản lý phiếu đặt phòng
 */

export interface TaoPhieuDatRequest {
  ngayCheckIn: number;
  ngayCheckOut: number;
  soLuongKhach: number;
  ghiChu?: string;
  danhSachLoaiPhong: {
    idLoaiPhong: string;
    soLuong: number;
  }[];
}

export interface GanKhachHangRequest {
  idPhieuDat: string;
  idKhachHang: string;
}

export interface GanPhongRequest {
  idPhieuDat: string;
  danhSachIdPhong: string[];
}

export interface PhieuDatPhongFilterRequest {
  keyword?: string;
  status?: string;
  tuNgay?: number;
  denNgay?: number;
  page?: number;
  size?: number;
}

// Tạo phiếu đặt mới
export const apiTaoPhieuDat = (data: TaoPhieuDatRequest) => {
  return request.post("/api/le-tan/phieu-dat-phong", data);
};

// Lấy danh sách phiếu đặt
export const apiGetDanhSachPhieuDat = (filter: PhieuDatPhongFilterRequest) => {
  return request.get("/api/le-tan/phieu-dat-phong", { params: filter });
};

// Lấy chi tiết phiếu đặt
export const apiGetChiTietPhieuDat = (id: string) => {
  return request.get(`/api/le-tan/phieu-dat-phong/${id}`);
};

// Cập nhật phiếu đặt
export const apiCapNhatPhieuDat = (id: string, data: TaoPhieuDatRequest) => {
  return request.put(`/api/le-tan/phieu-dat-phong/${id}`, data);
};

// Hủy phiếu đặt
export const apiHuyPhieuDat = (id: string) => {
  return request.delete(`/api/le-tan/phieu-dat-phong/${id}`);
};

// Gắn khách hàng
export const apiGanKhachHang = (data: GanKhachHangRequest) => {
  return request.post(
    `/api/le-tan/phieu-dat-phong/${data.idPhieuDat}/gan-khach-hang`,
    data,
  );
};

// Lấy phòng khả dụng
export const apiGetPhongKhaDung = (idPhieuDat: string, idLoaiPhong: string) => {
  return request.get(
    `/api/le-tan/phieu-dat-phong/${idPhieuDat}/phong-kha-dung`,
    {
      params: { idLoaiPhong },
    },
  );
};

// Gắn phòng
export const apiGanPhong = (data: GanPhongRequest) => {
  return request.post(
    `/api/le-tan/phieu-dat-phong/${data.idPhieuDat}/gan-phong`,
    data,
  );
};

// Xác nhận phiếu đặt
export const apiXacNhanPhieuDat = (id: string) => {
  return request.post(`/api/le-tan/phieu-dat-phong/${id}/xac-nhan`);
};

// Tìm kiếm khách hàng
export const apiSearchKhachHang = (keyword: string) => {
  return request.get(`/api/v1/leTan/booking/khach-hang/search`, {
    params: { keyword },
  });
};
