package com.manridy.sdk.bean;

public class Weather {
    private int id;
    private int weatherRegime;
    private int maxTemperature;
    private int minTemperature;
    private int nowTemperature;
    private String day;

    public Weather(){};

    public Weather(int weatherRegime, int maxTemperature, int minTemperature, int nowTemperature, String day){
        this.weatherRegime = weatherRegime;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.nowTemperature = nowTemperature;
        this.day = day;
    };

    public Weather(int weatherRegime, int maxTemperature, int minTemperature, int nowTemperature){
        this.weatherRegime = weatherRegime;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.nowTemperature = nowTemperature;
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWeatherRegime() {
        return weatherRegime;
    }

    public void setWeatherRegime(int weatherRegime) {
        this.weatherRegime = weatherRegime;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(int maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(int minTemperature) {
        this.minTemperature = minTemperature;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getNowTemperature() {
        return nowTemperature;
    }

    public void setNowTemperature(int nowTemperature) {
        this.nowTemperature = nowTemperature;
    }
}
