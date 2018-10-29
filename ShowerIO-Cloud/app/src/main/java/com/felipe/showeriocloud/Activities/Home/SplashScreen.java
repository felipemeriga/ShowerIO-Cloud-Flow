package com.felipe.showeriocloud.Activities.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.R;



public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {

                //Start
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*Intent loginActivity = new Intent(SplashScreen.this, LoginActivity.class);
                        startActivity(loginActivity);
                        finish();*/
                        Intent searchForDevicesActivity = new Intent(SplashScreen.this, SearchForDevices.class);
                        startActivity(searchForDevicesActivity);
                        finish();


                    }
                }, SPLASH_TIME_OUT);

            }
        }).execute();

    }

}
