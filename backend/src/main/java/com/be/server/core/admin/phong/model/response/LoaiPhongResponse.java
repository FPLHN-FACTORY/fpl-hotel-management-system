package com.be.server.core.admin.phong.model.response;

import com.be.server.infrastructure.constant.EnLoaiPhong;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoaiPhongResponse {
    private String id;
    private BigDecimal giaCaNgay;
    private Integer soLuongNguoiToiDa;
    private String ten;

    public LoaiPhongResponse(String id , Integer soLuongNguoiToiDa,
                             BigDecimal giaCaNgay , String ten) {
        this.id = id;
        this.soLuongNguoiToiDa = soLuongNguoiToiDa;
        this.giaCaNgay = giaCaNgay;
        this.ten = ten;
    }
}
