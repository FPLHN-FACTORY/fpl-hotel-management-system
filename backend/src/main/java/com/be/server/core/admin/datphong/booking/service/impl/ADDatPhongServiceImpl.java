package com.be.server.core.admin.datphong.booking.service.impl;

import com.be.server.core.admin.datphong.booking.model.request.CheckPhongTrongRequest;
import com.be.server.core.admin.datphong.booking.model.request.CreateDatPhongRequest;
import com.be.server.core.admin.datphong.booking.model.request.DatPhongTheoLoaiRequest;
import com.be.server.core.admin.datphong.booking.model.response.LoaiPhongAvailableResponse;
import com.be.server.core.admin.datphong.booking.model.response.PhongDatResponse;
import com.be.server.core.admin.datphong.booking.repository.ADDatPhongRepository;
import com.be.server.core.admin.datphong.booking.repository.ADKhachDatPhongRepository;
import com.be.server.core.admin.datphong.booking.service.ADDatPhongService;
import com.be.server.core.admin.phong.repository.ADLoaiPhongRepository;
import com.be.server.core.admin.phong.repository.ADPhongTagRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.*;
import com.be.server.infrastructure.constant.StatusChiTietDatPhong;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.repository.ChiTietDatPhongRepository;
import com.be.server.repository.KhachHangRepository;
import com.be.server.repository.PhieuDatPhongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ADDatPhongServiceImpl implements ADDatPhongService {

    private final ADLoaiPhongRepository adLoaiPhongRepository;

    private final ADDatPhongRepository adDatPhongRepository;

    private final ADPhongTagRepository adphongTagRepository;

    private final PhieuDatPhongRepository phieuDatPhongRepository;

    private final KhachHangRepository khachHangRepository;

    private final ADKhachDatPhongRepository khachDatPhongRepository;

    private final ChiTietDatPhongRepository chiTietDatPhongRepository;


    @Override
    public ResponseObject<?> checkPhongTrong(CheckPhongTrongRequest request) {

        if(request.getNgayNhan() == null || request.getNgayTra() == null) {
            return ResponseObject.errorForward("Vui long chon ngay nhan va tra phong", HttpStatus.BAD_REQUEST);
        }

        if(request.getNgayTra() <= request.getNgayNhan()) {
            return ResponseObject.errorForward("Ngay nhan phai truoc ngay trar", HttpStatus.BAD_REQUEST);
        }

        if(request.getSoLuongKhach() == null || request.getSoLuongKhach() <= 0) {
            return ResponseObject.errorForward("So luong khach phai lon hon 0", HttpStatus.BAD_REQUEST);
        }

        List<LoaiPhong> allLoaiPhong = adLoaiPhongRepository.findAll();
        List<LoaiPhongAvailableResponse> responses = new ArrayList<>();

        for(LoaiPhong lp : allLoaiPhong) {
            Long soPhongTrong = adDatPhongRepository.countPhongTrongByLoaiPhong(
                    lp.getId(),
                    request.getNgayNhan(),
                    request.getNgayTra(),
                    TrangThaiHoatDong.DANG_HOAT_DONG
            );

            if(soPhongTrong > 0) {
                LoaiPhongAvailableResponse response = new LoaiPhongAvailableResponse();
                response.setIdLoaiPhong(lp.getId());
                response.setTenLoaiPhong(lp.getTen());
                response.setMoTa(lp.getMoTa());
                response.setSoGiuongDon(lp.getSoGiuongDon());
                response.setSoGiuongDOi(lp.getSoGiuongDoi());
                response.setSoNguoiToiDa(lp.getSoNguoiToiDa());
                response.setGiaCaNgay(lp.getGiaCaNgay());
                response.setSoPhongTrong(soPhongTrong.intValue());
                responses.add(response);
            }
        }

        if(responses.isEmpty()) {
            return ResponseObject.errorForward("Khong co phong trong trong khoang thoi gian nay", HttpStatus.NOT_FOUND);
        }

        return ResponseObject.successForward(responses, "Lay danh sach phong trong thanh cong");
    }

    @Override
    public ResponseObject<?> getPhongByLoaiPhong(DatPhongTheoLoaiRequest request) {
        if (request.getNgayNhan() == null || request.getNgayTra() == null) {
            return ResponseObject.errorForward("Vui long chon ngay nhan va ngay tra phong", HttpStatus.BAD_REQUEST);
        }

        if(request.getNgayTra() <= request.getNgayNhan()) {
            return ResponseObject.errorForward("Ngay nhan phai truoc ngay tra", HttpStatus.BAD_REQUEST);
        }

        List<PhongDatResponse> allPhongDat = new ArrayList<>();

        for(DatPhongTheoLoaiRequest.ChonLoaiPhong chonLoaiPhong : request.getDanhSachLoaiPhong()) {
            List<Phong> phongTrong = adDatPhongRepository.findPhongTrongByLoaiPhong(
                    chonLoaiPhong.getIdLoaiPhong(),
                    request.getNgayNhan(),
                    request.getNgayTra(),
                    TrangThaiHoatDong.DANG_HOAT_DONG
            );

            int soLuongCanLay = Math.min(chonLoaiPhong.getSoLuong(), phongTrong.size());

            for (int i = 0; i < soLuongCanLay; i++) {
                Phong phong = phongTrong.get(i);

                PhongDatResponse phongDatResponse = new PhongDatResponse();
                phongDatResponse.setIdPhong(phong.getId());
                phongDatResponse.setMaPhong(phong.getMa());
                phongDatResponse.setTenPhong(phong.getTen());
                phongDatResponse.setTenLoaiPhong(phong.getLoaiPhong().getTen());
                phongDatResponse.setTang(phong.getTang());
                phongDatResponse.setGia(phong.getLoaiPhong().getGiaCaNgay());
                phongDatResponse.setSucChua(phong.getLoaiPhong().getSoNguoiToiDa());


                List<PhongTag> tags = adphongTagRepository.findByPhongId(phong.getId());
                List<PhongDatResponse.TagInfo> tagInfos = tags.stream()
                        .filter(phongTag -> phongTag.getTag() != null)
                        .map(phongTag -> new PhongDatResponse.TagInfo(
                                phongTag.getTag().getId(),
                                phongTag.getTag().getMa(),
                                phongTag.getTag().getTen(),
                                phongTag.getTag().getMau()
                        ))
                        .collect(Collectors.toList());
                phongDatResponse.setTags(tagInfos);

                allPhongDat.add(phongDatResponse);
            }
        }

        return ResponseObject.successForward(allPhongDat, "Lay danh sach phong dat thanh cong");
    }

    @Override
    public ResponseObject<?> createDatPhong(CreateDatPhongRequest request) {
        if (request.getNgayNhan() == null || request.getNgayTra() == null) {
            return ResponseObject.errorForward("Vui long nhap vao ngay nhan va ngay tra phong", HttpStatus.BAD_REQUEST);
        }

        if (request.getNgayTra() <= request.getNgayNhan()) {
            return ResponseObject.errorForward("Ngay nhan phai truoc ngay tra", HttpStatus.BAD_REQUEST);
        }

        if (request.getIdKhachHang() == null || request.getIdKhachHang().isEmpty()) {
            return ResponseObject.errorForward("Vui long chon khach hang", HttpStatus.BAD_REQUEST);
        }

        if (request.getDanhSachIdPhong() == null || request.getDanhSachIdPhong().isEmpty()) {
            return ResponseObject.errorForward("Vui long chon it nhat 1 phong", HttpStatus.BAD_REQUEST);
        }

        Optional<KhachHang> khachHangOptional = khachHangRepository.findById(request.getIdKhachHang());
        if (khachHangOptional.isEmpty()) {
            return ResponseObject.errorForward("Khong tim thay khach hang", HttpStatus.NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getNgayNhan())) {
            long currentTime = Instant.now().toEpochMilli();
            long oneHourBeforeCheckIn = request.getNgayNhan() - (60*60*1000);

            if (oneHourBeforeCheckIn > currentTime) {
                return ResponseObject.errorForward(
                        "Chi co the nhan phong ngay khi con toi da 1 gio truoc thoi gian checkin",
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        PhieuDatPhong phieuDatPhong = new PhieuDatPhong();
        phieuDatPhong.setCheckInDate(request.getNgayNhan());
        phieuDatPhong.setCheckOutDate(request.getNgayTra());
        phieuDatPhong.setKhachHang(khachHangOptional.get());
        phieuDatPhong.setStatus_phieu_dat_phong(
                Boolean.TRUE.equals(request.getNhanNgay())
                ? StatusPhieuDatPhong.CONFIRMED
                        : StatusPhieuDatPhong.PENDING
        );

        PhieuDatPhong savedPhieu = phieuDatPhongRepository.save(phieuDatPhong);

        List<ChiTietDatPhong> chiTietDatPhongList = new ArrayList<>();
        for(String idPhong : request.getDanhSachIdPhong()) {
            Optional<Phong> phongOptional = adDatPhongRepository.findById(idPhong);
            if (phongOptional.isPresent()) {
                Phong phong = phongOptional.get();
                ChiTietDatPhong chiTietDatPhong = new ChiTietDatPhong();
                chiTietDatPhong.setPhieuDatPhong(phieuDatPhong);
                chiTietDatPhong.setRoom(phong);
                chiTietDatPhong.setPrice(phong.getLoaiPhong().getGiaCaNgay());
                chiTietDatPhong.setStatus_chi_tiet(
                        Boolean.TRUE.equals(request.getNhanNgay())
                                ? StatusChiTietDatPhong.CHECKIN
                                : StatusChiTietDatPhong.BOOKED
                );

                if (Boolean.TRUE.equals(request.getNhanNgay())) {
                    chiTietDatPhong.setCheckIn(Instant.now().toEpochMilli());
                }

                chiTietDatPhongList.add(chiTietDatPhong);
            }
        }
        chiTietDatPhongRepository.saveAll(chiTietDatPhongList);

        return ResponseObject.successForward(savedPhieu.getId(), "Dat phong thanh cong");
    }

    @Override
    public ResponseObject<?> searchKhachHang(String keyword) {

        return ResponseObject.successForward(
                khachDatPhongRepository.findByKeyword(keyword),
                "Tim kiem khach hang thanh cong"
        );
    }
}
