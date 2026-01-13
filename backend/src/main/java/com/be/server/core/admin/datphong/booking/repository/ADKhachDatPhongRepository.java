package com.be.server.core.admin.datphong.booking.repository;

import com.be.server.core.admin.datphong.booking.model.response.TimKhachHangResponse;
import com.be.server.repository.KhachHangRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADKhachDatPhongRepository extends KhachHangRepository {

    @Query("""
        SELECT new com.be.server.core.admin.datphong.booking.model.response.TimKhachHangResponse(
            kh.id,
            kh.maNguoiDung,
            kh.hoTen,
            kh.email,
            kh.soGiayTo,
            kh.soDienThoai,
            kh.diaChi,
            kh.quocTich
        )
        FROM KhachHang kh
        WHERE (
            kh.hoTen LIKE CONCAT('%', :keyword, '%')
            OR kh.soDienThoai LIKE CONCAT('%', :keyword, '%')
            OR kh.email LIKE CONCAT('%', :keyword, '%')
            OR kh.soGiayTo LIKE CONCAT('%', :keyword, '%')
        )
        AND kh.status = 0
        ORDER BY kh.hoTen ASC
""")
    List<TimKhachHangResponse> findByKeyword(String keyword);
}
