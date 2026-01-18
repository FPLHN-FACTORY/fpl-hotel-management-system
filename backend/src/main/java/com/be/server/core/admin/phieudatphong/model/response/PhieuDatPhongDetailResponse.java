package com.be.server.core.admin.phieudatphong.model.response;

import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response chi tiết phiếu đặt phòng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuDatPhongDetailResponse {

    String id;

    String maPhieu;

    Long ngayCheckIn;

    Long ngayCheckOut;

    Integer soLuongKhach;

    String ghiChu;

    StatusPhieuDatPhong trangThai;

    // Thông tin khách hàng
    KhachHangInfo khachHang;

    // Danh sách loại phòng đã chọn
    List<LoaiPhongDatInfo> danhSachLoaiPhong;

    // Danh sách phòng cụ thể đã gắn
    List<PhongDaGanInfo> danhSachPhongDaGan;

    BigDecimal tongTien;

    Long ngayTao;

    String tenNhanVienTao;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class KhachHangInfo {
        String id;
        String hoTen;
        String soDienThoai;
        String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class LoaiPhongDatInfo {
        String idLoaiPhong;
        String tenLoaiPhong;
        Integer soLuong;
        Integer soLuongDaGan; // Số phòng đã gắn
        BigDecimal gia;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PhongDaGanInfo {
        String idPhong;
        String maPhong;
        String tenPhong;
        String tenLoaiPhong;
        Integer tang;
        BigDecimal gia;
    }
}
