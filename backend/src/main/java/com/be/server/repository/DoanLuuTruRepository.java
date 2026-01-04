package com.be.server.repository;

import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.entity.DoanLuuTru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoanLuuTruRepository extends JpaRepository<DoanLuuTru, String> {

}
