package com.be.server.core.admin.doanluutru.service.impl;

import com.be.server.core.admin.datphong.booking.repository.ADDatPhongRepository;
import com.be.server.core.admin.datphong.trangthaiphong.repository.SoDoPhongRepository;
import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.AssignRoomRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.modal.response.CheckSoLuongToiDaResponse;
import com.be.server.core.admin.doanluutru.repository.ChiTietDoanExtendRepository;
import com.be.server.core.admin.doanluutru.repository.DoanLuuTruExtendRepository;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChiTietDoan;

import com.be.server.entity.DoanLuuTru;
import com.be.server.entity.KhachHang;
import com.be.server.entity.PhieuDatPhong;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.DoanLuuTruStatus;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.EntityTrangThaiChiTietDoan;
import com.be.server.infrastructure.constant.EntityVaiTroDoan;
import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.repository.DoanLuuTruRepository;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
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
}
