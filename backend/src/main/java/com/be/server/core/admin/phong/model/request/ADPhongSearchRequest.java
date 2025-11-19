package com.be.server.core.admin.phong.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ADPhongSearchRequest extends PageableRequest {


    private String tuKhoa;

    private Integer tang;

    private String loaiPhong;

    private TrangThaiHoatDong trangThaiHoatDong;

    private BigDecimal giaMin;

    private BigDecimal giaMax;

    private Integer sucChuaMin;

    private Integer sucChuaMax;

    private List<String> tagIds;


}