package com.felipe.showeriocloud;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "showerio-mobilehub-1561120802-Device")

public class DeviceDO {
    private String _userId;
    private String _name;
    private String _iotCoreARN;
    private String _iotCoreEndPoint;
    private String _localNetworkIp;
    private String _localNetworkSubnet;
    private Double _microprocessorId;
    private String _status;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "name")
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
    @DynamoDBAttribute(attributeName = "microprocessorId")
    public Double getMicroprocessorId() {
        return _microprocessorId;
    }

    public void setMicroprocessorId(final Double _microprocessorId) {
        this._microprocessorId = _microprocessorId;
    }
    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return _status;
    }

    public void setStatus(final String _status) {
        this._status = _status;
    }

}
