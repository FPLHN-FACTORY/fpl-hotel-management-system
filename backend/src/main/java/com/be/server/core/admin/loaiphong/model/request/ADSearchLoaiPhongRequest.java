package com.be.server.core.admin.loaiphong.model.request;

import com.be.server.core.common.base.PageableRequest;
import com.be.server.infrastructure.constant.EntityStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ADSearchLoaiPhongRequest extends PageableRequest {
    private String tuKhoa;
    private EntityStatus trangThai;
}
