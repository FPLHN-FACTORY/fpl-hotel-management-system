package com.be.server.core.admin.datphong.trangthaiphong.service.impl;

import com.be.server.core.admin.datphong.trangthaiphong.model.response.SoDoPhongResponse;
import com.be.server.core.admin.datphong.trangthaiphong.repository.ADDPLoaiPhongRepository;
import com.be.server.core.admin.datphong.trangthaiphong.repository.SoDoPhongRepository;
import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.admin.datphong.trangthaiphong.service.SoDoPhongService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoDoPhongServiceImpl implements SoDoPhongService {

    private final SoDoPhongRepository soDoPhongRepository;
    private final com.be.server.repository.ChiTietDatPhongRepository chiTietDatPhongRepository;

    private final ADDPLoaiPhongRepository loaiPhongRepository;

    @Override
    public ResponseObject<?> getAllSoDoPhong(SoDoSearch request) {
        Long timestamp = Instant.now().toEpochMilli();

        List<SoDoPhongResponse> rooms = soDoPhongRepository.getRoomOverview(
                request.getMa(),
                request.getTen(),
                request.getLoaiPhong(),
                request.getTang(),
                timestamp,
                request
        ).stream().map(p -> {
            // Map trạng thái phòng
            TrangThaiPhongDat trangThai = chiTietDatPhongRepository
                    .findActiveBookingsByRoomId(p.getId(), timestamp)
                    .orElse(TrangThaiPhongDat.TRONG);


            // Tạo đối tượng DTO với trạng thái map sau
            return new SoDoPhongResponse() {
                @Override public String getId() { return p.getId(); }
                @Override public String getMa() { return p.getMa(); }
                @Override public String getTen() { return p.getTen(); }
                @Override public String getLoaiPhong() { return p.getLoaiPhong(); }
                @Override public Integer getTang() { return p.getTang(); }
                @Override public String getTrangThaiHoatDong() { return p.getTrangThaiHoatDong(); }
                @Override public Integer getSucChua() { return p.getSucChua(); }
                @Override public java.math.BigDecimal getPrice() { return p.getPrice(); }
                @Override public TrangThaiPhongDat getTrangThaiPhong() { return trangThai; }
                @Override public String getTrangThaiVeSinh() { return p.getTrangThaiVeSinh();}
            };
        }).collect(Collectors.toList());

        return ResponseObject.successForward(rooms, "Lấy danh sách thành công");
    }

    @Override
    public ResponseObject<?> getDataLoaiPhong() {
        return ResponseObject.successForward(
                loaiPhongRepository.getDataCombobox(),
                "SUCCESS"
        );
    }
}
