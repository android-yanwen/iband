package com.manridy.iband.bean;

import org.litepal.crud.DataSupport;

public class WeatherModel extends DataSupport {
    private int id;
    private String country;
    private String province;
    private String city;
    private String cityCode;
    private String weatherRegime;
    private String maxTemperature;
    private String minTemperature;
    private String nowTemperature;
    private String day;

    public WeatherModel(){};

    public WeatherModel(String country, String province, String city, String cityCode){
        this.country = country;
        this.province = province;
        this.city = city;
        this.cityCode = cityCode;
    };

    public void setWeatherInfo(String weatherRegime,String nowTemperature){
        this.weatherRegime = weatherRegime;
        this.nowTemperature = nowTemperature;
    }

    public WeatherModel(String country, String province, String city, String cityCode, String weatherRegime, String maxTemperature, String minTemperature, String day){
        this.country = country;
        this.province = province;
        this.city = city;
        this.cityCode = cityCode;
        this.weatherRegime = weatherRegime;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.day = day;
    };

    public WeatherModel(String weatherRegime, String maxTemperature, String minTemperature){
        this.weatherRegime = weatherRegime;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWeatherRegime() {
        return weatherRegime;
    }

    public void setWeatherRegime(String weatherRegime) {
        this.weatherRegime = weatherRegime;
    }

    public String getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(String maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public String getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(String minTemperature) {
        this.minTemperature = minTemperature;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getNowTemperature() {
        return nowTemperature;
    }

    public void setNowTemperature(String nowTemperature) {
        this.nowTemperature = nowTemperature;
    }
}
