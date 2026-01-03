package com.be.server.repository;

import com.be.server.entity.PhieuDatPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhieuDatPhongRepository extends JpaRepository<PhieuDatPhong, String> {
}
