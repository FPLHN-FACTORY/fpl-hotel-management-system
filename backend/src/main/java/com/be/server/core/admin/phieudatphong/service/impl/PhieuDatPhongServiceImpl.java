package com.be.server.core.admin.phieudatphong.service.impl;

import com.be.server.core.admin.datphong.booking.repository.ADDatPhongRepository;
import com.be.server.core.admin.phieudatphong.model.request.*;
import com.be.server.core.admin.phieudatphong.model.response.*;
import com.be.server.core.admin.phieudatphong.repository.PhieuDatPhongExtendRepository;
import com.be.server.core.admin.phieudatphong.service.PhieuDatPhongService;
import com.be.server.core.admin.phong.repository.ADPhongTagRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.*;
import com.be.server.infrastructure.constant.*;
import com.be.server.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhieuDatPhongServiceImpl implements PhieuDatPhongService {

    private final PhieuDatPhongExtendRepository phieuDatPhongRepository;
    private final ChiTietLoaiPhongDatRepository chiTietLoaiPhongDatRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhongRepository phongRepository;
    private final ChiTietDatPhongRepository chiTietDatPhongRepository;
    private final ADDatPhongRepository adDatPhongRepository;
    private final ADPhongTagRepository adPhongTagRepository;

    @Override
    @Transactional
    public ResponseObject<?> taoPhieuDat(TaoPhieuDatRequest request) {
        try {
            // Log request for debugging
            log.info("=== Tạo phiếu đặt - Request received ===");
            log.info("NgayCheckIn: {}", request.getNgayCheckIn());
            log.info("NgayCheckOut: {}", request.getNgayCheckOut());
            log.info("SoLuongKhach: {}", request.getSoLuongKhach());
            log.info("DanhSachLoaiPhong size: {}",
                    request.getDanhSachLoaiPhong() != null ? request.getDanhSachLoaiPhong().size() : "NULL");

            if (request.getDanhSachLoaiPhong() != null) {
                for (int i = 0; i < request.getDanhSachLoaiPhong().size(); i++) {
                    var chon = request.getDanhSachLoaiPhong().get(i);
                    log.info("  [{}] idLoaiPhong: {}, soLuong: {}", i, chon.getIdLoaiPhong(), chon.getSoLuong());
                }
            }

            // Validation
            if (request.getNgayCheckOut() <= request.getNgayCheckIn()) {
                return ResponseObject.errorForward("Ngày check-out phải sau ngày check-in", HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra số lượng phòng trống cho từng loại
            for (var chonLoai : request.getDanhSachLoaiPhong()) {
                if (chonLoai.getIdLoaiPhong() == null || chonLoai.getIdLoaiPhong().trim().isEmpty()) {
                    log.error("ID loại phòng bị null hoặc rỗng!");
                    return ResponseObject.errorForward("ID loại phòng không hợp lệ", HttpStatus.BAD_REQUEST);
                }

                LoaiPhong loaiPhong = loaiPhongRepository.findById(chonLoai.getIdLoaiPhong())
                        .orElseThrow(
                                () -> new RuntimeException("Loại phòng không tồn tại: " + chonLoai.getIdLoaiPhong()));

                Long soPhongTrong = adDatPhongRepository.countPhongTrongByLoaiPhong(
                        chonLoai.getIdLoaiPhong(),
                        request.getNgayCheckIn(),
                        request.getNgayCheckOut(),
                        TrangThaiHoatDong.DANG_HOAT_DONG);

                if (soPhongTrong < chonLoai.getSoLuong()) {
                    return ResponseObject.errorForward(
                            "Loại phòng " + loaiPhong.getTen() + " chỉ còn " + soPhongTrong + " phòng trống",
                            HttpStatus.BAD_REQUEST);
                }
            }

            // Tạo PhieuDatPhong
            PhieuDatPhong phieu = new PhieuDatPhong();
            phieu.setCheckInDate(request.getNgayCheckIn());
            phieu.setCheckOutDate(request.getNgayCheckOut());
            phieu.setSoLuongKhach(request.getSoLuongKhach());
            phieu.setGhiChu(request.getGhiChu());
            phieu.setStatus_phieu_dat_phong(StatusPhieuDatPhong.PENDING);
            phieu.setCheckInDate(Calendar.getInstance().getTimeInMillis());
            // Note: nhanVienTao is null - will be set when authentication is implemented
            phieu = phieuDatPhongRepository.save(phieu);

            // Tạo ChiTietLoaiPhongDat
            for (var chonLoai : request.getDanhSachLoaiPhong()) {
                LoaiPhong loaiPhong = loaiPhongRepository.findById(chonLoai.getIdLoaiPhong()).orElseThrow();

                ChiTietLoaiPhongDat chiTiet = new ChiTietLoaiPhongDat();
                chiTiet.setPhieuDatPhong(phieu);
                chiTiet.setLoaiPhong(loaiPhong);
                chiTiet.setSoLuong(chonLoai.getSoLuong());
                chiTiet.setGiaDatTruoc(loaiPhong.getGiaCaNgay());
                chiTietLoaiPhongDatRepository.save(chiTiet);
            }

            return ResponseObject.successForward(phieu.getId(), "Tạo phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error creating phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi tạo phiếu đặt: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseObject<?> getDanhSachPhieuDat(PhieuDatPhongFilterRequest filter) {
        try {
            Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

            Page<PhieuDatPhong> page = phieuDatPhongRepository.findByFilter(
                    filter.getKeyword(),
                    filter.getStatus(),
                    filter.getTuNgay(),
                    filter.getDenNgay(),
                    pageable);

            Page<PhieuDatPhongListResponse> responsePage = page.map(phieu -> {
                PhieuDatPhongListResponse response = new PhieuDatPhongListResponse();
                response.setId(phieu.getId());
                response.setMaPhieu(
                        phieu.getId() != null ? "PDP" + phieu.getId().substring(0, Math.min(8, phieu.getId().length()))
                                : "N/A");
                response.setNgayCheckIn(phieu.getCheckInDate());
                response.setNgayCheckOut(phieu.getCheckOutDate());
                response.setTenKhachHang(phieu.getKhachHang() != null ? phieu.getKhachHang().getHoTen() : "Chưa gắn");
                response.setTrangThai(phieu.getStatus_phieu_dat_phong());
                response.setNgayTao(phieu.getCreatedDate());

                // Tính tổng số phòng
                if (phieu.getChiTietLoaiPhong() != null) {
                    int tongSoPhong = phieu.getChiTietLoaiPhong().stream()
                            .mapToInt(ChiTietLoaiPhongDat::getSoLuong)
                            .sum();
                    response.setTongSoPhong(tongSoPhong);
                }

                // Tính tổng tiền
                if (phieu.getBookingDetails() != null && !phieu.getBookingDetails().isEmpty()) {
                    BigDecimal tongTien = phieu.getBookingDetails().stream()
                            .map(ChiTietDatPhong::getPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    response.setTongTien(tongTien);
                } else if (phieu.getChiTietLoaiPhong() != null) {
                    BigDecimal tongTien = phieu.getChiTietLoaiPhong().stream()
                            .map(ct -> ct.getGiaDatTruoc().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    response.setTongTien(tongTien);
                }

                return response;
            });

            return ResponseObject.successForward(responsePage, "Lấy danh sách phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error getting danh sach phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi lấy danh sách: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseObject<?> getChiTietPhieuDat(String idPhieuDat) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findByIdWithDetails(idPhieuDat)
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            PhieuDatPhongDetailResponse response = new PhieuDatPhongDetailResponse();
            response.setId(phieu.getId());
            response.setMaPhieu(
                    phieu.getId() != null ? "PDP" + phieu.getId().substring(0, Math.min(8, phieu.getId().length()))
                            : "N/A");
            response.setNgayCheckIn(phieu.getCheckInDate());
            response.setNgayCheckOut(phieu.getCheckOutDate());
            response.setSoLuongKhach(phieu.getSoLuongKhach());
            response.setGhiChu(phieu.getGhiChu());
            response.setTrangThai(phieu.getStatus_phieu_dat_phong());
            response.setNgayTao(phieu.getCreatedDate());

            // Khách hàng
            if (phieu.getKhachHang() != null) {
                var khInfo = PhieuDatPhongDetailResponse.KhachHangInfo.builder()
                        .id(phieu.getKhachHang().getId())
                        .hoTen(phieu.getKhachHang().getHoTen())
                        .soDienThoai(phieu.getKhachHang().getSoDienThoai())
                        .email(phieu.getKhachHang().getEmail())
                        .build();
                response.setKhachHang(khInfo);
            }

            // Nhân viên tạo
            if (phieu.getNhanVienTao() != null) {
                response.setTenNhanVienTao(phieu.getNhanVienTao().getTen());
            }

            // Danh sách loại phòng đã chọn
            List<PhieuDatPhongDetailResponse.LoaiPhongDatInfo> loaiPhongList = new ArrayList<>();
            if (phieu.getChiTietLoaiPhong() != null) {
                for (ChiTietLoaiPhongDat ct : phieu.getChiTietLoaiPhong()) {
                    // Đếm số phòng đã gắn của loại này
                    int soLuongDaGan = 0;
                    if (phieu.getBookingDetails() != null) {
                        soLuongDaGan = (int) phieu.getBookingDetails().stream()
                                .filter(bd -> bd.getRoom() != null &&
                                        bd.getRoom().getLoaiPhong().getId().equals(ct.getLoaiPhong().getId()))
                                .count();
                    }

                    var lpInfo = PhieuDatPhongDetailResponse.LoaiPhongDatInfo.builder()
                            .idLoaiPhong(ct.getLoaiPhong().getId())
                            .tenLoaiPhong(ct.getLoaiPhong().getTen())
                            .soLuong(ct.getSoLuong())
                            .soLuongDaGan(soLuongDaGan)
                            .gia(ct.getGiaDatTruoc())
                            .build();
                    loaiPhongList.add(lpInfo);
                }
            }
            response.setDanhSachLoaiPhong(loaiPhongList);

            // Danh sách phòng đã gắn
            List<PhieuDatPhongDetailResponse.PhongDaGanInfo> phongDaGanList = new ArrayList<>();
            if (phieu.getBookingDetails() != null) {
                for (ChiTietDatPhong bd : phieu.getBookingDetails()) {
                    if (bd.getRoom() != null) {
                        var phongInfo = PhieuDatPhongDetailResponse.PhongDaGanInfo.builder()
                                .idPhong(bd.getRoom().getId())
                                .maPhong(bd.getRoom().getMa())
                                .tenPhong(bd.getRoom().getTen())
                                .tenLoaiPhong(bd.getRoom().getLoaiPhong().getTen())
                                .tang(bd.getRoom().getTang())
                                .gia(bd.getPrice())
                                .build();
                        phongDaGanList.add(phongInfo);
                    }
                }
            }
            response.setDanhSachPhongDaGan(phongDaGanList);

            // Tính tổng tiền
            if (!phongDaGanList.isEmpty()) {
                BigDecimal tongTien = phongDaGanList.stream()
                        .map(PhieuDatPhongDetailResponse.PhongDaGanInfo::getGia)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTongTien(tongTien);
            } else {
                BigDecimal tongTien = loaiPhongList.stream()
                        .map(lp -> lp.getGia().multiply(BigDecimal.valueOf(lp.getSoLuong())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                response.setTongTien(tongTien);
            }

            return ResponseObject.successForward(response, "Lấy chi tiết phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error getting chi tiet phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi lấy chi tiết: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseObject<?> ganKhachHang(GanKhachHangRequest request) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findById(request.getIdPhieuDat())
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            if (phieu.getStatus_phieu_dat_phong() != StatusPhieuDatPhong.PENDING) {
                return ResponseObject.errorForward("Chỉ có thể gắn khách hàng khi phiếu ở trạng thái PENDING",
                        HttpStatus.BAD_REQUEST);
            }

            KhachHang khachHang = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

            phieu.setKhachHang(khachHang);
            phieuDatPhongRepository.save(phieu);

            return ResponseObject.successForward(null, "Gắn khách hàng thành công");

        } catch (Exception e) {
            log.error("Error gan khach hang: ", e);
            return ResponseObject.errorForward("Lỗi khi gắn khách hàng: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseObject<?> getPhongKhaDung(String idPhieuDat, String idLoaiPhong) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findById(idPhieuDat)
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            List<Phong> phongTrong = adDatPhongRepository.findPhongTrongByLoaiPhong(
                    idLoaiPhong,
                    phieu.getCheckInDate(),
                    phieu.getCheckOutDate(),
                    TrangThaiHoatDong.DANG_HOAT_DONG);

            List<PhongKhaDungResponse> responses = phongTrong.stream().map(phong -> {
                PhongKhaDungResponse response = new PhongKhaDungResponse();
                response.setId(phong.getId());
                response.setMaPhong(phong.getMa());
                response.setTenPhong(phong.getTen());
                response.setTang(phong.getTang());
                response.setGia(phong.getLoaiPhong().getGiaCaNgay());

                // Tags
                List<PhongTag> tags = adPhongTagRepository.findByPhongId(phong.getId());
                List<PhongKhaDungResponse.TagInfo> tagInfos = tags.stream()
                        .filter(phongTag -> phongTag.getTag() != null)
                        .map(phongTag -> PhongKhaDungResponse.TagInfo.builder()
                                .id(phongTag.getTag().getId())
                                .ma(phongTag.getTag().getMa())
                                .ten(phongTag.getTag().getTen())
                                .mau(phongTag.getTag().getMau())
                                .build())
                        .collect(Collectors.toList());
                response.setTags(tagInfos);

                return response;
            }).collect(Collectors.toList());

            return ResponseObject.successForward(responses, "Lấy danh sách phòng khả dụng thành công");

        } catch (Exception e) {
            log.error("Error getting phong kha dung: ", e);
            return ResponseObject.errorForward("Lỗi: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseObject<?> ganPhong(GanPhongRequest request) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findByIdWithDetails(request.getIdPhieuDat())
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            if (phieu.getStatus_phieu_dat_phong() != StatusPhieuDatPhong.PENDING) {
                return ResponseObject.errorForward("Chỉ có thể gắn phòng khi phiếu ở trạng thái PENDING",
                        HttpStatus.BAD_REQUEST);
            }

            // Validate số lượng phòng gắn = tổng số lượng đã đặt
            int tongSoLuongDat = phieu.getChiTietLoaiPhong().stream()
                    .mapToInt(ChiTietLoaiPhongDat::getSoLuong)
                    .sum();

            if (request.getDanhSachIdPhong().size() != tongSoLuongDat) {
                return ResponseObject.errorForward(
                        "Số lượng phòng gắn (" + request.getDanhSachIdPhong().size() +
                                ") không khớp với số lượng đã đặt (" + tongSoLuongDat + ")",
                        HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra phòng trống và tạo ChiTietDatPhong
            for (String idPhong : request.getDanhSachIdPhong()) {
                Phong phong = phongRepository.findById(idPhong)
                        .orElseThrow(() -> new RuntimeException("Phòng không tồn tại: " + idPhong));

                // Check phòng có trống trong khoảng thời gian không
                boolean isBooked = adDatPhongRepository.isRoomBookedInPeriod(
                        idPhong,
                        phieu.getCheckInDate(),
                        phieu.getCheckOutDate());

                if (isBooked) {
                    return ResponseObject.errorForward(
                            "Phòng " + phong.getMa() + " đã được đặt trong thời gian này",
                            HttpStatus.CONFLICT);
                }

                // Tạo ChiTietDatPhong
                ChiTietDatPhong chiTiet = new ChiTietDatPhong();
                chiTiet.setPhieuDatPhong(phieu);
                chiTiet.setRoom(phong);
                chiTiet.setPrice(phong.getLoaiPhong().getGiaCaNgay());
                chiTiet.setStatus_chi_tiet(StatusChiTietDatPhong.BOOKED);
                chiTietDatPhongRepository.save(chiTiet);
            }

            return ResponseObject.successForward(null, "Gắn phòng thành công");

        } catch (Exception e) {
            log.error("Error gan phong: ", e);
            return ResponseObject.errorForward("Lỗi khi gắn phòng: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseObject<?> xacNhanPhieuDat(String idPhieuDat) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findByIdWithDetails(idPhieuDat)
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            if (phieu.getStatus_phieu_dat_phong() != StatusPhieuDatPhong.PENDING) {
                return ResponseObject.errorForward("Chỉ có thể xác nhận phiếu PENDING", HttpStatus.BAD_REQUEST);
            }

            // Validate đã gắn khách hàng
            if (phieu.getKhachHang() == null) {
                return ResponseObject.errorForward("Vui lòng gắn khách hàng trước khi xác nhận",
                        HttpStatus.BAD_REQUEST);
            }

            // Validate đã gắn đủ số lượng phòng
            int tongSoLuongDat = phieu.getChiTietLoaiPhong().stream()
                    .mapToInt(ChiTietLoaiPhongDat::getSoLuong)
                    .sum();

            int soPhongDaGan = phieu.getBookingDetails() != null ? phieu.getBookingDetails().size() : 0;

            if (soPhongDaGan < tongSoLuongDat) {
                return ResponseObject.errorForward(
                        "Chưa gắn đủ số lượng phòng (" + soPhongDaGan + "/" + tongSoLuongDat + ")",
                        HttpStatus.BAD_REQUEST);
            }

            // Chuyển trạng thái CONFIRMED
            phieu.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CONFIRMED);
            phieuDatPhongRepository.save(phieu);

            return ResponseObject.successForward(null, "Xác nhận phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error xac nhan phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi xác nhận: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseObject<?> huyPhieuDat(String idPhieuDat) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findById(idPhieuDat)
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            if (phieu.getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CANCELLED) {
                return ResponseObject.errorForward("Phiếu đã bị hủy trước đó", HttpStatus.BAD_REQUEST);
            }

            if (phieu.getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKIN ||
                    phieu.getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKOUT) {
                return ResponseObject.errorForward("Không thể hủy phiếu đã check-in hoặc check-out",
                        HttpStatus.BAD_REQUEST);
            }

            phieu.setStatus_phieu_dat_phong(StatusPhieuDatPhong.CANCELLED);

            // Cập nhật trạng thái chi tiết đặt phòng
            if (phieu.getBookingDetails() != null) {
                for (ChiTietDatPhong chiTiet : phieu.getBookingDetails()) {
                    chiTiet.setStatus_chi_tiet(StatusChiTietDatPhong.CANCELLED);
                }
            }

            phieuDatPhongRepository.save(phieu);

            return ResponseObject.successForward(null, "Hủy phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error huy phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi hủy phiếu: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ResponseObject<?> capNhatPhieuDat(String idPhieuDat, TaoPhieuDatRequest request) {
        try {
            PhieuDatPhong phieu = phieuDatPhongRepository.findByIdWithDetails(idPhieuDat)
                    .orElseThrow(() -> new RuntimeException("Phiếu đặt không tồn tại"));

            if (phieu.getStatus_phieu_dat_phong() != StatusPhieuDatPhong.PENDING) {
                return ResponseObject.errorForward("Chỉ có thể cập nhật phiếu PENDING", HttpStatus.BAD_REQUEST);
            }

            // Validation
            if (request.getNgayCheckOut() <= request.getNgayCheckIn()) {
                return ResponseObject.errorForward("Ngày check-out phải sau ngày check-in", HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra phòng đã gắn có available trong thời gian mới không
            if (phieu.getBookingDetails() != null && !phieu.getBookingDetails().isEmpty()) {
                for (ChiTietDatPhong chiTiet : phieu.getBookingDetails()) {
                    if (chiTiet.getRoom() != null) {
                        boolean isBooked = adDatPhongRepository.isRoomBookedInPeriod(
                                chiTiet.getRoom().getId(),
                                request.getNgayCheckIn(),
                                request.getNgayCheckOut());
                        if (isBooked) {
                            return ResponseObject.errorForward(
                                    "Phòng " + chiTiet.getRoom().getMa() + " không available trong thời gian mới",
                                    HttpStatus.CONFLICT);
                        }
                    }
                }
            }

            // Cập nhật thông tin phiếu
            phieu.setCheckInDate(request.getNgayCheckIn());
            phieu.setCheckOutDate(request.getNgayCheckOut());
            phieu.setSoLuongKhach(request.getSoLuongKhach());
            phieu.setGhiChu(request.getGhiChu());

            // Xóa chi tiết loại phòng cũ và tạo mới
            chiTietLoaiPhongDatRepository.deleteByPhieuDatPhongId(idPhieuDat);

            for (var chonLoai : request.getDanhSachLoaiPhong()) {
                LoaiPhong loaiPhong = loaiPhongRepository.findById(chonLoai.getIdLoaiPhong())
                        .orElseThrow(() -> new RuntimeException("Loại phòng không tồn tại"));

                ChiTietLoaiPhongDat chiTiet = new ChiTietLoaiPhongDat();
                chiTiet.setPhieuDatPhong(phieu);
                chiTiet.setLoaiPhong(loaiPhong);
                chiTiet.setSoLuong(chonLoai.getSoLuong());
                chiTiet.setGiaDatTruoc(loaiPhong.getGiaCaNgay());
                chiTietLoaiPhongDatRepository.save(chiTiet);
            }

            phieuDatPhongRepository.save(phieu);

            return ResponseObject.successForward(null, "Cập nhật phiếu đặt thành công");

        } catch (Exception e) {
            log.error("Error cap nhat phieu dat: ", e);
            return ResponseObject.errorForward("Lỗi khi cập nhật: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
