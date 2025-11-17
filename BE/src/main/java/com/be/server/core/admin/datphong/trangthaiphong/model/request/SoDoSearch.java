package com.be.server.core.admin.datphong.trangthaiphong.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
