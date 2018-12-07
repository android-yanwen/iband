package com.manridy.iband.view.alert;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.MenuItems;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 提醒九宫格菜单
 * Created by jarLiao on 17/5/4.
 */

public class AlertMenuActivity extends BaseActionActivity {

    @BindView(R.id.menu_sedentary)
    MenuItems menuSedentary;
    @BindView(R.id.menu_clock)
    MenuItems menuClock;
    @BindView(R.id.menu_sms)
    MenuItems menuSms;
    @BindView(R.id.menu_phone)
    MenuItems menuPhone;
    @BindView(R.id.menu_lost)
    MenuItems menuLost;
    @BindView(R.id.menu_app)
    MenuItems menuApp;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alert_menu);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.title_alert));
        registerEventBus();
        initMenuState();
    }

    private void initMenuState() {
        boolean phoneEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_PHONE,false);
        boolean smsEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_SMS,false);
        boolean sedentaryEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_SEDENTARY,false);
        boolean clockEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_CLOCK,false);
        boolean lostEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_LOST,false);
        boolean appEnable = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_APP,false);

        menuSedentary.setMenuOpenState(sedentaryEnable);
        menuClock.setMenuOpenState(clockEnable);
        menuSms.setMenuOpenState(smsEnable);
        menuPhone.setMenuOpenState(phoneEnable);
        menuLost.setMenuOpenState(lostEnable);
        menuApp.setMenuOpenState(appEnable);
    }

    @Override
    protected void initListener() {

    }

    @OnClick({R.id.menu_sedentary, R.id.menu_clock, R.id.menu_sms, R.id.menu_phone, R.id.menu_lost, R.id.menu_app})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_sedentary:
                startActivity(SedentaryActivity.class);
                break;
            case R.id.menu_clock:
                startActivity(ClockActivity.class);
                break;
            case R.id.menu_sms:
                startActivity(SmsActivity.class);
                break;
            case R.id.menu_phone:
                startActivity(PhoneActivity.class);
                break;
            case R.id.menu_lost:
                startActivity(LostActivity.class);
                break;
            case R.id.menu_app:
                startActivity(AppActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_CHANGE_MENU) {
            initMenuState();
        }
    }
}
