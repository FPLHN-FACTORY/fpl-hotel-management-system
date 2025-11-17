package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.infrastructure.constant.TrangThaiVeSinh;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
@Entity
@Table(name = "phong")
public class Phong extends PrimaryEntity implements Serializable {

    @Column(name = "tang")
    private Integer tang;

    @JoinColumn(name = "loai_phong_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LoaiPhong loaiPhong;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai_hoat_dong")
    private TrangThaiHoatDong trangThaiHoatDong;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai_ve_sinh")
    private TrangThaiVeSinh trangThaiVeSinh;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhongTag> tags;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BangGia> priceHistory;

    // Các liên kết tới ChiTietDatPhong (booking details) --- optional, không bắt buộc nhưng hữu ích
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDatPhong> chiTietDatPhongs;

}