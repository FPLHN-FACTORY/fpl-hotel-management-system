package com.be.server.core.admin.phong.service;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.LsDatPhongRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADPhongService {

    ResponseObject<?> getAllPhong(ADPhongSearchRequest request);

    ResponseObject<?> changeStatusPhong(String id);
}
