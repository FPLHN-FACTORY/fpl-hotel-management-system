package com.be.server.core.admin.phieudatphong.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response cho danh sách phòng khả dụng theo loại phòng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhongKhaDungResponse {

    String id;

    String maPhong;

    String tenPhong;

    Integer tang;

    BigDecimal gia;

    List<TagInfo> tags;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class TagInfo {
        String id;
        String ma;
        String ten;
        String mau;
    }
}
