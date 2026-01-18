package com.be.server.core.admin.phieudatphong.model.request;

import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request để filter danh sách phiếu đặt phòng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuDatPhongFilterRequest {

    String keyword; // Tìm theo mã phiếu, tên khách hàng

    StatusPhieuDatPhong status;

    Long tuNgay;

    Long denNgay;

    Integer page = 0;

    Integer size = 20;
}
