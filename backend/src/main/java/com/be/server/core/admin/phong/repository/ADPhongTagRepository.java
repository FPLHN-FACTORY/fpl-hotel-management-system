package com.be.server.core.admin.phong.repository;

import com.be.server.repository.PhongTagRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ADPhongTagRepository extends PhongTagRepository {

    @Modifying
    @Query("DELETE FROM PhongTag pt WHERE pt.phong.id = :phongId")
    void deleteByPhongId(@Param("phongId") String phongId);
}