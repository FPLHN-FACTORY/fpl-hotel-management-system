package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EntityProperties;
import jakarta.persistence.*;
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
@Table(name = "dich_vu")
public class DichVu extends PrimaryEntity implements Serializable {

    @Column(name = "ma_dich_vu", unique = true)
    private String maDichVu;

    @Column(name = "ten_dich_vu")
    private String tenDichVu;

    @Column(name = "don_vi_tinh")
    private String donViTinh;

    @Column(name = "don_gia")
    private BigDecimal donGia;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai = EntityProperties.HOAT_DONG;
}
