package com.be.server.core.admin.phong.repository;

import com.be.server.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ADTagRoomRepository extends JpaRepository<Tag, String> {

    @Query("SELECT t FROM Tag t WHERE t.status = 0 ORDER BY t.ma")
    List<Tag> getAllActiveTags();
}