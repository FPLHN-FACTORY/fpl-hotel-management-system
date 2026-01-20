package com.be.server.core.admin.doanluutru.modal.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AddMemberRequest {
    private String idDoanLuuTru;

    // Existing customer ID (if selected)
    private String idKhachHang;

    // New customer info (if creating new)
    private String hoTen;
    private Integer gioiTinh;
    private LocalDate ngaySinh;
    private String email;
    private Integer loaiGiayTo;
    private String soGiayTo;
    private String soDienThoai;
    private String diaChi;
    private String quocTich;
    // ... add more if needed

    private Boolean confirmUseOld;
}
