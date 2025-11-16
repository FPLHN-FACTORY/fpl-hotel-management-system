package com.be.server.core.admin.datphong.trangthaiphong.repository;

import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.admin.datphong.trangthaiphong.model.response.SoDoPhongResponse;
import com.be.server.repository.PhongRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface SoDoPhongRepository extends PhongRepository {

    @Query(value = """
        SELECT 
            p.id AS id,
            p.ma AS ma,
            p.ten AS ten,
            lp.ten AS loaiPhong,
            p.tang AS tang,
            p.trang_thai_hoat_dong AS trangThaiHoatDong,
            lp.so_nguoi_toi_da AS sucChua,
            p.trang_thai_ve_sinh AS trangThaiVeSinh,
            COALESCE(
                              (
                                  SELECT bg.gia_ap_dung
                                  FROM bang_gia bg
                                  WHERE bg.phong_id = p.id
                                  AND :now BETWEEN bg.ngay_bat_dau AND bg.ngay_ket_thuc
                                  ORDER BY bg.ngay_bat_dau DESC
                                  LIMIT 1
                              ),
                              lp.gia_ca_ngay -- nếu không có bảng giá, lấy giá mặc định
                          ) AS price
            
        FROM phong p
        LEFT JOIN loai_phong lp ON lp.id = p.loai_phong_id
        WHERE (:ma IS NULL OR p.ma LIKE CONCAT('%', :ma, '%'))
          AND (:ten IS NULL OR p.ten LIKE CONCAT('%', :ten, '%'))
          AND (:loaiPhong IS NULL OR lp.ten = :loaiPhong)
          AND (:tang IS NULL OR p.tang = :tang)
        """,
            nativeQuery = true)
    List<SoDoPhongResponse> getRoomOverview(
            @Param("ma") String ma,
            @Param("ten") String ten,
            @Param("loaiPhong") String loaiPhong,
            @Param("tang") Integer tang,
            @Param("now") Long now,
            SoDoSearch request
    );


}
