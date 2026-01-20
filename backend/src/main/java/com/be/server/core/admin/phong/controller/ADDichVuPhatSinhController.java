package com.be.server.core.admin.phong.controller;

import com.be.server.core.admin.phong.model.request.ADSaveDichVuPhatSinhRequest;
import com.be.server.core.admin.phong.service.ADDichVuPhatSinhService;
import com.be.server.infrastructure.constant.MappingConstants;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(MappingConstants.API_LE_TAN_DICH_VU_PHAT_SINH)
public class ADDichVuPhatSinhController {

    private final ADDichVuPhatSinhService adDichVuPhatSinhService;

    @PostMapping
    public ResponseEntity<?> saveDichVu(@RequestBody ADSaveDichVuPhatSinhRequest request) {
        return Helper.createResponseEntity(adDichVuPhatSinhService.saveDichVu(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDichVu(@PathVariable String id) {
        return Helper.createResponseEntity(adDichVuPhatSinhService.deleteDichVu(id));
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<?> getDichVuByBooking(@PathVariable String id) {
        return Helper.createResponseEntity(adDichVuPhatSinhService.getDichVuByBooking(id));
    }

    @GetMapping("/room-booking/{id}")
    public ResponseEntity<?> getDichVuByRoomBooking(@PathVariable String id) {
        return Helper.createResponseEntity(adDichVuPhatSinhService.getDichVuByRoomBooking(id));
    }

    @GetMapping("/active-booking/{roomId}")
    public ResponseEntity<?> getActiveBookingByRoom(@PathVariable String roomId) {
        return Helper.createResponseEntity(adDichVuPhatSinhService.getActiveBookingByRoom(roomId));
    }
}
