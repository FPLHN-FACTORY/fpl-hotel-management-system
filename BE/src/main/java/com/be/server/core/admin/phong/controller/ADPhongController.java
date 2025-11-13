package com.be.server.core.admin.phong.controller;


import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.ADSavePhongRequest;
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

    @PutMapping("changeStatus/{id}")
    public ResponseEntity<?>changeStatus(@PathVariable String id) {
        return Helper.createResponseEntity(adPhongService.changeStatusPhong(id));
    }

    @PostMapping("/addPhong")
    public ResponseEntity<?> taoPhong(@RequestBody ADSavePhongRequest request){
        return Helper.createResponseEntity(adPhongService.savePhong(request));
    }

    @GetMapping("/loai-phong")
    public ResponseEntity<?> loaiPhong(){
        return Helper.createResponseEntity(adPhongService.getAllLoaiPhong());
    }
}