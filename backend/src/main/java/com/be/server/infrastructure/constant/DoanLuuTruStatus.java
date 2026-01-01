package com.be.server.infrastructure.constant;

public enum DoanLuuTruStatus {
    CHUA_CHECK_IN(0, "Chưa check-in"),
    DANG_O(1, "Đang ở"),
    DA_CHECK_OUT(2, "Đã check-out");

    private final int value;
    private final String description;

    DoanLuuTruStatus(int value, String description) {
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
