package com.be.server.core.admin.phieudatphong.model.response;

import com.be.server.infrastructure.constant.StatusPhieuDatPhong;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response cho danh sách phiếu đặt phòng (list view).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PhieuDatPhongListResponse {

    String id;

    String maPhieu;

    Long ngayCheckIn;

    Long ngayCheckOut;

    String tenKhachHang;

    Integer tongSoPhong;

    StatusPhieuDatPhong trangThai;

    BigDecimal tongTien;

    Long ngayTao;
}
