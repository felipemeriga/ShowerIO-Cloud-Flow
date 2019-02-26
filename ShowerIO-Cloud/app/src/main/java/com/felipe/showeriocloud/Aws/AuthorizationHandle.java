package com.felipe.showeriocloud.Aws;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.facebook.AccessToken;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;

public class AuthorizationHandle {

    public static String TAG = "AuthorizationHandle";

    public static String COGNITO_POOL = "COGNITO_POOL";
    public static String FEDERATED_IDENTITIES = "FEDERATED_IDENTITIES";
    public static String NOT_SIGNED = "NOT_SIGNED";
    public static CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    public static CognitoUserAttributes cognitoUserAttributes;

    public static String mainAuthMethod;


    public static void initializeAuthMethods(Context context) {
        //Initializing CognitoSync for Federated Identities and Oauth2
        CognitoSyncClientManager.init(context);
        //Initializing Cognito Identity Pool for direct email/password(registration) login
        CognitoIdentityPoolManager.init(context);
    }

    public static void verifySignedAccounts() {

        CognitoUser user = CognitoIdentityPoolManager.getPool().getCurrentUser();
        String username = user.getUserId();
        if (username != null) {
            mainAuthMethod = COGNITO_POOL;
            return;
        }

        final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
        new FacebookInformationSeeker.GetFbInformation(fbAccessToken).execute();
        if (fbAccessToken != null) {
            mainAuthMethod = FEDERATED_IDENTITIES;
            return;
        }

        mainAuthMethod = NOT_SIGNED;
    }

    public static void setCredentialsProvider(Context context) {
        cognitoCachingCredentialsProvider = CognitoSyncClientManager.credentialsProvider;
    }

    public static String getCurrentUserId() {
        if (mainAuthMethod.equals(COGNITO_POOL)) {
            CognitoIdentityPoolManager.getCurrUser();
        } else if (mainAuthMethod.equals(FEDERATED_IDENTITIES)) {
            return FacebookInformationSeeker.facebookEmail;
        }

        return NOT_SIGNED;
    }

    public static void setSession() {
        if(mainAuthMethod.equals(FEDERATED_IDENTITIES)){
            final AccessToken fbAccessToken = AccessToken.getCurrentAccessToken();
            new FacebookInformationSeeker.GetFbInformation(fbAccessToken).execute();
            setFacebookSession(fbAccessToken);

        } else if(mainAuthMethod.equals(COGNITO_POOL)){
            setCognitoPoolSession();
        }
    }

    private static void setFacebookSession(AccessToken accessToken) {
        Log.i(TAG, "facebook token: " + accessToken.getToken());
        CognitoSyncClientManager.addLogins("graph.facebook.com",
                accessToken.getToken());
    }

    private static void setCognitoPoolSession() {
        String token = CognitoIdentityPoolManager.getCurrSession().getIdToken().getJWTToken();
        Log.i(TAG, "Cognito Pool token: " + token);
        CognitoSyncClientManager.addLogins(CognitoSyncClientManager.COGNITO_POOL_PROVIDER_IDENTIFIER,
                token);
    }
}
