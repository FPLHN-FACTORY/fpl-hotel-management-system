package com.be.server.core.admin.doanluutru.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindDoanRequest {
    private String tuKhoa;
    private Long thoiGianTaoTu;
    private Long thoiGianTaoDen;
}
