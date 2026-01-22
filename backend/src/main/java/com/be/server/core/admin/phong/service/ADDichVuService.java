package com.be.server.core.admin.phong.service;

import com.be.server.core.admin.phong.model.request.ADDichVuRequest;
import com.be.server.core.admin.phong.model.request.ADDichVuSearchRequest;
import com.be.server.core.admin.phong.model.response.ADDichVuResponse;
import com.be.server.core.common.base.ResponseObject;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ADDichVuService {
    
    ResponseObject<?> create(ADDichVuRequest request);
    
    ResponseObject<?> update(String id, ADDichVuRequest request);
    
    ResponseObject<?> delete(String id);
    
    ResponseObject<ADDichVuResponse> getById(String id);
    
    ResponseObject<Page<ADDichVuResponse>> search(ADDichVuSearchRequest request);
    
    ResponseObject<List<ADDichVuResponse>> getAllActive();
}
