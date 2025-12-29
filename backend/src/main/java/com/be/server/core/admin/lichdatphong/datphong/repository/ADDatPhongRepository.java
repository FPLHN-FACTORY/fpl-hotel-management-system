package com.be.server.core.admin.lichdatphong.datphong.repository;

import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.repository.PhongRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ADDatPhongRepository extends PhongRepository {

    @Query("""
        SELECT p From Phong p
        JOIN p.loaiPhong lp
        WHERE p.trangThaiHoatDong = :trangThai
        AND lp.id = :idLoaiPhong
        AND NOT EXISTS (
            SELECT 1
            FROM ChiTietDatPhong ctdp
            WHERE ctdp.room = p
            AND ctdp.status_chi_tiet IN ('BOOKED', 'CHECKIN')
            AND (
                (:ngayNhan BETWEEN ctdp.phieuDatPhong.checkInDate AND ctdp.phieuDatPhong.checkOutDate)
                OR (:ngayTra BETWEEN ctdp.phieuDatPhong.checkInDate AND ctdp.phieuDatPhong.checkOutDate)
                OR (ctdp.phieuDatPhong.checkInDate BETWEEN :ngayNhan AND :ngayTra)
            )
        )
        ORDER BY p.ma ASC
""")
    List<Phong> findPhongTrongByLoaiPhong(
            @Param("idLoaiPhong") String idLoaiPhong,
            @Param("ngayNhan") Long ngayNhan,
            @Param("ngayTra") Long ngayTra,
            @Param("trangThai")TrangThaiHoatDong trangThai
            );


    @Query("""
        SELECT COUNT(p) FROM Phong p
        WHERE p.trangThaiHoatDong = :trangThai
        AND p.loaiPhong.id = :idLoaiPhong
        AND NOT EXISTS (
            SELECT 1
            FROM ChiTietDatPhong ctdp
            WHERE ctdp.room = p
            AND ctdp.status_chi_tiet IN ('BOOKED', 'CHECKIN')
            AND (
                (:ngayNhan BETWEEN ctdp.phieuDatPhong.checkInDate AND ctdp.phieuDatPhong.checkOutDate)
                OR (:ngayTra BETWEEN ctdp.phieuDatPhong.checkInDate AND ctdp.phieuDatPhong.checkOutDate)
                OR (ctdp.phieuDatPhong.checkInDate BETWEEN :ngayNhan AND :ngayTra)
                
            )
        )
""")
    Long countPhongTrongByLoaiPhong(
            @Param("idLoaiPhong") String idLoaiPhong,
            @Param("ngayNhan") Long ngayNhan,
            @Param("ngayTra") Long ngayTra,
            @Param("trangThai") TrangThaiHoatDong trangThai
    );
}
