package com.be.server.core.admin.datphong.trangthaiphong.repository;

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
    @Query(
            value = """
                SELECT 
                    CASE
                        WHEN pdp.id IS NULL  THEN 0
                        WHEN :now < pdp.ngay_check_in THEN 1
                        WHEN :now BETWEEN pdp.ngay_check_in AND pdp.ngay_check_out THEN 2
                        WHEN pdp.ngay_check_out < :now AND pdp.ngay_check_out >= :nowMinus1Hour THEN 3
                        ELSE 4
                    END
                FROM phong p
                LEFT JOIN chi_tiet_dat_phong ctdp
                    ON ctdp.phong_id = p.id
                LEFT JOIN phieu_dat_phong pdp
                    ON pdp.id = ctdp.phieu_dat_phong_id
                    AND pdp.ngay_check_out >= :nowMinus1Hour
                WHERE p.id = :phongId
                ORDER BY pdp.ngay_check_in DESC
                LIMIT 1
""", nativeQuery=true
    )
    Integer getDynamicRoomStatusOrdinal(
            @Param("phongId") String phongId,
            @Param("now") Long now,
            @Param("nowMinus1Hour") Long nowMinus1Hour
    );

    default TrangThaiPhongDat getDynamicRoomStatus(String phongId, Long now, Long nowMinus1Hour) {
        Integer ordinal = getDynamicRoomStatusOrdinal(phongId, now, nowMinus1Hour);
        return ordinal == null
                ? TrangThaiPhongDat.TRONG
                : TrangThaiPhongDat.values()[ordinal];
    }
}
