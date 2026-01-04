package com.be.server.core.admin.khachhang.service.impl;

import com.be.server.core.admin.khachhang.model.request.ADAddAndUpdateKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.ADSearchKhachHangRequest;
import com.be.server.core.admin.khachhang.model.request.GiayToRequest;
import com.be.server.core.admin.khachhang.model.request.UpdateKhachHangLuuTruRequest;
import com.be.server.core.admin.khachhang.repository.ADKhachHangRepository;
import com.be.server.core.admin.khachhang.repository.ADLoaiKhachHangRepository;
import com.be.server.core.admin.khachhang.service.ADKhachHangService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.KhachHang;
import com.be.server.entity.LoaiKhachHang;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.GioiTinh;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ADKhachHangServiceImpl implements ADKhachHangService {
    private final ADKhachHangRepository adKhachHangRepository;
    private final ADLoaiKhachHangRepository adLoaiKhachHangRepository;

    @Override
    public ResponseObject<?> getAllKhachHang(ADSearchKhachHangRequest request) {

        if (request.getPage() <= 0) {
            request.setPage(1);
        }

        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(
                PageableObject.of(adKhachHangRepository.getAllKhachHang(pageable, request)),
                HttpStatus.OK,
                "Lấy thành công khách hàng thành công"
        );
    }

    @Override
    public ResponseObject<?> addKhachHang(ADAddAndUpdateKhachHangRequest request) {
        LoaiKhachHang loaiKhachHang = null;
        if (request.getIdLoaiKhachHang() != null) {
            Optional<LoaiKhachHang> loaiKhachHangOptional = adLoaiKhachHangRepository.findById(request.getIdLoaiKhachHang());
            if (loaiKhachHangOptional.isPresent()) {
                loaiKhachHang = loaiKhachHangOptional.get();
            }
        }


        if (request.getLoaiGiayTo() != null && request.getSoGiayTo() != null) {
            if (adKhachHangRepository.existsByLoaiGiayToAndSoGiayTo(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU, request.getSoGiayTo())) {
                return new ResponseObject<>(null, HttpStatus.CONFLICT, "Giấy tờ này đã tồn tại trong hệ thống");
            }
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setHoTen(request.getHoTen());
        khachHang.setTen(Helper.extractLastName(request.getHoTen()));
        khachHang.setNgaySinh(request.getNgaySinh());
        khachHang.setGioiTinh(request.getGioiTinh()==0? GioiTinh.NAM:request.getGioiTinh()==1?GioiTinh.NU:GioiTinh.KHAC);
        khachHang.setEmail(Helper.toLower(request.getEmail()));
        khachHang.setSoDienThoai(request.getSoDienThoai());
        khachHang.setLoaiGiayTo(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU);
        khachHang.setSoGiayTo(request.getSoGiayTo());
        khachHang.setDiaChi(request.getDiaChi());
        khachHang.setStatus(EntityStatus.ACTIVE);
        khachHang.setLoaiKhachHang(loaiKhachHang);
        adKhachHangRepository.save(khachHang);
        return new ResponseObject<>(null, HttpStatus.OK, "Thêm khách hàng thành công");

    }

    @Override
    public ResponseObject<?> changeStatusKhachHang(String id) {
        Optional<KhachHang> khachHangOptional = adKhachHangRepository.findById(id);

        if (khachHangOptional.isPresent()) {
            KhachHang khachHang = khachHangOptional.get();
            if (khachHang.getStatus().equals(EntityStatus.ACTIVE)) {
                khachHang.setStatus(EntityStatus.INACTIVE);
            } else khachHang.setStatus(EntityStatus.ACTIVE);
            adKhachHangRepository.save(khachHang);
            return new ResponseObject<>(null, HttpStatus.OK, "Thay đổi trạng thái khách hàng thành công");

        } else return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");

    }

    @Override
    public ResponseObject<?> updateKhachHang(ADAddAndUpdateKhachHangRequest request, String id) {
        Optional<KhachHang> khachHangOptional = adKhachHangRepository.findById(id);
        LoaiKhachHang loaiKhachHang = null;
        if (request.getIdLoaiKhachHang() != null) {
            Optional<LoaiKhachHang> loaiKhachHangOptional = adLoaiKhachHangRepository.findById(request.getIdLoaiKhachHang());
            if (loaiKhachHangOptional.isPresent()) {
                loaiKhachHang = loaiKhachHangOptional.get();
            }
        }
        if (khachHangOptional.isPresent()) {
            KhachHang khachHang = khachHangOptional.get();

            if (request.getLoaiGiayTo() != null && request.getSoGiayTo() != null) {
                if (adKhachHangRepository.existsByLoaiGiayToAndSoGiayToAndIdNot(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU, request.getSoGiayTo(),id)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Giấy tờ này đã tồn tại trong hệ thống");
                }
            }

            khachHang.setHoTen(request.getHoTen());
            khachHang.setTen(Helper.extractLastName(request.getHoTen()));
            khachHang.setNgaySinh(request.getNgaySinh());
            khachHang.setGioiTinh(request.getGioiTinh()==0? GioiTinh.NAM:request.getGioiTinh()==1?GioiTinh.NU:GioiTinh.KHAC);
            khachHang.setEmail(Helper.toLower(request.getEmail()));
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setLoaiGiayTo(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU);
            khachHang.setSoGiayTo(request.getSoGiayTo());
            khachHang.setDiaChi(request.getDiaChi());
            khachHang.setLoaiKhachHang(loaiKhachHang);
            adKhachHangRepository.save(khachHang);
            return new ResponseObject<>(null, HttpStatus.OK, "Cập nhật khách hàng thành công");
        } else return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");

    }

    @Override
    public ResponseObject<?> getDataLoaiKhachHang() {
        return ResponseObject.successForward(adLoaiKhachHangRepository.getLoaiKhachHangCombobox(), "Lấy data loại khách hàng thành công");
    }

    @Override
    public ResponseObject<?> findKhachHangByGiayTo(GiayToRequest request) {
        if(request.getLoaiGiayTo()!=null && request.getSoGiayTo()!=null && !request.getSoGiayTo().isBlank()){
            Optional<KhachHang>optionalKhachHang=adKhachHangRepository.findByLoaiGiayToAndSoGiayTo(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU,request.getSoGiayTo());
            if(optionalKhachHang.isPresent()){
                KhachHang khachHang = optionalKhachHang.get();
                return new ResponseObject<>(khachHang,HttpStatus.OK,"Tìm kiếm theo giấy tờ thành công ");
            }
            else{
                return new ResponseObject<>(null,HttpStatus.NOT_FOUND,"Khách hàng không tồn tại theo giấy tờ");
            }
        }
        return new ResponseObject<>(null,HttpStatus.BAD_REQUEST,"Không đc để trống giấy tờ");
    }

    @Override
    public ResponseObject<?> updateKhachHangLuuTru(UpdateKhachHangLuuTruRequest request, String id) {
        Optional<KhachHang> khachHangOptional = adKhachHangRepository.findById(id);

        if (khachHangOptional.isPresent()) {
            KhachHang khachHang = khachHangOptional.get();

            if (request.getLoaiGiayTo() != null && request.getSoGiayTo() != null && !request.getSoGiayTo().isBlank()) {
                if (adKhachHangRepository.existsByLoaiGiayToAndSoGiayToAndIdNot(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU, request.getSoGiayTo(),id)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Giấy tờ này đã tồn tại trong hệ thống");
                }
            }

            khachHang.setHoTen(request.getHoTen());
            khachHang.setTen(Helper.extractLastName(request.getHoTen()));
            khachHang.setNgaySinh(request.getNgaySinh());
            khachHang.setGioiTinh(request.getGioiTinh()==0? GioiTinh.NAM:request.getGioiTinh()==1?GioiTinh.NU:GioiTinh.KHAC);
            khachHang.setLoaiGiayTo(request.getLoaiGiayTo()==0? LoaiGiayTo.CCCD:LoaiGiayTo.HO_CHIEU);
            khachHang.setSoGiayTo(request.getSoGiayTo());
            adKhachHangRepository.save(khachHang);
            return new ResponseObject<>(null, HttpStatus.OK, "Cập nhật khách hàng thành công");
        } else return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng");

    }
}
