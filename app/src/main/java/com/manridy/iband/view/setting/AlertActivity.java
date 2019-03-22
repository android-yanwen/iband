package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.AlertMenuItems;
import com.manridy.iband.view.alert.AlertMenuActivity;
import com.manridy.iband.view.alert.AppActivity;
import com.manridy.iband.view.alert.BloodAlertActivity;
import com.manridy.iband.view.alert.ClockActivity;
import com.manridy.iband.view.alert.HearAlertActivity;
import com.manridy.iband.view.alert.HearBloodAlertActivity;
import com.manridy.iband.view.alert.LostActivity;
import com.manridy.iband.view.alert.PhoneActivity;
import com.manridy.iband.view.alert.SedentaryActivity;
import com.manridy.iband.view.alert.SmsActivity;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 提醒功能页面
 * Created by jarLiao on 17/5/4.
 */

public class AlertActivity extends BaseActionActivity {

    @BindView(R.id.menu_phone)
    AlertMenuItems menuPhone;
    @BindView(R.id.menu_sms)
    AlertMenuItems menuSms;
    @BindView(R.id.menu_sedentary)
    AlertMenuItems menuSedentary;
    @BindView(R.id.menu_clock)
    AlertMenuItems menuClock;
    @BindView(R.id.bt_alert_more)
    Button btAlertMore;
    @BindView(R.id.menu_lost)
    AlertMenuItems menuLost;
    @BindView(R.id.menu_app)
    AlertMenuItems menuApp;
    @BindView(R.id.menu_lost_rightline)
    TextView menuLostRightline;

//    @BindView(R.id.menu_heart_blood_alert)
//    AlertMenuItems menuHeartBloodAlert;
    @BindView(R.id.menu_heart_alert)
    AlertMenuItems menuHeartAlert;
    @BindView(R.id.menu_blood_alert)
    AlertMenuItems menuBloodAlert;

    @Override
    protected void initView(Bundle savedInstanceState) {

        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.title_alert));
        initState();
    }

    @Override
    protected void initListener() {

    }

    @OnClick({
            R.id.menu_phone, R.id.menu_sms, R.id.menu_sedentary, R.id.menu_clock,
            R.id.menu_lost, R.id.menu_app, R.id.bt_alert_more, /*R.id.menu_heart_blood_alert,*/
            R.id.menu_heart_alert, R.id.menu_blood_alert
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_phone:
                startActivity(PhoneActivity.class);
                break;
            case R.id.menu_sms:
                startActivity(SmsActivity.class);
                break;
            case R.id.menu_sedentary:
                startActivity(SedentaryActivity.class);
                break;
            case R.id.menu_clock:
                startActivity(ClockActivity.class);
                break;
            case R.id.bt_alert_more:
                startActivity(AlertMenuActivity.class);
                break;
            case R.id.menu_lost:
                startActivity(LostActivity.class);
                break;
            case R.id.menu_app:
                startActivity(AppActivity.class);
                break;
//            case R.id.menu_heart_blood_alert:
//                startActivity(HearBloodAlertActivity.class);
//                break;
            case R.id.menu_heart_alert:
                startActivity(HearAlertActivity.class);
                break;
            case R.id.menu_blood_alert:
                startActivity(BloodAlertActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_CHANGE_MENU) {
            initState();
        }
    }

    private void initState() {
        boolean phoneEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_PHONE, false);
        boolean smsEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_SMS, false);
        boolean sedentaryEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_SEDENTARY, false);
        boolean clockEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_CLOCK, false);
        boolean lostEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_LOST, false);
        boolean appEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_APP, false);
        boolean heartBloodEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_HEART_BLOOD, false);//心率血压报警设置标志位
        boolean heartEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_HEART_IFG, false);//心率血压报警设置标志位
        boolean bloodEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_BLOOD_IFG, false);//心率血压报警设置标志位
        menuPhone.setAlertState(phoneEnable);
        menuSms.setAlertState(smsEnable);
        menuSedentary.setAlertState(sedentaryEnable);
        menuClock.setAlertState(clockEnable);
        menuLost.setAlertState(lostEnable);
        menuApp.setAlertState(appEnable);
//        menuHeartBloodAlert.setAlertState(heartBloodEnable);
        menuHeartAlert.setAlertState(heartEnable);
        menuBloodAlert.setAlertState(bloodEnable);

        menuLost.setVisibility(View.VISIBLE);
        menuLostRightline.setVisibility(View.VISIBLE);
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST, "");
        String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
        String deviceFirm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
        if (strDeviceList == null || strDeviceList.isEmpty()) {
            return;
        }
        DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
            if (resultBean.getDevice_id().equals(deviceType)) {
                if (!"0".equals(resultBean.getIs_heart_rate_call_police())
                        && resultBean.getIs_heart_rate_call_police().compareTo(deviceFirm) <= 0) {
                    menuHeartAlert.setVisibility(View.VISIBLE);
                } else {
                    menuHeartAlert.setAlertState(false);
                    menuHeartAlert.setVisibility(View.GONE);
                }
                if (!"0".equals(resultBean.getBlood_pressure_police())
                        && resultBean.getBlood_pressure_police().compareTo(deviceFirm) <= 0) {
                    menuBloodAlert.setVisibility(View.VISIBLE);
                } else {
                    menuBloodAlert.setAlertState(false);
                    menuBloodAlert.setVisibility(View.GONE);
                }

                //防丢失提醒隐藏
                if ("1".equals(resultBean.getIs_hide_prevent_lose())) {
                    menuLost.setAlertState(false);
                    menuLost.setVisibility(View.GONE);
                    menuLostRightline.setVisibility(View.GONE);
                }
            }
        }

//        String deviceIDs[] = {"8077", "8078", "8079", "8080", "8092"};
//        if (deviceType == null || "".equals(deviceType)) {
//            menuLost.setAlertState(false);
//            menuLost.setVisibility(View.GONE);
//            menuLostRightline.setVisibility(View.GONE);
//        }
//        for (int i = 0; i < deviceIDs.length; i++) {
//            if (deviceType != null && deviceIDs[i].equals(deviceType.trim())) {
//                menuLost.setAlertState(false);
//                menuLost.setVisibility(View.GONE);
//                menuLostRightline.setVisibility(View.GONE);
//            }
//        }

//        // 判断id是否显示心率血压报警选项
//        if (deviceType != null && "8105".equals(deviceType.trim())) {
//            //隐藏血压心率报警按钮
//            menuHeartAlert.setVisibility(View.VISIBLE);
//            menuBloodAlert.setVisibility(View.VISIBLE);
//        } else {
//            //隐藏血压心率报警按钮
//            menuHeartAlert.setAlertState(false);
//            menuHeartAlert.setVisibility(View.GONE);
//            menuBloodAlert.setAlertState(false);
//            menuBloodAlert.setVisibility(View.GONE);
//        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

}
