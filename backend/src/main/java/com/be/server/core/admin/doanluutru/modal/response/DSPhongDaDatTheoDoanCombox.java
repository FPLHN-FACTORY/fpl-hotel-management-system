package com.be.server.core.admin.doanluutru.modal.response;

import com.be.server.entity.base.IsIdentified;

public interface DSPhongDaDatTheoDoanCombox extends IsIdentified {
        String getTen();
    String getTang();
    Integer getSoGiuongDon();
    Integer getSoGiuongDoi();
    Integer getSoNguoiQuyDinh();
    Integer getSoNguoiToiDa();
    Integer getSoNguoiHienTai();
}
