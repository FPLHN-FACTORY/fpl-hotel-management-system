package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Entity để ghi log khi thêm khách vào đoàn trong quá trình lưu trú
 * Audit trail for tracking guest additions during active stay
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "guest_log")
public class GuestLog extends PrimaryEntity implements Serializable {
    
    @ManyToOne
    @JoinColumn(name = "id_doan_luu_tru")
    private DoanLuuTru doanLuuTru;
    
    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
    
    @ManyToOne
    @JoinColumn(name = "id_phong")
    private Phong phong;
    
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
    
    @Column(name = "thoi_gian_them")
    private Long thoiGianThem;
    
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;
    
    @Column(name = "hanh_dong", length = 50)
    private String hanhDong; // "THEM_KHACH_DANG_LUU_TRU"
}
