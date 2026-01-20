package com.be.server.core.admin.doanluutru.modal.response;

import com.be.server.entity.base.IsIdentified;

import java.time.LocalDate;

public interface DoanLuuTruResponse extends IsIdentified {
    Integer getOrderNumber();
    String getMaDoan();
    String getTenDoan();
    String getHoTen();
    LocalDate getNgaySinh();
    Integer getGioiTinh();
    Integer getLoaiGiayTo();
    String getSoDienThoai();
    String getSoGiayTo();
    String getMaDatPhong();
    String getGhiChu();
    Long getThoiGianCheckIn();
    Long getThoiGianCheckOut();
    Integer getTrangThai();
}
