package com.felipe.showeriocloud.Model;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "BathStatisticsMonth")

public class BathStatisticsDailyDO {
    @SerializedName("id")
    private String _id;
    @SerializedName("microprocessorId")
    private String _microprocessorId;
    @SerializedName("bathDateTime")
    private String _bathDateTime;
    @SerializedName("bathDuration")
    private Double _bathDuration;
    @SerializedName("liters")
    private Double _liters;
    @SerializedName("userId")
    private String _userId;

    private Timestamp bathTimestamp;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAttribute(attributeName = "id")
    public String getId() {
        return _id;
    }

    public void setId(final String _id) {
        this._id = _id;
    }

    @DynamoDBRangeKey(attributeName = "microprocessorId")
    @DynamoDBAttribute(attributeName = "microprocessorId")
    public String getMicroprocessorId() {
        return _microprocessorId;
    }

    public void setMicroprocessorId(final String _microprocessorId) {
        this._microprocessorId = _microprocessorId;
    }

    @DynamoDBAttribute(attributeName = "bathDateTime")
    public String getBathDateTime() {
        return _bathDateTime;
    }

    public void setBathDateTime(final String _bathDateTime) {
        this._bathDateTime = _bathDateTime;
    }

    @DynamoDBAttribute(attributeName = "bathDuration")
    public Double getBathDuration() {
        return _bathDuration;
    }

    public void setBathDuration(final Double _bathDuration) {
        this._bathDuration = _bathDuration;
    }

    @DynamoDBAttribute(attributeName = "liters")
    public Double getLiters() {
        return _liters;
    }

    public void setLiters(final Double _liters) {
        this._liters = _liters;
    }

    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }

    public Timestamp getBathTimestamp() {
        return bathTimestamp;
    }

    public void setBathTimestamp(Timestamp bathTimestamp) {
        this.bathTimestamp = bathTimestamp;
    }
}
