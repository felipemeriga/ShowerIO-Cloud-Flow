package com.felipe.showeriocloud.Aws;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.facebook.AccessToken;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;

public class AuthorizationHandle {

    public static String COGNITO_POOL = "COGNITO_POOL";
    public static String FEDERATED_IDENTITIES = "FEDERATED_IDENTITIES";
    public static String NOT_SIGNED = "NOT_SIGNED";
    public static CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;

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
        if(username != null) {
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

        if(mainAuthMethod.equals(COGNITO_POOL)){
            cognitoCachingCredentialsProvider =  new CognitoCachingCredentialsProvider(
                    context,
                    CognitoIdentityPoolManager.getUserPoolId(),
                    CognitoIdentityPoolManager.getCognitoRegion());
        } else if(mainAuthMethod.equals(FEDERATED_IDENTITIES)){
            cognitoCachingCredentialsProvider = CognitoSyncClientManager.credentialsProvider;
        }

    }
}
