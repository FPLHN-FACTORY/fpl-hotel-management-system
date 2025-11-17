package com.be.server.core.admin.loaiphong.model.response;

import java.math.BigDecimal;

public interface ADLoaiPhongResponse {

    String getId();

    String getMa();

    String getTen();

    Integer getSoGiuongDon();

    Integer getSoGiuongDoi();

    Integer getSoNguoiQuyDinh();

    Integer getSoNguoiToiDa();

    BigDecimal getGiaCaNgay();

}