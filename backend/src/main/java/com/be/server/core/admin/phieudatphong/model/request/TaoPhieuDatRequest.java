package com.be.server.core.admin.phieudatphong.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request để tạo phiếu đặt mới theo loại phòng.
 * Phiếu sẽ có trạng thái PENDING, chưa gắn phòng cụ thể.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaoPhieuDatRequest {

    @NotNull(message = "Ngày check-in không được để trống")
    Long ngayCheckIn;

    @NotNull(message = "Ngày check-out không được để trống")
    Long ngayCheckOut;

    @NotNull(message = "Số lượng khách không được để trống")
    @Min(value = 1, message = "Số lượng khách phải lớn hơn 0")
    Integer soLuongKhach;

    String ghiChu;

    @NotEmpty(message = "Danh sách loại phòng không được để trống")
    List<ChonLoaiPhong> danhSachLoaiPhong;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ChonLoaiPhong {
        @NotBlank(message = "ID loại phòng không được để trống")
        String idLoaiPhong;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phòng phải lớn hơn 0")
        Integer soLuong;
    }
}
