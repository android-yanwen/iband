package com.manridy.iband.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.IbandApplication;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.type.PhoneType;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 提醒服务
 * Created by Administrator on 2016/8/15.
 */
public class AlertService extends Service {
    private String TAG = AlertService.class.getSimpleName();

    private IbandApplication ibandApplication;
    private AlertReceiver alertReceiver;
    private List<byte[]> smsList = new ArrayList<>();//短信内容字节集合
    private String smsContent;//短信内容字符串
    private int smsId = 1;//短信id(1-63)
    private boolean smsConfirm;//是否第一包名称返回
    private long callTime;//来电时间

    @Override
    public void onCreate() {
        super.onCreate();
        ibandApplication = (IbandApplication) getApplication();
        initReceiver();//初始化电话/短信监听/蓝牙状态广播
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    /**
     * 初始化广播
     */
    private void initReceiver() {
        IntentFilter filter = getIntentFilter();
        alertReceiver = new AlertReceiver();
        registerReceiver(alertReceiver,filter);
    }

    /**
     * 初始化过滤器
     * 电话/短信监听/蓝牙状态广播
     * @return
     */
    @NonNull
    private IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        return filter;
    }

    /**
     * 广播接收处理者
     */
    private class AlertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.provider.Telephony.SMS_RECEIVED")){//收到短信通知
                smsReceived(context, intent);
                LogUtil.d(TAG, "onReceive() called with: SMS_RECEIVED = [" + context + "], intent = [" + intent + "]");
            }else if(action.equals("android.intent.action.PHONE_STATE")){//电话状态监听
                TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);//设置监听电话回调
                tm.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
                LogUtil.d(TAG, "onReceive() called with: PHONE_STATE = [" + context + "], intent = [" + intent + "]");
            }else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {//蓝牙状态监听
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_OFF) {//蓝牙关闭
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_CHANGE_BLUETOOTH_OFF));
                }else if (state == BluetoothAdapter.STATE_TURNING_ON) {//蓝牙开启中
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_CHANGE_BLUETOOTH_ON_RUNING));
                }else if (state == BluetoothAdapter.STATE_ON) {//蓝牙已开启
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_CHANGE_BLUETOOTH_ON));
                }
                LogUtil.e(TAG, "onReceive() called with: ACTION_STATE_CHANGED, state = [" + state + "]");
            }else if (action.equals(Intent.ACTION_LOCALE_CHANGED)){
                EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_LOCALE_CHANGED));
            }
        }
    }


    /**
     * 电话状态监听回调
     */
    PhoneStateListener listener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (!checkOpenPhoneAlert()) {
                    return;//判断是否开启来电提醒
                }
                switch(state){
                    case TelephonyManager.CALL_STATE_RINGING://电话等待接听
                        callTime = System.currentTimeMillis();//记录来电时间
                        phoneReceived(incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK://电话接听
                        Watch.getInstance().sendCmd(BleCmd.setInfoAlert(3));//发送暂停来电提醒通知
                        LogUtil.i("PhoneReceiver", "CALL IN ACCEPT :" + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE://电话挂机
                        //增加挂断状态触发判断，少于1秒内手机触发挂断状态不发送挂断状态通知给设备
                        if (System.currentTimeMillis() - callTime >1000) {
                            Watch.getInstance().sendCmd(BleCmd.setInfoAlert(3));//发送暂停来电提醒通知
                        }
                        LogUtil.i("PhoneReceiver", "CALL IDLE");
                        break;
                }
            }
        };

    /**
     * 接收短信解析
     * @param context
     * @param intent
     */
    private void smsReceived(Context context, Intent intent) {
        try {
            if (!checkOpenSmsAlert(context)) {
                return;//判断是否开启短信提醒
            }
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                //初始化变量
                StringBuilder phoneNumber = new StringBuilder();//短信号码
                StringBuilder content = new StringBuilder();//短信内容
                smsId = smsId > 63 ? 1 : smsId++;//短信id
                smsConfirm = false;//是否首包
                //解析短信
                Object pdusData[] = (Object[]) bundle.get("pdus");//将pdus里面的内容转化成Object[]数组
                SmsMessage[] msg = new SmsMessage[pdusData.length];//解析短信
                for (int i = 0; i < msg.length; i++) {
                    byte pdus[] = (byte[]) pdusData[i];
                    msg[i] = SmsMessage.createFromPdu(pdus);
                }
                for (SmsMessage temp : msg) {//分析短信具体参数
                    content.append(temp.getMessageBody());
                    if (!temp.getOriginatingAddress().equals(phoneNumber.toString())) {
                        phoneNumber.append(temp.getOriginatingAddress());
                    }
                }
                //判断号码状态
                if (phoneNumber.toString().isEmpty()) {//1.号码为空,发送未知号码
                    ibandApplication.service.watch.setSmsAlertName(PhoneType.PHONE_NAME, smsId, getString(R.string.hint_phone_num_unknow),smsBleCallback);//发送短信提醒
                }else {
                    String name = getSmsFromPhone(AlertService.this,phoneNumber.toString());
                    if (name == null ||name.isEmpty()) {//2.名称为空，发送号码
                        ibandApplication.service.watch.setSmsAlertName(PhoneType.PHONE_NUMBER,smsId,getFilterNum(phoneNumber.toString()),smsBleCallback);//发送短信提醒
                    }else{//3.名称不为空，发送短信人名称
                        ibandApplication.service.watch.setSmsAlertName(PhoneType.PHONE_NAME,smsId,name,smsBleCallback);//发送短信提醒
                        System.out.println("发送者名称："+name);
                    }
                }
                //检测版本型号（没有默认全发，0003发送8包）
                String deviceType = (String) SPUtil.get(AlertService.this, AppGlobal.DATA_FIRMWARE_TYPE,"");
                //封装内容发送包
                int smsMax = -1;
                if (deviceType.equals("0003")){
                    smsMax = 8;
                }
                smsContent = content.toString();
                smsList = getCmdList(smsContent,smsMax);
                System.out.println("发送者号码："+phoneNumber.toString()+"  短信内容："+content.toString() +"发送包数;"+smsList.size());
            }
        }catch (Exception e){
            e.printStackTrace();
            SPUtil.put(AlertService.this,AppGlobal.DATA_ALERT_SMS,false);
            LogUtil.e(TAG, "短信提醒: 没有获得通讯录权限异常");
        }
    }

    /**
     * 接收来电解析
     * @param incomingNumber
     */
    private void phoneReceived(String incomingNumber) {
        //收到来电提醒
        if (incomingNumber == null || incomingNumber.isEmpty()) {//1.来电号码为空
            ibandApplication.service.watch.sendCmd(BleCmd.setPhoneAlert(1,getString(R.string.hint_phone_num_unknow)));
        }else{
            try {
                String name = getSmsFromPhone(AlertService.this,incomingNumber);
                if (null == name) {//2.来电号码不为空，获取不到名称
                    ibandApplication.service.watch.sendCmd(BleCmd.setPhoneAlert(2,getFilterNum(incomingNumber)));
                }else {//3.获取到来电名称
                    ibandApplication.service.watch.sendCmd(BleCmd.setPhoneAlert(1,name));
                }
                LogUtil.i("PhoneReceiver", "CALL IN RINGING :" + incomingNumber+"NAME : "+name);
            }catch (Exception e){
                e.printStackTrace();
                SPUtil.put(AlertService.this, AppGlobal.DATA_ALERT_PHONE,false);
                LogUtil.e(TAG, "来电提醒: 没有获得通讯录权限异常");
            }
        }
    }

    /**
     * 通过手机号码获取联系人名称
     * @param context
     * @param phoneNumber
     * @return
     */
    public String getSmsFromPhone(Context context,String phoneNumber) {
        Uri personUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber);
        Cursor cur = context.getContentResolver().query(personUri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME },
                null, null, null );
        if (cur != null && cur.moveToFirst()) {
            int nameIdx = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String name = cur.getString(nameIdx);
            cur.close();
            return name;
        }
        return null;
    }

    /**
     * 内容转化unicode编码字节数组
     * @param content
     * @return
     * @throws UnsupportedEncodingException
     */
    private List<byte[]> getCmdList(String content,int maxLen) throws UnsupportedEncodingException {
        List<byte[]> bytes = new ArrayList<>();
        byte[] contexts = content.getBytes("UnicodeBigUnmarked");//string转uicode编码 大端在前
        int len = contexts.length;//总长度
        int index = 0;//当前包数
        while (len >0){//如果总长度大于0，就继续封装字节数组
            int curLen = len>12 ? 12 : len;//如果当前长度大于12，则取12，否则取当前值
            byte[] cmd = new byte[curLen];
            System.arraycopy(contexts,index*12,cmd,0,curLen);//计算截取位置和截取数量，包数*12，
            if (bytes.size() >= maxLen && maxLen != -1) {
                return bytes;
            }
            bytes.add(cmd);//添加字节数组
            len = len - curLen;//总长度等于总长度减当前截取
            index++;//包数自增
        }
        return bytes;
    }

    /**
     * 短信发送回调结果
     * 迭代处理
     * 收到确认判断集合元素数量，大于0，删除集合首包，发送集合首包
     */
    BleCallback smsBleCallback = new BleCallback() {
        @Override
        public void onSuccess(Object o) {
            //收到确认，删除发送集合第0包；加判断是否名称返回确认，首次名称不删除
            if (smsList.size()>0 && smsConfirm) {
                smsList.remove(0);
            }
            smsConfirm = true;
            //如果发送集合数量大于0，继续发送第0包数据，否则显示发送完成
            if (smsList.size()>0) {
                ibandApplication.service.watch.setSmsAlertContent(smsId,smsList.get(0),smsBleCallback);
            }else {
                LogUtil.d(TAG, "短信发送完成");
            }
        }

        @Override
        public void onFailure(BleException exception) {
            LogUtil.d(TAG, "onFailure() called with: exception = [" + exception.toString() + "]");
        }
    };

    /**
     * 检查是否打开短信提醒
     * @param context
     * @return
     */
    private boolean checkOpenSmsAlert(Context context) {
        return (boolean) SPUtil.get(context, AppGlobal.DATA_ALERT_SMS,false);
    }

    /**
     * 检查是否打开手机提醒
     * @return
     */
    private boolean checkOpenPhoneAlert() {
        return (boolean) SPUtil.get(AlertService.this, AppGlobal.DATA_ALERT_PHONE,false);
    }

    private String getFilterNum(String num){
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(num);
        return m.replaceAll("").trim();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertReceiver != null) {//关闭接收广播
            unregisterReceiver(alertReceiver);
        }
        EventBus.getDefault().unregister(this);//EventBus卸载
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
