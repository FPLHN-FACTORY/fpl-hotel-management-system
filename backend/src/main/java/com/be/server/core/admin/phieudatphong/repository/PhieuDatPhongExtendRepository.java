package com.be.server.core.admin.phieudatphong.repository;

import com.be.server.entity.PhieuDatPhong;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhieuDatPhongExtendRepository extends JpaRepository<PhieuDatPhong, String> {

    /**
     * Tìm phiếu đặt phòng theo filter với pagination
     */
    @Query("""
                SELECT p FROM PhieuDatPhong p
                LEFT JOIN p.khachHang k
                WHERE (:keyword IS NULL OR :keyword = '' OR
                       p.ma LIKE %:keyword% OR
                       k.hoTen LIKE %:keyword% OR
                       k.soDienThoai LIKE %:keyword% OR
                       k.email LIKE %:keyword%)
                AND (:status IS NULL OR p.status_phieu_dat_phong = :status)
                AND (:tuNgay IS NULL OR p.checkInDate >= :tuNgay)
                AND (:denNgay IS NULL OR p.checkInDate <= :denNgay)
                ORDER BY p.createdDate DESC
            """)
    Page<PhieuDatPhong> findByFilter(
            @Param("keyword") String keyword,
            @Param("status") StatusPhieuDatPhong status,
            @Param("tuNgay") Long tuNgay,
            @Param("denNgay") Long denNgay,
            Pageable pageable);

    /**
     * Tìm phiếu đặt kèm theo tất cả relationships
     * Note: Không thể JOIN FETCH nhiều collections (Bags) cùng lúc
     * bookingDetails sẽ được lazy load sau nếu cần
     */
    @Query("""
                SELECT DISTINCT p FROM PhieuDatPhong p
                LEFT JOIN FETCH p.khachHang
                WHERE p.id = :id
            """)
    Optional<PhieuDatPhong> findByIdWithDetails(@Param("id") String id);

    /**
     * Đếm số phiếu đặt theo trạng thái
     */
    @Query("SELECT COUNT(p) FROM PhieuDatPhong p WHERE p.status_phieu_dat_phong = :status")
    Long countByStatus(@Param("status") StatusPhieuDatPhong status);
}
