package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.RoomStatus;
import com.be.server.infrastructure.constant.TrangThaiPhong;
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

    @Column(name = "gia_hien_tai")
    private BigDecimal giaHienTai;

    @Column(name = "suc_chua")
    private String sucChua;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai_phong")
    private TrangThaiPhong trangThaiPhong;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhongTag> tags;

    @OneToMany(mappedBy = "phong", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BangGia> priceHistory;

}