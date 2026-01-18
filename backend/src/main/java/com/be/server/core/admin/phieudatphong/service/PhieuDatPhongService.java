package com.be.server.core.admin.phieudatphong.service;

import com.be.server.core.admin.phieudatphong.model.request.*;
import com.be.server.core.common.base.ResponseObject;

/**
 * Service quản lý phiếu đặt phòng.
 */
public interface PhieuDatPhongService {

    /**
     * Tạo phiếu đặt mới theo loại phòng (trạng thái PENDING)
     */
    ResponseObject<?> taoPhieuDat(TaoPhieuDatRequest request);

    /**
     * Lấy danh sách phiếu đặt với filter và pagination
     */
    ResponseObject<?> getDanhSachPhieuDat(PhieuDatPhongFilterRequest filter);

    /**
     * Xem chi tiết phiếu đặt
     */
    ResponseObject<?> getChiTietPhieuDat(String idPhieuDat);

    /**
     * Gắn khách hàng vào phiếu đặt
     */
    ResponseObject<?> ganKhachHang(GanKhachHangRequest request);

    /**
     * Lấy danh sách phòng khả dụng cho phiếu đặt theo loại phòng
     */
    ResponseObject<?> getPhongKhaDung(String idPhieuDat, String idLoaiPhong);

    /**
     * Gắn phòng cụ thể vào phiếu đặt
     */
    ResponseObject<?> ganPhong(GanPhongRequest request);

    /**
     * Xác nhận phiếu đặt (PENDING → CONFIRMED)
     */
    ResponseObject<?> xacNhanPhieuDat(String idPhieuDat);

    /**
     * Hủy phiếu đặt
     */
    ResponseObject<?> huyPhieuDat(String idPhieuDat);

    /**
     * Cập nhật thông tin phiếu đặt (chỉ khi PENDING)
     */
    ResponseObject<?> capNhatPhieuDat(String idPhieuDat, TaoPhieuDatRequest request);
}
