package com.be.server.core.admin.khachhang.service;

import com.be.server.core.admin.khachhang.model.request.ADAddAndUpdateKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADSearchKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.GiayToRequest;
import com.be.server.core.admin.khachhang.model.request.UpdateKhachHangLuuTruRequest;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.infrastructure.constant.LoaiGiayTo;

public interface ADKhachHangService {
    ResponseObject<?>getAllKhachHang(ADSearchKhachHangRequest request);
    ResponseObject<?>addKhachHang(ADAddAndUpdateKhachHangRequest request);
    ResponseObject<?>changeStatusKhachHang(String id);
    ResponseObject<?>updateKhachHang(ADAddAndUpdateKhachHangRequest request,String id);
    ResponseObject<?>getDataLoaiKhachHang();
    ResponseObject<?>findKhachHangByGiayTo(GiayToRequest request);
    ResponseObject<?>updateKhachHangLuuTru(UpdateKhachHangLuuTruRequest request, String id);
}
