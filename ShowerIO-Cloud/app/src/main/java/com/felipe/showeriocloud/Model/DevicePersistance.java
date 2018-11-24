package com.felipe.showeriocloud.Model;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.felipe.showeriocloud.Aws.AwsDynamoDBManager;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.felipe.showeriocloud.Utils.ServerCallbackObject;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;

import java.util.List;

public class DevicePersistance {

    public static List<DeviceDO> lastUpdateUserDevices;
    public static boolean updatedUserDevices = true;
    public static DeviceDO selectedDevice;

/*    //Function used to get a single device
    public static void getSingleDevice(final String deviceName, final ServerCallbackObject serverCallback) {

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

*/
    //Function used to get a single device
    public static void getAllDevicesFromUser(final ServerCallbackObjects serverCallbackObjects) {

        DeviceDO hashKeyObject = new DeviceDO();
        hashKeyObject.setUserId(CognitoSyncClientManager.credentialsProvider.getCachedIdentityId());


        Condition rangeAndHashKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.NOT_NULL);


         final DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withHashKeyValues(hashKeyObject)
                .withConsistentRead(false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeviceDO> result = AwsDynamoDBManager.dynamoDBMapper.query(DeviceDO.class, queryExpression);
                    lastUpdateUserDevices = result;
                    serverCallbackObjects.onServerCallbackObject(true,"SUCCESS",(List<Object>) (List<?>) result);
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallbackObjects.onServerCallbackObject(false,e.getMessage(),null);


                }
            }
        });
        thread.start();


    }

    //Function used to get a single device
    public static void insertNewDevice(final DeviceDO deviceDO, final ServerCallbackObject serverCallback) {
        deviceDO.setUserId(CognitoSyncClientManager.credentialsProvider.getCachedIdentityId());
        int lastPointSubnet = deviceDO.getLocalNetworkSubnet().lastIndexOf(".");
        deviceDO.setLocalNetworkSubnet(deviceDO.getLocalNetworkSubnet().substring(0, lastPointSubnet));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AwsDynamoDBManager.dynamoDBMapper.save(deviceDO);
                    serverCallback.onServerCallbackObject(true,"Success!",deviceDO);
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallback.onServerCallbackObject(false,e.getMessage(),null);
                }
            }
        });
        thread.start();
    }


    public static void updateDevice(final DeviceDO device, final ServerCallback serverCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    AwsDynamoDBManager.dynamoDBMapper.save(device);
                    serverCallback.onServerCallback(true,"Name saved!");
                } catch (Exception e) {
                    serverCallback.onServerCallback(false,e.getMessage());
                }
            }
        }).start();
    }



}
