package com.be.server.core.admin.phong.model.request;

import com.be.server.core.common.base.PageableRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LsDatPhongRequest extends PageableRequest {
    private String tenKhachHang;
    private LocalDateTime tuNgay;
    private LocalDateTime denNgay;
}