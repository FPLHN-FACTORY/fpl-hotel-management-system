package com.be.server.core.admin.doanluutru.service.impl;

import com.be.server.core.admin.doanluutru.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.service.AdDoanLuuTruService;
import com.be.server.entity.ChiTietDoan;
import com.be.server.entity.DatPhong;
import com.be.server.entity.DoanLuuTru;
import com.be.server.entity.KhachHang;
import com.be.server.repository.ChiTietDoanRepository;
import com.be.server.repository.DatPhongRepository;
import com.be.server.repository.DoanLuuTruRepository;
import com.be.server.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdDoanLuuTruServiceImpl implements AdDoanLuuTruService {

    @Autowired
    private DoanLuuTruRepository doanLuuTruRepository;
    @Autowired
    private ChiTietDoanRepository chiTietDoanRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private DatPhongRepository datPhongRepository;

    @Override
    public DoanLuuTru createDoan(CreateDoanRequest request) {
        DoanLuuTru doan = new DoanLuuTru();
        // Auto-generate Code
        doan.setMaDoan("G_" + System.currentTimeMillis());
        doan.setGhiChu(request.getGhiChu());
        doan.setSoNguoi(request.getSoNguoi());
        doan.setTrangThai(com.be.server.infrastructure.constant.DoanLuuTruStatus.CHUA_CHECK_IN.getValue());

        // Handle Leader
        if (request.getSoCccdTruongDoan() != null && !request.getSoCccdTruongDoan().isEmpty()) {
            // Try to find by CCCD first
            Optional<KhachHang> existing = khachHangRepository.findBySoCCD(request.getSoCccdTruongDoan());
            KhachHang leader;
            if (existing.isPresent()) {
                leader = existing.get();
                // Update info if needed? For now, we reuse.
            } else {
                leader = new KhachHang();
                leader.setHoTen(request.getHoTenTruongDoan());
                leader.setSoDienThoai(request.getSoDienThoaiTruongDoan());

                String email = request.getEmailTruongDoan();
                if (email != null && !email.contains("@")) {
                    email += "@gmail.com";
                }
                leader.setEmail(email);

                leader.setSoCCD(request.getSoCccdTruongDoan());
                leader = khachHangRepository.save(leader);
            }
            doan.setTruongDoan(leader);

            // Auto-generate Name if not provided or consistent with "ko cần tên"
            String name = "Đoàn " + leader.getHoTen();
            doan.setTenDoan(name);
        } else {
            // Fallback if no leader info (should be required though based on usage)
            doan.setTenDoan("Đoàn " + doan.getMaDoan());
        }

        return doanLuuTruRepository.save(doan);
    }

    @Override
    public ChiTietDoan addMember(AddMemberRequest request) {
        DoanLuuTru doan = doanLuuTruRepository.findById(request.getIdDoanLuuTru())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        KhachHang kh;
        if (request.getIdKhachHang() != null && !request.getIdKhachHang().isEmpty()) {
            kh = khachHangRepository.findById(request.getIdKhachHang())
                    .orElseThrow(() -> new RuntimeException("Guest not found"));
        } else {
            // Create new guest
            kh = new KhachHang();
            kh.setHoTen(request.getHoTen());

            String email = request.getEmail();
            if (email != null && !email.contains("@")) {
                email += "@gmail.com";
            }
            kh.setEmail(email);

            kh.setSoDienThoai(request.getSoDienThoai());
            kh.setSoCCD(request.getSoCccd());
            kh.setDiaChi(request.getDiaChi());
            kh.setQuocTich(request.getQuocTich());
            // Map other fields from KhachHang if provided in User request...
            kh = khachHangRepository.save(kh);
        }

        ChiTietDoan ctd = new ChiTietDoan();
        ctd.setDoanLuuTru(doan);
        ctd.setKhachHang(kh);
        ctd.setVaiTro(request.getVaiTro());

        return chiTietDoanRepository.save(ctd);
    }

    @Override
    public List<ChiTietDoan> getMembers(String idDoan) {
        return chiTietDoanRepository.findByDoanLuuTru_Id(idDoan);
    }

    @Override
    public DoanLuuTru getDoanByBooking(String idDatPhong) {
        return doanLuuTruRepository.findByDatPhong_Id(idDatPhong).orElse(null);
    }

    @Override
    public List<DoanLuuTru> getAllDoan(com.be.server.core.admin.doanluutru.request.FindDoanRequest request) {
        return doanLuuTruRepository.findByFilter(request);
    }

    @Override
    public List<DatPhong> getAvailableBookings() {
        // Find bookings that are BOOKED or IN_USE
        List<DatPhong> activeBookings = datPhongRepository.findAll().stream()
                .filter(dp -> {
                    com.be.server.infrastructure.constant.RoomStatus st = dp.getTrangThaiPhong();
                    return st == com.be.server.infrastructure.constant.RoomStatus.DA_DAT
                            || st == com.be.server.infrastructure.constant.RoomStatus.DANG_SU_DUNG;
                })
                .toList();

        // Filter out those that already have a DoanLuuTru
        // Fetch all existing group booking IDs for optimization
        List<String> textIds = doanLuuTruRepository.findAll().stream()
                .map(d -> d.getDatPhong().getId())
                .toList();

        return activeBookings.stream()
                .filter(dp -> !textIds.contains(dp.getId()))
                .toList();
    }
}
