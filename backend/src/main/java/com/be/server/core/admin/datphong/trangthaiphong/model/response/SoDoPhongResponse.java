package com.be.server.core.admin.datphong.trangthaiphong.model.response;

import com.be.server.infrastructure.constant.TrangThaiPhongDat;

import java.math.BigDecimal;

public interface SoDoPhongResponse {
    String getId();
    String getMa();
    String getTen();
    String getLoaiPhong();
    Integer getTang();
    String getTrangThaiHoatDong();
    Integer getSucChua();
    BigDecimal getPrice();
    default TrangThaiPhongDat getTrangThaiPhong() {
        return null;
    }
    String getTrangThaiVeSinh();
}

