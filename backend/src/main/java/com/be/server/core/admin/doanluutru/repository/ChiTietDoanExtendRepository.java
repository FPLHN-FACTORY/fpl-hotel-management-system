package com.be.server.core.admin.doanluutru.repository;

import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.modal.response.CheckSoLuongToiDaResponse;
import com.be.server.core.admin.doanluutru.modal.response.ChiTietDoanResponse;
import com.be.server.entity.ChiTietDoan;
import com.be.server.infrastructure.constant.LoaiGiayTo;

import com.be.server.repository.ChiTietDoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface ChiTietDoanExtendRepository extends ChiTietDoanRepository {
    @Query(value = "SELECT\n" +
            "    ROW_NUMBER() OVER (\n" +
            "        ORDER BY\n" +
            "            CASE\n" +
            "                WHEN ctd.vai_tro = 0 THEN 0\n" +
            "                ELSE 1\n" +
            "            END,\n" +
            "            ctd.created_date DESC\n" +
            "    ) AS orderNumber,\n" +
            "    ctd.id,\n" +
            "    kh.id as khachHangId,\n" +
            "    p.id as phongId,\n" +
            "    p.ten as tenPhong,\n" +
            "    kh.ho_ten,\n" +
            "    kh.gioi_tinh,\n" +
            "    kh.ngay_sinh,\n" +
            "    kh.loai_giay_to,\n" +
            "    kh.so_giay_to,\n" +
            "    ctd.vai_tro,\n" +
            "    ctd.trang_thai_chi_tiet_doan as trangThaiChiTietDoan \n" +
            "FROM chi_tiet_doan ctd\n" +
            "JOIN khach_hang kh\n" +
            "    ON kh.id = ctd.id_khach_hang\n" +
            "left join phong p on p.id=ctd.id_phong\n" +
            "WHERE ctd.id_doan_luu_tru = :idDoanLuuTru\n" +
            "  AND (:#{#request.hoTen} IS NULL \n" +
            "       OR kh.ho_ten LIKE CONCAT('%', :#{#request.hoTen}, '%'))\n" +
            "  AND (:#{#request.loaiGiayTo} IS NULL \n" +
            "       OR kh.loai_giay_to = :#{#request.loaiGiayTo})\n" +
            "  AND (:#{#request.soGiayTo} IS NULL \n" +
            "       OR kh.so_giay_to = :#{#request.soGiayTo})\n" +
            "ORDER BY\n" +
            "    CASE\n" +
            "        WHEN ctd.vai_tro = 0 THEN 0\n" +
            "        ELSE 1\n" +
            "    END,\n" +
            "    ctd.created_date DESC",
            countQuery = "SELECT COUNT(*)\n" +
                    "FROM chi_tiet_doan ctd\n" +
                    "JOIN khach_hang kh\n" +
                    "    ON kh.id = ctd.id_khach_hang\n" +
                    "  left  join phong p on p.id=ctd.id_phong\n" +
                    "WHERE ctd.id_doan_luu_tru = :idDoanLuuTru\n" +
                    "  AND (:#{#request.hoTen} IS NULL \n" +
                    "       OR kh.ho_ten LIKE CONCAT('%', :#{#request.hoTen}, '%'))\n" +
                    "  AND (:#{#request.loaiGiayTo} IS NULL \n" +
                    "       OR kh.loai_giay_to = :#{#request.loaiGiayTo})\n" +
                    "  AND (:#{#request.soGiayTo} IS NULL \n" +
                    "       OR kh.so_giay_to = :#{#request.soGiayTo})", nativeQuery = true)
    Page<ChiTietDoanResponse> getAllChiTietDoan( @Param("idDoanLuuTru") String idDoanLuuTru,@Param("request") SearchMemberRequest request,Pageable pageable);
    boolean  existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(LoaiGiayTo loaiGiayTo,String soGiayTo,String idDoanLuuTru);
    Optional<ChiTietDoan> findByPhong_IdAndKhachHang_IdAndDoanLuuTru_Id(
            String phongId,
            String khachHangId,
            String doanLuuTruId
    );
    @Query(value = "    SELECT\n" +
            "    SUM(t.soNguoiHienTai)  AS tongSoNguoiHienTai,\n" +
            "    SUM(t.so_nguoi_toi_da) AS tongSoNguoiToiDa\n" +
            "FROM (\n" +
            "    SELECT\n" +
            "        p.id,\n" +
            "        lp.so_nguoi_toi_da,\n" +
            "        COUNT(ctd.id) AS soNguoiHienTai  \n" +
            "    FROM phieu_dat_phong dp\n" +
            "    JOIN doan_luu_tru dtl \n" +
            "        ON dtl.id_phieu_dat_phong = dp.id\n" +
            "    LEFT JOIN chi_tiet_doan ctd \n" +
            "        ON ctd.id_doan_luu_tru = dtl.id\n" +
            "    LEFT JOIN phong p \n" +
            "        ON p.id = ctd.id_phong\n" +
            "    LEFT JOIN loai_phong lp \n" +
            "        ON lp.id = p.loai_phong_id\n" +
            "    WHERE dp.status_phieu_dat_phong = 1\n" +
            "      AND dtl.id = :idDoan\n" +
            "    GROUP BY p.id, lp.so_nguoi_toi_da\n" +
            ") t",nativeQuery = true)
    CheckSoLuongToiDaResponse checkSoLuongToiDaBooking(@Param("idDoan") String idDoan);
    boolean existsByDoanLuuTru_IdAndPhong_IdIsNull(String doanLuuTruId);




}
