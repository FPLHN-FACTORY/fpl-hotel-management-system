package com.be.server.core.admin.nhanvien.repository;

import com.be.server.entity.NhanVien;
import com.be.server.repository.NhanVienRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.be.server.infrastructure.constant.EntityVaiTro;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ADNhanVienRepository extends NhanVienRepository {

    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(:q IS NULL OR :q = '' OR LOWER(nv.ma) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(nv.ten) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(nv.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(nv.cccd) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "AND (:vaiTro IS NULL OR nv.vaitro = :vaiTro) " +
            "AND (:gioiTinh IS NULL OR nv.gioiTinh = :gioiTinh)")
    Page<NhanVien> searchNhanVien(@Param("q") String q,
                                  @Param("vaiTro") EntityVaiTro vaiTro,
                                  @Param("gioiTinh") Boolean gioiTinh,
                                  Pageable pageable);

    Page<NhanVien> findByMaContainingOrTenContaining(String ma, String ten, Pageable pageable);

    boolean existsByCccd(String cccd);
    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);

    boolean existsByCccdAndIdNot(String cccd, String id);
    boolean existsBySdtAndIdNot(String sdt, String id);
    boolean existsByEmailAndIdNot(String email, String id);

}
