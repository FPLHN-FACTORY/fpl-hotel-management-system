package com.be.server.repository;

import com.be.server.entity.DatPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatPhongRepository extends JpaRepository<DatPhong, String> {

}
