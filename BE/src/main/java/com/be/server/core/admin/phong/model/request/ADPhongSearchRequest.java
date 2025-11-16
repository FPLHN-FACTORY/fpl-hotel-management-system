package com.be.server.core.admin.phong.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.TrangThaiHoatDong;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;


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

}