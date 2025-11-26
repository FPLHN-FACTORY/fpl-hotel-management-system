package com.be.server.infrastructure.job.identity.controller;

import com.be.server.infrastructure.job.identity.model.IdCardData;
import com.be.server.infrastructure.job.identity.service.IdCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class IdCardController {
    private static final Logger logger = LoggerFactory.getLogger(IdCardController.class);
    private final IdCardService idCardService;

    @Autowired
    public IdCardController(IdCardService idCardService) {
        this.idCardService = idCardService;
    }

    @PostMapping(value = "/scan-id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> scanIdCard(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "qrData", required = false) String qrData) { // Thêm qrData

        logger.info("Received ID card scan request. QR data provided: {}", qrData != null && !qrData.isEmpty());

        if (file == null || file.isEmpty()) {
            logger.warn("No file provided in request");
            return createErrorResponse("No file provided", HttpStatus.BAD_REQUEST);
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            logger.warn("Invalid file type: {}", file.getContentType());
            return createErrorResponse("Only image files are allowed", HttpStatus.BAD_REQUEST);
        }

        try {
            logger.info("Processing ID card image: {} ({} bytes)",
                    file.getOriginalFilename(),
                    file.getSize());

            // Gọi service với cả file và qrData
            IdCardData idCardData = idCardService.scanIdCard(file, qrData);

            logger.info("Successfully processed ID card for: {}",
                    idCardData.getFullName() != null ? idCardData.getFullName() : "Unknown");

            return ResponseEntity.ok(idCardData);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error processing ID card: {}", e.getMessage(), e);
            return createErrorResponse("Error processing ID card: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.getReasonPhrase());
        return ResponseEntity.status(status).body(response);
    }
}