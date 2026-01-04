package com.be.server.core.admin.khachhang.model.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ADAddAndUpdateKhachHangRequest {
    String hoTen;
    String soDienThoai;
    LocalDate ngaySinh;
    Integer gioiTinh;
    String email;
    Integer loaiGiayTo;
    String soGiayTo;
    String diaChi;
    String idLoaiKhachHang;
}
