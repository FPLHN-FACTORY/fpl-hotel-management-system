package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response hóa đơn đầy đủ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    // Thông tin hóa đơn
    private String maHoaDon;
    private Long thoiGianTao;
    private String loaiHoaDon; // TAM_TINH, CHINH_THUC
    
    // Thông tin đoàn
    private String maDoan;
    private String tenDoan;
    private String tenKhachHang; // Tên trưởng đoàn
    private String soDienThoai;
    
    // Thông tin lưu trú
    private Long ngayCheckIn;
    private Long ngayCheckOut;
    private Long soNgayLuuTru;
    
    // Chi tiết chi phí
    private List<RoomCostDetail> roomCosts;
    private List<ServiceCostDetail> serviceCosts;
    
    // Tổng chi phí
    private BigDecimal tongTienPhong;
    private BigDecimal tongTienDichVu;
    private BigDecimal tongCong;
    
    // Thông tin khách sạn (nếu cần in)
    private String tenKhachSan;
    private String diaChiKhachSan;
    private String soDienThoaiKhachSan;
}
