package com.be.server.repository;

import com.be.server.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, String> {
    java.util.Optional<KhachHang> findBySoCCD(String soCCD);
}
