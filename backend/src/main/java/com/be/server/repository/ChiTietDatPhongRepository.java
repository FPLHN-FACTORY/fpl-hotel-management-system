package com.be.server.repository;

import com.be.server.entity.ChiTietDatPhong;
import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChiTietDatPhongRepository extends JpaRepository<ChiTietDatPhong, String> {

    @Query(value = """
                SELECT
                    CASE
                        WHEN EXISTS (
                            SELECT 1 FROM chi_tiet_dat_phong ctdp
                            JOIN phieu_dat_phong pdp ON pdp.id = ctdp.phieu_dat_phong_id
                            WHERE ctdp.phong_id = :roomId
                            AND :now >= (pdp.ngay_check_out - :oneHour)
                            AND :now < pdp.ngay_check_out
                        ) THEN 3

                        WHEN EXISTS (
                            SELECT 1 FROM chi_tiet_dat_phong ctdp
                            JOIN phieu_dat_phong pdp ON pdp.id = ctdp.phieu_dat_phong_id
                            WHERE ctdp.phong_id = :roomId
                            AND :now >= pdp.ngay_check_in
                            AND :now < (pdp.ngay_check_out - :oneHour)
                        ) THEN 2

                        WHEN EXISTS (
                            SELECT 1 FROM chi_tiet_dat_phong ctdp
                            JOIN phieu_dat_phong pdp ON pdp.id = ctdp.phieu_dat_phong_id
                            WHERE ctdp.phong_id = :roomId
                            AND :now > pdp.ngay_check_out
                            AND :now <= (pdp.ngay_check_out + :oneDay)
                        ) THEN 4

                        WHEN EXISTS (
                            SELECT 1 FROM chi_tiet_dat_phong ctdp
                            JOIN phieu_dat_phong pdp ON pdp.id = ctdp.phieu_dat_phong_id
                            WHERE ctdp.phong_id = :roomId
                            AND :now >= (pdp.ngay_check_in - :oneDay)
                            AND :now < pdp.ngay_check_in
                        ) THEN 1

                        ELSE 0
                    END AS status
                FROM phong p
                WHERE p.id = :roomId
                LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> findActiveBookingStatusOrdinal(
            @Param("roomId") String roomId,
            @Param("now") Long now,
            @Param("oneHour") Long oneHour,
            @Param("oneDay") Long oneDay);

    default Optional<TrangThaiPhongDat> findActiveBookingsByRoomId(String roomId, Long now) {
        Long oneHour = 3600000L;
        Long oneDay = 86400000L;

        return findActiveBookingStatusOrdinal(roomId, now, oneHour, oneDay)
                .map(ordinal -> TrangThaiPhongDat.values()[ordinal]);
    }

    @Query("SELECT c FROM ChiTietDatPhong c " +
           "LEFT JOIN FETCH c.phieuDatPhong p " +
           "LEFT JOIN FETCH p.khachHang k " +
           "WHERE c.room.id = :roomId AND p.status_phieu_dat_phong = 2")
    Optional<ChiTietDatPhong> findCheckInBookingByRoomId(@Param("roomId") String roomId);
}