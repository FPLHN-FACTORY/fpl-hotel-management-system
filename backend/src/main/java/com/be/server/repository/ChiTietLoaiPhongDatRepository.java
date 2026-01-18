package com.be.server.repository;

import com.be.server.entity.ChiTietLoaiPhongDat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietLoaiPhongDatRepository extends JpaRepository<ChiTietLoaiPhongDat, String> {

    /**
     * Tìm tất cả chi tiết loại phòng đặt theo phiếu đặt phòng
     */
    List<ChiTietLoaiPhongDat> findByPhieuDatPhongId(String phieuDatPhongId);

    /**
     * Xóa tất cả chi tiết loại phòng đặt theo phiếu đặt phòng
     */
    void deleteByPhieuDatPhongId(String phieuDatPhongId);

    /**
     * Tính tổng số lượng phòng đã đặt theo loại cho một phiếu
     */
    @Query("SELECT SUM(c.soLuong) FROM ChiTietLoaiPhongDat c WHERE c.phieuDatPhong.id = :phieuDatPhongId")
    Integer sumSoLuongByPhieuDatPhongId(@Param("phieuDatPhongId") String phieuDatPhongId);
}
