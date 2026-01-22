package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response trạng thái thanh toán
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String idDoan;
    private String maDoan;
    private String tenDoan;
    
    private BigDecimal tongChiPhi; // Tổng chi phí cần thanh toán
    private BigDecimal tongDaThanhToan; // Tổng số tiền đã thanh toán
    private BigDecimal congNo; // Số tiền còn nợ
    
    private List<PaymentDetail> payments; // Danh sách các lần thanh toán
    
    private String trangThai; // DA_THANH_TOAN, CON_NO
}
