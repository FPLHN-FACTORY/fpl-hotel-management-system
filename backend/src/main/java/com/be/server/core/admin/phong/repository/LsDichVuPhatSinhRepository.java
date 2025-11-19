package com.be.server.core.admin.phong.repository;

import com.be.server.repository.DichVuPhatSinhRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LsDichVuPhatSinhRepository extends DichVuPhatSinhRepository {
//    @Query("select u from DichVuPhatSinh u where u.idDatPhong.id = ?1")
//    List<DichVuPhatSinh> findByDatPhongId(String datPhongId);
}
