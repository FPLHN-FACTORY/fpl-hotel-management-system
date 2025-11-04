package com.be.server.core.admin.phong.model.response;

import com.be.server.infrastructure.constant.RoomStatus;
import java.math.BigDecimal;

public interface PhongProjection {

    String getId();

    String getMaPhong();

    String getTenPhong();

    String getLoaiPhong();

    BigDecimal getGiaHienTai();

    Integer getSucChua();

    RoomStatus getTrangThaiPhong();

    BangGiaProjection getBangGia();

    interface BangGiaProjection {

        String getId();

        String getMaBangGia();

        BigDecimal getGiaNgayThuong();

        BigDecimal getGiaCuoiTuan();

        Double getHeSoCaoDiem();
    }
}