package com.mdx.smartcontainer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.others.Bungee;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class SplashExtendActivity extends Activity {
    private long ms = 0;
    private long splashTime = 1500;
    private boolean splashActive = true;
    private boolean paused = false;
    private SessionManager sessionManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_extend);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        changeStatusBarColor();

        Thread mythread = new Thread() {
            public void run() {
                try {
                    while (splashActive && ms < splashTime) {
                        if (!paused)
                            ms = ms + 100;
                        sleep(100);
                    }
                } catch (Exception e) {
                } finally {
                    sessionManager = new SessionManager(getApplicationContext());
                    if (sessionManager.isLoggedIn()) {
                        Intent intent = new Intent(SplashExtendActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(SplashExtendActivity.this, IntroActivity.class);
                        startActivity(intent);
                    }
                    finish();
                }
            }
        };
        mythread.start();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Bungee.fade(SplashExtendActivity.this);
    }
}