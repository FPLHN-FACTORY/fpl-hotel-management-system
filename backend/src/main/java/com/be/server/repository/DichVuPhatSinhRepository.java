package com.be.server.repository;

import com.be.server.entity.DichVuPhatSinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DichVuPhatSinhRepository extends JpaRepository<DichVuPhatSinh, String> {
}
