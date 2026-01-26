package com.be.server.core.admin.doanluutru.service.impl;

import com.be.server.core.admin.datphong.booking.repository.ADDatPhongRepository;

import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.AddGuestDuringStayRequest;
import com.be.server.core.admin.doanluutru.modal.request.AssignRoomRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.PaymentRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.modal.response.CheckSoLuongToiDaResponse;
import com.be.server.core.admin.doanluutru.modal.response.CostBreakdownResponse;
import com.be.server.core.admin.doanluutru.modal.response.InvoiceResponse;
import com.be.server.core.admin.doanluutru.modal.response.PaymentDetail;
import com.be.server.core.admin.doanluutru.modal.response.PaymentStatusResponse;
import com.be.server.core.admin.doanluutru.modal.response.RoomCostDetail;
import com.be.server.core.admin.doanluutru.modal.response.ServiceCostDetail;
import com.be.server.core.admin.doanluutru.repository.ChiTietDoanExtendRepository;
import com.be.server.core.admin.doanluutru.repository.DoanLuuTruExtendRepository;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChiTietDoan;

import com.be.server.entity.DichVuPhatSinh;
import com.be.server.entity.DoanLuuTru;
import com.be.server.entity.GuestLog;
import com.be.server.entity.KhachHang;
import com.be.server.entity.LoaiPhong;
import com.be.server.entity.PhieuDatPhong;
import com.be.server.entity.Phong;
import com.be.server.entity.ThanhToan;
import com.be.server.repository.DichVuPhatSinhRepository;
import com.be.server.repository.GuestLogRepository;
import com.be.server.repository.ThanhToanRepository;
import com.be.server.infrastructure.constant.DoanLuuTruStatus;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityTrangThaiChiTietDoan;
import com.be.server.infrastructure.constant.EntityVaiTroDoan;
import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;

import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdDoanLuuTruServiceImpl implements AdDoanLuuTruService {
    private final DoanLuuTruExtendRepository doanLuuTruRepository;
    private final ChiTietDoanExtendRepository chiTietDoanExtendRepository;
    private final ADKhachHangRepository adKhachHangRepository;
    private final ADDatPhongRepository adDatPhongRepository;
    private final ADPhongRepository adPhongRepository;
    private final DichVuPhatSinhRepository dichVuPhatSinhRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final GuestLogRepository guestLogRepository;

    @Override
    public ResponseObject<?> createDoan(CreateDoanRequest request) {
        DoanLuuTru doan = new DoanLuuTru();
        doan.setMaDoan("G_" + System.currentTimeMillis());
        doan.setGhiChu(request.getGhiChu());
        doan.setTrangThai(DoanLuuTruStatus.CHUA_CHECK_IN);
        Optional<PhieuDatPhong> optionalDatPhong = adDatPhongRepository.findById(request.getIdDatPhong());
        if (!optionalDatPhong.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy đặt phòng");
        }
        if (doanLuuTruRepository.existsByPhieuDatPhong_Id(request.getIdDatPhong())) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Mỗi phòng đã được đặt chỉ tạo được 1 đoàn lưu trú");
        }
        PhieuDatPhong phieuDatPhong = optionalDatPhong.get();
        doan.setPhieuDatPhong(phieuDatPhong);
        if (request.getSoGiayToTruongDoan() != null && !request.getSoGiayToTruongDoan().isEmpty() && request.getLoaiGiayToTruongDoan() != null) {
            Optional<KhachHang> existing = adKhachHangRepository.findByLoaiGiayToAndSoGiayTo(request.getLoaiGiayToTruongDoan() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU, request.getSoGiayToTruongDoan());
            KhachHang leader;
            if (existing.isPresent()) {
                leader = existing.get();
            } else {
                leader = new KhachHang();
                leader.setHoTen(request.getHoTenTruongDoan());
                leader.setSoDienThoai(request.getSoDienThoaiTruongDoan());
                leader.setNgaySinh(request.getNgaySinhTruongDoan());
                leader.setGioiTinh(request.getGioiTinhTruongDoan() == 0 ? GioiTinh.NAM : request.getGioiTinhTruongDoan() == 1 ? GioiTinh.NU : GioiTinh.KHAC);

                leader.setLoaiGiayTo(request.getLoaiGiayToTruongDoan() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU);
                leader.setSoGiayTo(request.getSoGiayToTruongDoan());
                leader.setStatus(EntityStatus.ACTIVE);
                leader = adKhachHangRepository.save(leader);
            }
            ChiTietDoan chiTietDoan = new ChiTietDoan();
            if (request.getTenDoan() != null && !request.getTenDoan().isBlank()) {
                doan.setTenDoan(request.getTenDoan());
            } else {
                String name = "Đoàn " + leader.getHoTen();
                doan.setTenDoan(name);
            }

            doanLuuTruRepository.save(doan);
            chiTietDoan.setDoanLuuTru(doan);
            chiTietDoan.setKhachHang(leader);
            chiTietDoan.setVaiTro(EntityVaiTroDoan.TRUONG_DOAN);
            chiTietDoan.setTrangThaiChiTietDoan(EntityTrangThaiChiTietDoan.BOOKED);
            chiTietDoanExtendRepository.save(chiTietDoan);

        } else {
            doan.setTenDoan("Đoàn " + doan.getMaDoan());
        }
        return new ResponseObject<>(null, HttpStatus.OK, "Tạo đoàn thành công");

    }

    //    @Override
