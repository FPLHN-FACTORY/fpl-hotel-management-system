package com.be.server.core.admin.phong.repository;


import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.repository.PhongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;


@Repository
public interface ADPhongRepository extends PhongRepository {

    @Query(
            value = """
        SELECT
            p.id AS id,
            p.ma AS ma,
            p.ten AS ten,
            lp.giaCaNgay AS price,
            p.tang AS tang,
            lp.ten AS loaiPhong,
            lp.soNguoiToiDa AS sucChua,
            p.trangThaiHoatDong AS trangThaiHoatDong
        FROM Phong p
        LEFT JOIN p.loaiPhong lp
        WHERE  (:tuKhoa IS NULL OR :tuKhoa = '' OR p.ma LIKE CONCAT('%', :tuKhoa, '%') OR p.ten LIKE CONCAT('%', :tuKhoa, '%'))
        AND (:loaiPhong IS NULL OR lp.id = :loaiPhong)
        AND (:trangThaiHoatDong IS NULL OR p.trangThaiHoatDong = :trangThaiHoatDong)
        AND (:giaMin IS NULL OR lp.giaCaNgay >= :giaMin)
        AND (:giaMax IS NULL OR lp.giaCaNgay <= :giaMax)
        AND (:sucChuaMin IS NULL OR lp.soNguoiToiDa >= :sucChuaMin)
        AND (:sucChuaMax IS NULL OR lp.soNguoiToiDa <= :sucChuaMax)
        AND (:tang IS NULL OR p.tang = :tang)
        """,
            countQuery = """
        SELECT COUNT(p)
        FROM Phong p
        LEFT JOIN p.loaiPhong lp
        WHERE  (:tuKhoa IS NULL OR :tuKhoa = '' OR p.ma LIKE CONCAT('%', :tuKhoa, '%') OR p.ten LIKE CONCAT('%', :tuKhoa, '%'))
        AND (:loaiPhong IS NULL OR lp.id = :loaiPhong)
        AND (:trangThaiHoatDong IS NULL OR p.trangThaiHoatDong = :trangThaiHoatDong)
        AND (:giaMin IS NULL OR lp.giaCaNgay >= :giaMin)
        AND (:giaMax IS NULL OR lp.giaCaNgay <= :giaMax)
        AND (:sucChuaMin IS NULL OR lp.soNguoiToiDa >= :sucChuaMin)
        AND (:sucChuaMax IS NULL OR lp.soNguoiToiDa <= :sucChuaMax)
        AND (:tang IS NULL OR p.tang = :tang)
        """
    )
    Page<PhongProjection> getAllPhong(
            @Param("tuKhoa") String tuKhoa,
            @Param("loaiPhong") String loaiPhong,
            @Param("trangThaiHoatDong") TrangThaiHoatDong trangThaiHoatDong,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("sucChuaMin") Integer sucChuaMin,
            @Param("sucChuaMax") Integer sucChuaMax,
            @Param("tang") Integer tang,
            Pageable pageable
    );


    Optional<Phong> findByMa(String ma);

    Optional<Phong> findByTen(String ten);
}

