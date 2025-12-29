package com.be.server.core.admin.lichdatphong.datphong.model.response;

import com.be.server.core.admin.phong.model.response.PhongResponse;
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
