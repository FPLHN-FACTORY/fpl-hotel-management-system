package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "loai_khach_hang")
public class LoaiKhachHang extends PrimaryEntity implements Serializable {
    @Column(name = "ma_loai")
    private String maLoai;

    @Column(name = "ten_loai")
    private String tenLoai;

    @Column(name = "mo_ta")
    private String moTa;
}
