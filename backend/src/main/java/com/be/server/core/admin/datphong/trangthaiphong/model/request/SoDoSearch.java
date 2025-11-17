package com.be.server.core.admin.datphong.trangthaiphong.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoDoSearch extends PageableRequest {
    private String ma;
    private String ten;
    private String loaiPhong;
    private Integer tang;
}
