package com.be.server.core.admin.datphong.booking.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookingResponse {

    String idPhieuDatPhong;

    String maPhieuDatPhong;

    String tenKhachHang;

    Long ngayCheckIn;

    Long ngayCheckOut;

    String trangThai;

    BigDecimal tongTien;

    List<ChiTietPhongInfo> danhSachPhong;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietPhongInfo {
        String idChiTiet;
        String idPhong;
        String maPhong;
        String tenPhong;
        BigDecimal giaPhong;
        String trangThaiChiTiet;
    }
}