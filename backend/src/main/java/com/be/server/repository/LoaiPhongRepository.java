package com.be.server.repository;

import com.be.server.entity.LoaiPhong;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoaiPhongRepository extends JpaRepository<LoaiPhong, String> {
}
