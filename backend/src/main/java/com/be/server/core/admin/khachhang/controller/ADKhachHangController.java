package com.be.server.core.admin.khachhang.controller;

import com.be.server.core.admin.khachhang.model.request.ADAddAndUpdateKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADSearchKhachHangRequest;
import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_KHACH_HANG)
public class ADKhachHangController {
    private final ADKhachHangService adKhachHangService;
    @GetMapping()
    public ResponseEntity<?>getAllKhachHang(@ModelAttribute  ADSearchKhachHangRequest request) {
        return Helper.createResponseEntity(adKhachHangService.getAllKhachHang(request));
    }
    @PostMapping("/add-khach-hang")
    public ResponseEntity<?>addKhachHang(@RequestBody ADAddAndUpdateKhachHangRequest request) {
        return Helper.createResponseEntity(adKhachHangService.addKhachHang(request));
    }
    @PutMapping("/update-khach-hang/{id}")
    public ResponseEntity<?>updateKhachHang(@PathVariable String id,@RequestBody ADAddAndUpdateKhachHangRequest request) {
        return Helper.createResponseEntity(adKhachHangService.updateKhachHang(request,id));
    }
    @PutMapping("/changeStatus/{id}")
    public ResponseEntity<?>changeStatusKhachHang(@PathVariable("id") String id) {
        return Helper.createResponseEntity(adKhachHangService.changeStatusKhachHang(id));
    }
    @GetMapping("/loai-khach-hang")
    public ResponseEntity<?>getDataLoaiKhachHang() {
        return Helper.createResponseEntity(adKhachHangService.getDataLoaiKhachHang());
    }
}
