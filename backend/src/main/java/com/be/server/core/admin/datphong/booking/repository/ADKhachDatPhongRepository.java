package com.be.server.core.admin.datphong.booking.repository;

import com.be.server.entity.KhachHang;
import com.be.server.repository.KhachHangRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADKhachDatPhongRepository extends KhachHangRepository {

    @Query("""
                    SELECT kh
                    FROM KhachHang kh
                    WHERE (
                        LOWER(kh.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
                        OR LOWER(kh.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR kh.soGiayTo LIKE CONCAT('%', :keyword, '%')
                    )
                    ORDER BY kh.hoTen ASC
            """)
    List<KhachHang> findByKeyword(@Param("keyword") String keyword);
}
