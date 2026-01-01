package com.be.server.core.admin.doanluutru.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDoanRequest {
    private String tenDoan;
    // Leader Info
    private String hoTenTruongDoan;
    private String soDienThoaiTruongDoan;
    private String emailTruongDoan;
    private String soCccdTruongDoan;

    private Integer soNguoi;
    private String ghiChu;
}
