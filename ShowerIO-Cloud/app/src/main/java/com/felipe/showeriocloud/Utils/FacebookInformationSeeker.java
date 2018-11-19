package com.felipe.showeriocloud.Utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;


public class FacebookInformationSeeker {


    private static final String TAG = "FacebookSeeker";
    private static AccessToken accessToken;
    public static String facebookProfilePhotoUrl;
    public static String facebookName;


    public static class GetFbInformation extends AsyncTask<Void, Void, JSONObject> {
        private final AccessToken accessToken;
        private ProgressDialog dialog;

        public GetFbInformation(AccessToken accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            // Application code
                            Log.v("LoginActivity", response.toString());
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,email,picture.type(large)");
            request.setParameters(parameters);
            GraphResponse graphResponse = request.executeAndWait();
            try {
                return graphResponse.getJSONObject();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final JSONObject response) {

            try {
                String profilePicUrl = response.getJSONObject("picture").getJSONObject("data").getString("url");
                facebookProfilePhotoUrl = profilePicUrl;
                facebookName = response.getJSONObject("name").getString("name");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "GetFbInformation returned with error " + e.getMessage());
            }
        }
    }
}
