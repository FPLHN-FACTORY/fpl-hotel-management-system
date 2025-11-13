package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.LsDatPhongRequest;
import com.be.server.core.admin.phong.model.response.LeTanResponse;
import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.admin.phong.repository.LsDatPhongRepository;
import com.be.server.core.admin.phong.repository.LsDichVuPhatSinhRepository;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DichVuPhatSinh;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.PaginationConstant;
import com.be.server.infrastructure.constant.RoomStatus;
import com.be.server.infrastructure.constant.TrangThaiPhong;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ADPhongServiceImpl implements ADPhongService {

    private final ADPhongRepository adPhongRepository;

    private final LsDatPhongRepository lsDatPhongRepository;

    private final LsDichVuPhatSinhRepository dichVuPhatSinhRepository;

    @Override
    public ResponseObject<?> getAllPhong(ADPhongSearchRequest request) {

        if (request.getPage() <= 0) {
            request.setPage(1);
        }

        return ResponseObject.successForward(
                PageableObject.of(adPhongRepository.getAllPhong(
                request.getTuKhoa(),
                request.getLoaiPhong(),
                request.getTrangThaiPhong() != null ? request.getTrangThaiPhong() : null,
                request.getGiaMin(),
                request.getGiaMax(),
                request.getSucChuaMin(),
                request.getSucChuaMax(),
                request.getTang(),
                Helper.createPageable(request, "createDate")
        )),
        "Lấy danh sách thành công"
        );

    }

    @Override
    public ResponseObject<?> changeStatusPhong(String id) {
        Optional<Phong> existingPhong = adPhongRepository.findById(id);
        if (existingPhong.isPresent()) {
            Phong phong = existingPhong.get();
            if (phong.getStatus().equals(EntityStatus.INACTIVE)) {
                phong.setStatus(EntityStatus.ACTIVE);
            } else {
                if(phong.getTrangThaiPhong().equals(TrangThaiPhong.TRONG)) {
                    phong.setStatus(EntityStatus.INACTIVE);
                }
                else if (phong.getTrangThaiPhong().equals(TrangThaiPhong.DANG_SU_DUNG)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Không thể xóa phòng, phòng đang được sử dụng!");
                }
                else if (phong.getTrangThaiPhong().equals(TrangThaiPhong.DA_DAT)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Không thể xóa phòng, phòng đã được đặt!");
                }
                else if (phong.getTrangThaiPhong().equals(TrangThaiPhong.DANG_DON)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Không thể xóa phòng, phòng đang được dọn !");
                }
                else if (phong.getTrangThaiPhong().equals(TrangThaiPhong.BAO_TRI)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Không thể xóa phòng, phòng đang được bảo trì !");
                }
                else if (phong.getTrangThaiPhong().equals(TrangThaiPhong.TAM_KHOA)) {
                    return new ResponseObject<>(null, HttpStatus.CONFLICT, "Không thể xóa phòng, phòng đang được tạm khóa !");
                }
            }
            adPhongRepository.save(phong);
            return new ResponseObject<>(null, HttpStatus.OK, "Xóa phòng thành công!");
        }
        return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phòng không tồn tại!");

    }

}
