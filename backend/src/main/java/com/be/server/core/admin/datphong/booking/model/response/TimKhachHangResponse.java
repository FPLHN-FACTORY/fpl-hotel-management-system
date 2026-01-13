package com.be.server.core.admin.datphong.booking.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

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

    String soCCCD;

    String soDienThoai;

    String diaChi;

    String quocTich;
}
