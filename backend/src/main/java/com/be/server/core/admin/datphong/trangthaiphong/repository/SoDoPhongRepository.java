package com.be.server.core.admin.datphong.trangthaiphong.repository;

import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.admin.datphong.trangthaiphong.model.response.DSPhongDaDatCombox;
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
        LEFT JOIN dat_phong dp on p.id = dp.id_phong 
        WHERE((:#{#request.q} IS NULL OR p.ma LIKE CONCAT('%', :#{#request.q}, '%'))
            OR (:#{#request.q} IS NULL OR p.ten LIKE CONCAT('%', :#{#request.q}, '%')))
            AND (:idLoaiPhong IS NULL OR lp.id = :idLoaiPhong)
            AND (:#{#request.maxPrice} IS NULL OR :#{#request.minPrice} IS NULL OR (lp.gia_ca_ngay >= :#{#request.minPrice} AND lp.gia_ca_ngay <= :#{#request.maxPrice}))
            AND (:idsRoomUnavailable IS NULL OR p.id NOT IN :idsRoomUnavailable)
        """,
            nativeQuery = true)
    List<SoDoPhongResponse> getRoomOverview(
            @Param("idLoaiPhong") String idLoaiPhong,
            @Param("now") Long now,
            SoDoSearch request,
            List<String> idsRoomUnavailable
    );

    @Query("""
    SELECT p.id
    FROM Phong p
    LEFT JOIN DatPhong dp on dp.phong.id = p.id
    WHERE :ngayDen IS NULL OR :ngayDi IS NULL OR (dp.thoiGianCheckIn < :ngayDi AND dp.thoiGianCheckOut > :ngayDen)
    """)
    List<String> findRoomsByNgayDenAndNgayDi(Long ngayDen,Long ngayDi);




}
