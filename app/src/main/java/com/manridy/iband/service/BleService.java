package com.manridy.iband.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.R;
import com.manridy.iband.view.MainActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.callback.BleActionListener;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.scan.TimeMacScanCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.manridy.iband.common.AppGlobal.DEVICE_STATE_CONNECTED;
import static com.manridy.iband.common.AppGlobal.DEVICE_STATE_CONNECTING;
import static com.manridy.iband.common.AppGlobal.DEVICE_STATE_UNCONNECT;
import static com.manridy.iband.common.EventGlobal.ACTION_BATTERY_NOTIFICATION;
import static com.manridy.iband.common.EventGlobal.ACTION_BO_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_BO_TESTED;
import static com.manridy.iband.common.EventGlobal.ACTION_BP_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_BP_TESTED;
import static com.manridy.iband.common.EventGlobal.ACTION_CALL_END;
import static com.manridy.iband.common.EventGlobal.ACTION_CALL_RUN;
import static com.manridy.iband.common.EventGlobal.ACTION_CAMERA_CAPTURE;
import static com.manridy.iband.common.EventGlobal.ACTION_CAMERA_EXIT;
import static com.manridy.iband.common.EventGlobal.ACTION_FIND_PHONE_START;
import static com.manridy.iband.common.EventGlobal.ACTION_FIND_PHONE_STOP;
import static com.manridy.iband.common.EventGlobal.ACTION_FIND_WATCH_STOP;
import static com.manridy.iband.common.EventGlobal.ACTION_HEALTH_TESTED;
import static com.manridy.iband.common.EventGlobal.ACTION_HEALTH_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_HR_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_HR_TESTED;
import static com.manridy.sdk.BluetoothLeManager.ACTION_DATA_AVAILABLE;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_CONNECT;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_DISCONNECTED;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_RECONNECT;
import static com.manridy.sdk.BluetoothLeManager.ACTION_NOTIFICATION_ENABLE;
import static com.manridy.sdk.BluetoothLeManager.ACTION_SERVICES_DISCOVERED;

/**
 * 蓝牙后台服务
 * Created by jarLiao .
 */
public class BleService extends Service {
    private String TAG = "BleService";
    public Watch watch;

    public void init(){
        watch = Watch.getInstance(this);//初始化手表sdk
        initListener();//初始化监听器
        initBroadcast();//初始化ble广播
        initConnect(true);//初始化连接
        EventBus.getDefault().register(this);
    }

    private void initListener() {
        watch.setActionListener(actionListener);//设置动作监听
        watch.setStepNotifyListener(notifyListener);//设置分段计步上报监听
        watch.setRunNotifyListener(notifyListener);//设置跑步上报监听
    }

    /**
     * 初始化连接
     * @param isScan 手否扫描设备
     */
    public void initConnect(boolean isScan){
        initConnect(isScan,mBleConnectCallback);
    }

