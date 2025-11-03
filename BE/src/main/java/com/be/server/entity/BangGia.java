package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "bang_gia")
public class BangGia extends PrimaryEntity implements Serializable {

    @Column(name = "ma_bang_gia")
    private String maBangGia;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "gia_ngay_thuong")
    private BigDecimal giaNgayThuong;

    @Column(name = "gia_cuoi_tuan")
    private BigDecimal giaCuoiTuan;

    @Column(name = "he_so_cao_diem")
    private Double heSoCaoDiem;

    @Column(name = "ghi_chu")
    private String ghiChu;
}
