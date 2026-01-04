package com.be.server.core.admin.doanluutru.service;

import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DatPhong;
import com.be.server.entity.DoanLuuTru;
import java.util.List;

import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;

public interface AdDoanLuuTruService {
    ResponseObject<?> createDoan(CreateDoanRequest request);

    ResponseObject<?>addMember(AddMemberRequest request);

    ResponseObject<?> getMembers(String idDoanLuuTru,SearchMemberRequest request);

    DoanLuuTru getDoanByBooking(String idDatPhong);

    ResponseObject<?> getAllDoan(FindDoanRequest request);

    ResponseObject<?> getAllBooked();
}
