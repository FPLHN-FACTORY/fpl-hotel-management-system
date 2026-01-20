package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
@Entity
@Table(name = "phieu_dat_phong")
public class PhieuDatPhong extends PrimaryEntity implements Serializable {

    @Column(name = "ngay_check_in")
    private Long checkInDate;

    @Column(name = "ngay_check_out")
    private Long checkOutDate;

    @Column(name = "so_luong_khach")
    private Integer soLuongKhach;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_phieu_dat_phong")
    private StatusPhieuDatPhong status_phieu_dat_phong;

    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    // Một booking có nhiều phòng
    @OneToMany(mappedBy = "phieuDatPhong", cascade = CascadeType.ALL)
    private List<ChiTietDatPhong> bookingDetails;

    // Relationship với chi tiết loại phòng đặt (cho phiếu PENDING)
    @OneToMany(mappedBy = "phieuDatPhong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietLoaiPhongDat> chiTietLoaiPhong;

    // Số lượng khách
    @Column(name = "so_luong_khach")
    private Integer soLuongKhach;

    // Ghi chú
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    // Người tạo phiếu (nhân viên lễ tân)
    @ManyToOne
    @JoinColumn(name = "nhan_vien_tao_id")
    private NhanVien nhanVienTao;
}
