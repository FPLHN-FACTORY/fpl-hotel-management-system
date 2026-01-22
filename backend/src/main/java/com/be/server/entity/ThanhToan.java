package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.EntityPhuongThucThanhToan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity thanh toán
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "thanh_toan")
public class ThanhToan extends PrimaryEntity implements Serializable {
    
    @ManyToOne
    @JoinColumn(name = "id_doan_luu_tru")
    private DoanLuuTru doanLuuTru;
    
    @Column(name = "ma_thanh_toan", unique = true)
    private String maThanhToan; // Mã thanh toán
    
    @Column(name = "so_tien", precision = 15, scale = 2)
    private BigDecimal soTien; // Số tiền thanh toán
    
    @Enumerated(EnumType.STRING)
    @Column(name = "phuong_thuc")
    private EntityPhuongThucThanhToan phuongThuc; // Phương thức thanh toán
    
    @Column(name = "thoi_gian_thanh_toan")
    private Long thoiGianThanhToan; // Timestamp thanh toán
    
    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien; // Nhân viên thu tiền
    
    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;
}
