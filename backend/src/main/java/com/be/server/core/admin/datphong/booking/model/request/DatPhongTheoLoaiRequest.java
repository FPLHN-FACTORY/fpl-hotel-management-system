package com.be.server.core.admin.datphong.booking.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DatPhongTheoLoaiRequest {

    Long ngayNhan;

    Long ngayTra;

    Integer soLuongKhach;

    List<ChonLoaiPhong> danhSachLoaiPhong;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChonLoaiPhong {
        String idLoaiPhong;

        Integer soLuong;
    }
}
