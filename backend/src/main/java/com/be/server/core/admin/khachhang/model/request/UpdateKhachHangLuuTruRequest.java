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
public class UpdateKhachHangLuuTruRequest {
    String hoTen;
    LocalDate ngaySinh;
    Integer gioiTinh;
    Integer loaiGiayTo;
    String soGiayTo;
}
