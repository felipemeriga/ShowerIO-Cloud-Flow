package com.felipe.showeriocloud.Model;

import com.google.gson.annotations.SerializedName;

public class BathStatisticsMonthly {

    @SerializedName("totalLiters")
    private Double totalLiters;
    @SerializedName("totalTime")
    private Double totalTime;
    @SerializedName("waterPrice")
    private Double waterPrice;
    @SerializedName("energyPrice")
    private Double energyPrice;

    public Double getTotalLiters() {
        return totalLiters;
    }

    public void setTotalLiters(Double totalLiters) {
        this.totalLiters = totalLiters;
    }

    public Double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Double totalTime) {
        this.totalTime = totalTime;
    }

    public Double getWaterPrice() {
        return waterPrice;
    }

    public void setWaterPrice(Double waterPrice) {
        this.waterPrice = waterPrice;
    }

    public Double getEnergyPrice() {
        return energyPrice;
    }

    public void setEnergyPrice(Double energyPrice) {
        this.energyPrice = energyPrice;
    }
}
