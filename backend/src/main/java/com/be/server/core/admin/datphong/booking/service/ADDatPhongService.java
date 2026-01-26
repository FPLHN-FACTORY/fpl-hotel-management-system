package com.be.server.core.admin.datphong.booking.service;

import com.be.server.core.admin.datphong.booking.model.request.*;
import com.be.server.core.common.base.ResponseObject;

public interface ADDatPhongService {

    ResponseObject<?> checkPhongTrong(CheckPhongTrongRequest request);

    ResponseObject<?> getPhongByLoaiPhong(DatPhongTheoLoaiRequest request);

    ResponseObject<?> searchKhachHang(String keyword);

    ResponseObject<?> confirmBooking(ConfirmBookingRequest request);

    ResponseObject<?> checkout(String idChiTietDatPhong);


    ResponseObject<?> addTruongDoanDatPhong(CreateTruongDoanRequest request);
}
