package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.StatusChiTietDatPhong;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
@Entity
@Table(name = "chi_tiet_dat_phong")
public class ChiTietDatPhong extends PrimaryEntity implements Serializable {

    @Column(name = "check_in_thuc_te")
    private Long checkIn;

    @Column(name = "checkout_thuc_te")
    private Long checkOut;

    @Column(name = "gia")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_chi_tiet")
    private StatusChiTietDatPhong status_chi_tiet = StatusChiTietDatPhong.BOOKED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_phong_id")
    private PhieuDatPhong phieuDatPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id")
    private Phong room;

}
