package com.be.server.core.admin.datphong.booking.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoaiPhongAvailableResponse {

    String idLoaiPhong;

    String tenLoaiPhong;

    String moTa;

    Integer soGiuongDon;

    Integer soGiuongDOi;

    Integer soNguoiToiDa;

    BigDecimal giaCaNgay;

    Integer soPhongTrong;
}
