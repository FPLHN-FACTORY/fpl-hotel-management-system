package com.be.server.core.admin.doanluutru.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequest {
    private String idDoanLuuTru;

    // Existing customer ID (if selected)
    private String idKhachHang;

    // New customer info (if creating new)
    private String hoTen;
    private String email;
    private String soCccd;
    private String soDienThoai;
    private String diaChi;
    private String quocTich;
    // ... add more if needed

    private String vaiTro; // "MEMBER", "LEADER"
}
