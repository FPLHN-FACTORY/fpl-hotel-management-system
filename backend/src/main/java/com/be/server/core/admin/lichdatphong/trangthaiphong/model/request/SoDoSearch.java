package com.be.server.core.admin.lichdatphong.trangthaiphong.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoDoSearch extends PageableRequest {

    private String ma;

    private String ten;

    private String idLoaiPhong;

    private Integer tang;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Long ngayDen;

    private Long ngayDi;

}
