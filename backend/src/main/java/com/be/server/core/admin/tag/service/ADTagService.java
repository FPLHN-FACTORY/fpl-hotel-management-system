package com.be.server.core.admin.tag.service;

import com.be.server.core.admin.tag.model.request.ADAddAndUpdateTagRequest;
import com.be.server.core.admin.tag.model.request.ADSearchTagRequest;
import com.be.server.core.common.base.ResponseObject;
import jakarta.validation.Valid;

public interface ADTagService {
    ResponseObject<?>getAllTags(ADSearchTagRequest request);
    ResponseObject<?>changeStatusTag(String id);
    ResponseObject<?>addTag(@Valid ADAddAndUpdateTagRequest request);
    ResponseObject<?>updateTag( @Valid ADAddAndUpdateTagRequest request,String id);
}
