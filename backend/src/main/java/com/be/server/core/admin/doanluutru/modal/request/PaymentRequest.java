package com.be.server.core.admin.doanluutru.modal.request;

import com.be.server.infrastructure.constant.EntityPhuongThucThanhToan;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Request DTO để xử lý thanh toán
 */
@Getter
@Setter
public class PaymentRequest {
    
    @NotNull(message = "ID đoàn lưu trú không được để trống")
    private String idDoanLuuTru;
    
    @NotNull(message = "Số tiền thanh toán không được để trống")
    private BigDecimal soTien;
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private EntityPhuongThucThanhToan phuongThuc;
    
    private String ghiChu;
}
