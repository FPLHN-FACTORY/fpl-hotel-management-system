package com.be.server.repository;

import com.be.server.entity.PhongTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongTagRepository extends JpaRepository<PhongTag, String> {
}
