package com.be.server.infrastructure.constant;

public class EntityProperties {

    private EntityProperties() {
    }

    public static final byte LENGTH_ID = 36;

    public static final byte LENGTH_CODE = 50;

    public static final short LENGTH_NAME = 255;

    public static final short LENGTH_PICTURE = 15000;

    public static final short LENGTH_URL = 2000;

    public static final short LENGTH_DESCRIPTION = 5000;

    public static final short LENGTH_CONTENT = 2000;

    // Entity Status
    public static final int HOAT_DONG = 0;
    public static final int NGUNG_HOAT_DONG = 1;
    public static final int DA_XOA = 2;

}
