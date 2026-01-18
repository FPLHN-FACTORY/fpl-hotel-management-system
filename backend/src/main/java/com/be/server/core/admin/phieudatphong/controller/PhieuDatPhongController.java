package com.be.server.core.admin.phieudatphong.controller;

import com.be.server.core.admin.phieudatphong.model.request.*;
import com.be.server.core.admin.phieudatphong.service.PhieuDatPhongService;
import com.be.server.utils.Helper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller quản lý phiếu đặt phòng.
 */
@RestController
@RequestMapping("/api/le-tan/phieu-dat-phong")
@RequiredArgsConstructor
public class PhieuDatPhongController {

    private final PhieuDatPhongService service;

    /**
     * Tạo phiếu đặt mới
     */
    @PostMapping
    public ResponseEntity<?> taoPhieuDat(@RequestBody @Valid TaoPhieuDatRequest request) {
        return Helper.createResponseEntity(service.taoPhieuDat(request));
    }

    /**
     * Lấy danh sách phiếu đặt với filter
     */
    @GetMapping
    public ResponseEntity<?> getDanhSach(@ModelAttribute PhieuDatPhongFilterRequest filter) {
        return Helper.createResponseEntity(service.getDanhSachPhieuDat(filter));
    }

    /**
     * Xem chi tiết phiếu đặt
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChiTiet(@PathVariable String id) {
        return Helper.createResponseEntity(service.getChiTietPhieuDat(id));
    }

    /**
     * Cập nhật thông tin phiếu đặt (chỉ khi PENDING)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> capNhat(
            @PathVariable String id,
            @RequestBody @Valid TaoPhieuDatRequest request) {
        return Helper.createResponseEntity(service.capNhatPhieuDat(id, request));
    }

    /**
     * Hủy phiếu đặt
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> huy(@PathVariable String id) {
        return Helper.createResponseEntity(service.huyPhieuDat(id));
    }

    /**
     * Gắn khách hàng vào phiếu đặt
     */
    @PostMapping("/{id}/gan-khach-hang")
    public ResponseEntity<?> ganKhachHang(@RequestBody @Valid GanKhachHangRequest request) {
        return Helper.createResponseEntity(service.ganKhachHang(request));
    }

    /**
     * Lấy danh sách phòng khả dụng theo loại phòng
     */
    @GetMapping("/{id}/phong-kha-dung")
    public ResponseEntity<?> getPhongKhaDung(
            @PathVariable String id,
            @RequestParam String idLoaiPhong) {
        return Helper.createResponseEntity(service.getPhongKhaDung(id, idLoaiPhong));
    }

    /**
     * Gắn phòng cụ thể vào phiếu đặt
     */
    @PostMapping("/{id}/gan-phong")
    public ResponseEntity<?> ganPhong(@RequestBody @Valid GanPhongRequest request) {
        return Helper.createResponseEntity(service.ganPhong(request));
    }

    /**
     * Xác nhận phiếu đặt (PENDING → CONFIRMED)
     */
    @PostMapping("/{id}/xac-nhan")
    public ResponseEntity<?> xacNhan(@PathVariable String id) {
        return Helper.createResponseEntity(service.xacNhanPhieuDat(id));
    }
}
