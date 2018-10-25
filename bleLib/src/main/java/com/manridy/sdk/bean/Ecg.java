package com.manridy.sdk.bean;

import java.util.List;

/**
 * Created by jarLiao on 17/11/14.
 */

public class Ecg {
    private int id;
    private int userId;
    private int dataPackage;//包号
    private List<Integer> list;

    public Ecg(int userId, int dataPackage, List<Integer> list) {
        this.userId = userId;
        this.dataPackage = dataPackage;
        this.list = list;
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

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public int getDataPackage() {
        return dataPackage;
    }

    public void setDataPackage(int dataPackage) {
        this.dataPackage = dataPackage;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Ecg{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", list=").append(list);
        sb.append('}');
        return sb.toString();
    }
}
