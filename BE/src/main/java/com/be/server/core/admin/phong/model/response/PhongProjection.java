package com.be.server.core.admin.phong.model.response;

import com.be.server.core.common.base.IsEntityPhong;
import com.be.server.core.common.base.IsIdentify;

import java.math.BigDecimal;

public interface PhongProjection extends IsIdentify, IsEntityPhong {

    String getMa();

    String getTen();

    BigDecimal getPrice();

    Integer getTang();

    String getLoaiPhong();

    String getSucChua();

}