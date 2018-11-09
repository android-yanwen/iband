package com.manridy.iband.bean;

import org.litepal.crud.DataSupport;

public class EcgHistoryModel extends DataSupport {
    public static int TYPE_DAY = 1;
    public static int TYPE_DATA = 2;

    private int id;//主键
    private int type; //类型
    private String ecgStartDate;//时间
    private String ecgEndDate;
    private String ecgDate;
    private String ecgDay;//天
    private int userId;//
    private int dataPackage;//包号
    private String ecg_data_id;
    private boolean isSave;
    private int avgHr;
    private int minHr;
    private int maxHr;
    private int lastHr;
    private String lastHrDate;

    private String username;

    public EcgHistoryModel(){
//        this.username = HealthApplication.getIntance().getUsername();
        this.username = "1";
    }

    public int getDataPackage() {
        return dataPackage;
    }

    public void setDataPackage(int dataPackage) {
        this.dataPackage = dataPackage;
    }

    public String getEcgEndDate() {
        return ecgEndDate;
    }

    public void setEcgEndDate(String ecgEndDate) {
        this.ecgEndDate = ecgEndDate;
    }

    public String getEcgStartDate() {
        return ecgStartDate;
    }

    public void setEcgStartDate(String ecgStartDate) {
        this.ecgStartDate = ecgStartDate;
    }

    public String getEcgDay() {
        return ecgDay;
    }

    public void setEcgDay(String ecgDay) {
        this.ecgDay = ecgDay;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEcg_data_id() {
        return ecg_data_id;
    }

    public void setEcg_data_id(String ecg_data_id) {
        this.ecg_data_id = ecg_data_id;
    }

    public String getEcgDate() {
        return ecgDate;
    }

    public void setEcgDate(String ecgDate) {
        this.ecgDate = ecgDate;
    }

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAvgHr() {
        return avgHr;
    }

    public void setAvgHr(int avgHr) {
        this.avgHr = avgHr;
    }


    public int getMaxHr() {
        return maxHr;
    }

    public void setMaxHr(int maxHr) {
        this.maxHr = maxHr;
    }

    public int getMinHr() {
        return minHr;
    }

    public void setMinHr(int minHr) {
        this.minHr = minHr;
    }

    public String getLastHrDate() {
        return lastHrDate;
    }

    public void setLastHrDate(String lastHrDate) {
        this.lastHrDate = lastHrDate;
    }

    public int getLastHr() {
        return lastHr;
    }

    public void setLastHr(int lastHr) {
        this.lastHr = lastHr;
    }
}
