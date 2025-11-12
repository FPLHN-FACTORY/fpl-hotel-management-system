package com.be.server.core.admin.phong.repository;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.EntityStatus;
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

    @Query(value = """
    SELECT
        p.id AS id,
        p.ma AS ma,
        p.ten AS ten,
        p.giaHienTai AS price,
        lp.moTa AS loaiPhong,
        lp.soNguoiToiDa AS sucChua,
        p.trangThaiPhong AS trangThai
    FROM Phong p
    LEFT JOIN p.loaiPhong lp
    LEFT JOIN p.priceHistory bg
    WHERE
        (:#{#request.tuKhoa} IS NULL 
            OR p.ten LIKE CONCAT('%', :#{#request.tuKhoa}, '%') 
            OR p.ma LIKE CONCAT('%', :#{#request.tuKhoa}, '%'))
      AND (:#{#request.loaiPhongId} IS NULL OR lp.id = :#{#request.loaiPhongId})
      AND (:#{#request.trangThaiPhong} IS NULL OR p.trangThaiPhong = :#{#request.trangThaiPhong})
      AND (:#{#request.giaMin} IS NULL OR p.giaHienTai >= :#{#request.giaMin})
      AND (:#{#request.giaMax} IS NULL OR p.giaHienTai <= :#{#request.giaMax})
      AND (:#{#request.tang} IS NULL OR p.tang = :#{#request.tang})
      AND p.status = 0 
    ORDER BY p.createdDate DESC
""",
            countQuery = """
    SELECT COUNT(p.id)
    FROM Phong p
    LEFT JOIN p.loaiPhong lp
    LEFT JOIN p.priceHistory bg
    WHERE
        (:#{#request.tuKhoa} IS NULL 
            OR p.ten LIKE CONCAT('%', :#{#request.tuKhoa}, '%') 
            OR p.ma LIKE CONCAT('%', :#{#request.tuKhoa}, '%'))
      AND (:#{#request.loaiPhongId} IS NULL OR lp.id = :#{#request.loaiPhongId})
      AND (:#{#request.trangThaiPhong} IS NULL OR p.trangThaiPhong = :#{#request.trangThaiPhong})
      AND (:#{#request.giaMin} IS NULL OR p.giaHienTai >= :#{#request.giaMin})
      AND (:#{#request.giaMax} IS NULL OR p.giaHienTai <= :#{#request.giaMax})
      AND (:#{#request.tang} IS NULL OR p.tang = :#{#request.tang})
      AND p.status = 0 
""")

    Page<PhongProjection> getAllPhong(Pageable pageable, @Param("request") ADPhongSearchRequest request);




//
//    @Query("SELECT p FROM Phong p LEFT JOIN FETCH p.bangGia WHERE p.status = 0")
//    Page<PhongProjection> findAllProjection(Pageable pageable);
//
//    @Query("SELECT p FROM Phong p LEFT JOIN FETCH p.bangGia WHERE p.id = :id AND p.status = 0")
//    Optional<PhongProjection> findByIdProjection(@Param("id") String id);
}