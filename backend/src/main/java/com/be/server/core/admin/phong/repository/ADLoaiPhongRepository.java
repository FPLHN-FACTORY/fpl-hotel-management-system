package com.be.server.core.admin.phong.repository;

import com.be.server.core.admin.phong.model.response.LoaiPhongResponse;
import com.be.server.entity.LoaiPhong;
import com.be.server.repository.LoaiPhongRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADLoaiPhongRepository extends LoaiPhongRepository {

    @Query("""
       SELECT new com.be.server.core.admin.phong.model.response.LoaiPhongResponse(
           lp.id,
           lp.soNguoiToiDa,
           lp.soNguoiQuyDinh,
           lp.soGiuongDon,
           lp.soGiuongDoi,
           lp.giaCaNgay,
           lp.ten
       )
       FROM LoaiPhong lp
       WHERE lp.status = 0
       ORDER BY lp.ten
       """)
    List<LoaiPhongResponse> getAllLoaiPhong();

    @Query("SELECT lp FROM LoaiPhong lp WHERE lp.status = 0 ORDER BY lp.ten")
    List<LoaiPhong> getAllLoaiPhongEntities();
}