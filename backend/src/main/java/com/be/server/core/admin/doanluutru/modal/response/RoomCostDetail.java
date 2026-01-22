package com.be.server.core.admin.doanluutru.modal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Chi tiết chi phí phòng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomCostDetail {
    private String idPhong;
    private String tenPhong;
    private String maPhong;
    private String tenLoaiPhong;
    private BigDecimal giaPhong; // Giá phòng mỗi đêm
    private Long soNgay; // Số ngày lưu trú
    private BigDecimal thanhTien; // Thành tiền = giaPhong * soNgay
}
