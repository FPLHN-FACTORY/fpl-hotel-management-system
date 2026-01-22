package com.be.server.core.admin.phong.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ADDichVuSearchRequest {
    private String q; // Search keyword
    private Integer trangThai;
    private Integer page = 0;
    private Integer size = 10;
}
