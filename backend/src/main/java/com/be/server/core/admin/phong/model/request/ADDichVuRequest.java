package com.be.server.core.admin.phong.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ADDichVuRequest {

    private String id;

    @NotBlank(message = "Mã dịch vụ không được để trống")
    private String maDichVu;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String tenDichVu;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String donViTinh;

    @NotNull(message = "Đơn giá không được để trống")
    private BigDecimal donGia;

    private String moTa;

    private Integer trangThai;
}
