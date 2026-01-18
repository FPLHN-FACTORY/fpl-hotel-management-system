package com.be.server.core.admin.khachhang.model.response;

import com.be.server.core.common.base.IsIdentify;

import java.time.LocalDate;

public interface ADKhachHangResponse extends IsIdentify {
    Integer getOrderNumber();

    String getMa();

    String getHoTen();

    LocalDate getNgaySinh();

    Integer getGioiTinh();

    Integer getLoaiGiayTo();

    String getSoGiayTo();

    String getSoDienThoai();

    String getEmail();

    String getDiaChi();

    Integer getStatus();

    String getidLoaiKhachHang();

    String getTenLoaiKhachHang();
}