    /**
     * 初始化连接
     * @param isScan 是否需要扫描
     * @param bleConnectCallback 结果回调
     */
    public void initConnect(boolean isScan,final BleConnectCallback bleConnectCallback) {
        if (!watch.isBluetoothEnable()) {
            SPUtil.put(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
        }
        final String mac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        if (mac==null || mac.isEmpty()) {
            bleConnectCallback.onConnectFailure(new BleException(999,"mac is null!"));
            return;
        }
        if (isScan) {
            scanAndConnect(mac,bleConnectCallback);
        }else{
            watch.connect(mac,true,bleConnectCallback);
        }
    }

    /**
     * 扫描后连接
     * @param mac 设备mac地址
     * @param bleConnectCallback 结果回调
     */
    private void scanAndConnect(final String mac, final BleConnectCallback bleConnectCallback){
        watch.startScan(new TimeMacScanCallback(mac,5000) {
            @Override
            public void onDeviceFound(boolean isFound, BluetoothDevice device) {
                if (isFound) {
                    watch.connect(mac,true,bleConnectCallback);
                }else {
                    bleConnectCallback.onConnectFailure(new BleException(1000,"no find device!"));
                }
            }
        });
    }

    /**
     * 显示常驻通知栏消息
     * @param connectState 连接状态
     */
    private void showNotification(int connectState) {
        try {
            String state = getState(connectState);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(state)
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setOngoing(false);//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            mBuilder.setContentIntent(pendingIntent);
            Notification notification = mBuilder.build();
            startForeground(1, notification);
        }catch (Exception e){
            e.toString();
        }
    }

    /**
     * 取消显示常驻消息栏
     */
    public void stopNotification(){
        stopForeground(true);
    }

    /**
     * 获取连接状态对应文字显示
     * @param connectState
     * @return
     */
    @NonNull
    private String getState(int connectState) {
        String state = getString(R.string.hint_device_unconnect);
        if (connectState == 1){
            state = getString(R.string.hint_device_connected);
        }else if (connectState == 2){
            state = getString(R.string.hint_device_connecting);
        }
        return state;
    }

    /**
     * 初始化ble过滤意图
     */
    private void initBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GATT_CONNECT);
        filter.addAction(ACTION_GATT_DISCONNECTED);
        filter.addAction(ACTION_GATT_RECONNECT);
        filter.addAction(ACTION_SERVICES_DISCOVERED);
        filter.addAction(ACTION_NOTIFICATION_ENABLE);
        filter.addAction(ACTION_DATA_AVAILABLE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(bleReceiver,filter);
    }

    /**
     *
     */
    private BroadcastReceiver bleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String macStr = intent.getStringExtra("BLUETOOTH_MAC");
            byte[] macData = intent.getByteArrayExtra("BLUETOOTH_DATA");
            switch (action){
                case ACTION_GATT_CONNECT:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----蓝牙已连接");
//                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_CONNECT));
                    break;
                case ACTION_GATT_RECONNECT:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----蓝牙重连中");
                    String mac = (String) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_BIND_MAC,"");
                    if (mac!=null && !mac.isEmpty()) {
                        SPUtil.put(BleService.this, AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_CONNECTING);
                        showNotification(2);
                    }
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_CONNECTING));
                    startConnectWardThread();
                    break;
                case ACTION_GATT_DISCONNECTED:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----蓝牙已断开");
                    String mac2 = (String) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_BIND_MAC,"");
                    if (mac2!=null && !mac2.isEmpty()) {
                        showNotification(0);
                    }
                    SPUtil.put(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_DISCONNECT,macData));
                    break;
                case ACTION_SERVICES_DISCOVERED:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----发现服务");
                    break;
                case ACTION_NOTIFICATION_ENABLE:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----打开通知");
                    showNotification(1);
                    SPUtil.put(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_CONNECTED);
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_CONNECT));
                    break;
                case ACTION_DATA_AVAILABLE:
                    final byte[] data = intent.getByteArrayExtra("BLUETOOTH_DATA");
