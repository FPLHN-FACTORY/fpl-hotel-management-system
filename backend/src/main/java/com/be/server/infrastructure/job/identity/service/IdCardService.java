package com.be.server.infrastructure.job.identity.service;

import com.be.server.infrastructure.job.identity.model.IdCardData;
import org.springframework.web.multipart.MultipartFile;

public interface IdCardService {
    IdCardData scanIdCard(MultipartFile file, String qrData) throws Exception;
}
