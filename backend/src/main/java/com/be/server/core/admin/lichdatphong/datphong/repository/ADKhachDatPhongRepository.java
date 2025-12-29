package com.be.server.core.admin.lichdatphong.datphong.repository;

import com.be.server.core.admin.lichdatphong.datphong.model.response.TimKhachHangResponse;
import com.be.server.repository.KhachHangRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADKhachDatPhongRepository extends KhachHangRepository {

    @Query("""
        SELECT new com.be.server.core.admin.lichdatphong.datphong.model.response.TimKhachHangResponse(
            kh.id, kh.maNguoiDung, kh.hoTen, kh.email, kh.soCCD,
            kh.soDienThoai, kh.diaChi, kh.quocTich
        )
        FROM KhachHang kh
        WHERE (:keyword IS NULL OR :keyword = ''
                OR kh.hoTen LIKE CONCAT('%', :keyword,'%')
                OR kh.soDienThoai LIKE CONCAT('%', :keyword,'%')
                OR kh.email LIKE CONCAT('%', :keyword,'%')
                OR kh.soCCD LIKE CONCAT('%', :keyword,'%')
        )
        AND kh.status = 0
        ORDER BY kh.hoTen ASC
""")
    List<TimKhachHangResponse> findByKeyword(String keyword);
}
