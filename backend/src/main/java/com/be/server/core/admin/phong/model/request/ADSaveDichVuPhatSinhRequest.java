package com.be.server.core.admin.phong.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ADSaveDichVuPhatSinhRequest {
    private String tenDichVu;
    private int soLuong;
    private BigDecimal donGia;
    private String donVi; // Optional if needed
    private String idPhieuDatPhong; // For Booking/Group
    private String idChiTietDatPhong; // For Room
}
