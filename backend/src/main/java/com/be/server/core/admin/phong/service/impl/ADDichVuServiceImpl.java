package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADDichVuRequest;
import com.be.server.core.admin.phong.model.request.ADDichVuSearchRequest;
import com.be.server.core.admin.phong.model.response.ADDichVuResponse;
import com.be.server.core.admin.phong.repository.ADDichVuRepository;
import com.be.server.core.admin.phong.service.ADDichVuService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DichVu;
import com.be.server.infrastructure.constant.EntityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ADDichVuServiceImpl implements ADDichVuService {

    private final ADDichVuRepository dichVuRepository;

    @Override
    @Transactional
    public ResponseObject<?> create(ADDichVuRequest request) {
        // Kiểm tra mã dịch vụ đã tồn tại
        if (dichVuRepository.existsByMaDichVu(request.getMaDichVu())) {
            return ResponseObject.errorForward("Mã dịch vụ đã tồn tại", HttpStatus.BAD_REQUEST);
        }

        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(request.getMaDichVu());
        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setDonViTinh(request.getDonViTinh());
        dichVu.setDonGia(request.getDonGia());
        dichVu.setMoTa(request.getMoTa());
        dichVu.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : EntityProperties.HOAT_DONG);

        dichVuRepository.save(dichVu);

        return ResponseObject.successForward(null, "Tạo dịch vụ thành công");
    }

    @Override
    @Transactional
    public ResponseObject<?> update(String id, ADDichVuRequest request) {
        DichVu dichVu = dichVuRepository.findById(id)
                .orElse(null);
        
        if (dichVu == null) {
            return ResponseObject.errorForward("Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND);
        }

        // Kiểm tra mã dịch vụ trùng (nếu thay đổi)
        if (!dichVu.getMaDichVu().equals(request.getMaDichVu())) {
            if (dichVuRepository.existsByMaDichVu(request.getMaDichVu())) {
                return ResponseObject.errorForward("Mã dịch vụ đã tồn tại", HttpStatus.BAD_REQUEST);
            }
            dichVu.setMaDichVu(request.getMaDichVu());
        }

        dichVu.setTenDichVu(request.getTenDichVu());
        dichVu.setDonViTinh(request.getDonViTinh());
        dichVu.setDonGia(request.getDonGia());
        dichVu.setMoTa(request.getMoTa());
        if (request.getTrangThai() != null) {
            dichVu.setTrangThai(request.getTrangThai());
        }

        dichVuRepository.save(dichVu);

        return ResponseObject.successForward(null, "Cập nhật dịch vụ thành công");
    }

    @Override
    @Transactional
    public ResponseObject<?> delete(String id) {
        DichVu dichVu = dichVuRepository.findById(id)
                .orElse(null);
        
        if (dichVu == null) {
            return ResponseObject.errorForward("Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND);
        }

        // Soft delete
        dichVu.setTrangThai(EntityProperties.DA_XOA);
        dichVuRepository.save(dichVu);

        return ResponseObject.successForward(null, "Xóa dịch vụ thành công");
    }

    @Override
    public ResponseObject<ADDichVuResponse> getById(String id) {
        DichVu dichVu = dichVuRepository.findById(id)
                .orElse(null);
        
        if (dichVu == null) {
            return ResponseObject.errorForward("Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND);
        }

        ADDichVuResponse response = mapToResponse(dichVu);
        return ResponseObject.successForward(response, "Lấy thông tin dịch vụ thành công");
    }

    @Override
    public ResponseObject<Page<ADDichVuResponse>> search(ADDichVuSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<ADDichVuResponse> page = dichVuRepository.searchDichVu(
                request.getQ(),
                request.getTrangThai(),
                pageable
        );
        return ResponseObject.successForward(page, "Tìm kiếm dịch vụ thành công");
    }

    @Override
    public ResponseObject<List<ADDichVuResponse>> getAllActive() {
        List<ADDichVuResponse> list = dichVuRepository.getAllActiveDichVu();
        return ResponseObject.successForward(list, "Lấy danh sách dịch vụ thành công");
    }

    private ADDichVuResponse mapToResponse(DichVu dichVu) {
        ADDichVuResponse response = new ADDichVuResponse();
        response.setId(dichVu.getId());
        response.setMaDichVu(dichVu.getMaDichVu());
        response.setTenDichVu(dichVu.getTenDichVu());
        response.setDonViTinh(dichVu.getDonViTinh());
        response.setDonGia(dichVu.getDonGia());
        response.setMoTa(dichVu.getMoTa());
        response.setTrangThai(dichVu.getTrangThai());
        response.setCreatedDate(dichVu.getCreatedDate());
        return response;
    }
}
