package com.mdx.smartcontainer.app;

public class AppConfig {
    public static String HOST="http://192.168.43.195:7003/";
    public static String GOOGLE_LOGIN = HOST+"auth/google_sign_in";
    public static String FB_LOGIN = HOST+"auth/facebook_sign_in";
    public static String EMAIL_REGISTER = HOST+"auth/email_register";
    public static String EMAIL_LOGIN = HOST+"auth/email_login";
    public static String FORGOT_PASSWORD = HOST+"users/reset_password";
    public static String UPDATE_PROFILE = HOST+"users/update_profile";
    public static String UPDATE_WHOLE_APP = HOST+"app/update_whole_app";
    public static String ADD_CONTAINER_ONE = HOST+"container/add_container_one";
    public static String DETECT_OBJECT = HOST+"container/detect_object";
    public static String ADD_CONTAINER_TWO = HOST+"container/add_container_two";
    public static String CHECK_FOR_ONE = HOST+"container/check_for_one";
    public static String CALIBRATE = HOST+"container/calibrate";
    public static String GET_CONTAINERS = HOST+"container/get_containers";
    public static String GET_NOTIFICATIONS = HOST+"notification/get_notifications";
    public static String VIEW_CONTAINER = HOST+"container/view_containers";
    public static String DELETE_CONTAINER = HOST+"container/delete";
    public static String GET_SHOPPING_LIST = HOST+"shopping/get_shopping_list";
    public static String SET_BOUGHT = HOST+"shopping/set_bought";
    public static String GET_ALL_INGREDIENT = HOST+"meal/get_all_ingredient";
    public static String NEW_MEAL_SAVE = HOST+"meal/new_meal_save";
    public static String GET_MEAL_LIST = HOST+"meal/get_meal_list";
    public static String SUGGEST_MEAL_LIST = HOST+"meal/suggest_meal_list";
}
