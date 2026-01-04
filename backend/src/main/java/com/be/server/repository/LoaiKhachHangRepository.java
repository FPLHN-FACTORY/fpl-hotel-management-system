package com.be.server.repository;

import com.be.server.entity.LoaiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiKhachHangRepository extends JpaRepository<LoaiKhachHang, String> {
}
