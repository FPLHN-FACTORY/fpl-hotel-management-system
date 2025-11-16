package com.be.server.core.admin.tag.repository;

import com.be.server.core.admin.tag.model.request.ADSearchTagRequest;
import com.be.server.core.admin.tag.model.response.TagResponse;
import com.be.server.entity.Tag;
import com.be.server.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ADTagRepository extends TagRepository {
    @Query(
            value = """
        SELECT 
            ROW_NUMBER() OVER (ORDER BY t.created_date DESC) AS rowNumber,
            t.id,
            t.ma,
            t.ten,
            t.mo_ta AS moTa,
            t.status,
            t.created_date
        FROM tag t
        WHERE 
            (:#{#req.maOrTen} IS NULL OR :#{#req.maOrTen} = '' 
                OR LOWER(t.ten) LIKE LOWER(CONCAT('%', :#{#req.maOrTen}, '%'))
                OR LOWER(t.ma) LIKE LOWER(CONCAT('%', :#{#req.maOrTen}, '%')))
        AND
            (:#{#req.status} IS NULL OR t.status = :#{#req.status})
        ORDER BY t.created_date DESC
    """,
            countQuery = """
        SELECT COUNT(*)
        FROM tag t
        WHERE 
            (:#{#req.maOrTen} IS NULL OR :#{#req.maOrTen} = '' 
                OR LOWER(t.ten) LIKE LOWER(CONCAT('%', :#{#req.maOrTen}, '%'))
                OR LOWER(t.ma) LIKE LOWER(CONCAT('%', :#{#req.maOrTen}, '%')))
        AND
            (:#{#req.status} IS NULL OR t.status = :#{#req.status})
    """,
            nativeQuery = true
    )
    Page<TagResponse> getAllTags(
            @Param("req") ADSearchTagRequest req,
            Pageable pageable
    );

    List<Tag> findAllByTen(String ten);

}
