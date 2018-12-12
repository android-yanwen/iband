package com.manridy.iband.bean.data;

import org.litepal.crud.DataSupport;

public class RunLocationModel extends DataSupport {
    private int id;//主键
    private String deviceMac;//设备
    private String stepDate;//时间
    private String stepDay;//天
    private String curMinute;
    private String locationDataPackageId;
    private String locationData;

    public RunLocationModel(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getStepDate() {
        return stepDate;
    }

    public void setStepDate(String stepDate) {
        this.stepDate = stepDate;
    }

    public String getLocationDataPackageId() {
        return locationDataPackageId;
    }

    public void setLocationDataPackageId(String locationDataPackageId) {
        this.locationDataPackageId = locationDataPackageId;
    }

    public String getLocationData() {
        return locationData;
    }

    public void setLocationData(String locationData) {
        this.locationData = locationData;
    }

    public String getStepDay() {
        return stepDay;
    }

    public void setStepDay(String stepDay) {
        this.stepDay = stepDay;
    }

    public String getCurMinute() {
        return curMinute;
    }

    public void setCurMinute(String curMinute) {
        this.curMinute = curMinute;
    }
}
