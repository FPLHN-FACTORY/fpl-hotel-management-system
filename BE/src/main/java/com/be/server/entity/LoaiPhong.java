package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EnLoaiPhong;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table( name = "loai_phong")
public class LoaiPhong extends PrimaryEntity implements Serializable {

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "gia_hien_tai")
    private BigDecimal giaHienTai;

    @Column(name = "so_nguoi_quy_dinh")
    private Integer soNguoiQuyDinh;

    @Column( name = "so_nguoi_toi_da")
    private Integer soNguoiToiDa;
}
