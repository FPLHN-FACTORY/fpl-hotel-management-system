package com.be.server.core.admin.phieudatphong.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request để gắn phòng cụ thể vào phiếu đặt.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GanPhongRequest {

    @NotBlank(message = "ID phiếu đặt không được để trống")
    String idPhieuDat;

    @NotEmpty(message = "Danh sách phòng không được để trống")
    List<String> danhSachIdPhong;
}
