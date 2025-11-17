package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
@Entity
@Table(name = "phieu_dat_phong")
public class PhieuDatPhong extends PrimaryEntity implements Serializable  {

    @Column(name = "ngay_check_in")
    private Long checkInDate;

    @Column(name = "ngay_check_out")
    private Long checkOutDate;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status_phieu_dat_phong")
    private StatusPhieuDatPhong status_phieu_dat_phong;

    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    // Một booking có nhiều phòng
    @OneToMany(mappedBy = "phieuDatPhong", cascade = CascadeType.ALL)
    private List<ChiTietDatPhong> bookingDetails;
}
