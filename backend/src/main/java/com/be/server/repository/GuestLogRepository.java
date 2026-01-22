package com.be.server.repository;

import com.be.server.entity.GuestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestLogRepository extends JpaRepository<GuestLog, String> {
    
    List<GuestLog> findByDoanLuuTru_IdOrderByThoiGianThemDesc(String idDoanLuuTru);
    
    List<GuestLog> findByKhachHang_Id(String idKhachHang);
}
