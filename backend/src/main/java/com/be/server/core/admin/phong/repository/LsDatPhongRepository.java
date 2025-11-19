package com.be.server.core.admin.phong.repository;

import com.be.server.repository.DatPhongRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LsDatPhongRepository extends DatPhongRepository {
//
//    @Query("""
//                SELECT new com.be.server.core.admin.phong.model.response.LeTanResponse(
//                    u.id,
//                    kh.hoTen,
//                    u.maPhong,
//                    u.tenPhong,
//                    u.thoiGianDat,
//                    u.giaHienTai,
//                    u.trangThaiPhong,
//                    u.thoiGianCheckIn,
//                    u.thoiGianCheckOut
//                )
//                FROM DatPhong u
//                LEFT JOIN LichSuDatPhong ls ON u.id = ls.datPhong.id
//                LEFT JOIN LeTan lt ON ls.leTan.id = lt.id
//                LEFT JOIN KhachHang kh ON u.khachHang.id = kh.id
//                WHERE u.trangThaiPhong = 1
//                  AND (:tenKhachHang IS NULL OR kh.id = :tenKhachHang)
//                  AND (:tuNgay IS NULL OR u.thoiGianDat >= :tuNgay)
//                  AND (:denNgay IS NULL OR u.thoiGianDat <= :denNgay)
//                GROUP BY u.id, kh.hoTen, u.maPhong, u.tenPhong, u.thoiGianDat, u.giaHienTai, u.trangThaiPhong
//            """)
//    Page<LeTanResponse> roomUsageHistory(
//            @Param("tenKhachHang") String tenKhachHang,
//            @Param("tuNgay") LocalDateTime tuNgay,
//            @Param("denNgay") LocalDateTime denNgay,
//            Pageable pageable);
}