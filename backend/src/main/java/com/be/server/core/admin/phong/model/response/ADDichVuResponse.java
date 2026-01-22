package com.be.server.core.admin.phong.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ADDichVuResponse {
    private String id;
    private String maDichVu;
    private String tenDichVu;
    private String donViTinh;
    private BigDecimal donGia;
    private String moTa;
    private Integer trangThai;
    private Long createdDate;
}
