package com.manridy.iband.common;

/**
 * App全局变量
 * Created by jarLiao on 17/5/18.
 */
public class AppGlobal {
    public static final String DATA_APP_FIRST = "data_app_first";
    public static final String DATA_USER_HEAD = "data_user_head";
    public static final String DATA_USER_SEND = "data_user_send";
    //device bind
    public static final String DATA_DEVICE_BIND_NAME = "data_device_bind_name";
    public static final String DATA_DEVICE_BIND_MAC = "data_device_bind_mac";
    public static final String DATA_DEVICE_BIND_IMG = "data_device_bind_img";
    public static final String DATA_DEVICE_CONNECT_STATE = "data_device_connect_state";
    //alert
    public static final String DATA_ALERT_SMS = "data_alert_sms";
    public static final String DATA_ALERT_PHONE = "data_alert_phone";
    public static final String DATA_ALERT_LOST = "data_alert_lost";
    public static final String DATA_ALERT_CLOCK = "data_alert_clock";
    public static final String DATA_ALERT_SEDENTARY = "data_alert_sedentary";
    public static final String DATA_ALERT_APP = "data_alert_app";
    public static final String DATA_ALERT_WRIST = "data_alert_wrist";
    //setting
    public static final String DATA_SETTING_LIGHT = "data_setting_light";
    public static final String DATA_SETTING_UNIT= "data_setting_unit";
    public static final String DATA_SETTING_UNIT_TIME = "data_setting_unit_time";
    public static final String DATA_SETTING_TARGET_STEP = "data_setting_target_step";
    public static final String DATA_SETTING_TARGET_SLEEP = "data_setting_target_sleep";
    public static final String DATA_SETTING_HRCORRECT ="data_setting_hrcorrect";

    //version
    public static final String DATA_FIRMWARE_VERSION = "data_setting_firmware";
    public static final String DATA_FIRMWARE_VERSION_NEW = "data_setting_firmware_new";

    public static final String DATA_FIRMWARE_TYPE = "data_firmware_type";
    //
    public static final String DATA_TIMING_HR = "data_timing_hr";
    public static final String DATA_TIMING_HR_SPACE = "data_timing_hr_space";
    //
    public static final String DATA_BATTERY_NUM = "data_battery_num";
    public static final String DATA_BATTERY_STATE = "data_battery_state";
    public static final String DATA_SYNC_TIME = "data_sync_time";

    public static final String DATA_DEVICE_LIST = "data_device_list";
    public static final String DATA_DEVICE_FILTER = "data_device_filter";

    public static final String STATE_APP_OTA_RUN = "state_app_ota_run";

    //device state
    public static final int DEVICE_STATE_UNCONNECT = 0;//未连接
    public static final int DEVICE_STATE_CONNECTED = 1;//已连接
    public static final int DEVICE_STATE_CONNECTING = 2;//连接中
    public static final int DEVICE_STATE_CONNECT_FAIL = 3;//连接失败
    public static final int DEVICE_STATE_CONNECT_SUCCESS = 4;//连接成功
    public static final int DEVICE_STATE_UNFIND = 5;
    public static final int DEVICE_STATE_UNBIND = 6;
    public static final int DEVICE_STATE_SYNC_OK = 7;
    public static final int DEVICE_STATE_SYNC_NO = 8;
    public static final int DEVICE_STATE_BLUETOOTH_DISENABLE = 9;
    public static final int DEVICE_STATE_BLUETOOTH_ENABLEING = 10;
}
