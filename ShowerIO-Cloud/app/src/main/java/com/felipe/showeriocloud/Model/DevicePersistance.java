package com.felipe.showeriocloud.Model;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.felipe.showeriocloud.Aws.AuthorizationHandle;
import com.felipe.showeriocloud.Aws.AwsDynamoDBManager;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.felipe.showeriocloud.Utils.ServerCallbackObject;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        hashKeyObject.setUserId(AuthorizationHandle.getCurrentUserId());

        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(AuthorizationHandle.getCurrentUserId()));
        final DynamoDBScanExpression dynamoDBScanExpression = new DynamoDBScanExpression();
        dynamoDBScanExpression.withFilterExpression("userId = :val1").withExpressionAttributeValues(eav);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<DeviceDO> scanResult = AwsDynamoDBManager.dynamoDBMapper.scan(DeviceDO.class, dynamoDBScanExpression);

                    //List<DeviceDO> result = AwsDynamoDBManager.dynamoDBMapper.query(DeviceDO.class, queryExpression);
                    lastUpdateUserDevices = scanResult;
                    serverCallbackObjects.onServerCallbackObject(true, "SUCCESS", (List<Object>) (List<?>) scanResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallbackObjects.onServerCallbackObject(false, e.getMessage(), new ArrayList<Object>());

                }
            }
        });
        thread.start();
    }

    //Function used to get a single device
    public static void fastGetAllDevicesFromUser(final ServerCallback serverCallback) {

        DeviceDO hashKeyObject = new DeviceDO();
        hashKeyObject.setUserId(AuthorizationHandle.getCurrentUserId());

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
                    serverCallback.onServerCallback(true, "SUCCESS");
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallback.onServerCallback(false, e.getMessage());

                }
            }
        });
        thread.start();
    }

    //Function used to get a single device
    public static void insertNewDevice(final DeviceDO deviceDO, final ServerCallbackObject serverCallback) {
        deviceDO.setUserId(AuthorizationHandle.getCurrentUserId());
        int lastPointSubnet = deviceDO.getLocalNetworkSubnet().lastIndexOf(".");
        deviceDO.setLocalNetworkSubnet(deviceDO.getLocalNetworkSubnet().substring(0, lastPointSubnet));
        deviceDO.setBathTime(0);
        deviceDO.setStoppedTime(1);
        deviceDO.setWaitingTime(0);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AwsDynamoDBManager.dynamoDBMapper.save(deviceDO);
                    serverCallback.onServerCallbackObject(true, "Success!", deviceDO);
                } catch (Exception e) {
                    e.printStackTrace();
                    serverCallback.onServerCallbackObject(false, e.getMessage(), null);
                }
            }
        });
        thread.start();
    }


    public static void updateDevice(final DeviceDO device, final ServerCallback serverCallback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AwsDynamoDBManager.dynamoDBMapper.save(device);
                    serverCallback.onServerCallback(true, "Name saved!");
                } catch (Exception e) {
                    serverCallback.onServerCallback(false, e.getMessage());
                }
            }
        }).start();
    }

    public static void fastUpdateDevice(final DeviceDO device) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                AwsDynamoDBManager.dynamoDBMapper.save(device);
            }
        }).start();
    }



    public static void deleteDevice(final DeviceDO device) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                AwsDynamoDBManager.dynamoDBMapper.delete(device);
            }
        }).start();
    }

}
