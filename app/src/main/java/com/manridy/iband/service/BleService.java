package com.manridy.iband.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.google.gson.Gson;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.ToastUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.R;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.view.main.MainActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleActionListener;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.scan.TimeMacScanCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
import static com.manridy.iband.common.EventGlobal.ACTION_HEALTH_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_HEALTH_TESTED;
import static com.manridy.iband.common.EventGlobal.ACTION_HR_TEST;
import static com.manridy.iband.common.EventGlobal.ACTION_HR_TESTED;
import static com.manridy.iband.common.EventGlobal.ACTION_MICRO_TESTED;
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
        watch = Watch.getInstance();//初始化手表sdk
        watch.init(getApplicationContext());
//        threadIsRun = true; //tread线程控制标志
//        thread.start();
        startThread();
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
        Log.i(TAG,"initConnect(boolean isScan)");
        initConnect(isScan,mBleConnectCallback);
    }

    /**
     * 初始化连接
     * @param isScan 是否需要扫描
     * @param bleConnectCallback 结果回调
     */
    public void initConnect(boolean isScan,final BleConnectCallback bleConnectCallback) {
        LogUtil.d(TAG, "initConnect() called with: isScan = [" + isScan + "], bleConnectCallback = [" + bleConnectCallback + "]");
        if (!watch.isBluetoothEnable()) {
            SPUtil.put(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
        }
        final String mac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        Log.i(TAG,"initConnect:mac:"+mac);
        if (mac==null || mac.isEmpty()) {
            bleConnectCallback.onConnectFailure(new BleException(999,"mac is null!"));
            return;
        }
        Log.i(TAG,"initConnect:scanAndConnect(mac,bleConnectCallback);"+isScan);
        Log.i(TAG,"initConnect:scanAndConnect(mac,bleConnectCallback);mac:"+mac);
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
        Log.i(TAG,"scanAndConnect");
        watch.startScan(new TimeMacScanCallback(mac,12000) {
            @Override
            public void onDeviceFound(boolean isFound, BluetoothDevice device) {
                Log.i(TAG,"onDeviceFound:"+isFound);
                if (isFound) {
                    Watch.getInstance().connect(mac,true,bleConnectCallback);
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
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter1);
    }

    /**
     * @Name yanwen
     * @Date 18/11/20
     * */
    private void unregisterReceiver() {
//        unregisterReceiver(bleReceiver);
        unregisterReceiver(netWorkStateReceiver);
    }

    private BroadcastReceiver  netWorkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取ConnectivityManager对象对应的NetworkInfo对象
                //获取WIFI连接的信息
                NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //获取移动数据连接的信息
                NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifiNetworkInfo.isConnected() || dataNetworkInfo.isConnected()) {
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_CHANGE_NETWOOK_ON));
                    HttpService.getInstance().getDeviceList(new OnResultCallBack() {
                        @Override
                        public void onResult(boolean result, Object o) {
                            if (result) {
                                String strDeviceList = o.toString();
                                //解析服务器设备列表数据
                                SPUtil.put(getApplication(),AppGlobal.DATA_DEVICE_LIST, strDeviceList);
                                DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
                                //筛选iband设备数据
                                ArrayList<String> nameList = new ArrayList<>();
                                for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                                    if (resultBean.getIdentifier().equals("iband")) {
                                        nameList.add(resultBean.getDevice_name());
                                    }
                                }
                                String str =new Gson().toJson(nameList);
                                SPUtil.put(getApplication(),AppGlobal.DATA_DEVICE_FILTER,str);
                                IbandApplication.isNeedRefresh = true;
                            }
                        }
                    });
                }
                //API大于23时使用下面的方式进行网络监听
            }else {
                //获得ConnectivityManager对象
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //获取所有网络连接的信息
                Network[] networks = connMgr.getAllNetworks();
                //用于存放网络连接信息
                StringBuilder sb = new StringBuilder();
                boolean isConnected = false;
                //通过循环将网络信息逐个取出来
                for (int i=0; i < networks.length; i++){
                    //获取ConnectivityManager对象对应的NetworkInfo对象
                    NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                    if(networkInfo!=null&&networkInfo.isConnected()) {
                        isConnected = true;
                    }
                }
                if(isConnected) {
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_CHANGE_NETWOOK_ON));
                    HttpService.getInstance().getDeviceList(new OnResultCallBack() {
                        @Override
                        public void onResult(boolean result, Object o) {
                            if (result) {
                                String strDeviceList = o.toString();
                                //解析服务器设备列表数据
                                SPUtil.put(getApplication(),AppGlobal.DATA_DEVICE_LIST, strDeviceList);
                                DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
                                //筛选iband设备数据
                                ArrayList<String> nameList = new ArrayList<>();
                                for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                                    if (resultBean.getIdentifier().equals("iband")) {
                                        nameList.add(resultBean.getDevice_name());
                                    }
                                }
                                String str =new Gson().toJson(nameList);
                                SPUtil.put(getApplication(),AppGlobal.DATA_DEVICE_FILTER,str);
                                IbandApplication.isNeedRefresh = true;
                            }
                        }
                    });
                }

            }
        }
    };

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
//                    reConnectHandler.removeCallbacksAndMessages(null);
                    break;
                case ACTION_GATT_RECONNECT:
                    LogUtil.e(TAG,"设备地址 "+macStr+" 蓝牙状态----蓝牙重连中");
                    String mac = (String) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_BIND_MAC,"");
                    if (mac!=null && !mac.isEmpty()) {
                        SPUtil.put(BleService.this, AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_CONNECTING);
                        showNotification(2);
                    }
                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_CONNECTING));
