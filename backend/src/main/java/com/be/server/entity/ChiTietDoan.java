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
@Table(name = "chi_tiet_doan")
public class ChiTietDoan extends PrimaryEntity implements Serializable {
    @ManyToOne
    @JoinColumn(name = "id_doan_luu_tru")
    private DoanLuuTru doanLuuTru;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "vai_tro")
    private String vaiTro;
}
