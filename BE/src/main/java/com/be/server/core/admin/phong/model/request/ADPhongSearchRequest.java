package com.be.server.core.admin.phong.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.RoomStatus;
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


    private String q;

    private String loaiPhong;

    private RoomStatus trangThaiPhong;

    private BigDecimal giaMin;

    private BigDecimal giaMax;

    private Integer sucChuaMin;

    private Integer sucChuaMax;
}