package com.be.server.core.admin.datphong.trangthaiphong.service;

import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.common.base.ResponseObject;

public interface SoDoPhongService {

    ResponseObject<?> getAllSoDoPhong(SoDoSearch search);

    ResponseObject<?> getDataLoaiPhong();

    ResponseObject<?> updateTrangThaiVeSinh(String id, Integer trangThaiVeSinh );
}
