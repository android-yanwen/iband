package com.manridy.iband.bean;

import org.litepal.crud.DataSupport;

public class MicrocirculationModel extends DataSupport {

    private int id;
    private int tr;
    private String date;
    private String day;
    private float micro;
    private int microNum;
    private int microLength;
    private long updateDate;//数据更新时间

    public int getMicroLength() {
        return microLength;
    }

    public void setMicroLength(int microLength) {
        this.microLength = microLength;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getTr() {
        return tr;
    }

    public void setMicroNum(int microNum) {
        this.microNum = microNum;
    }

    public int getMicroNum() {
        return microNum;

    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public float getMicro() {
        return micro;
    }

    public void setTr(int tr) {
        this.tr = tr;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setMicro(float micro) {
        this.micro = micro;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void saveToDate(){
        this.setUpdateDate(System.currentTimeMillis());
        save();
    }
}
