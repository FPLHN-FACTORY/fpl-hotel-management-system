package com.be.server.infrastructure.constant;

public class MappingConstants {

    /* API BASE ROLE */
    public static final String ADMIN = "/admin";
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
    public static final String API_ADMIN_PREFIX = API_VERSION_PREFIX + ADMIN;

    public static final String API_ADMIN_NHAN_VIEN= API_ADMIN_PREFIX + "/nhan-vien";

    public static final String API_ADMIN_ROOM = API_VERSION_PREFIX + "/room";


}
