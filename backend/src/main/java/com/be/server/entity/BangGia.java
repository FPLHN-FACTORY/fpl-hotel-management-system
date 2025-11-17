package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//Lịch sử giá của Phòng và Loại phòng
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "bang_gia")
public class BangGia extends PrimaryEntity implements Serializable {

    @Column(name = "gia_ap_dung")
    private BigDecimal giaApDung;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_id")
    private Phong phong;
}
