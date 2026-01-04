package com.be.server.core.admin.doanluutru.repository;

import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
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
    Optional<DoanLuuTru> findByDatPhong_Id(String idDatPhong);

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
                    d.ghi_chu,
                    d.thoi_gian_check_in,
                    d.thoi_gian_check_out,
                    d.trang_thai

                FROM doan_luu_tru d
                JOIN dat_phong dp ON dp.id = d.id_dat_phong

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
                                                JOIN dat_phong dp ON dp.id = d.id_dat_phong
                                           
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
    boolean existsByDatPhong_Id(String Id);
}
