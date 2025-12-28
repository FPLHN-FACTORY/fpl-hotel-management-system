package com.be.server.core.admin.khachhang.model.request;

import com.be.server.core.common.base.PageableRequest;

import com.be.server.infrastructure.constant.LoaiGiayTo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;



@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ADSearchKhachHangRequest extends PageableRequest {
    String ten;
    Integer loaiGiayTo;
    String soGiayTo;
  String  sdtEmail;
String status;
String idLoaiKhachHang;
}
