package com.be.server.core.admin.loaiphong.controller;

import com.be.server.core.admin.loaiphong.model.request.ADSaveLoaiPhongRequest;
import com.be.server.core.admin.loaiphong.service.ADLoaiPhongService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.infrastructure.constant.MappingConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(MappingConstants.API_LE_TAN_LOAI_PHONG)
@RequiredArgsConstructor
public class ADLoaiPhongController {

    private final ADLoaiPhongService service;

    @GetMapping
    public ResponseObject<?> getAll() {
        return service.getAllLoaiPhong();
    }

    @PostMapping
    public ResponseObject<?> add(@RequestBody ADSaveLoaiPhongRequest request) {
        return service.saveLoaiPhong(request);
    }

    @PutMapping("/{id}")
    public ResponseObject<?> update(@PathVariable String id, @RequestBody ADSaveLoaiPhongRequest request) {
        return service.updateLoaiPhong(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseObject<?> delete(@PathVariable String id) {
        return service.deleteLoaiPhong(id);
    }
}
