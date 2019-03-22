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
        private String notify_version;
        private String not_disturb;//防丢免打扰
        private String edit_bluetooth_name;//支持编辑蓝牙名称
        private String clear_away;//支持清除数据
        private String heartrate_version;
        private String heartrate_isopen;
        private String heartrate_interval;
        private String need_update;
        private String support_software;
        private String need_autoUpdate;
        private String is_chk_heart_rate;
        private String is_heart_rate_timing_chk;
        private String is_heart_rate_call_police;
        private String blood_pressure_police;
        private String ecg;
        private String is_hide_prevent_lose;

        public String getIs_hide_prevent_lose() {
            return is_hide_prevent_lose;
        }

        public void setIs_hide_prevent_lose(String is_hide_prevent_lose) {
            this.is_hide_prevent_lose = is_hide_prevent_lose;
        }

        public String getBlood_pressure_police() {
            return blood_pressure_police;
        }

        public void setBlood_pressure_police(String blood_pressure_police) {
            this.blood_pressure_police = blood_pressure_police;
        }

        private String no_exec;
        private String mcu_platform;

        public String getMcu_platform() {
            return mcu_platform;
        }

        private String microcirculation;

        public String getMicrocirculation() {
            return microcirculation;
        }


        public void setNo_exec(String no_exec) {
            this.no_exec = no_exec;
        }

        public String getNo_exec() {
            return no_exec;
        }

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

        public String getNotify_version() {
            return notify_version;
        }

        public void setNotify_version(String notify_version) {
            this.notify_version = notify_version;
        }

        public String getNot_disturb() {
            return not_disturb;
        }

        public void setNot_disturb(String not_disturb) {
            this.not_disturb = not_disturb;
        }

        public String getEdit_bluetooth_name() {
            return edit_bluetooth_name;
        }

        public void setEdit_bluetooth_name(String edit_bluetooth_name) {
            this.edit_bluetooth_name = edit_bluetooth_name;
        }

        public String getClear_away() {
            return clear_away;
        }

        public void setClear_away(String clear_away) {
            this.clear_away = clear_away;
        }

        public String getHeartrate_version() {
            return heartrate_version;
        }

        public void setHeartrate_version(String heartrate_version) {
            this.heartrate_version = heartrate_version;
        }

        public String getHeartrate_isopen() {
            return heartrate_isopen;
        }

        public void setHeartrate_isopen(String heartrate_isopen) {
            this.heartrate_isopen = heartrate_isopen;
        }

        public String getHeartrate_interval() {
            return heartrate_interval;
        }

        public void setHeartrate_interval(String heartrate_interval) {
            this.heartrate_interval = heartrate_interval;
        }


        public String getSupport_software() {
            return support_software;
        }

        public void setSupport_software(String support_software) {
            this.support_software = support_software;
        }

        public String getNeed_update() {
            return need_update;
        }

        public void setNeed_update(String need_update) {
            this.need_update = need_update;
        }

        public String getNeed_autoUpdate() {
            return need_autoUpdate;
        }

        public void setNeed_autoUpdate(String need_autoUpdate) {
            this.need_autoUpdate = need_autoUpdate;
        }

        public String getIs_chk_heart_rate() {
            return is_chk_heart_rate;
        }

        public void setIs_chk_heart_rate(String is_chk_heart_rate) {
            this.is_chk_heart_rate = is_chk_heart_rate;
        }

        public String getIs_heart_rate_timing_chk() {
            return is_heart_rate_timing_chk;
        }

        public void setIs_heart_rate_timing_chk(String is_heart_rate_timing_chk) {
            this.is_heart_rate_timing_chk = is_heart_rate_timing_chk;
        }

        public String getIs_heart_rate_call_police() {
            return is_heart_rate_call_police;
        }

        public void setIs_heart_rate_call_police(String is_heart_rate_call_police) {
            this.is_heart_rate_call_police = is_heart_rate_call_police;
        }

        public String getEcg() {
            return ecg;
        }

        public void setEcg(String ecg) {
            this.ecg = ecg;
        }
    }
}
