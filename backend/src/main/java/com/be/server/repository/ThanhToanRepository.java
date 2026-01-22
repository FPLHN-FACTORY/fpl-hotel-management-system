package com.be.server.repository;

import com.be.server.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, String> {
    
    List<ThanhToan> findByDoanLuuTru_IdOrderByThoiGianThanhToanDesc(String idDoanLuuTru);
}
