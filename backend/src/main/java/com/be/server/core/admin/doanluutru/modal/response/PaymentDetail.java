package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Chi tiết một lần thanh toán
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {
    private String id;
    private String maThanhToan;
    private BigDecimal soTien;
    private String phuongThuc;
    private Long thoiGianThanhToan;
    private String tenNhanVien;
    private String ghiChu;
}
