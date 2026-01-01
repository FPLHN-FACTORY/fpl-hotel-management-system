package com.be.server.repository;

import com.be.server.entity.ChiTietDoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietDoanRepository extends JpaRepository<ChiTietDoan, String> {
    List<ChiTietDoan> findByDoanLuuTru_Id(String idDoanLuuTru);
    // Determine if guest is already in a group?
    // Ideally we need to check if a guest is in an active group, but that depends
    // on Booking status.
    // For now simple CRUD.
}
