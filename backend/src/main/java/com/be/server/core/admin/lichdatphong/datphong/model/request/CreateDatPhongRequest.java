package com.be.server.core.admin.lichdatphong.datphong.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateDatPhongRequest {

    Long ngayNhan;

    Long ngayTra;

    String idKhachHang;

    String ghiChu;

    Boolean nhanNgay;

    List<String> danhSachIdPhong;

}
