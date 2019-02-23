package com.felipe.showeriocloud.Processes;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.felipe.showeriocloud.Utils.ServerCallbackObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

public class RegisterNewDevices {

    public ServerCallback callback;
    private static final String TAG = "RegisterNewDevices";
    private Gson gson;

    public RegisterNewDevices() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
    }

    public void createNewDevice(IEsptouchResult result, RequestQueue requestQueue, final ServerCallback callback) {

        //new CreateOrUpdateNewDevice(result,callback).execute();

        final String ENDPOINT = "http://" + result.getInetAddress().getHostAddress() + "/check";
        //The http request will be done on the ENDPOINT in order to retrieve the private IOT params of the ESP8266
        Log.i(TAG, "createNewDevice() Doing the HTTP GET request on ENDPOINT: " + ENDPOINT);
        requestQueue.add(new StringRequest(Request.Method.GET, ENDPOINT, onPostsLoaded, onPostsError));
        this.callback = callback;
    }

    public Response.Listener<String> onPostsLoaded = new Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
            Log.i(TAG, "onPostsLoaded() The HTTP request was done successfully, getting the parameters from the response");
//            callback.onServerCallback(true, response);
            DeviceDO deviceDO = gson.fromJson(response.toString(), DeviceDO.class);
            boolean alreadyExists = false;
            if (DevicePersistance.lastUpdateUserDevices.size() > 0) {
                for (DeviceDO device : DevicePersistance.lastUpdateUserDevices) {
                    if (device.getStatus().equals("OFFLINE")) {
                        if (device.getMicroprocessorId().equals(deviceDO.getMicroprocessorId())) {
                            alreadyExists= true;
                            device.setStatus("ONLINE");
                            DevicePersistance.fastUpdateDevice(device);
                            callback.onServerCallback(true, "SUCCESS");
                        }
                    }
                }
            }
            if(!alreadyExists){
                DevicePersistance.insertNewDevice(deviceDO, new ServerCallbackObject() {
                    @Override
                    public void onServerCallbackObject(Boolean status, String response, Object object) {
                        if (status) {
                            callback.onServerCallback(true, "SUCCESS");
                        } else {
                            callback.onServerCallback(false, response);
                        }
                    }
                });
            }
        }
    };

    public Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onPostsError() Something wrong happened with the request. Error: " + error.getMessage());
            callback.onServerCallback(false, error.getMessage());
        }
    };


  /*  public class CreateOrUpdateNewDevice extends AsyncTask<Void, Void, Void> {

        IEsptouchResult result;
        ServerCallback serverCallback;

        public CreateOrUpdateNewDevice(IEsptouchResult result, ServerCallback serverCallback) {
            super();
            this.result = result;
            this.serverCallback = serverCallback;
        }


        protected void onRequestFinished() {

        }


        @Override
        protected Void doInBackground(Void... records) {
            final String ENDPOINT = "http://" + result.getInetAddress().getHostAddress() + "/check";
            OkHttpClient client = new OkHttpClient();
            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ENDPOINT)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    Log.i(TAG, "onPostsLoaded() The HTTP request was done successfully, getting the parameters from the response");
//                  callback.onServerCallback(true, response);
                    DeviceDO deviceDO = gson.fromJson(response.body().string(), DeviceDO.class);
                    boolean alreadyExists = false;
                    if (DevicePersistance.lastUpdateUserDevices.size() > 0) {
                        for (DeviceDO device : DevicePersistance.lastUpdateUserDevices) {
                            if (device.getStatus().equals("OFFLINE")) {
                                if (device.getMicroprocessorId().equals(deviceDO.getMicroprocessorId())) {
                                    alreadyExists= true;
                                    device.setStatus("ONLINE");
                                    DevicePersistance.fastUpdateDevice(device);
                                    callback.onServerCallback(true, "SUCCESS");
                                }
                            }
                        }
                    }
                    if(!alreadyExists){
                        DevicePersistance.insertNewDevice(deviceDO, new ServerCallbackObject() {
                            @Override
                            public void onServerCallbackObject(Boolean status, String response, Object object) {
                                if (status) {
                                    callback.onServerCallback(true, "SUCCESS");
                                } else {
                                    callback.onServerCallback(false, response);
                                }
                            }
                        });
                    }
                }
            });
            return null;
        }
    }*/
}
