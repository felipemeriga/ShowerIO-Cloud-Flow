package com.felipe.showeriocloud.Aws;


import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Regions;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Utils.ServerCallback;

// Initialize and handle IotCore web service to use MQTT protocols
public class AwsIotCoreManager {

    private static final String TAG = "AwsIotCoreManager";

    CognitoCachingCredentialsProvider credentialsProvider;

    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com";

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "us-east-1:50c009ca-a485-4e17-831a-ed522fb91724";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;

    static AWSIotMqttManager mqttManager;

    public AwsIotCoreManager() {

    }



    public void initializeIotCore(final String clientId, final String endpoint, final Activity activity, final ServerCallback serverCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    final AWSCredentialsProvider awsCredentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
                    final AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();

                    final CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = CognitoSyncClientManager.credentialsProvider;

                    mqttManager = new AWSIotMqttManager(clientId, endpoint);

                    credentialsProvider = new CognitoCachingCredentialsProvider(
                            activity.getApplicationContext(), // context
                            COGNITO_POOL_ID, // Identity Pool ID
                            MY_REGION // Region
                    );

                    try {
                        mqttManager.connect(credentialsProvider, new AWSIotMqttClientStatusCallback() {
                            @Override
                            public void onStatusChanged(final AWSIotMqttClientStatus status,
                                                        final Throwable throwable) {

                                Log.d(TAG, "Status = " + String.valueOf(status));

                                if (status == AWSIotMqttClientStatus.Connecting) {

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    serverCallback.onServerCallback(true,status.toString());

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(TAG, "Connection error.", throwable);
                                        serverCallback.onServerCallback(false,status.toString());
                                    }
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(TAG, "Connection error.", throwable);
                                        throwable.printStackTrace();
                                        serverCallback.onServerCallback(false,status.toString());
                                    }
                                } else {
                                    Log.e(TAG, "error matching the status");
                                    serverCallback.onServerCallback(false,"error matching the status");
                                }

                            }
                        });
                    } catch (final Exception e) {
                        Log.e(TAG, "Connection error.", e);
                        serverCallback.onServerCallback(false,e.getMessage());
                    }
                } catch (Exception e) {
                    serverCallback.onServerCallback(false, e.getMessage());
                }
            }
        }).start();

    }

    public void publishBathParams(int bathTime, int waitTime, int stoppedTime, DeviceDO device, ServerCallback serverCallback) {
        final String topic = "times";
        final String msg = bathTime + "-" + waitTime + "-" + stoppedTime;

        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            device.setBathTime(bathTime);
            device.setWaitTime(waitTime);
            device.setStoppedTime(stoppedTime);
            serverCallback.onServerCallback(true,"successful");
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
            serverCallback.onServerCallback(false, e.getMessage());
        }
    }

    public void publishReset(DeviceDO device, ServerCallback serverCallback){
        final String topic = "reset";
        final String msg = "reset";
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            device.setStatus("OFFLINE");
            serverCallback.onServerCallback(true,"successful");
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
            serverCallback.onServerCallback(false, e.getMessage());
        }
    }

    public void publishDelete(ServerCallback serverCallback){
        final String topic = "delete";
        final String msg = "delete";
        try {
            mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            serverCallback.onServerCallback(true,"successful");
        } catch (Exception e) {
            Log.e(TAG, "Publish error.", e);
            serverCallback.onServerCallback(false, e.getMessage());
        }
    }


}
