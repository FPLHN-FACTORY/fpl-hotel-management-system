package com.be.server.core.admin.datphong.booking.model.response;

import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimKhachHangResponse {

    String id;

    String maNguoiDung;

    String hoTen;

    String email;

    LoaiGiayTo loaiGiayTo;

    GioiTinh gioiTinh;

    LocalDate ngaySinh;

    String soGiayTo;

    String soDienThoai;

    String diaChi;

    String quocTich;
}
