package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.infrastructure.listener.CreateKhachHangEntityListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "khach_hang",uniqueConstraints = {
        @UniqueConstraint(
                name = "ux_loai_so_giay_to",
                columnNames = {"loai_giay_to", "so_giay_to"}
        )
})
@EntityListeners(CreateKhachHangEntityListener.class)
public class KhachHang extends PrimaryEntity implements Serializable {
    @Column(name = "ma_nguoi_dung")
    private String maNguoiDung;
    @Column(name = "ho_ten")
    private String hoTen;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "gioi_tinh")
    private GioiTinh gioiTinh;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;
    @Column(name = "email")
    private String email;
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "loai_giay_to", nullable = false)
    private LoaiGiayTo loaiGiayTo;


    @Column(name = "so_giay_to", nullable = false)
    private String soGiayTo;



    @Column(name = "quoc_tich")
    private String quocTich;

    @Column(name = "ngay_het_han_ho_chieu")
    private LocalDate ngayHetHanHoChieu;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @ManyToOne
    @JoinColumn(name = "id_loai_khach_hang")
    private LoaiKhachHang loaiKhachHang;
}
