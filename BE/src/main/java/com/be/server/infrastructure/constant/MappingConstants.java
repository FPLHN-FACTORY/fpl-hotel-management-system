package com.be.server.infrastructure.constant;

public class MappingConstants {

    /* API BASE ROLE */
    public static final String LE_TAN = "/leTan";
    public static final String STAFF = "/staff";

    /* API VERSION PREFIX */
    public static final String API_VERSION_PREFIX = "/api/v1";

    /* API COMMON */
    public static final String API_COMMON = API_VERSION_PREFIX + "/common";

    /* AUTHENTICATION */
    public static final String API_AUTH_PREFIX = API_VERSION_PREFIX + "/auth";

    /* Common */
    public static final String API_PERMITALL_PREFIX = API_VERSION_PREFIX + "/permitall";

    /* API FOR ROLE ADMIN */
    public static final String API_LE_TAN_PREFIX = API_VERSION_PREFIX + LE_TAN;

    public static final String API_ADMIN_NHAN_VIEN= API_LE_TAN_PREFIX + "/nhan-vien";

    public static final String API_LE_TAN_PHONG = API_LE_TAN_PREFIX + "/phong";

    public static final String API_LE_TAN_LOAI_PHONG = API_LE_TAN_PREFIX + "/loai-phong";


}