//                    connectWardHandler.sendEmptyMessage(0);
//                    reConnect_time_interval = 30;
//                    reConnect_times = 0;
//                    reConnectHandler.removeCallbacksAndMessages(null);
//                    reConnectHandler.postDelayed(reConnectThread,2000);
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
//                    connectWardHandler.sendEmptyMessage(2);
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

//    public Handler reConnectHandler = new Handler();
//
//
//    public Runnable reConnectThread = new Runnable() {
//        @Override
//        public void run() {
//            reConnect();
//        }
//    };

//    BluetoothAdapter mBluetoothAdapter;
//    BluetoothManager bluetoothManager;
//    public int reConnect_time_interval = 5;
//    public int reConnect_times = 0;
//    private boolean isReConnecting = false;
//    private synchronized void reConnect(){
//        int state = (int) SPUtil.get(this.getBaseContext(), AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
//        String mac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC,"");
//        if("".equals(mac)){
//            reConnectHandler.removeCallbacksAndMessages(null);
//            return;
//        }
//        Log.i(TAG,"reConnect():state:"+state);
//        if(state==AppGlobal.DEVICE_STATE_CONNECTED){
//            reConnectHandler.removeCallbacksAndMessages(null);
//            isReConnecting = false;
//            return;
//        }else{
//            isReConnecting = true;
//        }
//
////        if(bluetoothManager==null){
////            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
////        }
////        if(mBluetoothAdapter==null){
////            mBluetoothAdapter = bluetoothManager.getAdapter();
////        }
////        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
////            Log.i(TAG,"reConnect():mBluetoothAdapter"+mBluetoothAdapter+";mBluetoothAdapter.isEnabled()"+mBluetoothAdapter.isEnabled());
////            return;
////        }
//
//        EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_SEARCHING));
//        IbandApplication.getIntance().service.watch.closeBluetoothGatt(mac);
//        IbandApplication.getIntance().service.initConnect(true,new BleConnectCallback() {
//            @Override
//            public void onConnectSuccess() {
//                Log.i(TAG,"reConnect():onConnectSuccess()");
//                reConnectHandler.removeCallbacksAndMessages(null);
//                EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_CONNECTING));
//                isReConnecting = false;
//                reConnect_times = 0;
//            }
//
//            @Override
//            public void onConnectFailure(final BleException exception) {
//                Log.i(TAG,"reConnect():onConnectFailure()");
////                int multiple = (reConnect_times/5)+1;
//                int multiple = 1;
//                long delayMillis = reConnect_time_interval*1000*multiple;
//
////                if(delayMillis>=300*1000){
////                    delayMillis = 300*1000;
////                    reConnect_times--;
////                }
//
//                if(reConnect_times<2){
//                    reConnect_times++;
//                    reConnectHandler.removeCallbacksAndMessages(null);
//                    reConnectHandler.postDelayed(reConnectThread,delayMillis);
//                }else if(reConnect_times==2){
//                    reConnectHandler.removeCallbacksAndMessages(null);
//                    reConnect_times++;
//                    reConnectHandler.postDelayed(reConnectThread,60*1000);
//                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_UNFIND));
//                }else if(reConnect_times>=3){
//                    reConnectHandler.removeCallbacksAndMessages(null);
////                    reConnectHandler.postDelayed(reConnectThread,delayMillis);
//                    EventBus.getDefault().post(new EventMessage(EventGlobal.STATE_DEVICE_UNFIND));
//                }
//
//            }
//        });
//    }

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
//                    if (healths[3]) {
//                        EventBus.getDefault().post(new EventMessage(ACTION_MICRO_TESTED));
//                        if (IbandApplication.getIntance().service != null && IbandApplication.getIntance().service.watch != null) {
//                            IbandApplication.getIntance().service.watch.sendCmd(BleCmd.getMicroData(2));
//                        }
//                    }
                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
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
                    boolean isEnd = end(BleService.this);
                    if (!isEnd) {
                        toastHandler.sendEmptyMessage(0);
                    }
                    break;
                case ACTION_CALL_RUN:
                    answerRingingCall(BleService.this);
                    break;
                case ACTION_MICRO_TESTED:
                    byte[] data = (byte[]) o;
                    if (0x03 == data[1]) {  //32 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00微循环测量完成
                        EventBus.getDefault().post(new EventMessage(ACTION_MICRO_TESTED));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public Handler toastHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ToastUtil.showToast(getApplicationContext(),getString(R.string.error_call_end));
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

    public boolean end(Context context){
        boolean isEnd = false;
        try {
            Method getITelephonyMethod =TelephonyManager.class
                    .getDeclaredMethod("getITelephony", (Class[]) null);
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);//监听电话服务
            getITelephonyMethod.setAccessible(true);
            ITelephony  mITelephony = (ITelephony) getITelephonyMethod.invoke(tm,
                    (Object[]) null);
            // 拒接来电
            isEnd =mITelephony.endCall();
            LogUtil.d(TAG, "end() called with: context = [" + isEnd+ "]") ;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "end() called with: context = NoSuchMethodException");
        }catch (IllegalAccessException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "end() called with: context = IllegalAccessException");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "end() called with: context = InvocationTargetException");
        }catch (RemoteException e) {
            LogUtil.d(TAG, "end() called with: context = RemoteException");
            e.printStackTrace();
        }

        return isEnd;
    }


    public void answerRingingCall(Context context){
        try {
            LogUtil.d(TAG, "answerRingingCall() called ");
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
        boolean isTestHr,isTestBp,isTestBo,isTestMicro;
        if (healthType == 0) {
            isTestHr = isTestBp = isTestBo = isTestMicro = true;
        }else {
            isTestHr = (healthType & 0x1) == 1;
            isTestBp = (healthType >>1 & 0x1) == 1;
            isTestBo = (healthType >>2 & 0x1) == 1;
            isTestMicro = (healthType >>3 & 0x1) == 1;
        }
        return new boolean[]{isTestHr,isTestBp,isTestBo,isTestMicro};
    }

//    Handler connectWardHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            //检测运行状态
//            //运行任务
//            //抛出下次任务
//            if (msg.what == 0) {//开始任务
//                isConnectRun = true;
//                sendEmptyMessageDelayed(1,30*1000);
//            }else if (msg.what == 1){//执行任务
//                if (isConnectRun){
//                    int connectState = (int) SPUtil.get(BleService.this,AppGlobal.DATA_DEVICE_CONNECT_STATE, DEVICE_STATE_UNCONNECT);
//                    if (connectState != DEVICE_STATE_CONNECTED ) {
//                        initConnect(false,mBleConnectCallback);
//                        sendEmptyMessageDelayed(1,15*1000);
//                        LogUtil.d(TAG, "connectWardHandler() called initConnect = [" + msg + "]");
//                    }
//                }
//            }else if (msg.what == 2){//结束任务
//                isConnectRun = false;
//                removeMessages(1);
//            }
//        }
//    };


    List<ConnectMessage> messageList = new ArrayList<>();
    AtomicBoolean isConnectRun = new AtomicBoolean(false);
    class ConnectMessage {
        String mac;
        boolean isReConnect;
        BleConnectCallback connectCallback;

        public ConnectMessage(String mac, boolean isReConnect, BleConnectCallback connectCallback) {
            this.mac = mac;
            this.isReConnect = isReConnect;
            this.connectCallback = connectCallback;
        }
    }

    boolean threadIsRun = false;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (threadIsRun){
                if (!isConnectRun.get())
                {
                    if (messageList.size() > 0) {
                        final ConnectMessage connectMessage = messageList.get(0);//拿到消息
                        isConnectRun.set(true);
                        watch.closeBluetoothGatt(connectMessage.mac);
                        Watch.getInstance().connect(connectMessage.mac, connectMessage.isReConnect, new BleConnectCallback() {
                            @Override
                            public void onConnectSuccess() {
                                if (connectMessage.connectCallback != null) {
                                    connectMessage.connectCallback.onConnectSuccess();
                                    messageList.clear();//清空任务队列
                                }
                                isConnectRun.set(false);
                            }

                            @Override
                            public void onConnectFailure(BleException exception) {
                                if (connectMessage.connectCallback != null) {
                                    connectMessage.connectCallback.onConnectFailure(exception);
                                    if (messageList.size()>0) {
                                        messageList.remove(0);//删除消息队列消息
                                    }
                                }
                                isConnectRun.set(false);
                            }
                        });
                    } else {
                        synchronized (thread) {
                            try {
                                thread.wait();//线程休眠
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
    });

    /**
     * @Desc 控制线程启动
     * @Name yanwen
     * @Date 2018/11/20
     * */
    public void startThread() {
        if (thread != null) {
            threadIsRun = true;
            thread.start();
        }
    }
    /**
     * @Desc 控制线程停止
     * @Name yanwen
     * @Date 2018/11/20
     * */
    public void stopThread() {
        threadIsRun = false;
        thread = null;
    }


    private void connectAction(String mac, boolean isReConnect, BleConnectCallback connectCallback){
        messageList.add(new ConnectMessage(mac, isReConnect, connectCallback));
        synchronized (thread) {
            thread.notify();
        }
    }

        BleConnectCallback mBleConnectCallback = new BleConnectCallback() {
        @Override
        public void onConnectSuccess() {
            LogUtil.d(TAG, "onConnectSuccess() called");
        }

        @Override
        public void onConnectFailure(BleException exception) {
//            startConnectWardThread();
            LogUtil.d(TAG, "onConnectFailure() called with: exception = [" + exception.toString() + "]");
//            reConnect_time_interval = 30;
//            reConnect_times = 0;
//            reConnectHandler.removeCallbacksAndMessages(null);
//            reConnectHandler.postDelayed(reConnectThread,1000);
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
        unregisterReceiver();
        return super.onUnbind(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
