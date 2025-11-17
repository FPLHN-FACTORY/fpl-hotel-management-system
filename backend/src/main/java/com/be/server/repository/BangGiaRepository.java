package com.be.server.repository;

import com.be.server.entity.BangGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BangGiaRepository extends JpaRepository<BangGia, String> {
}
