package com.be.server.core.admin.phong.model.response;

import com.be.server.core.common.base.IsEntityPhong;
import com.be.server.core.common.base.IsIdentify;
import com.be.server.infrastructure.constant.RoomStatus;
import java.math.BigDecimal;

public interface PhongProjection extends IsIdentify, IsEntityPhong {

    String getMa();

    String getTen();

    BigDecimal getPrice();

    String getLoaiPhong();

    String getSucChua();

    String getTrangThai();

}