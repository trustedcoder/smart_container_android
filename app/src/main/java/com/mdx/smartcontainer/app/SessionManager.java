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
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_TOTAL_NOTIFY = "total_notify";
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

    public void setFullname(String data) {
        editor.putString(KEY_FULLNAME, data);
        editor.commit();
    }

    public String getFullname(){
        return pref.getString(KEY_FULLNAME, "");
    }

    public void setTotalNotify(int data) {
        editor.putInt(KEY_TOTAL_NOTIFY, data);
        editor.commit();
    }

    public int getTotalNotify(){
        return pref.getInt(KEY_TOTAL_NOTIFY, 0);
    }

    public void setEmail(String data) {
        editor.putString(KEY_EMAIL, data);
        editor.commit();
    }

    public String getEmail(){
        return pref.getString(KEY_EMAIL, "");
    }

}