//    public ResponseObject<?> addMember(AddMemberRequest request) {
//
//        Optional<DoanLuuTru> optionalDoanLuuTru = doanLuuTruRepository.findById(request.getIdDoanLuuTru());
//        if (optionalDoanLuuTru.isPresent()) {
//            DoanLuuTru doan=optionalDoanLuuTru.get();
//            KhachHang kh;
//            Optional<KhachHang> optionalKhachHang = adKhachHangRepository.findByLoaiGiayToAndSoGiayTo(request.getLoaiGiayTo() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU, request.getSoGiayTo());
//            if (optionalKhachHang.isPresent()) {
//                kh = optionalKhachHang.get();
//                if (!Boolean.TRUE.equals(request.getConfirmUseOld())) {
//                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Xác nhận");
//                }
//                if(chiTietDoanExtendRepository.existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(request.getLoaiGiayTo() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU, request.getSoGiayTo(),doan.getId())){
//                  return new ResponseObject<>(null, HttpStatus.CONFLICT, "Khách hàng đã tồn tại trong đoàn ");
//                }
//                ChiTietDoan ctd = new ChiTietDoan();
//                ctd.setDoanLuuTru(doan);
//                ctd.setKhachHang(kh);
//                ctd.setVaiTro(request.getVaiTro());
//
//                chiTietDoanExtendRepository.save(ctd);
//                return new ResponseObject<>(null, HttpStatus.OK, "Thêm khách hàng vào đoàn thành công");
//            } else {
//                // Create new guest
//                if(chiTietDoanExtendRepository.existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(request.getLoaiGiayTo() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU, request.getSoGiayTo(),doan.getId())){
//                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Khách hàng đã tồn tại trong đoàn ");
//                }
//                kh = new KhachHang();
//                kh.setHoTen(request.getHoTen());
//
//                String email = request.getEmail();
//                if (email != null && !email.contains("@")) {
//                    email += "@gmail.com";
//                }
//                kh.setEmail(email);
//
//                kh.setSoDienThoai(request.getSoDienThoai());
//                kh.setNgaySinh(request.getNgaySinh());
//                kh.setGioiTinh(request.getGioiTinh() == 0 ? GioiTinh.NAM : request.getGioiTinh() == 1 ? GioiTinh.NU : GioiTinh.KHAC);
//                kh.setLoaiGiayTo(request.getLoaiGiayTo() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU);
//                kh.setSoGiayTo(request.getSoGiayTo());
//                kh.setDiaChi(request.getDiaChi());
//                kh.setQuocTich(request.getQuocTich());
//                // Map other fields from KhachHang if provided in User request...
//                kh = adKhachHangRepository.save(kh);
//                ChiTietDoan ctd = new ChiTietDoan();
//                ctd.setDoanLuuTru(doan);
//                ctd.setKhachHang(kh);
//                ctd.setVaiTro(request.getVaiTro());
//
//                chiTietDoanExtendRepository.save(ctd);
//                return new ResponseObject<>(null, HttpStatus.OK, "Thêm khách hàng vào đoàn thành công");
//            }
//
//        } else {
//            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn không tồn tại");
//        }
//    }
    @Override
    public ResponseObject<?> addMember(AddMemberRequest request) {

        DoanLuuTru doan = doanLuuTruRepository
                .findById(request.getIdDoanLuuTru())
                .orElse(null);

        if (doan == null) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn không tồn tại");
        }

        LoaiGiayTo loaiGiayTo = request.getLoaiGiayTo() == 0
                ? LoaiGiayTo.CCCD
                : LoaiGiayTo.HO_CHIEU;

        Optional<KhachHang> optionalKhachHang =
                adKhachHangRepository.findByLoaiGiayToAndSoGiayTo(
                        loaiGiayTo, request.getSoGiayTo()
                );


        if (optionalKhachHang.isPresent()) {
            if (chiTietDoanExtendRepository
                    .existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(
                            loaiGiayTo, request.getSoGiayTo(), doan.getId()
                    )) {
                return new ResponseObject<>(
                        null, HttpStatus.BAD_REQUEST,
                        "Khách hàng đã tồn tại trong đoàn"
                );
            }
            if (!Boolean.TRUE.equals(request.getConfirmUseOld())) {
                return new ResponseObject<>(
                        optionalKhachHang.get(),
                        HttpStatus.CONFLICT,
                        "Khách đã tồn tại trong hệ thống"
                );
            }
            ChiTietDoan ctd = new ChiTietDoan();
            ctd.setDoanLuuTru(doan);
            ctd.setKhachHang(optionalKhachHang.get());
            ctd.setVaiTro(EntityVaiTroDoan.THANH_VIEN);
            ctd.setTrangThaiChiTietDoan(EntityTrangThaiChiTietDoan.BOOKED);
            chiTietDoanExtendRepository.save(ctd);

            return new ResponseObject<>(
                    null, HttpStatus.OK,
                    "Thêm khách hàng vào đoàn thành công"
            );
        }
        KhachHang kh = new KhachHang();
        kh.setHoTen(request.getHoTen());
        kh.setEmail(request.getEmail());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setGioiTinh(
                request.getGioiTinh() == 0 ? GioiTinh.NAM :
                        request.getGioiTinh() == 1 ? GioiTinh.NU : GioiTinh.KHAC
        );
        kh.setLoaiGiayTo(loaiGiayTo);
        kh.setSoGiayTo(request.getSoGiayTo());
        kh.setDiaChi(request.getDiaChi());
        kh.setQuocTich(request.getQuocTich());
        kh.setStatus(EntityStatus.ACTIVE);
        kh = adKhachHangRepository.save(kh);
        ChiTietDoan ctd = new ChiTietDoan();
        ctd.setDoanLuuTru(doan);
        ctd.setKhachHang(kh);
        ctd.setVaiTro(EntityVaiTroDoan.THANH_VIEN);
        chiTietDoanExtendRepository.save(ctd);
        return new ResponseObject<>(
                null, HttpStatus.OK,
                "Tạo mới và thêm khách hàng vào đoàn thành công"
        );
    }

    @Override
    public ResponseObject<?> getMembers(String idDoanLuuTru, SearchMemberRequest request) {
        if (request.getPage() <= 0) {
            request.setPage(1);
        }
        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(
                PageableObject.of(chiTietDoanExtendRepository.getAllChiTietDoan(idDoanLuuTru, request, pageable)),
                HttpStatus.OK,
                "Lấy thành công danh sach chi tiet doan"
        );
    }


    @Override
    public DoanLuuTru getDoanByBooking(String idDatPhong) {
        return doanLuuTruRepository.findByPhieuDatPhong_Id(idDatPhong).orElse(null);
    }

    @Override
    public ResponseObject<?> getAllDoan(FindDoanRequest request) {
        if (request.getPage() <= 0) {
            request.setPage(1);
        }

        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(
                PageableObject.of(doanLuuTruRepository.findByFilter(request, pageable)),
                HttpStatus.OK,
                "Lấy danh sách đoàn thành công"
        );
    }

    @Override
    public ResponseObject<?> getAllBookedTheoDoan(String idDoan) {
        return ResponseObject.successForward(
                doanLuuTruRepository.getDataComboboxDatPhongTheoDoan(idDoan),
                "SUCCESS"
        );
    }

    @Override
    public ResponseObject<?> assignRoom(String idChiTietDoan, AssignRoomRequest request) {
        Optional<ChiTietDoan> chiTietDoanOptional = chiTietDoanExtendRepository.findById(idChiTietDoan);
        if (chiTietDoanOptional.isPresent()) {
            ChiTietDoan chiTietDoan = chiTietDoanOptional.get();
            Optional<DoanLuuTru> doanLuuTruOptional = doanLuuTruRepository.findById(chiTietDoan.getDoanLuuTru().getId());
            if(!doanLuuTruOptional.isPresent()) {
                return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy đoàn");
            }
            DoanLuuTru doan = doanLuuTruOptional.get();
            if(doan.getTrangThai().equals(DoanLuuTruStatus.DANG_LUU_TRU) && chiTietDoan.getPhong()!=null){
                return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Đã check in không thể đổi phòng");
            }
            Optional<Phong> phongOptional = adPhongRepository.findById(request.getIdPhong());
            if (!phongOptional.isPresent()) {
                return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy phòng");
            }
            Phong phong = phongOptional.get();
            chiTietDoan.setPhong(phong);
            chiTietDoan.setTrangThaiChiTietDoan(EntityTrangThaiChiTietDoan.ASSIGNING);
            chiTietDoanExtendRepository.save(chiTietDoan);
            return new ResponseObject<>(null, HttpStatus.OK, "Gán khách vào phòng thành công");

        } else {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy chi tiết đoàn");
        }
    }

    @Override
    public ResponseObject<?> checkSoluongToiDaBooking(String idDoan) {
        Optional<DoanLuuTru> doanLuuTruOptional = doanLuuTruRepository.findById(idDoan);
        if (doanLuuTruOptional.isPresent()) {
            CheckSoLuongToiDaResponse check = chiTietDoanExtendRepository.checkSoLuongToiDaBooking(idDoan);
            Integer hienTai = Optional.ofNullable(check.getTongSoNguoiHienTai()).orElse(0);

            Integer toiDa = Optional.ofNullable(check.getTongSoNguoiToiDa()).orElse(0);
            log.info("Check idDoan={}, hienTai={}, toiDa={}",
                    idDoan, hienTai, toiDa
            );

            if (hienTai >= toiDa) {
                return new ResponseObject<>(
                        null,
                        HttpStatus.BAD_REQUEST,
                        "Số lượng khách đã vượt quá số lượng tối đa của phòng đã đặt"
                );
            }
            return new ResponseObject<>(null, HttpStatus.OK, "CheckOk");
        } else {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn k tồn tại");
        }
    }

    @Override
    public ResponseObject<?> checkInDoanLuuTru(String idDoan) {
        Optional<DoanLuuTru> doanLuuTruOptional = doanLuuTruRepository.findById(idDoan);
        if (doanLuuTruOptional.isPresent()) {
            DoanLuuTru doanLuuTru = doanLuuTruOptional.get();
            if (doanLuuTru.getTrangThai() == DoanLuuTruStatus.DANG_LUU_TRU) {
                return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Đoàn đã check-in rồi");
            }

            if (chiTietDoanExtendRepository.existsByDoanLuuTru_IdAndPhong_IdIsNull(idDoan)) {
                return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Có khách chưa được gán vào phòng");
            }
            List<ChiTietDoan> chiTietDoanList = chiTietDoanExtendRepository.findByDoanLuuTru_Id(idDoan);

            doanLuuTru.setTrangThai(DoanLuuTruStatus.DANG_LUU_TRU);
            doanLuuTru.setThoiGianCheckIn(Calendar.getInstance().getTimeInMillis());
            List<String> phongIds = chiTietDoanList.stream()
                    .map(ChiTietDoan::getPhong)   // lấy object Phong
                    .filter(Objects::nonNull)     // an toàn
                    .map(Phong::getId)             // lấy id
                    .distinct()
                    .toList();

            adPhongRepository.updateTrangThaiPhongKhiCheckIn(
                    phongIds,
                    TrangThaiHoatDong.DANG_SU_DUNG
            );
            doanLuuTruRepository.save(doanLuuTru);
            return new ResponseObject<>(null, HttpStatus.OK, "Đoàn đã check in thành công");
        } else {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn k tồn tại");
        }

    }

    @Override
    public ResponseObject<?> addGuestDuringStay(AddGuestDuringStayRequest request) {
        // 1. Validate DoanLuuTru exists and status = DANG_LUU_TRU
        Optional<DoanLuuTru> doanOptional = doanLuuTruRepository.findById(request.getIdDoanLuuTru());
        if (!doanOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn lưu trú không tồn tại");
        }

        DoanLuuTru doan = doanOptional.get();
        if (doan.getTrangThai() != DoanLuuTruStatus.DANG_LUU_TRU) {
            return new ResponseObject<>(
                    null, 
                    HttpStatus.BAD_REQUEST, 
                    "Chỉ có thể thêm khách khi đoàn đang lưu trú. Trạng thái hiện tại: " + doan.getTrangThai().getDescription()
            );
        }

        // 2. Validate room exists
        Optional<Phong> phongOptional = adPhongRepository.findById(request.getIdPhong());
        if (!phongOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phòng không tồn tại");
        }
        Phong phong = phongOptional.get();

        // 3. Check room capacity
        Integer soNguoiToiDa = phong.getLoaiPhong().getSoNguoiToiDa();
        Long soNguoiHienTai = chiTietDoanExtendRepository.countByDoanLuuTru_IdAndPhong_Id(
                request.getIdDoanLuuTru(), 
                request.getIdPhong()
        );

        if (soNguoiHienTai >= soNguoiToiDa) {
            return new ResponseObject<>(
                    null, 
                    HttpStatus.BAD_REQUEST, 
                    String.format("Phòng %s đã đầy. Sức chứa: %d/%d người", 
                            phong.getTen(), soNguoiHienTai, soNguoiToiDa)
            );
        }

        // 4. Process guest (create new or use existing)
        LoaiGiayTo loaiGiayTo = request.getLoaiGiayTo() == 0 ? LoaiGiayTo.CCCD : LoaiGiayTo.HO_CHIEU;
        Optional<KhachHang> existingCustomer = adKhachHangRepository.findByLoaiGiayToAndSoGiayTo(
                loaiGiayTo, 
                request.getSoGiayTo()
        );

        KhachHang khachHang;
        
        if (existingCustomer.isPresent()) {
            // Check if guest already in this group
            if (chiTietDoanExtendRepository.existsByKhachHang_LoaiGiayToAndKhachHang_SoGiayToAndDoanLuuTru_Id(
                    loaiGiayTo, request.getSoGiayTo(), doan.getId())) {
                return new ResponseObject<>(
                        null, 
                        HttpStatus.BAD_REQUEST, 
                        "Khách hàng đã tồn tại trong đoàn"
                );
            }

            if (!Boolean.TRUE.equals(request.getConfirmUseOld())) {
                return new ResponseObject<>(
                        existingCustomer.get(), 
                        HttpStatus.CONFLICT, 
                        "Khách hàng đã tồn tại trong hệ thống. Xác nhận sử dụng thông tin cũ?"
                );
            }
            khachHang = existingCustomer.get();
        } else {
            // Create new customer
            khachHang = new KhachHang();
            khachHang.setHoTen(request.getHoTen());
            khachHang.setEmail(request.getEmail());
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setNgaySinh(request.getNgaySinh());
            khachHang.setGioiTinh(
                    request.getGioiTinh() == 0 ? GioiTinh.NAM :
                    request.getGioiTinh() == 1 ? GioiTinh.NU : GioiTinh.KHAC
            );
            khachHang.setLoaiGiayTo(loaiGiayTo);
            khachHang.setSoGiayTo(request.getSoGiayTo());
            khachHang.setDiaChi(request.getDiaChi());
            khachHang.setQuocTich(request.getQuocTich());
            khachHang.setStatus(EntityStatus.ACTIVE);
            khachHang = adKhachHangRepository.save(khachHang);
        }

        // 5. Create ChiTietDoan with timestamp
        Long currentTime = Calendar.getInstance().getTimeInMillis();
        ChiTietDoan chiTietDoan = new ChiTietDoan();
        chiTietDoan.setDoanLuuTru(doan);
        chiTietDoan.setKhachHang(khachHang);
        chiTietDoan.setPhong(phong);
        chiTietDoan.setVaiTro(EntityVaiTroDoan.THANH_VIEN);
        chiTietDoan.setTrangThaiChiTietDoan(EntityTrangThaiChiTietDoan.ASSIGNING);
        chiTietDoan.setThoiGianThem(currentTime);
        chiTietDoan.setGhiChu(request.getGhiChu());
        chiTietDoanExtendRepository.save(chiTietDoan);

        // 6. Create audit log
        GuestLog guestLog = new GuestLog();
        guestLog.setDoanLuuTru(doan);
        guestLog.setKhachHang(khachHang);
        guestLog.setPhong(phong);
        guestLog.setHanhDong("THEM_KHACH");
        guestLog.setThoiGianThem(currentTime);
        guestLog.setGhiChu(request.getGhiChu());
        guestLogRepository.save(guestLog);

        log.info("Added guest {} to room {} in group {} at {}", 
                khachHang.getHoTen(), phong.getTen(), doan.getMaDoan(), currentTime);

        return new ResponseObject<>(
                null, 
                HttpStatus.OK, 
                String.format("Thêm khách %s vào phòng %s thành công", khachHang.getHoTen(), phong.getTen())
        );
    }

    @Override
    public ResponseObject<CostBreakdownResponse> calculateTotalCost(String idDoan) {
        // 1. Validate đoàn tồn tại
        Optional<DoanLuuTru> doanOptional = doanLuuTruRepository.findById(idDoan);
        if (!doanOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn lưu trú không tồn tại");
        }

        DoanLuuTru doan = doanOptional.get();
        PhieuDatPhong phieuDatPhong = doan.getPhieuDatPhong();

        // 2. Tính số ngày lưu trú
        Long checkInDate = doan.getThoiGianCheckIn();
        Long checkOutDate = doan.getThoiGianCheckOut();
        
        // Nếu chưa check out, dùng thời gian hiện tại
        if (checkOutDate == null) {
            checkOutDate = Calendar.getInstance().getTimeInMillis();
        }
        
        // Tính số đêm
        long soNgayLuuTru = (checkOutDate - checkInDate) / (1000 * 60 * 60 * 24);
        if (soNgayLuuTru == 0) {
            soNgayLuuTru = 1; // Tối thiểu 1 đêm
        }

        // 3. Tính chi phí phòng
        List<RoomCostDetail> roomCosts = new ArrayList<>();
        BigDecimal tongTienPhong = BigDecimal.ZERO;

        List<ChiTietDoan> chiTietDoanList = chiTietDoanExtendRepository.findByDoanLuuTru_Id(idDoan);
        
        // Nhóm theo phòng để tránh tính trùng
        Map<String, Phong> uniqueRooms = new HashMap<>();
        for (ChiTietDoan ctd : chiTietDoanList) {
            if (ctd.getPhong() != null) {
                uniqueRooms.put(ctd.getPhong().getId(), ctd.getPhong());
            }
        }

        for (Phong phong : uniqueRooms.values()) {
            LoaiPhong loaiPhong = phong.getLoaiPhong();
            BigDecimal giaPhong = loaiPhong.getGiaCaNgay();
            BigDecimal thanhTien = giaPhong.multiply(BigDecimal.valueOf(soNgayLuuTru));

            RoomCostDetail detail = new RoomCostDetail();
            detail.setIdPhong(phong.getId());
            detail.setTenPhong(phong.getTen());
            detail.setMaPhong(phong.getTen()); // Assuming ma is same as ten
            detail.setTenLoaiPhong(loaiPhong.getTen());
            detail.setGiaPhong(giaPhong);
            detail.setSoNgay(soNgayLuuTru);
            detail.setThanhTien(thanhTien);

            roomCosts.add(detail);
            tongTienPhong = tongTienPhong.add(thanhTien);
        }

        // 4. Tính chi phí dịch vụ
        List<ServiceCostDetail> serviceCosts = new ArrayList<>();
        BigDecimal tongTienDichVu = BigDecimal.ZERO;

        if (phieuDatPhong != null) {
            List<DichVuPhatSinh> dichVuList = dichVuPhatSinhRepository
                    .findByPhieuDatPhong_Id(phieuDatPhong.getId());

            for (DichVuPhatSinh dv : dichVuList) {
                ServiceCostDetail detail = new ServiceCostDetail();
                detail.setIdDichVu(dv.getId());
                detail.setTenDichVu(dv.getTenDichVu());
                detail.setSoLuong(dv.getSoLuong());
                detail.setDonGia(dv.getDonGia());
                detail.setThanhTien(dv.getThanhTien());
                detail.setThoiGianTao(dv.getCreatedDate());
                
                // Lấy tên phòng nếu dịch vụ áp dụng cho phòng cụ thể
                if (dv.getChiTietDatPhong() != null && dv.getChiTietDatPhong().getRoom() != null) {
                    detail.setPhongApDung(dv.getChiTietDatPhong().getRoom().getTen());
                }

                serviceCosts.add(detail);
                tongTienDichVu = tongTienDichVu.add(dv.getThanhTien());
            }
        }

        // 5. Tổng cộng
        BigDecimal tongCong = tongTienPhong.add(tongTienDichVu);

        // 6. Tạo response
        CostBreakdownResponse response = new CostBreakdownResponse();
        response.setIdDoan(doan.getId());
        response.setMaDoan(doan.getMaDoan());
        response.setTenDoan(doan.getTenDoan());
        response.setNgayCheckIn(checkInDate);
        response.setNgayCheckOut(checkOutDate);
        response.setSoNgayLuuTru(soNgayLuuTru);
        response.setRoomCosts(roomCosts);
        response.setServiceCosts(serviceCosts);
        response.setTongTienPhong(tongTienPhong);
        response.setTongTienDichVu(tongTienDichVu);
        response.setTongCong(tongCong);

        return new ResponseObject<>(response, HttpStatus.OK, "Tính toán chi phí thành công");
    }

    @Override
    public ResponseObject<InvoiceResponse> generateTemporaryInvoice(String idDoan) {
        // Tính toán chi phí
        ResponseObject<CostBreakdownResponse> costResult = calculateTotalCost(idDoan);
        if (costResult.getStatus() != HttpStatus.OK) {
            return new ResponseObject<>(null, costResult.getStatus(), costResult.getMessage());
        }

        CostBreakdownResponse costBreakdown = costResult.getData();
        
        // Lấy thông tin đoàn
        Optional<DoanLuuTru> doanOptional = doanLuuTruRepository.findById(idDoan);
        if (!doanOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn không tồn tại");
        }

        DoanLuuTru doan = doanOptional.get();

        // Lấy thông tin trưởng đoàn
        List<ChiTietDoan> members = chiTietDoanExtendRepository.findByDoanLuuTru_Id(idDoan);
        ChiTietDoan leader = members.stream()
                .filter(m -> m.getVaiTro() == EntityVaiTroDoan.TRUONG_DOAN)
                .findFirst()
                .orElse(members.isEmpty() ? null : members.get(0));

        // Tạo response
        InvoiceResponse invoice = new InvoiceResponse();
        invoice.setMaHoaDon("HD_TAM_" + System.currentTimeMillis());
        invoice.setThoiGianTao(Calendar.getInstance().getTimeInMillis());
        invoice.setLoaiHoaDon("TAM_TINH");
        
        invoice.setMaDoan(doan.getMaDoan());
        invoice.setTenDoan(doan.getTenDoan());
        
        if (leader != null && leader.getKhachHang() != null) {
            invoice.setTenKhachHang(leader.getKhachHang().getHoTen());
            invoice.setSoDienThoai(leader.getKhachHang().getSoDienThoai());
        }
        
        invoice.setNgayCheckIn(costBreakdown.getNgayCheckIn());
        invoice.setNgayCheckOut(costBreakdown.getNgayCheckOut());
        invoice.setSoNgayLuuTru(costBreakdown.getSoNgayLuuTru());
        
        invoice.setRoomCosts(costBreakdown.getRoomCosts());
        invoice.setServiceCosts(costBreakdown.getServiceCosts());
        
        invoice.setTongTienPhong(costBreakdown.getTongTienPhong());
        invoice.setTongTienDichVu(costBreakdown.getTongTienDichVu());
        invoice.setTongCong(costBreakdown.getTongCong());
        
        // Thông tin khách sạn (có thể lấy từ config)
        invoice.setTenKhachSan("FPL Hotel Management System");
        invoice.setDiaChiKhachSan("Hà Nội, Việt Nam");
        invoice.setSoDienThoaiKhachSan("0123456789");

        return new ResponseObject<>(invoice, HttpStatus.OK, "Tạo hóa đơn tạm tính thành công");
    }

    @Override
    public ResponseObject<?> processPayment(PaymentRequest request) {
        // 1. Validate đoàn tồn tại
        Optional<DoanLuuTru> doanOptional = doanLuuTruRepository.findById(request.getIdDoanLuuTru());
        if (!doanOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn lưu trú không tồn tại");
        }

        DoanLuuTru doan = doanOptional.get();

        // 2. Tính tổng chi phí nếu chưa có
        ResponseObject<CostBreakdownResponse> costResult = calculateTotalCost(request.getIdDoanLuuTru());
        if (costResult.getStatus() != HttpStatus.OK) {
            return new ResponseObject<>(null, costResult.getStatus(), costResult.getMessage());
        }
        BigDecimal tongChiPhi = costResult.getData().getTongCong();

        // 3. Tính số tiền đã thanh toán và còn nợ
        BigDecimal daThanhToan = doan.getTongTienThanhToan() != null ? doan.getTongTienThanhToan() : BigDecimal.ZERO;
        BigDecimal conNo = tongChiPhi.subtract(daThanhToan);

        //4. Validate số tiền thanh toán
        if (request.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Số tiền thanh toán phải lớn hơn 0");
        }

        if (request.getSoTien().compareTo(conNo) > 0) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, 
                String.format("Số tiền thanh toán (%s) vượt quá số tiền còn nợ (%s)", 
                    request.getSoTien(), conNo));
        }

        // 5. Tạo bản ghi thanh toán
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setDoanLuuTru(doan);
        thanhToan.setMaThanhToan("TT_" + System.currentTimeMillis());
        thanhToan.setSoTien(request.getSoTien());
        thanhToan.setPhuongThuc(request.getPhuongThuc());
        thanhToan.setThoiGianThanhToan(Calendar.getInstance().getTimeInMillis());
        thanhToan.setGhiChu(request.getGhiChu());
        // Note: nhanVien is null - will be set when authentication context is available
        
        thanhToanRepository.save(thanhToan);

        // 6. Cập nhật tổng tiền thanh toán và công nợ trên đoàn
        daThanhToan = daThanhToan.add(request.getSoTien());
        conNo = tongChiPhi.subtract(daThanhToan);

        doan.setTongTienThanhToan(daThanhToan);
        doan.setCongNo(conNo);

        // 7. Nếu đã thanh toán đủ, cập nhật trạng thái
        if (conNo.compareTo(BigDecimal.ZERO) == 0) {
            doan.setTrangThai(DoanLuuTruStatus.DA_CHECK_OUT);
        }

        doanLuuTruRepository.save(doan);

        log.info("Processed payment of {} for group {} using {}", 
            request.getSoTien(), doan.getMaDoan(), request.getPhuongThuc());

        return new ResponseObject<>(null, HttpStatus.OK, 
            String.format("Thanh toán thành công %s VNĐ. Còn nợ: %s VNĐ", 
                request.getSoTien(), conNo));
    }

    @Override
    public ResponseObject<PaymentStatusResponse> getPaymentStatus(String idDoan) {
        // 1. Validate đoàn tồn tại
        Optional<DoanLuuTru> doanOptional = doanLuuTruRepository.findById(idDoan);
        if (!doanOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Đoàn lưu trú không tồn tại");
        }

        DoanLuuTru doan = doanOptional.get();

        // 2. Tính tổng chi phí
        ResponseObject<CostBreakdownResponse> costResult = calculateTotalCost(idDoan);
        if (costResult.getStatus() != HttpStatus.OK) {
            return new ResponseObject<>(null, costResult.getStatus(), costResult.getMessage());
        }
        BigDecimal tongChiPhi = costResult.getData().getTongCong();

        // 3. Lấy danh sách thanh toán
        List<ThanhToan> payments = thanhToanRepository.findByDoanLuuTru_IdOrderByThoiGianThanhToanDesc(idDoan);
        List<PaymentDetail> paymentDetails = new ArrayList<>();
        
        for (ThanhToan tt : payments) {
            PaymentDetail detail = new PaymentDetail();
            detail.setId(tt.getId());
            detail.setMaThanhToan(tt.getMaThanhToan());
            detail.setSoTien(tt.getSoTien());
            detail.setPhuongThuc(tt.getPhuongThuc().name());
            detail.setThoiGianThanhToan(tt.getThoiGianThanhToan());
            if (tt.getNhanVien() != null) {
                detail.setTenNhanVien(tt.getNhanVien().getTen());
            }
            detail.setGhiChu(tt.getGhiChu());
            paymentDetails.add(detail);
        }

        // 4. Tạo response
        BigDecimal daThanhToan = doan.getTongTienThanhToan() != null ? doan.getTongTienThanhToan() : BigDecimal.ZERO;
        BigDecimal conNo = tongChiPhi.subtract(daThanhToan);

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setIdDoan(doan.getId());
        response.setMaDoan(doan.getMaDoan());
        response.setTenDoan(doan.getTenDoan());
        response.setTongChiPhi(tongChiPhi);
        response.setTongDaThanhToan(daThanhToan);
        response.setCongNo(conNo);
        response.setPayments(paymentDetails);
        response.setTrangThai(conNo.compareTo(BigDecimal.ZERO) == 0 ? "DA_THANH_TOAN" : "CON_NO");

        return new ResponseObject<>(response, HttpStatus.OK, "Lấy trạng thái thanh toán thành công");
    }
}
