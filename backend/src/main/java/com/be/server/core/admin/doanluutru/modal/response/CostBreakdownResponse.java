package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response chi tiết phân tích chi phí
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CostBreakdownResponse {
    private String idDoan;
    private String maDoan;
    private String tenDoan;
    
    private Long ngayCheckIn;
    private Long ngayCheckOut;
    private Long soNgayLuuTru; // Số đêm lưu trú
    
    private List<RoomCostDetail> roomCosts; // Chi phí phòng
    private List<ServiceCostDetail> serviceCosts; // Chi phí dịch vụ
    
    private BigDecimal tongTienPhong; // Tổng tiền phòng
    private BigDecimal tongTienDichVu; // Tổng tiền dịch vụ
    private BigDecimal tongCong; // Tổng cộng
}
