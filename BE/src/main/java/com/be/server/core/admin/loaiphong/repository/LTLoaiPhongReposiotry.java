package com.be.server.core.admin.loaiphong.repository;

import com.be.server.entity.LoaiPhong;
import com.be.server.repository.LoaiPhongRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LTLoaiPhongReposiotry extends LoaiPhongRepository {
    Optional<LoaiPhong> findByMa(String ma);

    Optional<LoaiPhong> findByTen(String ten);
}
