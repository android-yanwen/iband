package com.manridy.iband.service;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.bean.AppModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jarLiao on 17/11/11.
 */

public class AppNotificationListenerService extends NotificationListenerService {
    private static final String TAG = "NotificationService";
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private boolean cmdFirst = true;
    private int appAlert = -1;

    public static final String PAGE_NAME_QQ ="com.tencent.mobileqq";
    public static final String PAGE_NAME_QQ_I ="com.tencent.mobileqqi";//qq国际版
    public static final String PAGE_NAME_QQ_LITE ="com.tencent.qqlite";//qq轻聊版
    public static final String PAGE_NAME_WX ="com.tencent.mm";
    public static final String PAGE_NAME_WHATSAPP ="com.whatsapp";
    public static final String PAGE_NAME_FACEBOOK ="com.facebook.katana";
    public static final String PAGE_NAME_LINE ="jp.naver.line.android";
    public static final String PAGE_NAME_TWITTER ="com.twitter.android";
    public static final String PAGE_NAME_SKYPE ="com.skype.raider";
    public static final String PAGE_NAME_SKYPE_FOR_CHINA ="com.skype.rover";

    public static final String PAGE_NAME_INS ="com.instagram.android";

    public static final int APP_ID_QQ =2;
    public static final int APP_ID_WX =4;
    public static final int APP_ID_WHATSAPP =5;
    public static final int APP_ID_FACEBOOK =6;
    public static final int APP_ID_LINE =7;
    public static final int APP_ID_TWITTER =8;
    public static final int APP_ID_SKYPE =9;
    public static final int APP_ID_INS =10;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate() called");
    }

    /*----------------- 静态方法 -----------------*/
    public synchronized static void startNotificationService(Context context) {
        context.startService(new Intent(context, AppNotificationListenerService.class));
    }

    public synchronized static void stopNotificationService(Context context) {
        context.stopService(new Intent(context, AppNotificationListenerService.class));
    }


    public static void startNotificationListenSettings(Context context) {
        Intent intent = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
        if(!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static boolean isNotificationListenEnable(Context context) {
        return isNotificationListenEnable(context, context.getPackageName());
    }

    public static boolean isNotificationListenEnable(Context context, String pkgName) {
        final String flat = Settings.Secure.getString(context.getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*----------------- 监听通知状态 -----------------*/
    int infoId = 1;
    List<byte[]> cmdList = new ArrayList<>();
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        sendAppAlert(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
    String lastContentSkype = "";
    boolean isSendSkype = false;
    String lastContentLINE = "";
    boolean isSendLINE = false;
    private synchronized void sendAppAlert(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Notification notification = sbn.getNotification();
        if(!isHandleNotification(packageName)){
            return;
        }
        String content = String.valueOf(notification.tickerText);

        //通过以下方式可以获取Notification的详细信息
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            Bundle extras = sbn.getNotification().extras;
            String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
            CharSequence notificationText = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence notificationSubText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            LogUtil.i("SevenNLS", "notificationTitle:"+notificationTitle);
            LogUtil.i("SevenNLS", "notificationText:"+notificationText);
            LogUtil.i("SevenNLS", "notificationSubText:"+notificationSubText);
            if (packageName.equals(APP_ID_TWITTER) || isSkypePackage(packageName)){
                content = notificationTitle +":"+ notificationText;
            }
        }
        LogUtil.i(TAG,"sbn"+ sbn.toString());
        LogUtil.i(TAG,"sbn"+  content);
        boolean appOnOff = (boolean) SPUtil.get(this, AppGlobal.DATA_ALERT_APP,false);
        if (appOnOff && !content.equals("null")) {
            List<AppModel> appList = IbandDB.getInstance().getAppList();
            Map<Integer,AppModel> map = new HashMap<>();
            cmdList = getCmdList(content);
            appAlert = -1;
            for (AppModel appModel : appList) {
                map.put(appModel.getAppId(),appModel);
            }
            boolean qqAlert = map.containsKey(APP_ID_QQ) && map.get(APP_ID_QQ).isOnOff();
            boolean wxAlert = map.containsKey(APP_ID_WX) && map.get(APP_ID_WX).isOnOff();
            boolean whatsAlert = map.containsKey(APP_ID_WHATSAPP) && map.get(APP_ID_WHATSAPP).isOnOff();
            boolean facebookAlert = map.containsKey(APP_ID_FACEBOOK) && map.get(APP_ID_FACEBOOK).isOnOff();
            boolean lineAlert = map.containsKey(APP_ID_LINE) && map.get(APP_ID_LINE).isOnOff();
            boolean twitterAlert = map.containsKey(APP_ID_TWITTER) && map.get(APP_ID_TWITTER).isOnOff();
            boolean skypeAlert = map.containsKey(APP_ID_SKYPE) && map.get(APP_ID_SKYPE).isOnOff();
            boolean insAlert = map.containsKey(APP_ID_INS) && map.get(APP_ID_INS).isOnOff();

            if (isQQPackage(packageName) && qqAlert) {
                appAlert = APP_ID_QQ;
            }else if (packageName.equals(PAGE_NAME_WX) && wxAlert){
                appAlert = APP_ID_WX;
            }else if (packageName.equals(PAGE_NAME_WHATSAPP) && whatsAlert){
                appAlert = APP_ID_WHATSAPP;
            }else if (packageName.equals(PAGE_NAME_FACEBOOK) && facebookAlert){
                appAlert = APP_ID_FACEBOOK;
            }else if (packageName.equals(PAGE_NAME_LINE) && lineAlert) {
                appAlert = APP_ID_LINE;
                if(lastContentLINE.equals(content)){
                    if(isSendLINE) {
                        isSendLINE = false;
                        return;
                    }
                }
                lastContentLINE = content;
                isSendLINE = true;
            }else if (packageName.equals(PAGE_NAME_TWITTER) && twitterAlert) {
                appAlert = APP_ID_TWITTER;
            }else if (isSkypePackage(packageName) && skypeAlert) {
                appAlert = APP_ID_SKYPE;
                if(lastContentSkype.equals(content)){
                    if(isSendSkype) {
                        isSendSkype = false;
                        return;
                    }
                }
                lastContentSkype = content;
                isSendSkype = true;
            }else if (packageName.equals(PAGE_NAME_INS) && insAlert) {
                appAlert = APP_ID_INS;
            }

            if (appAlert != -1) {
                cmdFirst = true;
                infoId = infoId > 63 ? 1 : infoId++;
                if (IbandApplication.getIntance().service.watch != null) {
                    IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setAppAlertName(infoId,appAlert), AppleCallback);
                }
            }
        }
    }

    private boolean isSkypePackage(String packageName) {
        return packageName.equals(PAGE_NAME_SKYPE) || packageName.equals(PAGE_NAME_SKYPE_FOR_CHINA);
    }

    private boolean isQQPackage(String packageName) {
        return packageName.equals(PAGE_NAME_QQ)||packageName.equals(PAGE_NAME_QQ_I)||packageName.equals(PAGE_NAME_QQ_LITE);
    }

    BleCallback AppleCallback = new BleCallback() {
        @Override
        public void onSuccess(Object o) {
            if (cmdList.size()>0 && !cmdFirst) {
                cmdList.remove(0);
            }
            cmdFirst = false;
            if (cmdList.size()>0 && appAlert!= -1) {
                IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setAppAlertContext(infoId,appAlert,cmdList.get(0)), AppleCallback);
            }else {
                LogUtil.d(TAG, "app提醒发送完成");
            }
        }

        @Override
        public void onFailure(BleException exception) {
            LogUtil.d(TAG, "onFailure() called with: exception = [" + exception.toString() + "]");
        }
    };

    private List<byte[]> getCmdList(String content)  {
        List<byte[]> bytes = new ArrayList<>();
        byte[] contexts = new byte[0];//string转uicode编码 大端在前
        try {
            contexts = content.getBytes("UnicodeBigUnmarked");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int len = contexts.length;
        int index = 0;
        while (len >0){
            byte[] cmd = new byte[len>12?12:len];
            System.arraycopy(contexts,index*12,cmd,0,len>12?12:len);
            bytes.add(cmd);
            len = len>12?len-12:0;
            index++;
        }
        return bytes;
    }


    public boolean isHandleNotification(String packageName){
        boolean flag = false;
        if (isQQPackage(packageName)) {
            flag = true;
        }else if (packageName.equals(PAGE_NAME_WX)){
            flag = true;
        }else if (packageName.equals(PAGE_NAME_WHATSAPP)){
            flag = true;
        }else if (packageName.equals(PAGE_NAME_FACEBOOK)){
            flag = true;
        }else if (packageName.equals(PAGE_NAME_LINE)) {
            flag = true;
        }else if (packageName.equals(PAGE_NAME_TWITTER)) {
            flag = true;
        }else if (isSkypePackage(packageName)) {
            flag = true;
        }else if (packageName.equals(PAGE_NAME_INS)) {
            flag = true;
        }
        return flag;
    }

}
