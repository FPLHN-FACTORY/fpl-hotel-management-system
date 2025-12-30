package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.RoomStatus;
import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "dat_phong")
public class DatPhong extends PrimaryEntity implements Serializable {
    @Column(name = "thoi_gian_dat")
    private Long thoiGianDat;

    @Column(name = "thoi_gian_check_in")
    private Long thoiGianCheckIn;

    @Column(name = "thoi_gian_check_out")
    private Long thoiGianCheckOut;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_phong")
    private Phong phong;

    @Column(name = "ma_phong")
    private String maPhong;

    @Column(name = "ten_phong")
    private String tenPhong;

    @Column(name = "loai_phong")
    private String loaiPhong;

    @Column(name = "gia_hien_tai")
    private BigDecimal giaHienTai;

    @Column(name = "suc_chua")
    private String sucChua;


}
