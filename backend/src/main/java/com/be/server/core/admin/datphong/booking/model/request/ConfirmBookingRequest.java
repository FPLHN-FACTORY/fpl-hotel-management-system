package com.be.server.core.admin.datphong.booking.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfirmBookingRequest {

    String idKhachHang;

    Long ngayNhan;

    Long ngayTra;

    String ghiChu;

    Boolean nhanNgay;

    BigDecimal tienKhachTra;

    java.util.List<String> danhSachIdPhong;

    java.util.List<DatPhongTheoLoaiRequest.ChonLoaiPhong> danhSachLoaiPhong;
}