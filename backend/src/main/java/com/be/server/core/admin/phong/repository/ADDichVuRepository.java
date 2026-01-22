package com.be.server.core.admin.phong.repository;

import com.be.server.core.admin.phong.model.response.ADDichVuResponse;
import com.be.server.repository.DichVuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ADDichVuRepository extends DichVuRepository {

    @Query(value = """
            SELECT new com.be.server.core.admin.phong.model.response.ADDichVuResponse(
                dv.id,
                dv.maDichVu,
                dv.tenDichVu,
                dv.donViTinh,
                dv.donGia,
                dv.moTa,
                dv.trangThai,
                dv.createdDate
            )
            FROM DichVu dv
            WHERE (:q IS NULL OR LOWER(dv.tenDichVu) LIKE LOWER(CONCAT('%', :q, '%')) 
                   OR LOWER(dv.maDichVu) LIKE LOWER(CONCAT('%', :q, '%')))
            AND (:trangThai IS NULL OR dv.trangThai = :trangThai)
            ORDER BY dv.createdDate DESC
            """)
    Page<ADDichVuResponse> searchDichVu(
            @Param("q") String q,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );

    @Query(value = """
            SELECT new com.be.server.core.admin.phong.model.response.ADDichVuResponse(
                dv.id,
                dv.maDichVu,
                dv.tenDichVu,
                dv.donViTinh,
                dv.donGia,
                dv.moTa,
                dv.trangThai,
                dv.createdDate
            )
            FROM DichVu dv
            WHERE dv.trangThai = 0
            ORDER BY dv.tenDichVu ASC
            """)
    java.util.List<ADDichVuResponse> getAllActiveDichVu();
}
