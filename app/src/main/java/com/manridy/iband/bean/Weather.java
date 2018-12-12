package com.manridy.iband.bean;

import java.util.List;

public class Weather {
    /**
     * status : 1
     * msg : 操作成功
     * data : {"nowWeather":{"tmp":"10","weather_type":1,"tmp_max":"13","tmp_min":"2","chk_country":"蒙古","chk_city":"巴伦图伦","checktime":"2018-10-13 14:35:30"},"forecastWeather":[{"weather_type":1,"tmp_max":"13","tmp_min":"2","weather_id":"28","date":"2018-10-13 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-14 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-15 00:00:00"},{"weather_type":0,"tmp_max":"11","tmp_min":"1","weather_id":"28","date":"2018-10-16 00:00:00"},{"weather_type":1,"tmp_max":"11","tmp_min":"2","weather_id":"28","date":"2018-10-17 00:00:00"},{"weather_type":1,"tmp_max":"10","tmp_min":"1","weather_id":"28","date":"2018-10-18 00:00:00"},{"weather_type":1,"tmp_max":"5","tmp_min":"-2","weather_id":"28","date":"2018-10-19 00:00:00"}]}
     * code : 10001
     */

    private int status;
    private String msg;
    private DataBean data;
    private int code;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class DataBean {
        /**
         * nowWeather : {"tmp":"10","weather_type":1,"tmp_max":"13","tmp_min":"2","chk_country":"蒙古","chk_city":"巴伦图伦","checktime":"2018-10-13 14:35:30"}
         * forecastWeather : [{"weather_type":1,"tmp_max":"13","tmp_min":"2","weather_id":"28","date":"2018-10-13 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-14 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-15 00:00:00"},{"weather_type":0,"tmp_max":"11","tmp_min":"1","weather_id":"28","date":"2018-10-16 00:00:00"},{"weather_type":1,"tmp_max":"11","tmp_min":"2","weather_id":"28","date":"2018-10-17 00:00:00"},{"weather_type":1,"tmp_max":"10","tmp_min":"1","weather_id":"28","date":"2018-10-18 00:00:00"},{"weather_type":1,"tmp_max":"5","tmp_min":"-2","weather_id":"28","date":"2018-10-19 00:00:00"}]
         */

        private NowWeatherBean nowWeather;
        private List<ForecastWeatherBean> forecastWeather;

        public NowWeatherBean getNowWeather() {
            return nowWeather;
        }

        public void setNowWeather(NowWeatherBean nowWeather) {
            this.nowWeather = nowWeather;
        }

        public List<ForecastWeatherBean> getForecastWeather() {
            return forecastWeather;
        }

        public void setForecastWeather(List<ForecastWeatherBean> forecastWeather) {
            this.forecastWeather = forecastWeather;
        }

        public static class NowWeatherBean {
            /**
             * tmp : 10
             * weather_type : 1
             * tmp_max : 13
             * tmp_min : 2
             * chk_country : 蒙古
             * chk_city : 巴伦图伦
             * checktime : 2018-10-13 14:35:30
             */

            private String tmp;
            private int weather_type;
            private String tmp_max;
            private String tmp_min;
            private String chk_country;
            private String chk_city;
            private String checktime;

            public String getTmp() {
                return tmp;
            }

            public void setTmp(String tmp) {
                this.tmp = tmp;
            }

            public int getWeather_type() {
                return weather_type;
            }

            public void setWeather_type(int weather_type) {
                this.weather_type = weather_type;
            }

            public String getTmp_max() {
                return tmp_max;
            }

            public void setTmp_max(String tmp_max) {
                this.tmp_max = tmp_max;
            }

            public String getTmp_min() {
                return tmp_min;
            }

            public void setTmp_min(String tmp_min) {
                this.tmp_min = tmp_min;
            }

            public String getChk_country() {
                return chk_country;
            }

            public void setChk_country(String chk_country) {
                this.chk_country = chk_country;
            }

            public String getChk_city() {
                return chk_city;
            }

            public void setChk_city(String chk_city) {
                this.chk_city = chk_city;
            }

            public String getChecktime() {
                return checktime;
            }

            public void setChecktime(String checktime) {
                this.checktime = checktime;
            }
        }

        public static class ForecastWeatherBean {
            /**
             * weather_type : 1
             * tmp_max : 13
             * tmp_min : 2
             * weather_id : 28
             * date : 2018-10-13 00:00:00
             */

            private int weather_type;
            private String tmp_max;
            private String tmp_min;
            private String weather_id;
            private String date;

            public int getWeather_type() {
                return weather_type;
            }

            public void setWeather_type(int weather_type) {
                this.weather_type = weather_type;
            }

            public String getTmp_max() {
                return tmp_max;
            }

            public void setTmp_max(String tmp_max) {
                this.tmp_max = tmp_max;
            }

            public String getTmp_min() {
                return tmp_min;
            }

            public void setTmp_min(String tmp_min) {
                this.tmp_min = tmp_min;
            }

            public String getWeather_id() {
                return weather_id;
            }

            public void setWeather_id(String weather_id) {
                this.weather_id = weather_id;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }
        }
    }
    //{"status":1,"msg":"\u64cd\u4f5c\u6210\u529f","data":{"nowWeather":{"tmp":"10","weather_type":1,"tmp_max":"13","tmp_min":"2","chk_country":"\u8499\u53e4","chk_city":"\u5df4\u4f26\u56fe\u4f26","checktime":"2018-10-13 14:35:30"},"forecastWeather":[{"weather_type":1,"tmp_max":"13","tmp_min":"2","weather_id":"28","date":"2018-10-13 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-14 00:00:00"},{"weather_type":1,"tmp_max":"12","tmp_min":"1","weather_id":"28","date":"2018-10-15 00:00:00"},{"weather_type":0,"tmp_max":"11","tmp_min":"1","weather_id":"28","date":"2018-10-16 00:00:00"},{"weather_type":1,"tmp_max":"11","tmp_min":"2","weather_id":"28","date":"2018-10-17 00:00:00"},{"weather_type":1,"tmp_max":"10","tmp_min":"1","weather_id":"28","date":"2018-10-18 00:00:00"},{"weather_type":1,"tmp_max":"5","tmp_min":"-2","weather_id":"28","date":"2018-10-19 00:00:00"}]},"code":10001}

}
