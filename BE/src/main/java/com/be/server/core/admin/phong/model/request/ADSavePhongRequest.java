package com.be.server.core.admin.phong.model.request;

import com.be.server.infrastructure.constant.TrangThaiPhong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ADSavePhongRequest {
    private String ma;
    private String ten;
    private String moTa;
    private BigDecimal gia;
    private Integer sucChua;
    private String idLoaiPhong;
    private BigDecimal giaHienTai;
    private Integer tang;
    private TrangThaiPhong trangThaiPhong;
}