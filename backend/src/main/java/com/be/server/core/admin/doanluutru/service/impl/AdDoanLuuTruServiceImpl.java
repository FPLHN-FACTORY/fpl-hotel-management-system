package com.be.server.core.admin.doanluutru.service.impl;

import com.be.server.core.admin.datphong.trangthaiphong.repository.SoDoPhongRepository;
import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.admin.doanluutru.repository.ChiTietDoanExtendRepository;
import com.be.server.core.admin.doanluutru.repository.DoanLuuTruExtendRepository;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChiTietDoan;
import com.be.server.entity.DatPhong;
import com.be.server.entity.DoanLuuTru;
import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.EntityVaiTroDoan;
import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.repository.DatPhongRepository;
import com.be.server.repository.DoanLuuTruRepository;
import com.be.server.utils.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdDoanLuuTruServiceImpl implements AdDoanLuuTruService {

    @Autowired
    private DoanLuuTruExtendRepository doanLuuTruRepository;
    @Autowired
    private ChiTietDoanExtendRepository chiTietDoanExtendRepository;
    @Autowired
    private ADKhachHangRepository adKhachHangRepository;
    @Autowired
    private DatPhongRepository datPhongRepository;

    @Override
    public ResponseObject<?> createDoan(CreateDoanRequest request) {
        DoanLuuTru doan = new DoanLuuTru();
        doan.setMaDoan("G_" + System.currentTimeMillis());
        doan.setGhiChu(request.getGhiChu());
        doan.setTrangThai(com.be.server.infrastructure.constant.DoanLuuTruStatus.CHUA_CHECK_IN.getValue());
        Optional<DatPhong> optionalDatPhong = datPhongRepository.findById(request.getIdDatPhong());
        if (!optionalDatPhong.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy đặt phòng");
        }
        if(doanLuuTruRepository.existsByDatPhong_Id(request.getIdDatPhong())){
            return new ResponseObject<>(null,HttpStatus.CONFLICT,"Mỗi phòng đã được đặt chỉ tạo được 1 đoàn lưu trú");
        }
        DatPhong datPhong = optionalDatPhong.get();
        doan.setDatPhong(datPhong);
        doan.setThoiGianCheckIn(datPhong.getThoiGianCheckIn());
        doan.setThoiGianCheckOut(datPhong.getThoiGianCheckOut());
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
        return doanLuuTruRepository.findByDatPhong_Id(idDatPhong).orElse(null);
    }

    @Override
    public ResponseObject<?> getAllDoan(FindDoanRequest request) {
        if (request.getPage() <= 0) {
            request.setPage(1);
        }

        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(
                PageableObject.of(doanLuuTruRepository.findByFilter( request,pageable)),
                HttpStatus.OK,
                "Lấy danh sách đoàn thành công"
        );
    }

    @Override
    public ResponseObject<?> getAllBooked() {
        return ResponseObject.successForward(
                datPhongRepository.getDataComboboxDatPhong(),
                "SUCCESS"
        );
    }
}
