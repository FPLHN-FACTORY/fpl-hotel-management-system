package com.be.server.core.admin.phong.model.response;



import java.math.BigDecimal;

public interface RoomResponse {

     String getMaPhong();

     String getTenPhong();

     String getLoaiPhong();

     BigDecimal getGiaHienTai();

     Integer getSucChua();

     Integer getTrangThaiPhong();

     BigDecimal getBangGia();
}
