package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "khach_hang")
public class KhachHang extends PrimaryEntity implements Serializable {
    @Column(name = "ma_nguoi_dung")
    private String maNguoiDung;

    @Column(name = "ho_ten")
    private String hoTen;

    @Column(name = "email")
    private String email;

    @Column(name = "so-cccd")
    private String soCCD;

    @Column(name = "so_ho_chieu")
    private String soHoChieu;

    @Column(name = "quoc_tich")
    private String quocTich;

    @Column(name = "ngay_het_han_ho_chieu")
    private String ngayHetHanHoChieu;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @ManyToOne
    @JoinColumn(name = "id_loai_khach_hang")
    private LoaiKhachHang loaiKhachHang;
}
