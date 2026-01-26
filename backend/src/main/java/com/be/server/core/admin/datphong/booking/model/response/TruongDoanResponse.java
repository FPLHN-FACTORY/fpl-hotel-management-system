package com.be.server.core.admin.datphong.booking.model.response;

import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TruongDoanResponse {
    private String id;
    private String idChiTietDoan;
    private String tenDoan;
    // Leader Info

    private String hoTen;
    private String soDienThoai;

    private LocalDate ngaySinh;
    private LoaiGiayTo loaiGiayTo;
    private String soGiayTo;
    private GioiTinh gioiTinh;
}
