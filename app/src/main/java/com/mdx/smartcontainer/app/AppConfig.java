package com.mdx.smartcontainer.app;

public class AppConfig {
    public static String HOST="http://192.168.43.195:7003/";
    public static String GOOGLE_LOGIN=HOST+"auth/google_sign_in";
    public static String FB_LOGIN=HOST+"auth/facebook_sign_in";
    public static String EMAIL_REGISTER=HOST+"auth/email_register";
    public static String EMAIL_LOGIN=HOST+"auth/email_login";
    public static String FORGOT_PASSWORD=HOST+"users/reset_password";
    public static String UPDATE_PROFILE = HOST+"users/update_profile";
    public static String UPDATE_WHOLE_APP = HOST+"app/update_whole_app";
    public static String ADD_CONTAINER_ONE= HOST+"container/add_container_one";
    public static String DETECT_OBJECT= HOST+"container/detect_object";
}