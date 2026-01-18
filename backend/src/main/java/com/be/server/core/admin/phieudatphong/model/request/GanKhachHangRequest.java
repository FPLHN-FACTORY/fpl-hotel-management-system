package com.be.server.core.admin.phieudatphong.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request để gắn khách hàng vào phiếu đặt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GanKhachHangRequest {

    @NotBlank(message = "ID phiếu đặt không được để trống")
    String idPhieuDat;

    @NotBlank(message = "ID khách hàng không được để trống")
    String idKhachHang;
}
