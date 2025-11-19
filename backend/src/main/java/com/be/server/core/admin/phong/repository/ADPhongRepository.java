package com.be.server.core.admin.phong.repository;


import com.be.server.core.admin.phong.model.response.ADPhongDetail;
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
import java.util.List;
import java.util.Optional;


@Repository
public interface ADPhongRepository extends PhongRepository {

    @Query(
            value = """
        SELECT DISTINCT
            p.id AS id,
            p.ma AS ma,
            p.ten AS ten,
            lp.giaCaNgay AS price,
            p.tang AS tang,
            lp.ten AS loaiPhong,
            lp.soNguoiToiDa AS sucChua,
            lp.soNguoiQuyDinh AS soNguoiQuyDinh,
            p.trangThaiHoatDong AS trangThaiHoatDong
        FROM Phong p
        LEFT JOIN p.loaiPhong lp
        LEFT JOIN p.tags pt
        LEFT JOIN pt.tag t
        WHERE  (:tuKhoa IS NULL OR :tuKhoa = '' OR p.ma LIKE CONCAT('%', :tuKhoa, '%') OR p.ten LIKE CONCAT('%', :tuKhoa, '%'))
        AND (:loaiPhong IS NULL OR lp.id = :loaiPhong)
        AND (:trangThaiHoatDong IS NULL OR p.trangThaiHoatDong = :trangThaiHoatDong)
        AND (:giaMin IS NULL OR lp.giaCaNgay >= :giaMin)
        AND (:giaMax IS NULL OR lp.giaCaNgay <= :giaMax)
        AND (:sucChuaMin IS NULL OR lp.soNguoiToiDa >= :sucChuaMin)
        AND (:sucChuaMax IS NULL OR lp.soNguoiToiDa <= :sucChuaMax)
        AND (:tang IS NULL OR p.tang = :tang)
        AND (:tagIds IS NULL OR t.id IN :tagIds)
        """,
            countQuery = """
        SELECT COUNT(DISTINCT p)
        FROM Phong p
        LEFT JOIN p.loaiPhong lp
        LEFT JOIN p.tags pt
        LEFT JOIN pt.tag t
        WHERE  (:tuKhoa IS NULL OR :tuKhoa = '' OR p.ma LIKE CONCAT('%', :tuKhoa, '%') OR p.ten LIKE CONCAT('%', :tuKhoa, '%'))
        AND (:loaiPhong IS NULL OR lp.id = :loaiPhong)
        AND (:trangThaiHoatDong IS NULL OR p.trangThaiHoatDong = :trangThaiHoatDong)
        AND (:giaMin IS NULL OR lp.giaCaNgay >= :giaMin)
        AND (:giaMax IS NULL OR lp.giaCaNgay <= :giaMax)
        AND (:sucChuaMin IS NULL OR lp.soNguoiToiDa >= :sucChuaMin)
        AND (:sucChuaMax IS NULL OR lp.soNguoiToiDa <= :sucChuaMax)
        AND (:tang IS NULL OR p.tang = :tang)
        AND (:tagIds IS NULL OR t.id IN :tagIds)
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
            @Param("tagIds") List<String> tagIds,
            Pageable pageable
    );

    @Query("""
        SELECT
            p.id AS id,
            p.ma AS ma,
            p.ten AS ten,
            p.tang AS tang,
            lp.id AS idLoaiPhong,
            lp.ten AS tenLoaiPhong,
            lp.soNguoiToiDa AS soNguoiToiDa,
            lp.soNguoiQuyDinh AS soNguoiQuyDinh,
            lp.soGiuongDon AS soGiuongDon,
            lp.soGiuongDoi AS soGiuongDoi,
            lp.giaCaNgay AS giaCaNgay,
            p.trangThaiHoatDong AS trangThaiHoatDong
        FROM Phong p
        LEFT JOIN p.loaiPhong lp
        WHERE p.id = :id
    """)
    Optional<ADPhongDetail> getPhongDetailById(@Param("id") String id);

    @Query("""
        SELECT t.id AS id, t.ten AS ten
        FROM PhongTag pt
        JOIN pt.tag t
        WHERE pt.phong.id = :phongId AND t.status = 0
        ORDER BY t.ten
    """)
    List<Object[]> getTagsByPhongId(@Param("phongId") String phongId);

    @Query("""
        SELECT p FROM Phong p 
        LEFT JOIN FETCH p.tags pt 
        LEFT JOIN FETCH pt.tag
        LEFT JOIN FETCH p.loaiPhong
        WHERE p.id IN :ids
    """)
    List<Phong> findAllWithTagsByIds(@Param("ids") List<String> ids);

    Optional<Phong> findByMa(String ma);

    Optional<Phong> findByTen(String ten);


}

