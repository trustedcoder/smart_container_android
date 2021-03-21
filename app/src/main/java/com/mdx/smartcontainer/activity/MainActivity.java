package com.mdx.smartcontainer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mdx.smartcontainer.R;
import com.mdx.smartcontainer.app.SessionManager;
import com.mdx.smartcontainer.fragment.HomeFragment;
import com.mdx.smartcontainer.fragment.MealFragment;
import com.mdx.smartcontainer.fragment.ShoppingFragment;
import com.mdx.smartcontainer.others.Bungee;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class MainActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private RelativeLayout notifyView;
    private ImageView logOutIcon;
    private TextView numberNotify;
    HomeFragment homeFragment = HomeFragment.newInstance();
    ShoppingFragment shoppingFragment = ShoppingFragment.newInstance();
    MealFragment mealFragment = MealFragment.newInstance();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            switch (item.getItemId()) {
                case R.id.navigation_shop:
                    fragmentManager.beginTransaction().replace(R.id.flContent, shoppingFragment).commit();
                    return true;
                case R.id.navigation_meal:
                    fragmentManager.beginTransaction().replace(R.id.flContent, mealFragment).commit();
                    return true;
                case R.id.navigation_home:
                default:
                    fragmentManager.beginTransaction().replace(R.id.flContent, homeFragment).commit();
                    return true;
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bungee.swipeLeft(MainActivity.this);
        setContentView(R.layout.activity_main);
        numberNotify = findViewById(R.id.numberNotify);
        initializeViews();

    }

    @Override
    public void onResume(){
        super.onResume();
        if (sessionManager.getTotalNotify() > 0){
            numberNotify.setVisibility(View.VISIBLE);
            numberNotify.setText(String.valueOf(sessionManager.getTotalNotify()));
        }
        else {
            numberNotify.setVisibility(View.GONE);
        }

    }

    private void initializeViews(){
        sessionManager = new SessionManager(getApplicationContext());
        toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        notifyView = findViewById(R.id.notifyView);
        logOutIcon = findViewById(R.id.logOutIcon);

        //load the first page
        navigation.setSelectedItemId(R.id.navigation_home);
        notifyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
            }
        });

        logOutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.setLogin(false);
                Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
