package com.mdx.smartcontainer.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.activity.IntroActivity;
import com.mdx.smartcontainer.activity.SplashActivity;
import com.mdx.smartcontainer.activity.SplashExtendActivity;
import com.mdx.smartcontainer.activity.WelcomeActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;


public class AppController extends Application {
    public static final String TAG = AppController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private static AppController mInstance;
    private ScheduledExecutorService foregroundScheduler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/helvetica_normal.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
        mInstance = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }

    public void startSchedule(final Activity activity){
        if(foregroundScheduler == null){
            foregroundScheduler = Executors.newSingleThreadScheduledExecutor();
            foregroundScheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            HelperClass.updateUserDetails(activity);
                        }
                    });
                }
            }, 5, 5, TimeUnit.SECONDS);

        }
    }

    public void stopScheduleGetUserDetails(){
        if (!(foregroundScheduler == null)){
            foregroundScheduler.shutdownNow();
            foregroundScheduler = null;
        }
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        public void onActivityCreated(final Activity activity, Bundle bundle) {

            if(activity instanceof WelcomeActivity){

            }
            else if (activity instanceof SplashActivity){

            }
            else if (activity instanceof SplashExtendActivity){

            }
            else if (activity instanceof IntroActivity){

            }
            else {
                startSchedule(activity);
            }
        }

        public void onActivityDestroyed(Activity activity) {

        }

        public void onActivityPaused(Activity activity) {
            if(activity instanceof WelcomeActivity){

            }
            else if (activity instanceof SplashActivity){

            }
            else if (activity instanceof SplashExtendActivity){

            }
            else if (activity instanceof IntroActivity){

            }
            else {
                stopScheduleGetUserDetails();
            }
        }

        public void onActivityResumed(final Activity activity) {
            if(activity instanceof WelcomeActivity){

            }
            else if (activity instanceof SplashActivity){

            }
            else if (activity instanceof SplashExtendActivity){

            }
            else if (activity instanceof IntroActivity){

            }
            else {
                startSchedule(activity);
            }
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        public void onActivityStarted(final Activity activity) {
            if(activity instanceof WelcomeActivity){

            }
            else if (activity instanceof SplashActivity){

            }
            else if (activity instanceof SplashExtendActivity){

            }
            else if (activity instanceof IntroActivity){

            }
            else {
                startSchedule(activity);
            }
        }

        public void onActivityStopped(Activity activity) {

        }
    }

}