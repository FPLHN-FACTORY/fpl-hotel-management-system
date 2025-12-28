package com.be.server.core.admin.khachhang.service;

import com.be.server.core.admin.khachhang.model.request.ADAddAndUpdateKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADSearchKhachHangRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADKhachHangService {
    ResponseObject<?>getAllKhachHang(ADSearchKhachHangRequest request);
    ResponseObject<?>addKhachHang(ADAddAndUpdateKhachHangRequest request);
    ResponseObject<?>changeStatusKhachHang(String id);
    ResponseObject<?>updateKhachHang(ADAddAndUpdateKhachHangRequest request,String id);
    ResponseObject<?>getDataLoaiKhachHang();
}
