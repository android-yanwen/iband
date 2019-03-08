package com.manridy.iband.bean;

import com.manridy.applib.utils.TimeUtil;

import org.litepal.crud.DataSupport;

/**
 * 睡眠模板
 * 应用于睡眠数据显示
 * Created by jarLiao on 2016/10/25.
 */
public class SleepModel extends DataSupport {

    private int id;//主键
    private String sleepDay;//天
    private int sleepLength;//总条数
    private int sleepNum;//编号
    private String sleepStartTime;//开始时间
    private String sleepEndTime;//结束时间
    private int sleepDataType;//数据类型
    private int sleepDeep;//深睡
    private int sleepLight;//浅睡
    private int sleepAwake;//浅睡
    private long updateDate;//数据更新时间
    private String s_sleep_data;//睡眠原始数据

    public String getS_sleep_data() {
        return s_sleep_data;
    }

    public void setS_sleep_data(String s_sleep_data) {
        this.s_sleep_data = s_sleep_data;
    }

    public SleepModel() {
    }

    public SleepModel(int sleepDeep, int sleepLight) {
        this.sleepDeep = sleepDeep;
        this.sleepLight = sleepLight;
    }

    public SleepModel(String sleepDay, int sleepLength, int sleepNum, String sleepStartTime, String sleepEndTime, int sleepDataType, int sleepDeep, int sleepLight) {
        this.sleepDay = sleepDay;
        this.sleepLength = sleepLength;
        this.sleepNum = sleepNum;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.sleepDataType = sleepDataType;
        this.sleepDeep = sleepDeep;
        this.sleepLight = sleepLight;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSleepDay() {
        return sleepDay;
    }

    public void setSleepDay(String sleepDay) {
        this.sleepDay = sleepDay;
    }

    public int getSleepLength() {
        return sleepLength;
    }

    public void setSleepLength(int sleepLength) {
        this.sleepLength = sleepLength;
    }

    public int getSleepNum() {
        return sleepNum;
    }

    public void setSleepNum(int sleepNum) {
        this.sleepNum = sleepNum;
    }

    public String getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(String sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }

    public String getSleepEndTime() {
        return sleepEndTime;
    }

    public void setSleepEndTime(String sleepEndTime) {
        this.sleepEndTime = sleepEndTime;
    }

    public int getSleepDataType() {
        return sleepDataType;
    }

    public void setSleepDataType(int sleepDataType) {
        this.sleepDataType = sleepDataType;
    }

    public int getSleepDeep() {
        return sleepDeep;
    }

    public void setSleepDeep(int sleepDeep) {
        this.sleepDeep = sleepDeep;
    }

    public int getSleepLight() {
        return sleepLight;
    }

    public void setSleepLight(int sleepLight) {
        this.sleepLight = sleepLight;
    }

    public int getSleepAwake() {
        return sleepAwake;
    }

    public void setSleepAwake(int sleepAwake) {
        this.sleepAwake = sleepAwake;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public void saveToDate(){
        this.setUpdateDate(System.currentTimeMillis());
        save();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SleepModel{");
        sb.append("id=").append(id);
        sb.append(", sleepDay='").append(sleepDay).append('\'');
        sb.append(", sleepLength=").append(sleepLength);
        sb.append(", sleepNum=").append(sleepNum);
        sb.append(", sleepStartTime='").append(sleepStartTime).append('\'');
        sb.append(", sleepEndTime='").append(sleepEndTime).append('\'');
        sb.append(", sleepDataType=").append(sleepDataType);
        sb.append(", sleepDeep=").append(sleepDeep);
        sb.append(", sleepLight=").append(sleepLight);
        sb.append(", sleepAwake=").append(sleepAwake);
        sb.append(", updateDate=").append(TimeUtil.getNowYMDHMSTime(updateDate));
        sb.append(", s_sleep_data=").append(s_sleep_data);
        sb.append('}');
        return sb.toString();
    }
}
