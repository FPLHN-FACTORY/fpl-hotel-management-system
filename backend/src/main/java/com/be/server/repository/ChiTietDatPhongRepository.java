package com.be.server.repository;

import com.be.server.entity.ChiTietDatPhong;
import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChiTietDatPhongRepository extends JpaRepository<ChiTietDatPhong, Long> {

    @Query("SELECT c FROM ChiTietDatPhong c WHERE c.room.id = :roomCode AND c.phieuDatPhong.id = :bookingId")
    Optional<TrangThaiPhongDat> findActiveBookingsByRoomId(@Param("roomCode") String roomCode, @Param("bookingId") Long bookingId);
}
