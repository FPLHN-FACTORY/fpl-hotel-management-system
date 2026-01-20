package com.be.server.repository;

import com.be.server.entity.LoaiPhong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoaiPhongRepository extends JpaRepository<LoaiPhong, String> {
}
