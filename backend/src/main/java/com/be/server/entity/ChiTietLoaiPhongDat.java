package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity lưu thông tin số lượng phòng theo từng loại trong phiếu đặt PENDING.
 * Khi phiếu ở trạng thái PENDING, chưa gắn phòng cụ thể, chỉ biết loại phòng và
 * số lượng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@DynamicUpdate
@Entity
@Table(name = "chi_tiet_loai_phong_dat")
public class ChiTietLoaiPhongDat extends PrimaryEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_dat_phong_id", nullable = false)
    private PhieuDatPhong phieuDatPhong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loai_phong_id", nullable = false)
    private LoaiPhong loaiPhong;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    /**
     * Giá của loại phòng tại thời điểm đặt.
     * Lưu lại để tránh thay đổi giá sau ảnh hưởng đến phiếu đặt cũ.
     */
    @Column(name = "gia_dat_truoc", precision = 19, scale = 2)
    private BigDecimal giaDatTruoc;
}
