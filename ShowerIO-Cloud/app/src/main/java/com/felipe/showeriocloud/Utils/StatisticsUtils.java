package com.felipe.showeriocloud.Utils;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.felipe.showeriocloud.Model.BathStatisticsDailyDO;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatisticsUtils {

    private static final String TAG = "StatisticsUtils";
    public ServerCallback callback;
    public ServerCallbackObjects callbackObjects;
    private Gson gson;


    public StatisticsUtils() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
    }


    public void getDailyStatistics(DeviceDO deviceDO, RequestQueue requestQueue, final ServerCallbackObjects serverCallbackObjects) {
        String ENDPOINT = "https://sn62jy992i.execute-api.us-east-1.amazonaws.com/tst?";
        ENDPOINT = ENDPOINT + "microprocessorId=" + deviceDO.getMicroprocessorId() + "&userId=" + deviceDO.getUserId();

        Log.i(TAG, "getMonthlyStatistics() Doing the HTTP GET request on ENDPOINT: " + ENDPOINT);
        requestQueue.add(new StringRequest(Request.Method.GET, ENDPOINT, onDailySuccessful, onDailyError));
        this.callbackObjects = serverCallbackObjects;
    }


    public Response.Listener<String> onDailySuccessful = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onPostsLoaded() The HTTP request was done successfully, getting the parameters from the response");
            List<BathStatisticsDailyDO> statistics = new ArrayList<>();
            statistics = Arrays.asList(new Gson().fromJson(response.toString(), BathStatisticsDailyDO[].class));
            callbackObjects.onServerCallbackObject(true, "SUCCESS", (List<Object>) (List<?>) statistics);

        }
    };

    public Response.ErrorListener onDailyError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onPostsError() Something wrong happened with the request. Error: " + error.getMessage());
            callbackObjects.onServerCallbackObject(false, error.getMessage(),null);
        }
    };


}
