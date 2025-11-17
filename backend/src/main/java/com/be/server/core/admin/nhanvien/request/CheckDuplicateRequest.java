package com.be.server.core.admin.nhanvien.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckDuplicateRequest {
    private String field;
    private String value;
    private String excludeId;
}
