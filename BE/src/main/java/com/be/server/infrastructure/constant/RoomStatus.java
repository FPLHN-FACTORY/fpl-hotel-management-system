package com.be.server.infrastructure.constant;

public enum RoomStatus {
    TRONG(0, "Trống"),
    DA_DAT(1, "Đã đặt"),
    DANG_SU_DUNG(2, "Đang sử dụng");

    private final int value;
    private final String description;

    RoomStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
