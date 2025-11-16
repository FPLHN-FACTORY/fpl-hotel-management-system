package com.be.server.core.admin.datphong.trangthaiphong.controller;

import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.admin.datphong.trangthaiphong.service.SoDoPhongService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MappingConstants.API_LE_TAN_SO_DO_PHONG)
@RequiredArgsConstructor
public class SoDoPhongController {

    private final SoDoPhongService soDoPhongService;

    @GetMapping
    public ResponseObject<?> getAllSoDoPhong(@ModelAttribute SoDoSearch soDoSearch) {
        return soDoPhongService.getAllSoDoPhong(soDoSearch);
    }

    @GetMapping("/loai-phong")
    public ResponseEntity<?> getDataLoaiPhong() {
        return Helper.createResponseEntity(soDoPhongService.getDataLoaiPhong());
    }

    @PutMapping("/vesinh/{id}")
    public ResponseEntity<?> updateTrangThaiVeSinh(
            @PathVariable String id,
            @RequestParam("status") Integer status
    ) {
        return ResponseEntity.ok(soDoPhongService.updateTrangThaiVeSinh(id, status));
    }

}
