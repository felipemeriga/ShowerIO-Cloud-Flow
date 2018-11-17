package com.felipe.showeriocloud.Activities.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.Aws.AwsDynamoDBManager;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.R;
import com.google.gson.Gson;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    private Button btnLoginFacebook;

    private CallbackManager callbackManager;

    public String failedAuthResult;
    public Boolean existingAccount = false;

    @BindView(R.id.input_email)
    public EditText _emailText;
    @BindView(R.id.input_password)
    public EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Autenticando...");
        progressDialog.setCanceledOnTouchOutside(false);
        _signupLink.setEnabled(false);
        this.initializeFacebookAuth();
    }

    public void initializeFacebookAuth() {

        /**
         * Initialize Facebook SDK
         */
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        btnLoginFacebook = (Button) findViewById(R.id.facebook_login_button);

        //If access token is already here, set fb session
        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        if (fbAccessToken != null) {
            setFacebookSession(fbAccessToken);
            btnLoginFacebook.setVisibility(View.GONE);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (CognitoSyncClientManager.credentialsProvider.getCredentials().getSessionToken().isEmpty()) {
                            Toast.makeText(LoginActivity.this, "Error in Facebook login ", Toast.LENGTH_LONG).show();
                            CognitoSyncClientManager.credentialsProvider.clearCredentials();
                            CognitoSyncClientManager.credentialsProvider.clear();

                        } else {
                            Log.d(TAG, "CognitoSyncClientManger returned a valid token, user is authenticated, changing activity");
                            initializeAwsServices();
                            AWSMobileClient.getInstance().setCredentialsProvider(CognitoSyncClientManager.credentialsProvider);
                            Intent testActivity = new Intent(LoginActivity.this, SearchForDevices.class);
                            startActivity(testActivity);
                            finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }

        btnLoginFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start Facebook Login
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        initializeAwsServices();
                        setFacebookSession(loginResult.getAccessToken());
                        progressDialog.dismiss();
                        Intent searchForDevices = new Intent(LoginActivity.this, SearchForDevices.class);
                        startActivity(searchForDevices);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        finish();
/*                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (CognitoSyncClientManager.credentialsProvider.getCredentials().getSessionToken().isEmpty()) {
                                        Toast.makeText(LoginActivity.this, "Error in Facebook login ", Toast.LENGTH_LONG).show();
                                        CognitoSyncClientManager.credentialsProvider.clearCredentials();
                                        CognitoSyncClientManager.credentialsProvider.clear();
                                        progressDialog.dismiss();

                                    } else {
                                        Log.d(TAG, "CognitoSyncClientManger returned a valid token, user is authenticated, changing activity");
                                        AWSMobileClient.getInstance().setCredentialsProvider(CognitoSyncClientManager.credentialsProvider);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        thread.start();*/

                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Facebook login cancelled",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(LoginActivity.this, "Error in Facebook login " +
                                error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        btnLoginFacebook.setEnabled(getString(R.string.facebook_app_id) != "facebook_app_id");
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        progressDialog.show();
        _loginButton.setEnabled(false);

    }

    public void initializeAwsServices() {
        //Initializing DynamoDB instances
        AwsDynamoDBManager awsDynamoDBManager = new AwsDynamoDBManager();
        awsDynamoDBManager.initializeDynamoDb();
    }


    public void onPostAuthenticate() {

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        progressDialog.dismiss();

    }

    public void onFailedPostAuthenticate() {
        if (failedAuthResult.equals("EMAIL")) {
            _emailText.setError("enter a valid email address");
            Log.d("LoginActivity Class", "Wrong Email");
            onLoginFailed();
        } else if (failedAuthResult.equals("PASSWORD")) {
            _passwordText.setError("enter a valid password");
            Log.d("LoginActivity Class", "Wrong Password");
            onLoginFailed();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void setFacebookSession(AccessToken accessToken) {
        Log.i(TAG, "facebook token: " + accessToken.getToken());
        CognitoSyncClientManager.addLogins("graph.facebook.com",
                accessToken.getToken());
//        btnLoginFacebook.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {


    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Erro de Login", Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Entre um endereço de email válido");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("Entre uma senha entre 4 e 10 dígitos");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public void accountExistance() {
        if (existingAccount == true) {
            _signupLink.setEnabled(false);
        } else {
            _signupLink.setEnabled(true);
        }
    }


}
