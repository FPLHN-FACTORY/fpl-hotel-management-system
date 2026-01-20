package com.be.server.core.admin.datphong.booking.service.impl;

import com.be.server.core.admin.datphong.booking.model.request.*;
import com.be.server.core.admin.datphong.booking.model.response.*;
import com.be.server.core.admin.datphong.booking.repository.*;
import com.be.server.core.admin.datphong.booking.service.ADDatPhongService;
import com.be.server.core.admin.phong.repository.ADLoaiPhongRepository;
import com.be.server.core.admin.phong.repository.ADPhongTagRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.*;
import com.be.server.infrastructure.constant.*;
import com.be.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ADDatPhongServiceImpl implements ADDatPhongService {

    private final ADLoaiPhongRepository adLoaiPhongRepository;
    private final ADDatPhongRepository adDatPhongRepository;
    private final ADPhongTagRepository adPhongTagRepository;
    private final ADKhachDatPhongRepository khachDatPhongRepository;
    private final PhieuDatPhongRepository phieuDatPhongRepository;
    private final ChiTietDatPhongRepository chiTietDatPhongRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhongRepository phongRepository;
    private final DichVuPhatSinhRepository dichVuPhatSinhRepository;

    @Override
    @Transactional
    public ResponseObject<?> checkout(String idChiTietDatPhong) {
        ChiTietDatPhong chiTiet = chiTietDatPhongRepository.findById(idChiTietDatPhong)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đặt phòng"));

        if (chiTiet.getStatus_chi_tiet() == StatusChiTietDatPhong.CHECKOUT) {
            return ResponseObject.errorForward("Phòng đã trả", HttpStatus.BAD_REQUEST);
        }

        // Calculate Room Cost
        long now = Instant.now().toEpochMilli();
        long checkIn = chiTiet.getCheckIn() != null ? chiTiet.getCheckIn() : chiTiet.getPhieuDatPhong().getCheckInDate();

        long diff = now - checkIn;
        long oneDay = 24 * 60 * 60 * 1000L;
        long days = (long) Math.ceil((double) diff / oneDay);
        if (days < 1) days = 1;

        BigDecimal roomCost = chiTiet.getPrice().multiply(BigDecimal.valueOf(days));

        // Get Incurred Services
        List<DichVuPhatSinh> services = dichVuPhatSinhRepository.findByChiTietDatPhong_Id(idChiTietDatPhong);
        BigDecimal serviceCost = services.stream()
                .map(dv -> dv.getThanhTien() != null ? dv.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = roomCost.add(serviceCost);

        // Update Status
        chiTiet.setStatus_chi_tiet(StatusChiTietDatPhong.CHECKOUT);
        chiTiet.setCheckOut(now);
        chiTietDatPhongRepository.save(chiTiet);

        // Check if all rooms in group are checkout
        PhieuDatPhong pdp = chiTiet.getPhieuDatPhong();
        
        // Reload details to ensure up-to-date statuses if mappedBy default doesn't refresh automatically in transaction
        // But here we rely on the object graph. Ideally we should check database or refresh.
        // Simple approach: Check all loaded details.
        boolean allCheckout = pdp.getBookingDetails().stream()
                .allMatch(ct -> ct.getStatus_chi_tiet() == StatusChiTietDatPhong.CHECKOUT);

        if(allCheckout) {
            pdp.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CHECKOUT);
            // pdp.setCheckOutDate(now); // Maybe keep original planned checkout or update to actual? 
            // Usually checkout date in invoice is actual. Let's update it.
            pdp.setCheckOutDate(now);
            phieuDatPhongRepository.save(pdp);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomCost", roomCost);
        result.put("serviceCost", serviceCost);
        result.put("total", total);
        result.put("days", days);

        return ResponseObject.successForward(result, "Trả phòng thành công");
    }

    @Override
    public ResponseObject<?> checkPhongTrong(CheckPhongTrongRequest request) {
        if (request.getNgayNhan() == null || request.getNgayTra() == null) {
            return ResponseObject.errorForward("Vui long chon ngay nhan va tra phong", HttpStatus.BAD_REQUEST);
        }

        if (request.getNgayTra() <= request.getNgayNhan()) {
            return ResponseObject.errorForward("Ngay nhan phai truoc ngay tra", HttpStatus.BAD_REQUEST);
        }

        if (request.getSoLuongKhach() == null || request.getSoLuongKhach() <= 0) {
            return ResponseObject.errorForward("So luong khach phai lon hon 0", HttpStatus.BAD_REQUEST);
        }

        List<LoaiPhong> allLoaiPhong = adLoaiPhongRepository.findAll();
        List<LoaiPhongAvailableResponse> responses = new ArrayList<>();

        for (LoaiPhong lp : allLoaiPhong) {
            Long soPhongTrong = adDatPhongRepository.countPhongTrongByLoaiPhong(
                    lp.getId(),
                    request.getNgayNhan(),
                    request.getNgayTra(),
                    TrangThaiHoatDong.DANG_HOAT_DONG);

            if (soPhongTrong > 0) {
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

        if (responses.isEmpty()) {
            return ResponseObject.errorForward("Khong co phong trong trong khoang thoi gian nay", HttpStatus.NOT_FOUND);
        }

        return ResponseObject.successForward(responses, "Lay danh sach phong trong thanh cong");
    }

    @Override
    public ResponseObject<?> getPhongByLoaiPhong(DatPhongTheoLoaiRequest request) {
        if (request.getNgayNhan() == null || request.getNgayTra() == null) {
            return ResponseObject.errorForward("Vui long chon ngay nhan va ngay tra phong", HttpStatus.BAD_REQUEST);
        }

        if (request.getNgayTra() <= request.getNgayNhan()) {
            return ResponseObject.errorForward("Ngay nhan phai truoc ngay tra", HttpStatus.BAD_REQUEST);
        }

        List<PhongDatResponse> allPhongDat = new ArrayList<>();

        for (DatPhongTheoLoaiRequest.ChonLoaiPhong chonLoaiPhong : request.getDanhSachLoaiPhong()) {
            List<Phong> phongTrong = adDatPhongRepository.findPhongTrongByLoaiPhong(
                    chonLoaiPhong.getIdLoaiPhong(),
                    request.getNgayNhan(),
                    request.getNgayTra(),
                    TrangThaiHoatDong.DANG_HOAT_DONG);

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

                List<PhongTag> tags = adPhongTagRepository.findByPhongId(phong.getId());
                List<PhongDatResponse.TagInfo> tagInfos = tags.stream()
                        .filter(phongTag -> phongTag.getTag() != null)
                        .map(phongTag -> new PhongDatResponse.TagInfo(
                                phongTag.getTag().getId(),
                                phongTag.getTag().getMa(),
                                phongTag.getTag().getTen(),
                                phongTag.getTag().getMau()))
                        .collect(Collectors.toList());
                phongDatResponse.setTags(tagInfos);

                allPhongDat.add(phongDatResponse);
            }
        }

        return ResponseObject.successForward(allPhongDat, "Lay danh sach phong dat thanh cong");
    }

    @Override
    public ResponseObject<?> searchKhachHang(String keyword) {
        if (keyword == null) {
            keyword = "";
        } else {
            keyword = keyword.trim();
        }
        log.info("=== Search Khach Hang ===");
        log.info("Keyword received: '{}'", keyword);

        List<KhachHang> khachHangs = khachDatPhongRepository.findByKeyword(keyword);
        log.info("Found {} customers", khachHangs.size());

        List<TimKhachHangResponse> results = khachHangs.stream()
                .map(kh -> new TimKhachHangResponse(
                        kh.getId(),
                        kh.getMaNguoiDung(),
                        kh.getHoTen(),
                        kh.getEmail(),
                        kh.getSoGiayTo(),
                        kh.getSoDienThoai(),
                        kh.getDiaChi(),
                        kh.getQuocTich()))
                .collect(Collectors.toList());

        return ResponseObject.successForward(
                results,
                "Tim kiem khach hang thanh cong");
    }

    @Override
    @Transactional
    public ResponseObject<?> confirmBooking(ConfirmBookingRequest request) {
        try {
            // Validation
            if (request.getIdKhachHang() == null || request.getIdKhachHang().isEmpty()) {
                return ResponseObject.errorForward("Vui long chon khach hang", HttpStatus.BAD_REQUEST);
            }

            if (request.getNgayNhan() == null || request.getNgayTra() == null) {
                return ResponseObject.errorForward("Vui long chon ngay nhan va tra phong", HttpStatus.BAD_REQUEST);
            }

            if (request.getNgayTra() <= request.getNgayNhan()) {
                return ResponseObject.errorForward("Ngay tra phai sau ngay nhan", HttpStatus.BAD_REQUEST);
            }

            if (request.getDanhSachIdPhong() == null || request.getDanhSachIdPhong().isEmpty()) {
                return ResponseObject.errorForward("Vui long chon phong", HttpStatus.BAD_REQUEST);
            }

            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Khach hang khong ton tai"));

            List<Phong> danhSachPhong = new ArrayList<>();
            for (String idPhong : request.getDanhSachIdPhong()) {
                Phong phong = phongRepository.findById(idPhong)
                        .orElseThrow(() -> new RuntimeException("Phong " + idPhong + " khong ton tai"));

                boolean isBooked = adDatPhongRepository.isRoomBookedInPeriod(
                        idPhong,
                        request.getNgayNhan(),
                        request.getNgayTra());

                if (isBooked) {
                    return ResponseObject.errorForward(
                            "Phong " + phong.getMa() + " da duoc dat trong khoang thoi gian nay",
                            HttpStatus.CONFLICT);
                }

                danhSachPhong.add(phong);
            }

            PhieuDatPhong phieuDatPhong = new PhieuDatPhong();
            phieuDatPhong.setCheckInDate(request.getNgayNhan());
            phieuDatPhong.setCheckOutDate(request.getNgayTra());
            phieuDatPhong.setKhachHang(khachHang);

            Long now = Instant.now().toEpochMilli();
            if (request.getNhanNgay() != null && request.getNhanNgay()) {
                if (now >= request.getNgayNhan() - 3600000L) {
                    phieuDatPhong.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CHECKIN);
                } else {
                    phieuDatPhong.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CONFIRMED);
                }
            } else {
                phieuDatPhong.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CONFIRMED);
            }

            PhieuDatPhong savedPhieu = phieuDatPhongRepository.save(phieuDatPhong);

            List<ChiTietDatPhong> chiTietList = new ArrayList<>();
            BigDecimal tongTien = BigDecimal.ZERO;

            for (Phong phong : danhSachPhong) {
                ChiTietDatPhong chiTiet = new ChiTietDatPhong();
                chiTiet.setPhieuDatPhong(savedPhieu);
                chiTiet.setRoom(phong);
                chiTiet.setPrice(phong.getLoaiPhong().getGiaCaNgay());

                if (savedPhieu.getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKIN) {
                    chiTiet.setStatus_chi_tiet(StatusChiTietDatPhong.CHECKIN);
                    chiTiet.setCheckIn(now);
                } else {
                    chiTiet.setStatus_chi_tiet(StatusChiTietDatPhong.BOOKED);
                }

                chiTietList.add(chiTiet);
                tongTien = tongTien.add(phong.getLoaiPhong().getGiaCaNgay());
            }

            List<ChiTietDatPhong> savedChiTiet = chiTietDatPhongRepository.saveAll(chiTietList);

            CreateBookingResponse response = new CreateBookingResponse();
            response.setIdPhieuDatPhong(savedPhieu.getId());
            response.setMaPhieuDatPhong("PDP" + savedPhieu.getId().substring(0, 8));
            response.setTenKhachHang(khachHang.getHoTen());
            response.setNgayCheckIn(savedPhieu.getCheckInDate());
            response.setNgayCheckOut(savedPhieu.getCheckOutDate());
            response.setTrangThai(savedPhieu.getStatus_phieu_dat_phong().name());
            response.setTongTien(tongTien);

            List<CreateBookingResponse.ChiTietPhongInfo> chiTietInfoList = savedChiTiet.stream()
                    .map(ct -> new CreateBookingResponse.ChiTietPhongInfo(
                            ct.getId(),
                            ct.getRoom().getId(),
                            ct.getRoom().getMa(),
                            ct.getRoom().getTen(),
                            ct.getPrice(),
                            ct.getStatus_chi_tiet().name()))
                    .collect(Collectors.toList());

            response.setDanhSachPhong(chiTietInfoList);

            String message = savedPhieu.getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKIN
                    ? "Nhan phong thanh cong"
                    : "Dat phong thanh cong";

            return ResponseObject.successForward(response, message);

        } catch (Exception e) {
            log.error("Error confirming booking: ", e);
            return ResponseObject.errorForward(
                    "Loi khi dat phong: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}