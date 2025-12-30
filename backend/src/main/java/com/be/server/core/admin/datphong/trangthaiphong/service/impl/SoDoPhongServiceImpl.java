package com.be.server.core.admin.datphong.trangthaiphong.service.impl;

import com.be.server.core.admin.datphong.trangthaiphong.model.response.SoDoPhongResponse;
import com.be.server.core.admin.datphong.trangthaiphong.repository.ADDPLoaiPhongRepository;
import com.be.server.core.admin.datphong.trangthaiphong.repository.SoDoPhongRepository;
import com.be.server.core.admin.datphong.trangthaiphong.model.request.SoDoSearch;
import com.be.server.core.admin.datphong.trangthaiphong.repository.UpdateTrangThaiVeSinhRepo;
import com.be.server.core.admin.datphong.trangthaiphong.service.SoDoPhongService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.TrangThaiPhongDat;
import com.be.server.infrastructure.constant.TrangThaiVeSinh;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SoDoPhongServiceImpl implements SoDoPhongService {

    private final SoDoPhongRepository soDoPhongRepository;

    private final com.be.server.repository.ChiTietDatPhongRepository chiTietDatPhongRepository;

    private final ADDPLoaiPhongRepository loaiPhongRepository;

    private final UpdateTrangThaiVeSinhRepo updateTrangThaiVeSinhRepo;


    @Override
    public ResponseObject<?> getAllSoDoPhong(SoDoSearch request) {
        Long timestamp = Instant.now().toEpochMilli();
        List<String> idsRoomUnavailable = new ArrayList<>();
        if (request.getNgayDen() != null && request.getNgayDi() != null) {
            idsRoomUnavailable = soDoPhongRepository.findRoomsByNgayDenAndNgayDi(request.getNgayDen(), request.getNgayDi());
        }
        List<SoDoPhongResponse> rooms = soDoPhongRepository.getRoomOverview(
                request.getIdLoaiPhong(),
                timestamp,
                request,
                idsRoomUnavailable
        ).stream().map(p -> {
            // Map trạng thái phòng
            TrangThaiPhongDat trangThai = chiTietDatPhongRepository
                    .findActiveBookingsByRoomId(p.getId(), timestamp)
                    .orElse(TrangThaiPhongDat.TRONG);


            // Tạo đối tượng DTO với trạng thái map sau
            return new SoDoPhongResponse() {
                @Override
                public String getId() {
                    return p.getId();
                }

                @Override
                public String getMa() {
                    return p.getMa();
                }

                @Override
                public String getTen() {
                    return p.getTen();
                }

                @Override
                public String getLoaiPhong() {
                    return p.getLoaiPhong();
                }

                @Override
                public Integer getTang() {
                    return p.getTang();
                }

                @Override
                public String getTrangThaiHoatDong() {
                    return p.getTrangThaiHoatDong();
                }

                @Override
                public Integer getSucChua() {
                    return p.getSucChua();
                }

                @Override
                public java.math.BigDecimal getPrice() {
                    return p.getPrice();
                }

                @Override
                public TrangThaiPhongDat getTrangThaiPhong() {
                    return trangThai;
                }

                @Override
                public String getTrangThaiVeSinh() {
                    return p.getTrangThaiVeSinh();
                }
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

    @Override
    @Transactional
    public ResponseObject<?> updateTrangThaiVeSinh(String id, Integer newStatusOrdinal) {

        // Lấy phòng
        Phong phong = soDoPhongRepository.findById(id).orElse(null);
        if (phong == null) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Phòng không tồn tại");
        }

        // Chuyển ordinal sang enum
        TrangThaiVeSinh newTrangThaiVeSinh;
        try {
            newTrangThaiVeSinh = TrangThaiVeSinh.values()[newStatusOrdinal];
        } catch (Exception e) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Trạng thái vệ sinh không hợp lệ");
        }

        // Lấy trạng thái phòng động từ đơn đặt phòng
        Long now = Instant.now().toEpochMilli();
        Long oneHourAgo = now - 3600_000;
        TrangThaiPhongDat trangThaiPhong = updateTrangThaiVeSinhRepo.getDynamicRoomStatus(id, now, oneHourAgo);

        // Kiểm tra nghiệp vụ
        if (newTrangThaiVeSinh == TrangThaiVeSinh.DANG_DON
                && trangThaiPhong == TrangThaiPhongDat.DANG_SU_DUNG) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST,
                    "Không thể đánh dấu đang dọn khi khách chưa check-out.");
        }

        // Cập nhật trạng thái vệ sinh
        phong.setTrangThaiVeSinh(newTrangThaiVeSinh);
        soDoPhongRepository.save(phong);

        // Trả về chỉ thông tin trạng thái vệ sinh
        return new ResponseObject<>(newTrangThaiVeSinh.name(), HttpStatus.OK,
                "Cập nhật trạng thái vệ sinh thành công.");
    }
}
