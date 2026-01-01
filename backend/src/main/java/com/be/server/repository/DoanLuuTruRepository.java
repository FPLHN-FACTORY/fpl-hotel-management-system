package com.be.server.repository;

import com.be.server.entity.DoanLuuTru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoanLuuTruRepository extends JpaRepository<DoanLuuTru, String> {
    Optional<DoanLuuTru> findByDatPhong_Id(String idDatPhong);

    @org.springframework.data.jpa.repository.Query("""
                SELECT d FROM DoanLuuTru d
                LEFT JOIN d.truongDoan td
                WHERE (:#{#req.tuKhoa} IS NULL OR :#{#req.tuKhoa} = '' OR
                       d.maDoan LIKE %:#{#req.tuKhoa}% OR
                       d.tenDoan LIKE %:#{#req.tuKhoa}% OR
                       td.hoTen LIKE %:#{#req.tuKhoa}%)
                ORDER BY d.createdDate DESC
            """)
    java.util.List<DoanLuuTru> findByFilter(
            @org.springframework.data.repository.query.Param("req") com.be.server.core.admin.doanluutru.request.FindDoanRequest req);
}
