package com.be.server.infrastructure.security.repository;

import com.be.server.entity.NhanVien;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.repository.NhanVienRepository;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienAuthRepository extends NhanVienRepository {

    Optional<NhanVien> findByEmail(@Size(max = 255) String email);

    Optional<NhanVien> findByEmailAndStatus(@Size(max = 255) String email, EntityStatus status);

}
