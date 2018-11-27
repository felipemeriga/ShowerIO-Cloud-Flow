package com.felipe.showeriocloud.Model;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "Devices")

public class DeviceDO implements Serializable {
    @SerializedName("userId")
    private String _userId;
    @SerializedName("name")
    private String _name;
    @SerializedName("iotCoreARN")
    private String _iotCoreARN;
    @SerializedName("iotCoreEndPoint")
    private String _iotCoreEndPoint;
    @SerializedName("localNetworkIp")
    private String _localNetworkIp;
    @SerializedName("localNetworkSubnet")
    private String _localNetworkSubnet;
    @SerializedName("microprocessorId")
    private String _microprocessorId;
    @SerializedName("status")
    private String _status;
    @SerializedName("bathTime")
    private Integer _bathTime;
    @SerializedName("waitTime")
    private Integer _waitTime;
    @SerializedName("stoppedTime")
    private Integer _stoppedTime;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }
    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }
    public void setName(final String _name) {
        this._name = _name;
    }

    @DynamoDBAttribute(attributeName = "iotCoreARN")
    public String getIotCoreARN() {
        return _iotCoreARN;
    }
    public void setIotCoreARN(final String _iotCoreARN) {
        this._iotCoreARN = _iotCoreARN;
    }

    @DynamoDBAttribute(attributeName = "iotCoreEndPoint")
    public String getIotCoreEndPoint() {
        return _iotCoreEndPoint;
    }
    public void setIotCoreEndPoint(final String _iotCoreEndPoint) {
        this._iotCoreEndPoint = _iotCoreEndPoint;
    }

    @DynamoDBAttribute(attributeName = "localNetworkIp")
    public String getLocalNetworkIp() {
        return _localNetworkIp;
    }
    public void setLocalNetworkIp(final String _localNetworkIp) {
        this._localNetworkIp = _localNetworkIp;
    }

    @DynamoDBAttribute(attributeName = "localNetworkSubnet")
    public String getLocalNetworkSubnet() {
        return _localNetworkSubnet;
    }
    public void setLocalNetworkSubnet(final String _localNetworkSubnet) {
        this._localNetworkSubnet = _localNetworkSubnet;
    }

    @DynamoDBRangeKey(attributeName = "microprocessorId")
    @DynamoDBAttribute(attributeName = "microprocessorId")
    public String getMicroprocessorId() {
        return _microprocessorId;
    }
    public void setMicroprocessorId(final String _microprocessorId) {
        this._microprocessorId = _microprocessorId;
    }

    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return _status;
    }
    public void setStatus(final String _status) {
        this._status = _status;
    }

    @DynamoDBAttribute(attributeName = "bathTime")
    public Integer getBathTime() {
        return _bathTime;
    }
    public void setBathTime(Integer _bathTime) {
        this._bathTime = _bathTime;
    }

    @DynamoDBAttribute(attributeName = "waitTime")
    public Integer getWaitTime() {
        return _waitTime;
    }
    public void setWaitTime(Integer _waitTime) {
        this._waitTime = _waitTime;
    }

    @DynamoDBAttribute(attributeName = "stoppedTime")
    public Integer getStoppedTime() {
        return _stoppedTime;
    }
    public void setStoppedTime(Integer _stoppedTime) {
        this._stoppedTime = _stoppedTime;
    }
}
