package com.be.server.core.admin.phong.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaiPhongResponse {
    private String id;
    private Integer soNguoiToiDa;
    private Integer soNguoiQuyDinh;
    private Integer soGiuongDon;
    private Integer soGiuongDoi;
    private BigDecimal giaCaNgay;
    private String ten;

    public LoaiPhongResponse(String id, Integer soNguoiToiDa, BigDecimal giaCaNgay, String ten) {
        this.id = id;
        this.soNguoiToiDa = soNguoiToiDa;
        this.giaCaNgay = giaCaNgay;
        this.ten = ten;
    }
}