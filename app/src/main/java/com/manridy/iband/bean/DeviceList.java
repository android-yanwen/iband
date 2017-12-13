package com.manridy.iband.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by jarLiao on 17/10/31.
 */

public class DeviceList {


    /**
     * count : 18
     * result : [{"0":"HB-M1","device_name":"HB-M1","1":"18","id":"18","2":"iband","identifier":"iband","3":"13453145.png","imageName":"13453145.png","4":null,"describe":null},{"0":"N67","device_name":"N67","1":"17","id":"17","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"watch","device_name":"watch","1":"16","id":"16","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"F07","device_name":"F07","1":"15","id":"15","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"HYIB01","device_name":"HYIB01","1":"14","id":"14","2":"Hokage","identifier":"Hokage","3":null,"imageName":null,"4":null,"describe":null},{"0":"F1Pro","device_name":"F1Pro","1":"13","id":"13","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"HB08","device_name":"HB08","1":"12","id":"12","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"X9_00","device_name":"X9_00","1":"11","id":"11","2":"iwear","identifier":"iwear","3":null,"imageName":null,"4":null,"describe":null},{"0":"N66","device_name":"N66","1":"10","id":"10","2":"iwear","identifier":"iwear","3":null,"imageName":null,"4":null,"describe":null},{"0":"Smart","device_name":"Smart","1":"9","id":"9","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"K2","device_name":"K2","1":"8","id":"8","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"DB-01","device_name":"DB-01","1":"7","id":"7","2":"iwear","identifier":"iwear","3":null,"imageName":null,"4":null,"describe":null},{"0":"N68","device_name":"N68","1":"6","id":"6","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"Smart B","device_name":"Smart B","1":"5","id":"5","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"N109","device_name":"N109","1":"4","id":"4","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"X9Pro","device_name":"X9Pro","1":"3","id":"3","2":"iwear","identifier":"iwear","3":null,"imageName":null,"4":null,"describe":null},{"0":"Smart-2","device_name":"Smart-2","1":"2","id":"2","2":"iband","identifier":"iband","3":null,"imageName":null,"4":null,"describe":null},{"0":"TF1","device_name":"TF1","1":"1","id":"1","2":"iband","identifier":"iband","3":"13453145.png","imageName":"13453145.png","4":null,"describe":null}]
     */

    private int count;
    private List<ResultBean> result;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * 0 : HB-M1
         * device_name : HB-M1
         * 1 : 18
         * id : 18
         * 2 : iband
         * identifier : iband
         * 3 : 13453145.png
         * imageName : 13453145.png
         * 4 : null
         * describe : null
         */

        private String device_name;
        private String id;
        private String identifier;
        private String imageName;
        private String describe;
        private String brightness;
        private String device_id;
        private String blood_pressure;
        private String oxygen_pressure;
        private String project_number;

        public String getDevice_name() {
            return device_name;
        }

        public void setDevice_name(String device_name) {
            this.device_name = device_name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public Object getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public String getBrightness() {
            return brightness;
        }

        public void setBrightness(String brightness) {
            this.brightness = brightness;
        }

        public String getDevice_id() {
            return device_id;
        }

        public void setDevice_id(String device_id) {
            this.device_id = device_id;
        }

        public String getBlood_pressure() {
            return blood_pressure;
        }

        public void setBlood_pressure(String blood_pressure) {
            this.blood_pressure = blood_pressure;
        }

        public String getOxygen_pressure() {
            return oxygen_pressure;
        }

        public void setOxygen_pressure(String oxygen_pressure) {
            this.oxygen_pressure = oxygen_pressure;
        }

        public String getProject_number() {
            return project_number;
        }

        public void setProject_number(String project_number) {
            this.project_number = project_number;
        }
    }
}
