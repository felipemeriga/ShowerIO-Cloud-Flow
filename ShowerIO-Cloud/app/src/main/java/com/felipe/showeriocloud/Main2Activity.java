package com.felipe.showeriocloud;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;

public class Main2Activity extends AppCompatActivity {

    // Declare a DynamoDBMapper object
    DynamoDBMapper dynamoDBMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        final AWSCredentialsProvider awsCredentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        final AWSConfiguration awsConfiguration = AWSMobileClient.getInstance().getConfiguration();

        final CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider = CognitoSyncClientManager.credentialsProvider;


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(cognitoCachingCredentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(awsConfiguration)
                .build();

//        final DeviceDO deviceDO = new DeviceDO();
//        deviceDO.setUserId(cognitoCachingCredentialsProvider.getCachedIdentityId());
//        deviceDO.setIotCoreEndPoint("agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com");
//        deviceDO.setIotCoreARN("agq6mvwjsctpy-ats.iot.us-east-2.amazonaws.com");
//        deviceDO.setLocalNetworkIp("192.168.25.15");
//        deviceDO.setLocalNetworkSubnet("192.168.25");
//        deviceDO.setName("Chuveiro Banheiro");
//        deviceDO.setStatus("ACTIVE");
//
        Runnable runnable = new Runnable() {
            public void run() {
              DeviceDO deviceDO =  dynamoDBMapper.load(DeviceDO.class,cognitoCachingCredentialsProvider.getCachedIdentityId(),"Chuveiro Banheiro");
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();

        DevicePersistance devicePersistance = new DevicePersistance();

        devicePersistance.getAllDevicesFromUser();


    }
}
