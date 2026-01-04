package com.be.server.core.admin.doanluutru.modal.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindDoanRequest extends PageableRequest {
    private String tuKhoa;
    private Long thoiGianCheckIn;
    private Long thoiGianCheckOut;
}
