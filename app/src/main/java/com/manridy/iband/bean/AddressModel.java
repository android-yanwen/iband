package com.manridy.iband.bean;

import com.manridy.sdk.bean.Weather;

import java.util.ArrayList;
import java.util.List;

public class AddressModel {


    private String cnty;
    private String parent_city;
    private List<ForecastWeather> forecastWeather = new ArrayList<>();

    public class ForecastWeather{
        private String cond_txt_d;
        private String tmp_max;
        private String tmp_min;
        private String tmp_now;
        private int weater_type;

        public int getWeater_type() {
            return weater_type;
        }

        public void setCond_txt_d(String cond_txt_d) {
            this.cond_txt_d = cond_txt_d;
            if (cond_txt_d ==null) return;

            for (int i =0; i <WeatherType.WeatherSunny.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherSunny[i])) {
                    weater_type = 0;
                    return;
                }
            }
            for (int i =0; i <WeatherType.WeatherOvercast.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherOvercast[i])) {
                    weater_type = 1;
                    return;
                }
            }
            for (int i =0; i <WeatherType.WeatherRain.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherRain[i])) {
                    weater_type = 2;
                    return;
                }
            }
            for (int i =0; i <WeatherType.WeatherSnow.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherSnow[i])) {
                    weater_type = 3;
                    return;
                }
            }
            for (int i =0; i <WeatherType.WeatherFoggy.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherFoggy[i])) {
                    weater_type = 4;
                    return;
                }
            }
            for (int i =0; i <WeatherType.WeatherRain.length;i++) {
                if (cond_txt_d.equals(WeatherType.WeatherRain[i])) {
                    weater_type = 5;
                    return;
                }
            }

        }

        public void setTmp_max(String tmp_max) {
            this.tmp_max = tmp_max;
        }

        public void setTmp_min(String tmp_min) {
            this.tmp_min = tmp_min;
        }

        public String getCond_txt_d() {
            return cond_txt_d;
        }

        public String getTmp_max() {
            return tmp_max;
        }

        public String getTmp_min() {
            return tmp_min;
        }

        public void setTmp_now(String tmp_now) {
            this.tmp_now = tmp_now;
        }

        public String getTmp_now() {
            return tmp_now;
        }
    }

    public AddressModel(int num) {
        for (int i = 0; i < num; i++) {
            ForecastWeather weather = new ForecastWeather();
            forecastWeather.add(weather);
        }
    }

    public List<ForecastWeather> getForecastWeather() {
        return forecastWeather;
    }

    public void setCnty(String cnty) {
        this.cnty = cnty;
    }

    public void setParent_city(String parent_city) {
        this.parent_city = parent_city;
    }


    public String getCnty() {
        return cnty;
    }

    public String getParent_city() {
        return parent_city;
    }


//    {"HeWeather6":[{"basic":[{"cid":"CN101090608","location":"霸州","parent_city":"廊坊","admin_area":"河北","cnty":"中国","lat":"39.11733246","lon":"116.39202118","tz":"+8.00","type":"city"}],"status":"ok"}]}

    private List<HeWeather6Bean> HeWeather6;

    public List<HeWeather6Bean> getHeWeather6() {
        return HeWeather6;
    }

    public void setHeWeather6(List<HeWeather6Bean> HeWeather6) {
        this.HeWeather6 = HeWeather6;
    }

    public static class HeWeather6Bean {
        /**
         * basic : [{"cid":"CN101090608","location":"霸州","parent_city":"廊坊","admin_area":"河北","cnty":"中国","lat":"39.11733246","lon":"116.39202118","tz":"+8.00","type":"city"}]
         * status : ok
         */

        private String status;
        private List<BasicBean> basic;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<BasicBean> getBasic() {
            return basic;
        }

        public void setBasic(List<BasicBean> basic) {
            this.basic = basic;
        }

        public static class BasicBean {
            /**
             * cid : CN101090608
             * location : 霸州
             * parent_city : 廊坊
             * admin_area : 河北
             * cnty : 中国
             * lat : 39.11733246
             * lon : 116.39202118
             * tz : +8.00
             * type : city
             */

            private String cid;
            private String location;
            private String parent_city;
            private String admin_area;
            private String cnty;
            private String lat;
            private String lon;
            private String tz;
            private String type;

            public String getCid() {
                return cid;
            }

            public void setCid(String cid) {
                this.cid = cid;
            }

            public String getLocation() {
                return location;
            }

            public void setLocation(String location) {
                this.location = location;
            }

            public String getParent_city() {
                return parent_city;
            }

            public void setParent_city(String parent_city) {
                this.parent_city = parent_city;
            }

            public String getAdmin_area() {
                return admin_area;
            }

            public void setAdmin_area(String admin_area) {
                this.admin_area = admin_area;
            }

            public String getCnty() {
                return cnty;
            }

            public void setCnty(String cnty) {
                this.cnty = cnty;
            }

            public String getLat() {
                return lat;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public String getLon() {
                return lon;
            }

            public void setLon(String lon) {
                this.lon = lon;
            }

            public String getTz() {
                return tz;
            }

            public void setTz(String tz) {
                this.tz = tz;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }


}
