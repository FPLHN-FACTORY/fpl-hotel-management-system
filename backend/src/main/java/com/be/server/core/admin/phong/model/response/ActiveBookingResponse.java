package com.be.server.core.admin.phong.model.response;

import com.be.server.infrastructure.constant.StatusChiTietDatPhong;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActiveBookingResponse {
    private String id;
    private Long checkIn;
    private Long checkOut;
    private BigDecimal price;
    private Integer soLuongKhach;
    private StatusChiTietDatPhong statusChiTiet;
    
    // Phieu dat phong info
    private String phieuDatPhongId;
    private Long phieuCheckInDate;
    private Long phieuCheckOutDate;
    private StatusPhieuDatPhong phieuStatus;
    private String customerName;
    private String customerId;
    
    // Room info
    private String roomId;
    private String roomCode;
    private String roomName;
}
