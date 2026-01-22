package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADSaveDichVuPhatSinhRequest;
import com.be.server.core.admin.phong.service.ADDichVuPhatSinhService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.ChiTietDatPhong;
import com.be.server.entity.DichVuPhatSinh;
import com.be.server.entity.PhieuDatPhong;
import com.be.server.infrastructure.constant.StatusChiTietDatPhong;
import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import com.be.server.repository.ChiTietDatPhongRepository;
import com.be.server.repository.DichVuPhatSinhRepository;
import com.be.server.repository.PhieuDatPhongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ADDichVuPhatSinhServiceImpl implements ADDichVuPhatSinhService {

    private final DichVuPhatSinhRepository dichVuPhatSinhRepository;
    private final PhieuDatPhongRepository phieuDatPhongRepository;
    private final ChiTietDatPhongRepository chiTietDatPhongRepository;

    @Override
    public ResponseObject<?> saveDichVu(ADSaveDichVuPhatSinhRequest request) {
        DichVuPhatSinh dichVu = new DichVuPhatSinh();
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setSoLuong(request.getSoLuong());
        dichVu.setDonGia(request.getDonGia());
        if (request.getDonGia() != null) {
            dichVu.setThanhTien(request.getDonGia().multiply(BigDecimal.valueOf(request.getSoLuong())));
        }

        if (request.getIdPhieuDatPhong() != null) {
            Optional<PhieuDatPhong> phieuDatPhong = phieuDatPhongRepository.findById(request.getIdPhieuDatPhong());
            if (phieuDatPhong.isPresent()) {
                if (phieuDatPhong.get().getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKOUT) {
                    return ResponseObject.errorForward("Không thể thêm dịch vụ cho phiếu đã thanh toán (CHECKOUT)",
                            HttpStatus.BAD_REQUEST);
                }
                dichVu.setPhieuDatPhong(phieuDatPhong.get());
            }
        }

        if (request.getIdChiTietDatPhong() != null) {
            Optional<ChiTietDatPhong> chiTietDatPhong = chiTietDatPhongRepository
                    .findById(request.getIdChiTietDatPhong());
            if (chiTietDatPhong.isPresent()) {
                if (chiTietDatPhong.get().getStatus_chi_tiet() == StatusChiTietDatPhong.CHECKOUT) {
                    return ResponseObject.errorForward("Không thể thêm dịch vụ cho phòng đã trả (CHECKOUT)",
                            HttpStatus.BAD_REQUEST);
                }
                dichVu.setChiTietDatPhong(chiTietDatPhong.get());
            }
        }

        return ResponseObject.successForward(dichVuPhatSinhRepository.save(dichVu), "Thêm thành công");
    }

    @Override
    public ResponseObject<?> deleteDichVu(String id) {
        Optional<DichVuPhatSinh> dichVuOpt = dichVuPhatSinhRepository.findById(id);
        if (dichVuOpt.isPresent()) {
            DichVuPhatSinh dichVu = dichVuOpt.get();
            // Check status before deleting
            if (dichVu.getChiTietDatPhong() != null
                    && dichVu.getChiTietDatPhong().getStatus_chi_tiet() == StatusChiTietDatPhong.CHECKOUT) {
                return ResponseObject.errorForward("Không thể xóa dịch vụ của phòng đã trả", HttpStatus.BAD_REQUEST);
            }
            if (dichVu.getPhieuDatPhong() != null
                    && dichVu.getPhieuDatPhong().getStatus_phieu_dat_phong() == StatusPhieuDatPhong.CHECKOUT) {
                return ResponseObject.errorForward("Không thể xóa dịch vụ của phiếu đã thanh toán",
                        HttpStatus.BAD_REQUEST);
            }

            dichVuPhatSinhRepository.deleteById(id);
            return ResponseObject.successForward(null, "Xóa thành công");
        }
        return ResponseObject.errorForward("Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseObject<?> getDichVuByBooking(String idPhieuDatPhong) {
        List<DichVuPhatSinh> list = dichVuPhatSinhRepository.findByPhieuDatPhong_Id(idPhieuDatPhong);
        return ResponseObject.successForward(list, "Lấy dữ liệu thành công");
    }

    @Override
    public ResponseObject<?> getDichVuByRoomBooking(String idChiTietDatPhong) {
        List<DichVuPhatSinh> list = dichVuPhatSinhRepository.findByChiTietDatPhong_Id(idChiTietDatPhong);
        return ResponseObject.successForward(list, "Lấy dữ liệu thành công");
    }

    @Override
    public ResponseObject<?> getActiveBookingByRoom(String roomId) {
        Long now = System.currentTimeMillis();
        List<ChiTietDatPhong> bookings = chiTietDatPhongRepository.findCheckInBookingByRoomId(
            roomId, 
            now, 
            org.springframework.data.domain.PageRequest.of(0, 1)
        );
        if (!bookings.isEmpty()) {
            ChiTietDatPhong booking = bookings.get(0);
            
            // Map to DTO to avoid lazy loading issues
            com.be.server.core.admin.phong.model.response.ActiveBookingResponse response = 
                new com.be.server.core.admin.phong.model.response.ActiveBookingResponse();
            
            response.setId(booking.getId());
            response.setCheckIn(booking.getCheckIn());
            response.setCheckOut(booking.getCheckOut());
            response.setPrice(booking.getPrice());
            response.setSoLuongKhach(booking.getSoLuongKhach());
            response.setStatusChiTiet(booking.getStatus_chi_tiet());
            
            // Map PhieuDatPhong info
            if (booking.getPhieuDatPhong() != null) {
                PhieuDatPhong phieu = booking.getPhieuDatPhong();
                response.setPhieuDatPhongId(phieu.getId());
                response.setPhieuCheckInDate(phieu.getCheckInDate());
                response.setPhieuCheckOutDate(phieu.getCheckOutDate());
                response.setPhieuStatus(phieu.getStatus_phieu_dat_phong());
                
                // Map customer info
                if (phieu.getKhachHang() != null) {
                    response.setCustomerName(phieu.getKhachHang().getHoTen());
                    response.setCustomerId(phieu.getKhachHang().getId());
                }
            }
            
            // Map room info
            if (booking.getRoom() != null) {
                response.setRoomId(booking.getRoom().getId());
                response.setRoomCode(booking.getRoom().getMa());
                response.setRoomName(booking.getRoom().getTen());
            }
            
            return ResponseObject.successForward(response, "Tìm thấy booking");
        }
        return ResponseObject.errorForward("Không tìm thấy booking đang hoạt động cho phòng này", HttpStatus.NOT_FOUND);
    }
}