//                    LogUtil.e(TAG,"蓝牙状态----数据:"+ BitUtil.parseByte2HexStr(data));
                    break;
                default:
                    break;
            }
        }
    };

    private void startConnectWardThread() {
        if (!isConnectRun) {
            connectWardThread.start();
        }
    }

    BleActionListener actionListener = new BleActionListener() {
        @Override
        public void onAction(int type, Object o) {
            switch (type) {
                case ACTION_CAMERA_EXIT:
                    EventBus.getDefault().post(new EventMessage(ACTION_CAMERA_EXIT));
                    break;
                case ACTION_CAMERA_CAPTURE:
                    EventBus.getDefault().post(new EventMessage(ACTION_CAMERA_CAPTURE));
                    break;
                case ACTION_FIND_PHONE_START:
                    EventBus.getDefault().post(new EventMessage(ACTION_FIND_PHONE_START));
                    break;
                case ACTION_FIND_PHONE_STOP:
                    EventBus.getDefault().post(new EventMessage(ACTION_FIND_PHONE_STOP));
                    break;
                case ACTION_FIND_WATCH_STOP:
                    EventBus.getDefault().post(new EventMessage(ACTION_FIND_WATCH_STOP));
                    break;
                case ACTION_BATTERY_NOTIFICATION:
                    SyncAlert.getInstance(BleService.this).parseBattery(o);
                    EventBus.getDefault().post(new EventMessage(ACTION_BATTERY_NOTIFICATION));
                    break;
                case ACTION_HEALTH_TESTED:
                    int healthType = (int) o;
                    boolean[] healths = getHealthTypes(healthType);
                    if (healths[0]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_HR_TESTED));
                    }
                    if (healths[1]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_BP_TESTED));
                    }
                    if (healths[2]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_BO_TESTED));
                    }
                    break;
                case ACTION_HEALTH_TEST:
                    int healthType2 = (int) o;
                    boolean[] healths2 = getHealthTypes(healthType2);
                    if (healths2[0]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_HR_TEST));
                    }
                    if (healths2[1]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_BP_TEST));
                    }
                    if (healths2[2]) {
                        EventBus.getDefault().post(new EventMessage(ACTION_BO_TEST));
                    }
                    break;
                case ACTION_CALL_END:
                    end(BleService.this);
                    break;
                case ACTION_CALL_RUN:
                    answerRingingCall(BleService.this);
                    break;
                default:
                    break;
            }
        }
    };

    BleNotifyListener notifyListener = new BleNotifyListener() {
        @Override
        public void onNotify(Object o) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_LOCALE_CHANGED) {
            int connectState = (int) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE,0);
            showNotification(connectState);
        }
    }

    public void end(Context context){
        try {
            Method getITelephonyMethod =TelephonyManager.class
                    .getDeclaredMethod("getITelephony", (Class[]) null);
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);//监听电话服务
            getITelephonyMethod.setAccessible(true);
            ITelephony  mITelephony = (ITelephony) getITelephonyMethod.invoke(tm,
                    (Object[]) null);
            // 拒接来电
            mITelephony.endCall();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void answerRingingCall(Context context){
        try {
            Log.d(TAG, "answerRingingCall() called ");
            Method getITelephonyMethod =TelephonyManager.class
                    .getDeclaredMethod("getITelephony", (Class[]) null);
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);//监听电话服务
            getITelephonyMethod.setAccessible(true);
            ITelephony  mITelephony = (ITelephony) getITelephonyMethod.invoke(tm,
                    (Object[]) null);
            // 拒接来电
            mITelephony.answerRingingCall();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean[] getHealthTypes(int healthType){
        boolean isTestHr,isTestBp,isTestBo;
        if (healthType == 0) {
            isTestHr = isTestBp = isTestBo = true;
        }else {
            isTestHr = (healthType & 0x1) == 1;
            isTestBp = (healthType >>1 & 0x1) == 1;
            isTestBo = (healthType >>2 & 0x1) == 1;
        }
        return new boolean[]{isTestHr,isTestBp,isTestBo};
    }

    boolean isConnectRun;
    Thread connectWardThread = new Thread(){
        @Override
        public void run() {
            super.run();
            isConnectRun = true;
            while (isConnectRun) {
                try {
                    sleep(15*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
               int connectState = (int) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
                if (connectState != DEVICE_STATE_CONNECTED ) {
                    Log.d(TAG, "connectWardThread run() =================");
                    initConnect(true,mBleConnectCallback);
                }
                Log.d(TAG, "connectWardThread isConnectRun() =================");
            }
        }
    };

    BleConnectCallback mBleConnectCallback = new BleConnectCallback() {
        @Override
        public void onConnectSuccess() {
            Log.d(TAG, "onConnectSuccess() called");
        }

        @Override
        public void onConnectFailure(BleException exception) {
            startConnectWardThread();
            Log.d(TAG, "onConnectFailure() called with: exception = [" + exception.toString() + "]");
        }
    };

    public class LocalBinder extends Binder {
        public BleService service(){
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = null;
        if (null == result){
            result = new LocalBinder();
        }
        return result;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        EventBus.getDefault().unregister(this);
        return super.onUnbind(intent);
    }

}
