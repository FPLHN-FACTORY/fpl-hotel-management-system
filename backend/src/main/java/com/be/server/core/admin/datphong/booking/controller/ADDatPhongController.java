package com.be.server.core.admin.datphong.booking.controller;

import com.be.server.core.admin.datphong.booking.model.request.CheckPhongTrongRequest;
import com.be.server.core.admin.datphong.booking.model.request.CreateDatPhongRequest;
import com.be.server.core.admin.datphong.booking.model.request.DatPhongTheoLoaiRequest;
import com.be.server.core.admin.datphong.booking.service.ADDatPhongService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_DAT_PHONG)
public class ADDatPhongController {

    private final ADDatPhongService adDatPhongService;

    @PostMapping("/check-phong-trong")
    public ResponseEntity<?> checkPhongTrong(@RequestBody CheckPhongTrongRequest request) {
        return Helper.createResponseEntity(adDatPhongService.checkPhongTrong(request));
    }

    @PostMapping("/phong-theo-loai")
    public ResponseEntity<?> getPhongTheoLoai(@RequestBody DatPhongTheoLoaiRequest request) {
        return Helper.createResponseEntity(adDatPhongService.getPhongByLoaiPhong(request));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createDatPhong(@RequestBody CreateDatPhongRequest request) {
        return Helper.createResponseEntity(adDatPhongService.createDatPhong(request));
    }

    @GetMapping("/khach-hang/search")
    public ResponseEntity<?> searchKhachHang(@RequestParam(required = false) String keyword) {
        return Helper.createResponseEntity(adDatPhongService.searchKhachHang(keyword));
    }

}
