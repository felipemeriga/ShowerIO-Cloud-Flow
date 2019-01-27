package com.felipe.showeriocloud.Utils;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.felipe.showeriocloud.Model.BathStatisticsDailyDO;
import com.felipe.showeriocloud.Model.BathStatisticsMonthly;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class StatisticsUtils {

    private static final String TAG = "StatisticsUtils";
    public ServerCallbackObject callbackObject;
    public ServerCallbackObjects callbackObjects;
    private Gson gson;
    private final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public StatisticsUtils() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
    }


    public void getDailyStatistics(String year, String month, String dayOfMonth, DeviceDO deviceDO, RequestQueue requestQueue, final ServerCallbackObjects serverCallbackObjects) {
        String ENDPOINT = "https://tnuxgabida.execute-api.us-east-1.amazonaws.com/Tst?";
        String dateToFilter = year + "-" + month + "-" + dayOfMonth;
        ENDPOINT = ENDPOINT + "microprocessorId=" + deviceDO.getMicroprocessorId() + "&userId=" + deviceDO.getUserId() + "&date=" + dateToFilter;
        Log.i(TAG, "getDailyStatistics() Doing the HTTP GET request on ENDPOINT: " + ENDPOINT);
        requestQueue.add(new StringRequest(Request.Method.GET, ENDPOINT, onDailySuccessful, onDailyError));
        this.callbackObjects = serverCallbackObjects;
    }


    public Response.Listener<String> onDailySuccessful = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onPostsLoaded() The HTTP request was done successfully, getting the parameters from the response");
            List<BathStatisticsDailyDO> statistics = new ArrayList<>();

            JsonParser jsonParser = new JsonParser();
            JsonObject json = (JsonObject) jsonParser.parse(response.toString());
            JsonArray jsonArray = (JsonArray) json.get("body");
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

            for (int i = 0; i < jsonArray.size(); i++) {
                BathStatisticsDailyDO bathStatisticsDailyDO = gson.fromJson(jsonArray.get(i).toString(), BathStatisticsDailyDO.class);
                try {
                    Date parsedDate = dateFormat.parse(jsonArray.get(i).getAsJsonObject().get("bathDateTime").getAsString());
                    Timestamp timestamp = new Timestamp(parsedDate.getTime());
                    bathStatisticsDailyDO.setBathTimestamp(timestamp);
                    statistics.add(bathStatisticsDailyDO);
                } catch (Exception e) {
                    Log.e(TAG, "Date format error", e);
                    callbackObjects.onServerCallbackObject(false, e.getMessage(), null);
                }
            }

            callbackObjects.onServerCallbackObject(true, "SUCCESS", (List<Object>) (List<?>) statistics);

        }
    };

    public Response.ErrorListener onDailyError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onPostsError() Something wrong happened with the request. Error: " + error.getMessage());
            callbackObjects.onServerCallbackObject(false, error.getMessage(), null);
        }
    };



    public void getMonthlyStatistics(String year, String month, DeviceDO deviceDO, RequestQueue requestQueue, final ServerCallbackObject serverCallbackObject) {
        String ENDPOINT = "https://qhqsyqomla.execute-api.us-east-1.amazonaws.com/tst?";
        ENDPOINT = ENDPOINT + "microprocessorId=" + deviceDO.getMicroprocessorId() + "&userId=" + deviceDO.getUserId() + "&month=" + month + "&year=" + year;
        Log.i(TAG, "getMonthlyStatistics() Doing the HTTP GET request on ENDPOINT: " + ENDPOINT);
        requestQueue.add(new StringRequest(Request.Method.GET, ENDPOINT, onMonthlySuccessful, onMonthlyError));
        this.callbackObject = serverCallbackObject;

    }


    public Response.Listener<String> onMonthlySuccessful = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onPostsLoaded() The HTTP request was done successfully, getting the parameters from the response");
            JsonParser jsonParser = new JsonParser();
            JsonObject json = (JsonObject) jsonParser.parse(response.toString());
            JsonObject jsonResponse = (JsonObject) json.get("body");
            BathStatisticsMonthly bathStatisticsMonthly = gson.fromJson(jsonResponse.toString(), BathStatisticsMonthly.class);
            callbackObject.onServerCallbackObject(true,"SUCCESS", bathStatisticsMonthly);

        }


    };

    public Response.ErrorListener onMonthlyError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onPostsError() Something wrong happened with the request. Error: " + error.getMessage());
            callbackObject.onServerCallbackObject(false,error.getMessage(),null);
        }
    };

}
