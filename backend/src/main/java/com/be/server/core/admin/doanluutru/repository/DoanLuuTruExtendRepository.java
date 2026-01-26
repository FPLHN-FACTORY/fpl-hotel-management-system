package com.be.server.core.admin.doanluutru.repository;

import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.response.DSPhongDaDatTheoDoanCombox;
import com.be.server.core.admin.doanluutru.modal.response.DoanLuuTruResponse;
import com.be.server.entity.DoanLuuTru;
import com.be.server.repository.DoanLuuTruRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoanLuuTruExtendRepository extends DoanLuuTruRepository {
    Optional<DoanLuuTru> findByPhieuDatPhong_Id(String idDatPhong);

    @Query(value = """
              SELECT \s
                    ROW_NUMBER() OVER (ORDER BY d.created_date DESC) AS orderNumber,
                    d.id,
                    d.ma_doan,
                    d.ten_doan,
                    kh.ho_ten,
                    kh.ngay_sinh,
                    kh.gioi_tinh,
                    kh.loai_giay_to,
                    kh.so_dien_thoai,
                    kh.so_giay_to,
                    dp.ma AS maDatPhong,
                    dp.ngay_check_in,
                    dp.ngay_check_out,
                    d.thoi_gian_check_in,
                    d.thoi_gian_check_out,
                    d.trang_thai

                FROM doan_luu_tru d
                JOIN phieu_dat_phong dp ON dp.id = d.id_phieu_dat_phong

                LEFT JOIN (
                    SELECT ctd.id_doan_luu_tru,
                           kh.*
                    FROM chi_tiet_doan ctd
                    JOIN khach_hang kh ON kh.id = ctd.id_khach_hang
                    WHERE ctd.vai_tro = 0  -- hoặc LIMIT 1 logic
                ) kh ON kh.id_doan_luu_tru = d.id

             WHERE (
                :#{#req.tuKhoa} IS NULL\s
                OR :#{#req.tuKhoa} = ''
                OR d.ma_doan LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                OR d.ten_doan LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                OR kh.ho_ten LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                        )
                        AND (
                (:#{#req.thoiGianCheckIn} IS NULL AND :#{#req.thoiGianCheckOut} IS NULL)
                OR (
                    d.thoi_gian_check_in >= :#{#req.thoiGianCheckIn}
                    AND d.thoi_gian_check_out <= :#{#req.thoiGianCheckOut}
                )
                        )
                ORDER BY d.created_date DESC
            """,
            countQuery = """
                       SELECT COUNT(*)
                                               FROM doan_luu_tru d
                                              JOIN phieu_dat_phong dp ON dp.id = d.id_phieu_dat_phong
                                           
                                               LEFT JOIN (
                                                   SELECT ctd.id_doan_luu_tru,
                                                          kh.*
                                                   FROM chi_tiet_doan ctd
                                                   JOIN khach_hang kh ON kh.id = ctd.id_khach_hang
                                                   WHERE ctd.vai_tro = 0   -- hoặc LIMIT 1 logic
                                               ) kh ON kh.id_doan_luu_tru = d.id
                                           
                                             WHERE (
                                               :#{#req.tuKhoa} IS NULL\s
                                               OR :#{#req.tuKhoa} = ''
                                               OR d.ma_doan LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                                               OR d.ten_doan LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                                               OR kh.ho_ten LIKE CONCAT('%', :#{#req.tuKhoa}, '%')
                                           )
                                           AND (
                                               (:#{#req.thoiGianCheckIn} IS NULL AND :#{#req.thoiGianCheckOut} IS NULL)
                                               OR (
                                                   d.thoi_gian_check_in >= :#{#req.thoiGianCheckIn}
                                                   AND d.thoi_gian_check_out <= :#{#req.thoiGianCheckOut}
                                               )
                                           )
                                           
                                           
                                               ORDER BY d.created_date DESC
                    """,
            nativeQuery = true)
    Page<DoanLuuTruResponse> findByFilter(
            @Param("req") FindDoanRequest req,
            Pageable pageable
    );
    @Query(value = """
               SELECT
                p.id,
                p.ten,
                p.tang,
                lp.so_giuong_doi,
                lp.so_giuong_don,
                lp.so_nguoi_quy_dinh,
                lp.so_nguoi_toi_da,
                COUNT(ctd.id) AS soNguoiHienTai
            FROM phong p
            JOIN loai_phong lp
                ON lp.id = p.loai_phong_id
            JOIN chi_tiet_dat_phong ctdp
                ON ctdp.phong_id = p.id
            JOIN phieu_dat_phong dp
                ON dp.id = ctdp.phieu_dat_phong_id
                AND dp.status_phieu_dat_phong = 1
            JOIN doan_luu_tru dtl
                ON dtl.id_phieu_dat_phong = dp.id
            LEFT JOIN chi_tiet_doan ctd
                ON ctd.id_phong = p.id
                AND ctd.id_doan_luu_tru = dtl.id
            WHERE dtl.id = :idDoan
            GROUP BY
                p.id,
                p.ten,
                p.tang,
                lp.so_giuong_doi,
                lp.so_giuong_don,
                lp.so_nguoi_quy_dinh,
                lp.so_nguoi_toi_da
            ORDER BY p.tang, p.ten
                        """, nativeQuery = true)
    List<DSPhongDaDatTheoDoanCombox> getDataComboboxDatPhongTheoDoan(@Param("idDoan") String idDoan);


    boolean existsByPhieuDatPhong_Id(String Id);
}
