package com.be.server.core.admin.datphong.booking.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongDatResponse {

    String idPhong;

    String maPhong;

    String tenPhong;

    String tenLoaiPhong;

    Integer tang;

    BigDecimal gia;

    Integer sucChua;

    List<TagInfo> tags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagInfo {

        String id;

        String ma;

        String ten;

        String mau;
    }
}
