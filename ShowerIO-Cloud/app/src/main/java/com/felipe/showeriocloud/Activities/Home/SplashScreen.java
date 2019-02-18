package com.felipe.showeriocloud.Activities.Home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Activities.Authentication.SignupActivity;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerListActivity;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerNavigationDrawer;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.Aws.AuthorizationHandle;
import com.felipe.showeriocloud.Aws.AwsDynamoDBManager;
import com.felipe.showeriocloud.Aws.CognitoIdentityPoolManager;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;


public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 0;
    private static final String TAG = "SplashScreen";
    private ImageView imageView;
    private SharedPreferences sharedPreferences;
    private static final String SHOWERLITE = "ShowerLite";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //AuthorizationHandle.mainAuthMethod = AuthorizationHandle.NOT_SIGNED;
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences(SHOWERLITE, MODE_PRIVATE);

        /**
         * Initializes the sync client. This must be call before you can use it.
         */
        AuthorizationHandle.initializeAuthMethods(getApplicationContext());
        AuthorizationHandle.verifySignedAccounts();

//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//            @Override
//            public void onComplete(AWSStartupResult awsStartupResult) {

                //awsStartupResult.isIdentityIdAvailable();
                //Start
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       if(AuthorizationHandle.mainAuthMethod.equals(AuthorizationHandle.COGNITO_POOL)){
                            String email = sharedPreferences.getString("email", null);
                            String password = sharedPreferences.getString("password", null);
                            if(email == null || password == null){
                                Log.d(TAG, "There isn't a saved email and password in shared preferences, going to LoginActivity");
                                Intent loginActivity = new Intent(SplashScreen.this, LoginActivity.class);
                                startActivity(loginActivity);
                                CognitoSyncClientManager.credentialsProvider.clearCredentials();
                                CognitoSyncClientManager.credentialsProvider.clear();
                                finish();
                            } else {
                                CognitoUser user = CognitoIdentityPoolManager.getPool().getCurrentUser();
                                user.getSessionInBackground(authenticationHandler);


                            }

                       } else if(AuthorizationHandle.mainAuthMethod.equals(AuthorizationHandle.FEDERATED_IDENTITIES)){
                           final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
                           new FacebookInformationSeeker.GetFbInformation(fbAccessToken).execute();

                           if (fbAccessToken != null) {
                               AuthorizationHandle.setSession();
                               Thread thread = new Thread(new Runnable() {
                                   @Override
                                   public void run() {
                                       try {
                                           if (CognitoSyncClientManager.credentialsProvider.getCredentials().getSessionToken().isEmpty()) {
                                               Toast.makeText(SplashScreen.this, "Error in Facebook login ", Toast.LENGTH_LONG).show();
                                               AuthorizationHandle.mainAuthMethod = AuthorizationHandle.NOT_SIGNED;
                                               CognitoSyncClientManager.credentialsProvider.clearCredentials();
                                               CognitoSyncClientManager.credentialsProvider.clear();
                                               Intent loginActivity = new Intent(SplashScreen.this, LoginActivity.class);
                                               startActivity(loginActivity);
                                               finish();
                                           } else {
                                               AuthorizationHandle.mainAuthMethod = AuthorizationHandle.FEDERATED_IDENTITIES;
                                               AuthorizationHandle.setCredentialsProvider(getApplicationContext());

                                               Log.d(TAG, "CognitoSyncClientManger returned a valid token, user is authenticated, changing activity");
                                               initializeAwsServices();
                                               //AWSMobileClient.getInstance().setCredentialsProvider(CognitoSyncClientManager.credentialsProvider);

                                               DevicePersistance.getAllDevicesFromUser(new ServerCallbackObjects() {
                                                   @Override
                                                   public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                                                       // TODO - CREATE A TRY CATCH AND RETURN != NULL IF THERE IS A CONNECTION ERROR
                                                       Intent listOfDevices = new Intent(SplashScreen.this, ShowerNavigationDrawer.class);
                                                       startActivity(listOfDevices);
                                                       overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                                       finish();
                                                   }
                                               });
                                           }
                                       } catch (Exception e) {
                                           e.printStackTrace();
                                       }
                                   }
                               });
                               thread.start();

                           } else {
                               Intent loginActivity = new Intent(SplashScreen.this, LoginActivity.class);
                               startActivity(loginActivity);
                               finish();
                           }

                       } else {
                           Intent loginActivity = new Intent(SplashScreen.this, LoginActivity.class);
                           startActivity(loginActivity);
                           finish();
                       }

                    }
                }, SPLASH_TIME_OUT);
//            }
//        }).execute();
    }

    public void initializeAwsServices() {
        //Initializing DynamoDB instances
        AwsDynamoDBManager awsDynamoDBManager = new AwsDynamoDBManager();
        awsDynamoDBManager.initializeDynamoDb();
    }

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.d(TAG, " -- Auth Success");
            CognitoIdentityPoolManager.setCurrSession(cognitoUserSession);
            CognitoIdentityPoolManager.newDevice(device);
            AuthorizationHandle.setCredentialsProvider(getApplicationContext());
            AuthorizationHandle.setSession();
            initializeAwsServices();

            DevicePersistance.getAllDevicesFromUser(new ServerCallbackObjects() {
                @Override
                public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                    // TODO - CREATE A TRY CATCH AND RETURN != NULL IF THERE IS A CONNECTION ERROR
                    Intent listOfDevices = new Intent(SplashScreen.this, ShowerNavigationDrawer.class);
                    startActivity(listOfDevices);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    finish();
                }
            });
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String username) {
            getUserAuthentication(authenticationContinuation, username);
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

        }

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {

        }

        @Override
        public void onFailure(Exception e) {

        }


    };

    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            CognitoIdentityPoolManager.setUser(username);
        }
        String email = sharedPreferences.getString("email", null);
        String password = sharedPreferences.getString("password", null);
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(email,password, null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }



}
