package com.be.server.core.admin.phong.repository;


import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.infrastructure.constant.TrangThaiPhong;
import com.be.server.repository.PhongRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;


@Repository
public interface ADPhongRepository extends PhongRepository {

    @Query("""
            SELECT
                p.id AS id,
                p.ma AS ma,
                p.ten AS ten,
                p.giaHienTai AS price,
                p.tang AS tang,
                lp.ten AS loaiPhong,
                lp.soNguoiToiDa AS sucChua,
                p.trangThaiPhong AS trangThaiPhong
            FROM Phong p
            LEFT JOIN p.loaiPhong lp
            WHERE p.status = 0
            AND (:tuKhoa IS NULL OR :tuKhoa = '' OR p.ma LIKE %:tuKhoa% OR p.ten LIKE %:tuKhoa%)
            AND (:loaiPhong IS NULL OR lp.ten = :loaiPhong)
            AND (:trangThaiPhong IS NULL OR p.trangThaiPhong = :trangThaiPhong)
            AND (:giaMin IS NULL OR p.giaHienTai >= :giaMin)
            AND (:giaMax IS NULL OR p.giaHienTai <= :giaMax)
            AND (:sucChuaMin IS NULL OR lp.soNguoiToiDa >= :sucChuaMin)
            AND (:sucChuaMax IS NULL OR lp.soNguoiToiDa <= :sucChuaMax)
            AND (:tang IS NULL OR p.tang = :tang)
            """)
    Page<PhongProjection> getAllPhong(
            @Param("tuKhoa") String tuKhoa,
            @Param("loaiPhong") String loaiPhong,
            @Param("trangThaiPhong") TrangThaiPhong trangThaiPhong,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("sucChuaMin") Integer sucChuaMin,
            @Param("sucChuaMax") Integer sucChuaMax,
            @Param("tang") Integer tang,
            Pageable pageable
    );
}

