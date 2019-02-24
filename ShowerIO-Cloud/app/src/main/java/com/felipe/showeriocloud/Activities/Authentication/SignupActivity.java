package com.felipe.showeriocloud.Activities.Authentication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChooseMfaContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerNavigationDrawer;
import com.felipe.showeriocloud.Aws.AuthorizationHandle;
import com.felipe.showeriocloud.Aws.AwsDynamoDBManager;
import com.felipe.showeriocloud.Aws.CognitoIdentityPoolManager;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private SharedPreferences sharedPreferences;
    @BindView(R.id.input_name)
    EditText _nameText;
    @BindView(R.id.input_email_sign)
    EditText _emailText;
    @BindView(R.id.input_password_sign)
    EditText _passwordText;
    @BindView(R.id.input_telephone)
    EditText _telefoneText;
    @BindView(R.id.btn_signup)
    Button _signupButton;
    @BindView(R.id.link_login)
    TextView _loginLink;
    String espIpAddress;
    private final String SHOWERLITE = "ShowerLite";
    private final String CREDENTIALS_URL = "/createCredentials?email=";
    private Boolean createCredentialsFlag = false;
    private ProgressDialog progressDialog;
    private AlertDialog userDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        sharedPreferences = getSharedPreferences(SHOWERLITE, MODE_PRIVATE);

        progressDialog = new ProgressDialog(SignupActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Criando uma conta...");
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        progressDialog.show();

        _signupButton.setEnabled(false);

        onAuthorizedSignup();

    }

    public void onAuthorizedSignup() {

        CognitoUserAttributes userAttributes = new CognitoUserAttributes();

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String phone = _telefoneText.getText().toString();

        if (name != null) {
            if (name.length() > 0) {
                userAttributes.addAttribute(CognitoIdentityPoolManager.getSignUpFieldsC2O().get("Given name").toString(), name);
            }
        }

        userAttributes.addAttribute(CognitoIdentityPoolManager.getSignUpFieldsC2O().get("Phone number").toString(), "+55" + phone);
        userAttributes.addAttribute(CognitoIdentityPoolManager.getSignUpFieldsC2O().get("Email").toString(), email);


        CognitoIdentityPoolManager.getPool().signUpInBackground(email, password, userAttributes, null, signUpHandler);

    }

    private void showDialogMessage(String title, String body, final boolean exit) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(body).setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userDialog.dismiss();
                progressDialog.show();
                AuthorizationHandle.mainAuthMethod = AuthorizationHandle.COGNITO_POOL;
                AuthorizationHandle.setCredentialsProvider(getApplicationContext());
                AuthorizationHandle.setSession();
                initializeAwsServices();

                DevicePersistance.getAllDevicesFromUser(new ServerCallbackObjects() {
                    @Override
                    public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                        // TODO - CREATE A TRY CATCH AND RETURN != NULL IF THERE IS A CONNECTION ERROR
                        Intent listOfDevices = new Intent(SignupActivity.this, ShowerNavigationDrawer.class);
                        startActivity(listOfDevices);
                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        finish();
                    }
                });

            }
        });
        userDialog = builder.create();
        userDialog.show();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Erro de Login", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        progressDialog.dismiss();
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _telefoneText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("Pelo menos 3 dígitos");
            valid = false;
        } else {
            _nameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Entre um endereço de email válido");
            valid = false;
        } else {
            _emailText.setError(null);
        }


        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("entre uma senha entre 4 e 10 dígitos");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    SignUpHandler signUpHandler = new SignUpHandler() {
        @Override
        public void onSuccess(CognitoUser user, boolean signUpConfirmationState,
                              CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            // Check signUpConfirmationState to see if the user is already confirmed
            //CognitoIdentityPoolManager.getPool().getUser(userName).confirmSignUpInBackground(confirmCode, true, confHandler);
            Boolean regState = signUpConfirmationState;
            CognitoIdentityPoolManager.getPool().getUser(_emailText.getText().toString()).getSessionInBackground(authenticationHandler);
        }

        @Override
        public void onFailure(Exception exception) {
            // TODO - HANDLE THE DIFFERENT TYPES OF EXCEPTION
            onSignupFailed();
        }
    };

    AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(CognitoUserSession cognitoUserSession, CognitoDevice device) {
            Log.d(TAG, " -- Auth Success");
            CognitoIdentityPoolManager.setCurrSession(cognitoUserSession);
            CognitoIdentityPoolManager.newDevice(device);
            progressDialog.dismiss();
            showDialogMessage("Cadastro","Usuário salvo com sucesso!",false);
            SharedPreferences.Editor editor = getSharedPreferences(SHOWERLITE, MODE_PRIVATE).edit();
            editor.putString("email",_emailText.getText().toString());
            editor.putString("password",_passwordText.getText().toString());
            editor.apply();

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
            onSignupFailed();
        }


    };

    public void initializeAwsServices() {
        //Initializing DynamoDB instances
        AwsDynamoDBManager awsDynamoDBManager = new AwsDynamoDBManager();
        awsDynamoDBManager.initializeDynamoDb();
    }


    private void getUserAuthentication(AuthenticationContinuation continuation, String username) {
        if(username != null) {
            CognitoIdentityPoolManager.setUser(username);
        }
        AuthenticationDetails authenticationDetails = new AuthenticationDetails(_emailText.getText().toString(), _passwordText.getText().toString(), null);
        continuation.setAuthenticationDetails(authenticationDetails);
        continuation.continueTask();
    }


    @Override
    public void onBackPressed() {
        Intent loginActivity = new Intent(SignupActivity.this, LoginActivity.class);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        startActivity(loginActivity);
        finish();
    }
}