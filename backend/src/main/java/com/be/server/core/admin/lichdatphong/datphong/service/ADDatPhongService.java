package com.be.server.core.admin.lichdatphong.datphong.service;

import com.be.server.core.admin.lichdatphong.datphong.model.request.CheckPhongTrongRequest;
import com.be.server.core.admin.lichdatphong.datphong.model.request.CreateDatPhongRequest;
import com.be.server.core.admin.lichdatphong.datphong.model.request.DatPhongTheoLoaiRequest;
import com.be.server.core.common.base.ResponseObject;

public interface ADDatPhongService {
    ResponseObject<?> checkPhongTrong(CheckPhongTrongRequest request);

    ResponseObject<?> getPhongByLoaiPhong(DatPhongTheoLoaiRequest request);

    ResponseObject<?> createDatPhong(CreateDatPhongRequest request);

    ResponseObject<?> searchKhachHang(String keyword);
}
