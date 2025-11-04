package com.be.server.core.admin.phong.repository;

import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.RoomStatus;
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

    @Query("""
            SELECT p FROM Phong p 
            LEFT JOIN FETCH p.bangGia
            WHERE 
            (:q IS NULL OR :q = '' OR p.maPhong LIKE %:q% OR p.tenPhong LIKE %:q%) AND 
            (:loaiPhong IS NULL OR p.loaiPhong = :loaiPhong) AND 
            (:trangThaiPhong IS NULL OR p.trangThaiPhong = :trangThaiPhong) AND 
            (:giaMin IS NULL OR p.giaHienTai >= :giaMin) AND 
            (:giaMax IS NULL OR p.giaHienTai <= :giaMax) AND 
            (:sucChuaMin IS NULL OR p.sucChua >= :sucChuaMin) AND 
            (:sucChuaMax IS NULL OR p.sucChua <= :sucChuaMax)
            """)
    Page<PhongProjection> searchPhong(@Param("q") String q,
                                      @Param("loaiPhong") String loaiPhong,
                                      @Param("trangThaiPhong") RoomStatus trangThaiPhong,
                                      @Param("giaMin") BigDecimal giaMin,
                                      @Param("giaMax") BigDecimal giaMax,
                                      @Param("sucChuaMin") Integer sucChuaMin,
                                      @Param("sucChuaMax") Integer sucChuaMax,
                                      Pageable pageable);

    @Query("SELECT p FROM Phong p LEFT JOIN FETCH p.bangGia")
    Page<PhongProjection> findAllProjection(Pageable pageable);

    @Query("SELECT p FROM Phong p LEFT JOIN FETCH p.bangGia WHERE p.id = :id")
    Optional<PhongProjection> findByIdProjection(@Param("id") String id);
}