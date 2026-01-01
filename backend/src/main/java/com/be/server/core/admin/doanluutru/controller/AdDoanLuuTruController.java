package com.be.server.core.admin.doanluutru.controller;

import com.be.server.core.admin.doanluutru.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.entity.ChiTietDoan;
import com.be.server.entity.DoanLuuTru;
import org.springframework.beans.factory.annotation.Autowired;
import com.be.server.core.common.base.ResponseObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.be.server.infrastructure.constant.MappingConstants;

@RestController
@RequestMapping(MappingConstants.API_ADMIN_DOAN_LUU_TRU)
@CrossOrigin("*")
public class AdDoanLuuTruController {

    @Autowired
    private AdDoanLuuTruService service;

    @PostMapping("/create")
    public ResponseObject<DoanLuuTru> createDoan(@RequestBody CreateDoanRequest request) {
        return ResponseObject.successForward(service.createDoan(request), "Tạo đoàn thành công");
    }

    @PostMapping("/add-member")
    public ResponseObject<ChiTietDoan> addMember(@RequestBody AddMemberRequest request) {
        return ResponseObject.successForward(service.addMember(request), "Thêm thành viên thành công");
    }

    @GetMapping("/booking/{idBooking}")
    public ResponseObject<DoanLuuTru> getByBooking(@PathVariable String idBooking) {
        DoanLuuTru doan = service.getDoanByBooking(idBooking);
        return ResponseObject.successForward(doan, "Lấy thông tin thành công");
    }

    @GetMapping("/{idDoan}/members")
    public ResponseObject<List<ChiTietDoan>> getMembers(@PathVariable String idDoan) {
        return ResponseObject.successForward(service.getMembers(idDoan), "Lấy danh sách thành viên thành công");
    }

    @GetMapping("/list")
    public ResponseObject<List<DoanLuuTru>> getAll(
            @ModelAttribute com.be.server.core.admin.doanluutru.request.FindDoanRequest request) {
        return ResponseObject.successForward(service.getAllDoan(request), "Lấy danh sách đoàn thành công");
    }

    @GetMapping("/active-bookings")
    public ResponseObject<List<com.be.server.entity.DatPhong>> getActiveBookings() {
        return ResponseObject.successForward(service.getAvailableBookings(),
                "Lấy danh sách booking khả dụng thành công");
    }
}
