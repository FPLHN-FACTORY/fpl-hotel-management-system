package com.be.server.core.admin.phong.service;

import com.be.server.core.admin.phong.model.request.ADSaveDichVuPhatSinhRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADDichVuPhatSinhService {
    ResponseObject<?> saveDichVu(ADSaveDichVuPhatSinhRequest request);

    ResponseObject<?> deleteDichVu(String id);

    ResponseObject<?> getDichVuByBooking(String idPhieuDatPhong);

    ResponseObject<?> getDichVuByRoomBooking(String idChiTietDatPhong);

    ResponseObject<?> getActiveBookingByRoom(String roomId);
}
