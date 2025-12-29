package com.be.server.core.admin.lichdatphong.trangthaiphong.service;

import com.be.server.core.admin.lichdatphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.common.base.ResponseObject;

public interface SoDoPhongService {

    ResponseObject<?> getAllSoDoPhong(SoDoSearch search);

    ResponseObject<?> updateTrangThaiVeSinh(String id, Integer trangThaiVeSinh );

    ResponseObject<?> getDataLoaiPhong();
}
