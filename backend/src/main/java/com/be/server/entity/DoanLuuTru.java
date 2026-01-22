package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.DoanLuuTruStatus;
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
@Table(name = "doan_luu_tru")
public class DoanLuuTru extends PrimaryEntity implements Serializable {
    @Column(name = "ma_doan")
    private String maDoan;

    @Column(name = "ten_doan")
    private String tenDoan;

    @OneToOne
    @JoinColumn(name = "id_phieu_dat_phong")
    private PhieuDatPhong phieuDatPhong;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thoi_gian_check_in")
    private Long thoiGianCheckIn;

    @Column(name = "thoi_gian_check_out")
    private Long thoiGianCheckOut;
@Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai")
    private DoanLuuTruStatus trangThai;

    @Column(name = "tong_tien_thanh_toan", precision = 15, scale = 2)
    private BigDecimal tongTienThanhToan; // Tổng số tiền đã thanh toán

    @Column(name = "cong_no", precision = 15, scale = 2)
    private BigDecimal congNo; // Số tiền còn nợ

}
