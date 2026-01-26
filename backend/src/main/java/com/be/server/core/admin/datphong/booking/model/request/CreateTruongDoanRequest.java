package com.be.server.core.admin.datphong.booking.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTruongDoanRequest {

    private String tenDoan;
    // Leader Info

    private String hoTen;
    private String soDienThoai;

    private LocalDate ngaySinh;
    private Integer loaiGiayTo;
    private String soGiayTo;
private Integer gioiTinh;
private  Boolean isDoan;

}
