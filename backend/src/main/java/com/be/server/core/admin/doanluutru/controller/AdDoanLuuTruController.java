package com.be.server.core.admin.doanluutru.controller;

import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.AssignRoomRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.entity.DoanLuuTru;
import com.be.server.utils.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import com.be.server.core.common.base.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import com.be.server.infrastructure.constant.MappingConstants;

@RestController
@RequestMapping(MappingConstants.API_LE_TAN_DOAN_LUU_TRU)
@CrossOrigin("*")
public class AdDoanLuuTruController {

    @Autowired
    private AdDoanLuuTruService service;

    @PostMapping("/create")
    public ResponseEntity<?> createDoan(@RequestBody CreateDoanRequest request) {
        return Helper.createResponseEntity(service.createDoan(request));
    }

    @PostMapping("/add-member")
    public ResponseEntity<?> addMember(@RequestBody AddMemberRequest request) {
        return Helper.createResponseEntity(service.addMember(request));
    }

    @GetMapping("/booking/{idBooking}")
    public ResponseObject<DoanLuuTru> getByBooking(@PathVariable String idBooking) {
        DoanLuuTru doan = service.getDoanByBooking(idBooking);
        return ResponseObject.successForward(doan, "Lấy thông tin thành công");
    }

    @GetMapping("/{idDoan}/members")
    public ResponseEntity<?> getMembers(@PathVariable String idDoan,@ModelAttribute SearchMemberRequest request) {
        return Helper.createResponseEntity(service.getMembers(idDoan,request));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getAllDoan(
            @ModelAttribute FindDoanRequest request) {
        return Helper.createResponseEntity(service.getAllDoan(request));
    }

    @GetMapping("{idDoan}/booked")
    public ResponseEntity<?> getAllBookedTheoDoan(@PathVariable String idDoan) {
        return Helper.createResponseEntity(service.getAllBookedTheoDoan(idDoan));
    }

    @PutMapping("/assign-room/{id}")
    public ResponseEntity<?>assignRoom(@PathVariable String id,@RequestBody AssignRoomRequest request) {
        return Helper.createResponseEntity(service.assignRoom(id,request));
    }

    @GetMapping("/check-room/{idDoan}")
    public ResponseEntity<?> checkSoLuongToiDa(@PathVariable String idDoan) {
        return Helper.createResponseEntity(service.checkSoluongToiDaBooking(idDoan));
    }

    @PutMapping("/{idDoan}/check-in")
    public ResponseEntity<?>checkInDoan(@PathVariable String idDoan) {
        return Helper.createResponseEntity(service.checkInDoanLuuTru(idDoan));
    }

}
