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
@Setter
@Entity
@Table(name = "doan_luu_tru")
public class DoanLuuTru extends PrimaryEntity implements Serializable {
    @Column(name = "ma_doan")
    private String maDoan;

    @Column(name = "ten_doan")
    private String tenDoan;

    @OneToOne
    @JoinColumn(name = "id_dat_phong")
    private DatPhong datPhong;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "thoi_gian_check_in")
    private Long thoiGianCheckIn;

    @Column(name = "thoi_gian_check_out")
    private Long thoiGianCheckOut;

    @Column(name = "trang_thai")
    private Integer trangThai;



}
