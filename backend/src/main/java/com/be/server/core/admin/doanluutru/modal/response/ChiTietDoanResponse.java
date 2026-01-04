package com.be.server.core.admin.doanluutru.modal.response;

import com.be.server.entity.base.IsIdentified;

import java.time.LocalDate;

public interface ChiTietDoanResponse extends IsIdentified {
//    orderNumber,
//    ctd.id,
//    kh.ho_ten,
//    kh.gioi_tinh,
//    kh.ngay_sinh,
//    kh.loai_giay_to,
//    kh.so_giay_to,
//    ctd.vai_tro
    Integer getOrderNumber();
    String getHoTen();
    Integer getGioiTinh();
    LocalDate getNgaySinh();
    Integer getLoaiGiayTo();
    String getSoGiayTo();
    Integer getVaiTro();
}
