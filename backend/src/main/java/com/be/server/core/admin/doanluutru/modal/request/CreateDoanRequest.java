package com.be.server.core.admin.doanluutru.modal.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateDoanRequest {
    private String idDatPhong;
    private String tenDoan;
    // Leader Info

    private String hoTenTruongDoan;
    private String soDienThoaiTruongDoan;

    private LocalDate ngaySinhTruongDoan;
    private Integer loaiGiayToTruongDoan;
    private String soGiayToTruongDoan;
private Integer gioiTinhTruongDoan;

    private String ghiChu;
}
