package com.felipe.showeriocloud.Model;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Utils.ServerCallback;

public class DevicePersistance {

    static DynamoDBMapper dynamoDBMapper;

    //Function used to get a single device
    void getSingleDevice(final String deviceName, final ServerCallback serverCallback) {

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    String message = "success";
                    DeviceDO deviceDO = dynamoDBMapper.load(DeviceDO.class, CognitoSyncClientManager.credentialsProvider.getCachedIdentityId(), deviceName);
                    if (CognitoSyncClientManager.credentialsProvider.getCredentials().getSessionToken().isEmpty()) {
                        message = "Actual User is not Authenticated";
                    } else if (deviceDO.equals(null)) {
                        message = "Any device with the name " + deviceName + " has been found";
                    }
                    serverCallback.onServerCallbackObject(true, message, deviceDO);
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallback.onServerCallbackObject(true, e.getMessage(), null);
                }

            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();

    }

}
