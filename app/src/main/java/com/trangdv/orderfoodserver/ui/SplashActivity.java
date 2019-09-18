package com.trangdv.orderfoodserver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.utils.SharedPrefs;

public class SplashActivity extends AppCompatActivity {
    protected int TIME_LOADING = 3000;
    public static final String CHECK_ALREADLY_LOGIN = "check already login";
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        onAfterView();
    }

    void onAfterView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onNext();
            }
        }, TIME_LOADING);
    }

    public void onNext() {
        Intent intent;
        int value = Integer.valueOf(SharedPrefs.getInstance().get(CHECK_ALREADLY_LOGIN, Integer.class, 0));
        if (value==0) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
