package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.Phong;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.PaginationConstant;
import com.be.server.infrastructure.constant.RoomStatus;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ADPhongServiceImpl implements ADPhongService {

    private final ADPhongRepository adPhongRepository;

    @Override
    public ResponseObject<?> getAllPhong(ADPhongSearchRequest request) {
        try {
            Pageable pageable = Helper.createPageable(request, PaginationConstant.DEFAULT_SORT_BY);

            Page<PhongProjection> page = hasFilters(request)
                    ? adPhongRepository.searchPhong(
                    request.getQ(),
                    request.getLoaiPhong(),
                    request.getTrangThaiPhong(),
                    request.getGiaMin(),
                    request.getGiaMax(),
                    request.getSucChuaMin(),
                    request.getSucChuaMax(),
                    pageable
            )
                    : adPhongRepository.findAllProjection(pageable);

            return new ResponseObject<>(
                    PageableObject.of(page),
                    HttpStatus.OK,
                    "Lấy danh sách phòng thành công"
            );

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phòng: ", e);
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tải danh sách phòng"
            );
        }
    }

    private boolean hasFilters(ADPhongSearchRequest request) {
        return (request.getQ() != null && !request.getQ().trim().isEmpty()) ||
                request.getLoaiPhong() != null ||
                request.getTrangThaiPhong() != null ||
                request.getGiaMin() != null ||
                request.getGiaMax() != null ||
                request.getSucChuaMin() != null ||
                request.getSucChuaMax() != null;
    }

    @Override
    public ResponseObject<?> getPhongById(String id) {
        try {
            return adPhongRepository.findByIdProjection(id)
                    .map(phong -> new ResponseObject<>(
                            phong,
                            HttpStatus.OK,
                            "Lấy thông tin phòng thành công"
                    ))
                    .orElseGet(() -> new ResponseObject<>(
                            null,
                            HttpStatus.NOT_FOUND,
                            "Không tìm thấy phòng"
                    ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin phòng: ", e);
            return new ResponseObject<>(
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể tải thông tin phòng"
            );
        }
    }

    @Override
    public ResponseObject<?> changeStatusPhong(String id) {
        Optional<Phong> existingPhong = adPhongRepository.findById(id);
        if (existingPhong.isPresent()) {
           Phong phong = existingPhong.get();
            if (phong.getStatus().equals(EntityStatus.INACTIVE)) {
                phong.setStatus(EntityStatus.ACTIVE);
            } else {
                if(phong.getTrangThaiPhong().equals(RoomStatus.TRONG)) {
                    phong.setStatus(EntityStatus.INACTIVE);
                }
                else if (phong.getTrangThaiPhong().equals(RoomStatus.DANG_SU_DUNG)) {
                    return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Phòng đang được sử dụng!");
                }
                else if (phong.getTrangThaiPhong().equals(RoomStatus.DA_DAT)) {
                    return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Phòng đã được đặt!");
                }
            }
            adPhongRepository.save(phong);
            return new ResponseObject<>(null, HttpStatus.OK, "Thay đổi trạng thái phòng thành công!");
        }
        return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Phòng không tồn tại!");

    }
}
