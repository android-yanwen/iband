package com.manridy.iband.bean;

import org.litepal.crud.DataSupport;

public class DoNotDisturbModel extends DataSupport {
    private int doNotDisturbOnOff;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;


    public DoNotDisturbModel(int doNotDisturbOnOff, int startHour, int startMinute, int endHour, int endMinute) {
        this.doNotDisturbOnOff = doNotDisturbOnOff;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }


    public void setDoNotDisturbOnOff(int doNotDisturbOnOff) {
        this.doNotDisturbOnOff = doNotDisturbOnOff;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }


    public int getDoNotDisturbOnOff() {
        return doNotDisturbOnOff;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }
}
