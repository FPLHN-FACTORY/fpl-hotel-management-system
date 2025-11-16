package com.be.server.core.admin.phong.model.request;

import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ADSavePhongRequest {
    private String ma;
    private String ten;
    private String idLoaiPhong;
    private Integer tang;
    private TrangThaiHoatDong trangThaiPhong;
}