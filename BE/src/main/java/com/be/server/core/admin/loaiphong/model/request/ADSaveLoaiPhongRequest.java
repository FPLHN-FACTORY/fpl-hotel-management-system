package com.be.server.core.admin.loaiphong.model.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ADSaveLoaiPhongRequest {

    private String id;

    private String ma;

    private String ten;

    private Integer soGiuongDon;

    private Integer soGiuongDoi;

    private Integer soNguoiQuyDinh;

    private Integer soNguoiToiDa;

    private BigDecimal giaCaNgay;

}
