package com.be.server.core.admin.phong.service;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.ADSavePhongRequest;
import com.be.server.core.admin.phong.model.request.ADUpdatePhongRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADPhongService {

    ResponseObject<?> getAllPhong(ADPhongSearchRequest request);

    ResponseObject<?> savePhong(ADSavePhongRequest phong);

    ResponseObject<?> deletePhong(String id);

    ResponseObject<?> getAllLoaiPhong();

    ResponseObject<?> updatePhong(String id, ADUpdatePhongRequest request);

    ResponseObject<?> getPhongById(String id);

    ResponseObject<?> getAllTags();

}