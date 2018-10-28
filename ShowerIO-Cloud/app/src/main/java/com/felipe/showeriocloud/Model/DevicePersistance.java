package com.felipe.showeriocloud.Model;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.felipe.showeriocloud.Utils.ServerCallbackObject;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;

public class DevicePersistance {

    static DynamoDBMapper dynamoDBMapper;

    //Function used to get a single device
    public void getSingleDevice(final String deviceName, final ServerCallbackObject serverCallback) {

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


    //Function used to get a single device
    public void getAllDevicesFromUser() {

        Condition rangeKeyCondition = new Condition()
                .withComparisonOperator(ComparisonOperator.NOT_NULL);

        DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                .withHashKeyValues(CognitoSyncClientManager.credentialsProvider.getCachedIdentityId())
                .withConsistentRead(false)
                .withRangeKeyCondition("name",rangeKeyCondition);


        PaginatedList<DeviceDO> result = dynamoDBMapper.query(DeviceDO.class, queryExpression);


    }

}
