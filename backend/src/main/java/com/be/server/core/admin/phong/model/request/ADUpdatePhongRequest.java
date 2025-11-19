package com.be.server.core.admin.phong.model.request;

import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ADUpdatePhongRequest {

    private String ma;

    private String ten;

    private String idLoaiPhong;

    private Integer tang;

    private TrangThaiHoatDong trangThaiPhong;

    private List<String> tagIds;
}
