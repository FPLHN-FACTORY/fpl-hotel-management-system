package com.be.server.infrastructure.constant;

public enum EntityTrangThaiChiTietDoan {
    BOOKED,        // mới tạo, chưa gán phòng
    ASSIGNING,     // đã gán phòng, có thể đổi
    CHECKED_IN,    // đã xác nhận check-in (KHÓA phòng)
    CHECKED_OUT,   // đã trả phòng
    CANCELLED
}