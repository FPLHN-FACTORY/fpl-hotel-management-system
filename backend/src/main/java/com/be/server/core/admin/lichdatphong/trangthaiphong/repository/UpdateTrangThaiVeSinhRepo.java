package com.be.server.core.admin.lichdatphong.trangthaiphong.repository;

import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import com.be.server.repository.PhongRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UpdateTrangThaiVeSinhRepo extends PhongRepository {

    /**
     * Lấy trạng thái phòng động (ordinal của enum TrangThaiPhongDat)
     */
    @Query("""
    SELECT 
        CASE
            WHEN dp.id IS NULL THEN 0
            WHEN :now < dp.checkInDate THEN 1
            WHEN :now BETWEEN dp.checkInDate AND dp.checkOutDate THEN 2
            WHEN dp.checkOutDate < :now AND dp.checkOutDate >= :nowMinus1Hour THEN 3
            ELSE 4
        END
    FROM Phong p
    LEFT JOIN p.chiTietDatPhongs ctdp
    LEFT JOIN ctdp.phieuDatPhong dp
    WHERE p.id = :id
""")
    Integer getDynamicRoomStatusOrdinal(
            @Param("id") String phongId,
            @Param("now") Long now,
            @Param("nowMinus1Hour") Long nowMinus1Hour
    );

    default TrangThaiPhongDat getDynamicRoomStatus(String phongId, Long now, Long nowMinus1Hour) {
        Integer ordinal = getDynamicRoomStatusOrdinal(phongId, now, nowMinus1Hour);
        return TrangThaiPhongDat.values()[ordinal];
    }
}
