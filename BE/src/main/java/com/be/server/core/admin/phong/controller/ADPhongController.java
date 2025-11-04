package com.be.server.core.admin.phong.controller;


import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.LsDatPhongRequest;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_ADMIN_ROOM)
@Slf4j
@CrossOrigin(origins = "*")
public class ADPhongController {

    private final ADPhongService service;

    @GetMapping("/history")
    public ResponseEntity<?> getAllRoomUsageHistory(LsDatPhongRequest request){
        return Helper.createResponseEntity(service.roomUsageHistory(request));
    }

    @GetMapping
    public ResponseEntity<?> getAll(ADPhongSearchRequest request) {
        log.info("Lấy danh sách phòng với filters: {}", request);
        return Helper.createResponseEntity(service.getAllPhong(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPhongById(@PathVariable String id) {
        log.info("Lấy thông tin phòng với id: {}", id);
        return Helper.createResponseEntity(service.getPhongById(id));
    }
    @PutMapping("changeStatus/{id}")
    public ResponseEntity<?>changeStatus(@PathVariable String id) {
        return Helper.createResponseEntity(service.changeStatusPhong(id));
    }
}