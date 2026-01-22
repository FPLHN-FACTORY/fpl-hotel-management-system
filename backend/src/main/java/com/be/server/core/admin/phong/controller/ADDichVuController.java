package com.be.server.core.admin.phong.controller;

import com.be.server.core.admin.phong.model.request.ADDichVuRequest;
import com.be.server.core.admin.phong.model.request.ADDichVuSearchRequest;
import com.be.server.core.admin.phong.service.ADDichVuService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_DICH_VU)
public class ADDichVuController {

    private final ADDichVuService dichVuService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ADDichVuRequest request) {
        return Helper.createResponseEntity(dichVuService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody ADDichVuRequest request) {
        return Helper.createResponseEntity(dichVuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return Helper.createResponseEntity(dichVuService.delete(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return Helper.createResponseEntity(dichVuService.getById(id));
    }

    @GetMapping
    public ResponseEntity<?> search(ADDichVuSearchRequest request) {
        return Helper.createResponseEntity(dichVuService.search(request));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActive() {
        return Helper.createResponseEntity(dichVuService.getAllActive());
    }
}
