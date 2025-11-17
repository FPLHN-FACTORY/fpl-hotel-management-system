package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
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
@Table(name = "dich_vu_phat_sinh")
public class DichVuPhatSinh extends PrimaryEntity implements Serializable {
    @Column(name = "ma_dich_vu")
    private String maDichVu;

    @Column(name = "ten_dich_vu")
    private String tenDichVu;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "don_gia")
    private String donGia;

    @Column(name = "thanh_tien")
    private BigDecimal thanhTien;

    @ManyToOne
    @JoinColumn(name = "id_dat_phong")
    private DatPhong idDatPhong;

    @ManyToOne
    @JoinColumn(name = "id_le_tan")
    private LeTan idLeTan;
}
