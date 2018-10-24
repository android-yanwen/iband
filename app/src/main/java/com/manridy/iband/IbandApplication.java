package com.manridy.iband;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.service.AlertService;
import com.manridy.iband.service.BleService;
import com.manridy.iband.service.NotificationCollectorMonitorService;
import com.manridy.iband.service.AppNotificationListenerService;
import com.manridy.iband.view.setting.UpdateActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.bean.Weather;
import com.mob.MobSDK;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import org.litepal.LitePalApplication;

import static com.manridy.iband.common.AppGlobal.DEVICE_STATE_UNCONNECT;
import static com.manridy.iband.view.setting.LangueActivity.getLocale;

/**
 * 应用全局
 * Created by jarLiao on 17/5/16.
 */

public class IbandApplication extends Application {
    private static final String TAG = IbandApplication.class.getSimpleName();
    private static IbandApplication intance;
    public BleService service;

    public static boolean isNeedRefresh = false;

    public String city = "";
    public String country = "";
    public Weather weather;
    public static double location_latitude;
    public static double location_longitude;


    @Override
    public void onCreate() {
        super.onCreate();
        intance = this;
        SPUtil.put(this, AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
        SPUtil.put(this, AppGlobal.STATE_APP_OTA_RUN, false);
//        startService(new Intent(this, AppNotificationListenerService.class));
        LitePalApplication.initialize(this);//初始化数据库
        Fresco.initialize(this);//初始化图片加载
        initBleSevrice();//初始化蓝牙服务
        initAlertService();//初始化提醒服务
        if(!UpdateActivity.isGoogle) {
            initBugly();//初始化bugly
        }
        initNotificationService();//初始化通知
        initLangue();//初始化语言
//        CrashHandler.getInstance().init(intance);
        MobSDK.init(this);

        try {
            String brand = android.os.Build.BRAND;
            if("HONOR".equalsIgnoreCase(brand)){
                brand = "huawei";
            }
            Watch.brand = brand;
            Log.i(TAG,brand);
        }catch (Exception e){
            Log.e(TAG,"fail:getBrand");
        }
    }

    private void initLangue() {
        int curSelect = (int) SPUtil.get(this, AppGlobal.DATA_APP_LANGUE,0);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (curSelect == 0) { //选择跟随系统
                conf.setLocale(conf.getLocales().get(0));
            } else { //设置选择的语言
                conf.setLocale(getLocale(curSelect));
            }
        } else {
            conf.locale = getLocale(curSelect);
        }
        res.updateConfiguration(conf, dm);
    }

    private void initBleSevrice() {
        Intent bindIntent = new Intent(this,BleService.class);
        bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void initAlertService() {
        startService(new Intent(this,AlertService.class));
    }

    private void initBugly() {
        Bugly.init(getApplicationContext(), "33139ca6ea",false);
        Beta.initDelay = 5 * 1000;//延迟两秒检测版本信息
    }

    private void initNotificationService() {
        boolean appOnOff = (boolean) SPUtil.get(this, AppGlobal.DATA_ALERT_APP,false);
        if (appOnOff) {
            startService(new Intent(this, NotificationCollectorMonitorService.class));
        }
    }

    public static IbandApplication getIntance() {
        return intance;
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service = ((BleService.LocalBinder) iBinder).service();
            service.init();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.d(TAG, "onServiceDisconnected() called with: name = [" + name + "]");
        }
    };
}
