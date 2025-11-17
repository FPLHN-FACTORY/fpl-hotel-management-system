package com.be.server.core.admin.datphong.trangthaiphong.model.response;

import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import com.be.server.infrastructure.constant.TrangThaiVeSinh;
import lombok.*;

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
    // Không map trong query, map sau trong service
    default TrangThaiPhongDat getTrangThaiPhong() {
        return null;
    }
    String getTrangThaiVeSinh();
}

