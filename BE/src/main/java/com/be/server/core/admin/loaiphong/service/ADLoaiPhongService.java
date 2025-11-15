package com.be.server.core.admin.loaiphong.service;

import com.be.server.core.admin.loaiphong.model.request.ADSaveLoaiPhongRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADLoaiPhongService {

    ResponseObject<?> getAllLoaiPhong();

    ResponseObject<?> saveLoaiPhong(ADSaveLoaiPhongRequest request);

    ResponseObject<?> updateLoaiPhong(String id, ADSaveLoaiPhongRequest request);

    ResponseObject<?> deleteLoaiPhong(String id);
}
