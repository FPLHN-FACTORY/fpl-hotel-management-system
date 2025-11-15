package com.be.server.core.admin.phong.repository;

import com.be.server.core.admin.phong.model.response.LoaiPhongResponse;
import com.be.server.entity.LoaiPhong;
import com.be.server.repository.LoaiPhongRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADLoaiPhongRepository extends LoaiPhongRepository {
//    @Query("""
//       SELECT new com.be.server.core.admin.phong.model.response.LoaiPhongResponse(
//           u.id,
//           u.soNguoiToiDa,
//           u.giaHienTai,
//           u.ten
//       )
//       FROM LoaiPhong u
//       """)
//    List<LoaiPhongResponse> getAllLoaiPhong();

}
