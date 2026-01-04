package com.be.server.core.admin.doanluutru.repository;

import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.modal.response.ChiTietDoanResponse;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.repository.ChiTietDoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChiTietDoanExtendRepository extends ChiTietDoanRepository {
    @Query(value = "SELECT \n" +
            "    ROW_NUMBER() OVER (\n" +
            "        ORDER BY \n" +
            "            CASE \n" +
            "                WHEN ctd.vai_tro = 0 THEN 0 \n" +
            "                ELSE 1 \n" +
            "            END,\n" +
            "            ctd.created_date DESC\n" +
            "    ) AS orderNumber,\n" +
            "    ctd.id,\n" +
            "    kh.ho_ten,\n" +
            "    kh.gioi_tinh,\n" +
            "    kh.ngay_sinh,\n" +
            "    kh.loai_giay_to,\n" +
            "    kh.so_giay_to,\n" +
            "    ctd.vai_tro\n" +
            "FROM chi_tiet_doan ctd\n" +
            "JOIN khach_hang kh \n" +
            "    ON kh.id = ctd.id_khach_hang\n" +
            "where ctd.id_doan_luu_tru=:idDoanLuuTru \n" +
            "and (:#{#request.hoTen} is null or kh.ho_ten LIKE CONCAT('%', :#{#request.hoTen}, '%'))\n" +
            "and (:#{#request.loaiGiayTo} is null or kh.loai_giay_to = :#{#request.loaiGiayTo})\n" +
            "and (:#{#request.soGiayTo} is null or kh.so_giay_to = :#{#request.soGiayTo})\n" +
            "ORDER BY \n" +
            "    CASE \n" +
            "        WHEN ctd.vai_tro = 0 THEN 0 \n" +
            "        ELSE 1 \n" +
            "    END,\n" +
            "    ctd.created_date DESC",
            countQuery = "SELECT \n" +
                    "count(*)\n" +
                    "FROM chi_tiet_doan ctd\n" +
                    "JOIN khach_hang kh ON kh.id = ctd.id_khach_hang\n" +
                    "where ctd.id_doan_luu_tru=:idDoanLuuTru\n" +
                    "and (:#{#request.hoTen} is null or kh.ho_ten LIKE CONCAT('%', :#{#request.hoTen}, '%'))\n" +
                    "and (:#{#request.loaiGiayTo} is null or kh.loai_giay_to = :#{#request.loaiGiayTo})\n" +
                    "and (:#{#request.soGiayTo} is null or kh.so_giay_to = :#{#request.soGiayTo})", nativeQuery = true)
    Page<ChiTietDoanResponse> getAllChiTietDoan( @Param("idDoanLuuTru") String idDoanLuuTru,@Param("request") SearchMemberRequest request,Pageable pageable);
    boolean  existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(LoaiGiayTo loaiGiayTo,String soGiayTo,String idDoanLuuTru);

}
