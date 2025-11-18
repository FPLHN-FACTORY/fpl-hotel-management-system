package com.be.server.core.admin.tag.service.impl;

import com.be.server.core.admin.tag.model.request.ADAddAndUpdateTagRequest;
import com.be.server.core.admin.tag.model.request.ADSearchTagRequest;
import com.be.server.core.admin.tag.repository.ADTagRepository;
import com.be.server.core.admin.tag.service.ADTagService;
import com.be.server.core.common.base.PageableObject;
import com.be.server.core.common.base.ResponseObject;
import com.be.server.entity.Tag;
import com.be.server.infrastructure.constant.EntityStatus;
import com.be.server.utils.Helper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ADTagServiceImpl implements ADTagService {
    private final ADTagRepository adTagRepository;

    @Override
    public ResponseObject<?> getAllTags(ADSearchTagRequest request) {

        if (request.getPage() <= 0) {
            request.setPage(1);
        }
        Pageable pageable = Helper.createPageable(request, "created_date");
        return new ResponseObject<>(
                PageableObject.of(adTagRepository.getAllTags(request, pageable)),
                HttpStatus.OK,
                "Lấy thành công danh sach Tag"
        );
    }


    @Override
    public ResponseObject<?> changeStatusTag(String id) {
        Optional<Tag> existingTag = adTagRepository.findById(id);
        if (existingTag.isPresent()) {
            Tag tag = existingTag.get();
            if (tag.getStatus().equals(EntityStatus.ACTIVE)) {
                tag.setStatus(EntityStatus.INACTIVE);
            } else tag.setStatus(EntityStatus.ACTIVE);
            adTagRepository.save(tag);
            return new ResponseObject<>(tag,HttpStatus.OK,"Thay đổi trạng thái tag thành công");
        } else {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Không tìm thấy Tag");
        }

    }

    @Override
    public ResponseObject<?> addTag( ADAddAndUpdateTagRequest request) {
        if (request.getTen() == null || request.getTen().isBlank()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Tên tag không được để trống");
        }
        String cleanName = Helper.replaceManySpaceToOneSpace(request.getTen().trim());
        List<Tag> tagOptional = adTagRepository.findAllByTen(cleanName);
        if (!tagOptional.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Tag đã tồn tại");
        }
        String code = "#" + Helper.generateCodeFromNameLowerCase(cleanName);
        Tag tag = new Tag();
        tag.setMa(code);
        tag.setTen(cleanName);
        tag.setStatus(EntityStatus.ACTIVE);
        tag.setMau(request.getMau());
        adTagRepository.save(tag);
        return new ResponseObject<>(tag, HttpStatus.OK, "Thêm Tag đã thành công");
    }

    @Override
    public ResponseObject<?> updateTag( ADAddAndUpdateTagRequest request, String id) {
        Optional<Tag> optionalTag = adTagRepository.findById(id);
        if (optionalTag.isEmpty()) {
            return new ResponseObject<>(null, HttpStatus.NOT_FOUND, "Tag không tồn tại");
        }
        Tag tag = optionalTag.get();
        if (request.getTen() == null || request.getTen().isBlank()) {
            return new ResponseObject<>(null, HttpStatus.BAD_REQUEST, "Tên tag không được để trống");
        }
        String cleanName = Helper.replaceManySpaceToOneSpace(request.getTen().trim());
        List<Tag> tagCheck = adTagRepository.findAllByTen(cleanName);
        if (!tagCheck.isEmpty() && !tagCheck.get(0).getId().equals(id)) {
            return new ResponseObject<>(null, HttpStatus.CONFLICT, "Tên tag đã tồn tại");
        }
        String code = "#" + Helper.generateCodeFromNameLowerCase(cleanName);
        tag.setTen(cleanName);
        tag.setMa(code);
        tag.setMau(request.getMau());
        adTagRepository.save(tag);
        return new ResponseObject<>(tag, HttpStatus.OK, "Cập nhật Tag thành công");
    }


}
