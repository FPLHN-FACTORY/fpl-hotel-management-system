package com.be.server.repository;

import com.be.server.entity.DichVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, String> {
    Optional<DichVu> findByMaDichVu(String maDichVu);
    boolean existsByMaDichVu(String maDichVu);
}
