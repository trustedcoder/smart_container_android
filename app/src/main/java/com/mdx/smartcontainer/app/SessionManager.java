package com.mdx.smartcontainer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
    SharedPreferences pref;
    Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "smartcontainer";
    private static final String KEY_AUTH = "session";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_EMAIL_VERIFIED = "is_email_verified";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_EMAIL = "email";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setAuth(String data) {
        editor.putString(KEY_AUTH, data);
        editor.commit();
    }

    public String getAuth(){
        return pref.getString(KEY_AUTH, "");
    }

    public void setLogin(boolean data) {
        editor.putBoolean(KEY_IS_LOGGEDIN, data);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public void setUsername(String data) {
        editor.putString(KEY_USERNAME, data);
        editor.commit();
    }

    public String getUsername(){
        return pref.getString(KEY_USERNAME, "");
    }

    public void setEmailVerified(boolean data) {
        editor.putBoolean(KEY_IS_EMAIL_VERIFIED, data);
        editor.commit();
    }

    public boolean isEmailVerified(){
        return pref.getBoolean(KEY_IS_EMAIL_VERIFIED, true);
    }

    public void setImage(String data) {
        editor.putString(KEY_IMAGE, data);
        editor.commit();
    }

    public String getImage(){
        return pref.getString(KEY_IMAGE, "no_pix.png");
    }


    public void setEmail(String data) {
        editor.putString(KEY_EMAIL, data);
        editor.commit();
    }

    public String getEmail(){
        return pref.getString(KEY_EMAIL, "");
    }

}