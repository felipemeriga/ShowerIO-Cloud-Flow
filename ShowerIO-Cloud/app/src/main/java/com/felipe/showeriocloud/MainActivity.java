package com.felipe.showeriocloud;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.facebook.FacebookButton;
import com.amazonaws.mobile.auth.google.GoogleButton;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                AuthUIConfiguration config =
                        new AuthUIConfiguration.Builder()
                                .userPools(true)  // true? show the Email and Password UI
                                .signInButton(FacebookButton.class) // Show Facebook button
                                .signInButton(GoogleButton.class) // Show Google button
                                .logoResId(R.drawable.showerlogo) // Change the logo
                                .backgroundColor(Color.CYAN) // Change the backgroundColor
                                .isBackgroundColorFullScreen(true) // Full screen backgroundColor the backgroundColor full screen
                                .fontFamily("sans-serif-light") // Apply sans-serif-light as the global font
                                .canCancel(true)
                                .build();
                SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(MainActivity.this, SignInUI.class);
                signin.login(MainActivity.this, Main2Activity.class).execute();
            }
        }).execute();

    }
}
