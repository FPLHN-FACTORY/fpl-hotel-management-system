package com.be.server.core.admin.phong.service.impl;

import com.be.server.core.admin.phong.model.request.ADPhongSearchRequest;
import com.be.server.core.admin.phong.model.request.ADSavePhongRequest;
import com.be.server.core.admin.phong.model.request.ADUpdatePhongRequest;
import com.be.server.core.admin.phong.model.response.ADPhongDetail;
import com.be.server.core.admin.phong.model.response.LoaiPhongResponse;
import com.be.server.core.admin.phong.model.response.PhongProjection;
import com.be.server.core.admin.phong.model.response.PhongResponse;
import com.be.server.core.admin.phong.repository.ADLoaiPhongRepository;
import com.be.server.core.admin.phong.repository.ADPhongRepository;
import com.be.server.core.admin.phong.repository.ADPhongTagRepository;
import com.be.server.core.admin.phong.repository.ADTagRoomRepository;
import com.be.server.core.admin.phong.service.ADPhongService;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.LoaiPhong;
import com.be.server.entity.Phong;
import com.be.server.entity.PhongTag;
import com.be.server.entity.Tag;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import com.be.server.utils.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ADPhongServiceImpl implements ADPhongService {

    private final ADPhongRepository adPhongRepository;
    private final ADLoaiPhongRepository adLoaiPhongRepository;
    private final ADPhongTagRepository adPhongTagRepository;
    private final ADTagRoomRepository adTagRepository;

    @Override
    public ResponseObject<?> getAllPhong(ADPhongSearchRequest request) {

        if (request.getPage() <= 0) {
            request.setPage(1);
        }

        Page<PhongProjection> page = adPhongRepository.getAllPhong(
                request.getTuKhoa(),
                request.getLoaiPhong(),
                request.getTrangThaiHoatDong() != null ? request.getTrangThaiHoatDong() : null,
                request.getGiaMin(),
                request.getGiaMax(),
                request.getSucChuaMin(),
                request.getSucChuaMax(),
                request.getTang(),
                request.getTagIds(),
                Helper.createPageable(request, "createDate")
        );

        List<String> phongIds = page.getContent().stream()
                .map(PhongProjection::getId)
                .toList();

        Map<String, List<PhongResponse.TagInfo>> tagsByPhongId = new HashMap<>();
        Map<String, Phong> phongDetailsMap = new HashMap<>();

        if (!phongIds.isEmpty()) {
            List<Phong> phongsWithTags = adPhongRepository.findAllWithTagsByIds(phongIds);
            for (Phong phong : phongsWithTags) {
                List<PhongResponse.TagInfo> tags = phong.getTags().stream()
                        .filter(pt -> pt.getTag() != null && pt.getTag().getStatus() == EntityStatus.ACTIVE)
                        .map(pt -> new PhongResponse.TagInfo(
                                pt.getTag().getId(),
                                pt.getTag().getMa(),
                                pt.getTag().getTen()
                        ))
                        .toList();
                tagsByPhongId.put(phong.getId(), tags);
                phongDetailsMap.put(phong.getId(), phong);
            }
        }

        List<PhongResponse> phongResponses = page.getContent().stream()
                .map(p -> {
                    PhongResponse pr = new PhongResponse();
                    pr.setId(p.getId());
                    pr.setMa(p.getMa());
                    pr.setTen(p.getTen());
                    pr.setPrice(p.getPrice());
                    pr.setTang(p.getTang());
                    pr.setLoaiPhong(p.getLoaiPhong());
                    pr.setSucChua(p.getSucChua());
                    pr.setSoNguoiQuyDinh(p.getSoNguoiQuyDinh());
                    pr.setTrangThaiHoatDong(TrangThaiHoatDong.valueOf(p.getTrangThaiHoatDong()));
                    pr.setTags(tagsByPhongId.getOrDefault(p.getId(), new ArrayList<>()));

                    Phong phongDetail = phongDetailsMap.get(p.getId());
                    if (phongDetail != null && phongDetail.getLoaiPhong() != null) {
                        pr.setSoGiuongDon(phongDetail.getLoaiPhong().getSoGiuongDon());
                        pr.setSoGiuongDoi(phongDetail.getLoaiPhong().getSoGiuongDoi());
                    }

                    return pr;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("data", phongResponses);
        result.put("totalPages", page.getTotalPages());
        result.put("currentPage", page.getNumber() + 1);
        result.put("totalElements", page.getTotalElements());

        return ResponseObject.successForward(result, "Lấy danh sách thành công");
    }

    @Override
    @Transactional
    public ResponseObject<?> savePhong(ADSavePhongRequest phong) {

        Optional<Phong> tenOptional = adPhongRepository.findByTen(phong.getTen());
        if (tenOptional.isPresent()) {
            return new ResponseObject<>(null, HttpStatus.OK, "Tên phòng này đã tồn tại");
        }

        Phong addPhong = new Phong();
        addPhong.setMa(phong.getTen());
        addPhong.setTen(phong.getTen());

        Optional<LoaiPhong> optionalLoaiPhong = adLoaiPhongRepository.findById(phong.getIdLoaiPhong());
        if (optionalLoaiPhong.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng");
        }

        if (phong.getTang() < 1 || phong.getTang() > 3 ) {
            return new ResponseObject<>(null, HttpStatus.OK, "Vị trí phòng không hợp lệ");
        }

        LoaiPhong loaiPhong = optionalLoaiPhong.get();

        addPhong.setTang(phong.getTang());
        addPhong.setTrangThaiHoatDong(TrangThaiHoatDong.DANG_HOAT_DONG);
        addPhong.setLoaiPhong(loaiPhong);

        Phong savedPhong = adPhongRepository.save(addPhong);

        if (phong.getTagIds() != null && !phong.getTagIds().isEmpty()) {
            for (String tagId : phong.getTagIds()) {
                Optional<Tag> tagOptional = adTagRepository.findById(tagId);
                if (tagOptional.isPresent()) {
                    PhongTag phongTag = new PhongTag();
                    phongTag.setPhong(savedPhong);
                    phongTag.setTag(tagOptional.get());
                    adPhongTagRepository.save(phongTag);
                }
            }
        }

        return new ResponseObject<>(null, HttpStatus.OK, "Thêm phòng thành công");
    }

    @Override
    public ResponseObject<?> deletePhong(String id) {
        return null;
    }

    @Override
    public ResponseObject<?> getAllLoaiPhong(){
        List<LoaiPhongResponse> responses = adLoaiPhongRepository.getAllLoaiPhong();
        return new ResponseObject<>(responses, HttpStatus.OK, "lấy thành công loại phòng");
    }

    @Override
    @Transactional
    public ResponseObject<?> updatePhong(String id, ADUpdatePhongRequest request) {
        Optional<Phong> existingPhong = adPhongRepository.findById(id);
        if (existingPhong.isEmpty()) {
            return ResponseObject.errorForward("Không tìm thấy phòng có id: " +id, HttpStatus.NOT_FOUND);
        }

        Phong existingPhong1 = existingPhong.get();

        Optional<Phong> tenOptional = adPhongRepository.findByTen(request.getTen());
        if (tenOptional.isPresent() && !tenOptional.get().getId().equals(id)) {
            return ResponseObject.errorForward("Tên phòng đã tồn tại", HttpStatus.CONFLICT);
        }

        if (request.getTang() < 1  || request.getTang() > 3){
            return ResponseObject.errorForward("Vị trí phòng không hợp lệ", HttpStatus.BAD_REQUEST);
        }

        Optional<LoaiPhong> optionalLoaiPhong = adLoaiPhongRepository.findById(request.getIdLoaiPhong());
        if (optionalLoaiPhong.isEmpty()) {
            return ResponseObject.errorForward("Không tìm thấy loại phòng", HttpStatus.NOT_FOUND);
        }

        existingPhong1.setMa(request.getTen());
        existingPhong1.setTen(request.getTen());
        existingPhong1.setTang(request.getTang());
        existingPhong1.setLoaiPhong(optionalLoaiPhong.get());
        existingPhong1.setTrangThaiHoatDong(request.getTrangThaiPhong());

        adPhongRepository.save(existingPhong1);

        adPhongTagRepository.deleteByPhongId(id);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (String tagId : request.getTagIds()) {
                Optional<Tag> tagOptional = adTagRepository.findById(tagId);
                if (tagOptional.isPresent()) {
                    PhongTag phongTag = new PhongTag();
                    phongTag.setPhong(existingPhong1);
                    phongTag.setTag(tagOptional.get());
                    adPhongTagRepository.save(phongTag);
                }
            }
        }

        return new ResponseObject<>(null, HttpStatus.OK, "Cập nhật phòng thành công");
    }

    @Override
    public ResponseObject<?> getPhongById(String id) {
        Optional<ADPhongDetail> phongDetail = adPhongRepository.getPhongDetailById(id);
        if(phongDetail.isEmpty()){
            return ResponseObject.errorForward("Không tìm thấy phòng có id: " + id, HttpStatus.NOT_FOUND);
        }

        List<Object[]> tagData = adPhongRepository.getTagsByPhongId(id);
        List<String> tagIds = new ArrayList<>();
        for (Object[] row : tagData) {
            tagIds.add((String) row[0]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("phong", phongDetail.get());
        result.put("tagIds", tagIds);

        return ResponseObject.successForward(result, "Lấy thông tin phòng thành công");
    }

    @Override
    public ResponseObject<?> getAllTags() {
        return new ResponseObject<>(adTagRepository.getAllActiveTags(), HttpStatus.OK, "Lấy thành công danh sách tags");
    }
}