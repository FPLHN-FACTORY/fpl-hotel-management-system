package com.be.server.core.admin.loaiphong.service.impl;

import com.be.server.core.admin.loaiphong.model.request.ADSaveLoaiPhongRequest;
import com.be.server.core.admin.loaiphong.repository.LTLoaiPhongReposiotry;
import com.be.server.core.admin.loaiphong.service.ADLoaiPhongService;
import com.be.server.core.admin.phong.repository.ADLoaiPhongRepository;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.LoaiPhong;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.utils.RandomNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ADLoaiPhongServiceImpl implements ADLoaiPhongService {

    private final LTLoaiPhongReposiotry repository;

    @Override
    public ResponseObject<?> getAllLoaiPhong() {
        List<LoaiPhong> list = repository.findAll();
        return new ResponseObject<>(list, HttpStatus.OK, "Lấy danh sách loại phòng thành công");
    }

    @Override
    public ResponseObject<?> saveLoaiPhong(ADSaveLoaiPhongRequest request) {

        if (repository.findByMa(request.getMa()).isPresent()) {
            return new ResponseObject<>(null, HttpStatus.OK, "Mã loại phòng đã tồn tại");
        }
        if (repository.findByTen(request.getTen()).isPresent()) {
            return new ResponseObject<>(null, HttpStatus.OK, "Tên loại phòng đã tồn tại");
        }

        LoaiPhong lp = new LoaiPhong();
        lp.setMa(RandomNumberGenerator.generateRoomType());
        lp.setTen(request.getTen());
        lp.setSoGiuongDon(request.getSoGiuongDon());
        lp.setSoGiuongDoi(request.getSoGiuongDoi());
        lp.setSoNguoiQuyDinh(request.getSoNguoiQuyDinh());
        lp.setSoNguoiToiDa(request.getSoNguoiToiDa());
        lp.setStatus(EntityStatus.ACTIVE);
        lp.setGiaCaNgay(request.getGiaCaNgay());

        repository.save(lp);
        return new ResponseObject<>(null, HttpStatus.OK, "Thêm loại phòng thành công");
    }

    @Override
    public ResponseObject<?> updateLoaiPhong(String id, ADSaveLoaiPhongRequest request) {
        Optional<LoaiPhong> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng");
        }
        LoaiPhong lp = optional.get();
        lp.setTen(request.getTen());
        lp.setSoGiuongDon(request.getSoGiuongDon());
        lp.setSoGiuongDoi(request.getSoGiuongDoi());
        lp.setSoNguoiQuyDinh(request.getSoNguoiQuyDinh());
        lp.setSoNguoiToiDa(request.getSoNguoiToiDa());
        lp.setGiaCaNgay(request.getGiaCaNgay());

        repository.save(lp);
        return new ResponseObject<>(null, HttpStatus.OK, "Cập nhật loại phòng thành công");
    }

    @Override
    public ResponseObject<?> deleteLoaiPhong(String id) {
        Optional<LoaiPhong> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng");
        }
        repository.deleteById(id);
        return new ResponseObject<>(null, HttpStatus.OK, "Xóa loại phòng thành công");
    }
}
