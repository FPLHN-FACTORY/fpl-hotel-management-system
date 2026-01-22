package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EntityLoaiHoaDon;
import com.be.server.infrastructure.constant.EntityTrangThaiHoaDon;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity hóa đơn cho đoàn lưu trú
 * Lưu trữ thông tin chi phí tổng hợp
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "hoa_don")
public class HoaDon extends PrimaryEntity implements Serializable {
    
    @Column(name = "ma_hoa_don", unique = true)
    private String maHoaDon; // Mã hóa đơn tự động tạo
    
    @ManyToOne
    @JoinColumn(name = "id_doan_luu_tru")
    private DoanLuuTru doanLuuTru;
    
    @Column(name = "tong_tien_phong", precision = 15, scale = 2)
    private BigDecimal tongTienPhong; // Tổng tiền phòng
    
    @Column(name = "tong_tien_dich_vu", precision = 15, scale = 2)
    private BigDecimal tongTienDichVu; // Tổng tiền dịch vụ
    
    @Column(name = "tong_cong", precision = 15, scale = 2)
    private BigDecimal tongCong; // Tổng cộng
    
    @Column(name = "thoi_gian_tao")
    private Long thoiGianTao; // Timestamp tạo hóa đơn
    
    @Enumerated(EnumType.STRING)
    @Column(name = "loai_hoa_don")
    private EntityLoaiHoaDon loaiHoaDon; // TAM_TINH, CHINH_THUC
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private EntityTrangThaiHoaDon trangThai; // CHO_THANH_TOAN, DA_THANH_TOAN, HUY
    
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;
}
