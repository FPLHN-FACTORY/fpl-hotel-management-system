package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Chi tiết chi phí dịch vụ phát sinh
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCostDetail {
    private String idDichVu;
    private String tenDichVu;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien; // = soLuong * donGia
    private Long thoiGianTao; // Thời gian tạo dịch vụ
    private String phongApDung; // Phòng áp dụng (nếu có)
}
