package com.be.server.core.admin.doanluutru.service;

import com.be.server.core.admin.doanluutru.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.request.CreateDoanRequest;
import com.be.server.entity.ChiTietDoan;
import com.be.server.entity.DoanLuuTru;
import java.util.List;

import com.be.server.core.admin.doanluutru.request.FindDoanRequest;

public interface AdDoanLuuTruService {
    DoanLuuTru createDoan(CreateDoanRequest request);

    ChiTietDoan addMember(AddMemberRequest request);

    List<ChiTietDoan> getMembers(String idDoan);

    DoanLuuTru getDoanByBooking(String idDatPhong);

    List<DoanLuuTru> getAllDoan(FindDoanRequest request);

    List<com.be.server.entity.DatPhong> getAvailableBookings();
}
