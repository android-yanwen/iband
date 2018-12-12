package com.manridy.iband.bean;

import com.manridy.applib.utils.TimeUtil;

import org.litepal.crud.DataSupport;

/**
 * 睡眠模板
 * 应用于睡眠数据显示
 * Created by jarLiao on 2016/10/25.
 */
public class SleepStatsModel extends SleepModel {
    private int sleepSum;
    private String deviceMac;

    public SleepStatsModel() {

    }

    public SleepStatsModel(SleepModel sleepModel,String mac){
        this.setSleepStartTime(sleepModel.getSleepStartTime());
        this.setSleepEndTime(sleepModel.getSleepEndTime());
        this.setSleepSum(sleepModel.getSleepLength());
        this.setSleepDeep(sleepModel.getSleepDeep());
        this.setSleepLight(sleepModel.getSleepLight());
        this.setSleepAwake(sleepModel.getSleepAwake());
        this.setSleepDay(sleepModel.getSleepDay());
        this.setUpdateDate(System.currentTimeMillis());
        this.deviceMac = mac;
    }

    public int getSleepSum() {
        return sleepSum;
    }

    public void setSleepSum(int sleepSum) {
        this.sleepSum = sleepSum;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SleepModel{");
        sb.append("id=").append(getId());
        sb.append(", sleepDay='").append(getSleepDay()).append('\'');
        sb.append(", sleepStartTime='").append(getSleepStartTime()).append('\'');
        sb.append(", sleepEndTime='").append(getSleepEndTime()).append('\'');
        sb.append(", sleepSum='").append(sleepSum).append('\'');
        sb.append(", sleepDeep=").append(getSleepDeep()).append('\'');
        sb.append(", sleepLight=").append(getSleepLight()).append('\'');
        sb.append(", sleepAwake=").append(getSleepAwake()).append('\'');
        sb.append(", deviceMac=").append(deviceMac).append('\'');
        sb.append(", updateDate=").append(TimeUtil.getNowYMDHMSTime(getUpdateDate()));
        sb.append('}');
        return sb.toString();
    }
}
