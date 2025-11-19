package com.be.server.core.admin.phong.controller;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.ADSavePhongRequest;
import com.be.server.core.admin.phong.model.request.ADUpdatePhongRequest;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_PHONG)
public class ADPhongController {

    private final ADPhongService adPhongService;

    @GetMapping
    public ResponseEntity<?> getDanhSachPhong(@ModelAttribute ADPhongSearchRequest request) {
        return Helper.createResponseEntity(adPhongService.getAllPhong(request));
    }

    @PostMapping("/addPhong")
    public ResponseEntity<?> taoPhong(@RequestBody ADSavePhongRequest request){
        return Helper.createResponseEntity(adPhongService.savePhong(request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoaPhong(@PathVariable String id) {
        return Helper.createResponseEntity(adPhongService.deletePhong(id));
    }

    @GetMapping("/loai-phong")
    public ResponseEntity<?> loaiPhong(){
        return Helper.createResponseEntity(adPhongService.getAllLoaiPhong());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPhongById(@PathVariable String id){
        return Helper.createResponseEntity(adPhongService.getPhongById(id));
    }

    @PutMapping("/updatePhong/{id}")
    public ResponseEntity<?> updatePhong(@PathVariable String id, @RequestBody ADUpdatePhongRequest request){
        return Helper.createResponseEntity(adPhongService.updatePhong(id, request));
    }

    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags(){
        return Helper.createResponseEntity(adPhongService.getAllTags());
    }

}