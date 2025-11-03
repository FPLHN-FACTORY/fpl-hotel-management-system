package com.be.server.repository;

import com.be.server.entity.Phong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhongRepository extends JpaRepository<Phong, String> {
}
