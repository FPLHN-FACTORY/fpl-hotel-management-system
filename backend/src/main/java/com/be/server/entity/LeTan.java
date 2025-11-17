package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "le_tan")
public class LeTan extends PrimaryEntity implements Serializable {
    @Column(name = "ma_le_tan")
    private String maLeTan;

    @Column(name = "ten_le_tan")
    private String tenLeTan;

    @Column(name = "gioi_tinh")
    private BigDecimal gioiTinh;

    @Column(name = "email")
    private String email;

    @Column(name = "ten_dang_nhap")
    private String tenDangNhap;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;
}
