package com.mdx.smartcontainer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mdx.smartcontainer.activity.WelcomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by celestine on 13/03/2018.
 */

public class HelperClass {

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static void updateUserDetails(final Activity activity) {
        final SessionManager sessionManager = new SessionManager(activity.getApplicationContext());
        Map<String, String> params = new HashMap<String, String>();
        params.put("fcm_token", "fcm_token");
        JSONObject parameters= new JSONObject(params);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.UPDATE_WHOLE_APP,parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.has("status")){
                        int status = response.getInt("status");
                        if (status == 1) {
                            sessionManager.setEmail(response.getString("email"));
                            sessionManager.setTotalNotify(response.getInt("total_notify"));
                            sessionManager.setFullname(response.getString("fullname"));

                        }
                        else {
                            if (sessionManager.isLoggedIn()){
                                sessionManager.setEmail("");
                                sessionManager.setTotalNotify(0);
                                sessionManager.setFullname("");
                                sessionManager.setAuth("");
                                sessionManager.setLogin(false);
                                final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
                                builder.setMessage(Html.fromHtml(response.getString("message")))
                                        .setNeutralButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface d, int id) {
                                                        d.cancel();
                                                        Intent intent = new Intent(activity.getApplicationContext(), WelcomeActivity.class);
                                                        activity.startActivity(intent);
                                                        activity.finish();
                                                    }
                                                })
                                        .setCancelable(false);
                                builder.create().show();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("authorization", sessionManager.getAuth());
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(jsonRequest);
    }
}
