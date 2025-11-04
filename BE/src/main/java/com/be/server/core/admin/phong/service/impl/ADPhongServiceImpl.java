package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.LsDatPhongRequest;
import com.be.server.core.admin.phong.model.response.LeTanResponse;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.admin.phong.repository.LsDatPhongRepository;
import com.be.server.core.admin.phong.repository.LsDichVuPhatSinhRepository;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.DichVuPhatSinh;
import com.be.server.entity.Phong;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
    public ResponseObject<?> roomUsageHistory(LsDatPhongRequest request) {
        Pageable pageable = Helper.createPageable(request, "created_date");
        Page<LeTanResponse> page = lsDatPhongRepository.roomUsageHistory(
                request.getTenKhachHang(),
                request.getTuNgay(),
                request.getDenNgay(),
                pageable
        );
        page.forEach(item -> {
            List<DichVuPhatSinh> dichVus = dichVuPhatSinhRepository.findByDatPhongId(item.getDatPhongId());
            List<String> tenDichVus = dichVus.stream()
                    .map(DichVuPhatSinh::getTenDichVu)
                    .collect(Collectors.toList());
            item.setDichVuPhatSinh(tenDichVus);
            BigDecimal tongThanhTien = dichVus.stream()
                    .map(DichVuPhatSinh::getThanhTien)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            item.setTongTienPhatSinh(tongThanhTien);
        });

        return new ResponseObject<>(
                PageableObject.of(page),
                HttpStatus.OK,
                "Lấy thành công lịch sử đặt phòng"
        );
    }

}
