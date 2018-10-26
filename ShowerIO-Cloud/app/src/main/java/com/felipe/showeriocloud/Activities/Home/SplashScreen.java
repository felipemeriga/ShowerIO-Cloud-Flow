package com.felipe.showeriocloud.Activities.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import com.felipe.showeriocloud.R;


public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3500;
    private SharedPreferences sharedPreferences;
    private final String SHOWERIO = "ShowerIO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, SPLASH_TIME_OUT);

    }


}
