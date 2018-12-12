package com.manridy.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.manridy.sdk.ble.BleParse;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.common.BitUtil;
import com.manridy.sdk.common.LogUtil;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.exception.GattException;
import com.manridy.sdk.exception.OtherException;
import com.manridy.sdk.exception.TimeOutException;
import com.manridy.sdk.scan.TimeMacScanCallback;
import com.manridy.sdk.scan.TimeScanCallback;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.manridy.sdk.exception.BleException.ERROR_CODE_WRITE;

/**
 * 低功耗蓝牙操作类 BLE
 * Created by Administrator on 2016/10/18.
 */

public class BluetoothLeManager {
//    /**************设置一个重连标志后期优化去掉**************/
//    public static boolean IS_RENECT = true;


    private static final String TAG = BluetoothLeManager.class.getSimpleName();
    private AtomicBoolean isScaning = new AtomicBoolean(false);

    private static final int CONNECT_TIME_OUT = 10000;
    private static final int DISCONNECT_TIME_OUT = 5000;

    public Context mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    public List<BluetoothLeDevice> bluetoothLeDevices = new ArrayList<>();
    private mBluetoothGattCallback mBluetoothGattCallback;
    private BleConnectCallback connectCallback;

    //华为手机
    public BluetoothGatt curBluetoothGatt = null;
    public boolean isConnected;
    public boolean isBluetoothReConnect;
    public String curBluetoothMac;
    public BluetoothDevice curBluetoothDevice;
    private List<BluetoothGatt> oldBluetoothGatts = new ArrayList<>();
    private void cleanCurBluetooth(){
        Log.i(TAG,"oldBluetoothGatts.cleanCurBluetooth:"+oldBluetoothGatts.size());
        curBluetoothMac = null;
        if(curBluetoothGatt!=null){
            curBluetoothGatt.disconnect();
            oldBluetoothGatts.add(curBluetoothGatt);
            Log.i(TAG,"oldBluetoothGatts.add:"+oldBluetoothGatts.size());
        }
        curBluetoothDevice = null;
        isConnected = false;
        isBluetoothReConnect = true;
    }
    String reConnectBluetoothMac;
    BleConnectCallback reConnectCallback;
    Handler reConnectHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(reConnectCallback!=null){
                        reConnectCallback = null;
                    }
                    reConnectCallback = new BleConnectCallback() {
                        @Override
                        public void onConnectSuccess() {

                        }

                        @Override
                        public void onConnectFailure(BleException exception) {
                            reConnectHandler.removeMessages(2);
                            Message message = reConnectHandler.obtainMessage(2);
                            reConnectHandler.sendMessageDelayed(message,20000);
                        }
                    };
                    connect(curBluetoothDevice,true,reConnectCallback);
                    break;
                case 2:
                    Log.i(TAG,"isConnected:"+isConnected+";curBluetoothMac:"+curBluetoothMac);
                    Log.i(TAG,"curBluetoothDevice:"+curBluetoothDevice+";curBluetoothGatt:"+curBluetoothGatt);
                    if(!isConnected){
                        if(curBluetoothMac!=null) {
                            reConnect(curBluetoothMac);
                        }
                    }else{
                        if(curBluetoothGatt==null){
                            isConnected = false;
                            if(curBluetoothMac!=null) {
                                reConnect(curBluetoothMac);
                            }
                        }
                    }
                    break;
            }
        }
    };

    Runnable reConnectRunnale = new Runnable() {
        @Override
        public void run() {
            if(curBluetoothMac!=null){
                startScan(new TimeMacScanCallback(curBluetoothMac,12000) {
                    @Override
                    public void onDeviceFound(boolean isFound, BluetoothDevice device) {
                        if(isFound){
                            curBluetoothDevice = device;
                            Message message = reConnectHandler.obtainMessage(1);
                            reConnectHandler.sendMessage(message);
                        }else {
                            reConnectHandler.removeMessages(2);
                            Message message = reConnectHandler.obtainMessage(2);
                            reConnectHandler.sendMessageDelayed(message,20000);
                        }
                    }
                });
            }
        }
    };

    private synchronized void reConnect(String mac){
        Log.i(TAG,"reConnect:"+mac);
        curBluetoothMac = mac;
        reConnectBluetoothMac = mac;
        reConnectHandler.post(reConnectRunnale);
    }



    private UUID service = UUID.fromString("f000efe0-0451-4000-0000-00000000b000");
    private UUID notify = UUID.fromString("f000efe3-0451-4000-0000-00000000b000");
    private UUID write = UUID.fromString("f000efe1-0451-4000-0000-00000000b000");

    public static final String ACTION_GATT_CONNECT = "ACTION_GATT_CONNECT";//蓝牙连接
    public static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";//蓝牙断开
    public static final String ACTION_GATT_RECONNECT = "ACTION_GATT_RECONNECT";//蓝牙断开
    public static final String ACTION_SERVICES_DISCOVERED = "ACTION_SERVICES_DISCOVERED";//蓝牙服务发现
    public static final String ACTION_NOTIFICATION_ENABLE ="ACTION_NOTIFICATION_ENABLE";//蓝牙通知开启
    public static final String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";//收到蓝牙数据

    public void init(Context mContext){
        this.mContext = mContext.getApplicationContext();
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }



    /**
     * 蓝牙BLE支持状态
     * @return true 支持
     */
    public boolean bluetoothLeSupport(){
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 蓝牙开启状态
     * @return true 开启
     */
    public boolean isBluetoothEnable(){
        if (null == mBluetoothAdapter || !mBluetoothAdapter.isEnabled()) {
            return false;
        }
        return true;
    }

    /**
     * 开启蓝牙
     */
    public void BluetoothEnable(Context context){
        if (!isBluetoothEnable()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableIntent);
        }
    }

     /*********BLE搜索********/
    /**
     * 周期搜索附近的ble设备
     */
    public synchronized boolean startScan(TimeScanCallback callback){
        callback.setBluetoothLeManager(this).notifyScanStated();//开始倒计时暂停
        boolean suc = mBluetoothAdapter.startLeScan(callback);
        if (suc){
            isScaning.set(true);
        }else {
            callback.removeHandlerMsg();
        }
        return suc;
    }

    /**
     * 暂停搜索
     */
    public synchronized void stopScan(BluetoothAdapter.LeScanCallback callback){
        if (callback instanceof TimeScanCallback) {
            ((TimeScanCallback) callback).removeHandlerMsg();
        }
        mBluetoothAdapter.stopLeScan(callback);
        isScaning.set(false);
    }

    /**
     * 查找指定设备
     */
    public void findDevice(TimeMacScanCallback callback){
        startScan(callback);
    }

    /**
     * 扫描状态
     */
    public boolean isScaning() {
        return isScaning.get();
    }

    /********BLE连接********/

    public void connect(String mac,boolean isReConnect,BleConnectCallback bleCallback){
        connect(getDevice(mac),isReConnect,bleCallback);
    }

    /**
     * 连接BLE设备
     * @param device 扫描返回设备
     * @param isReConnect 意外断开是否重连
     */
    public synchronized void connect(final BluetoothDevice device, final boolean isReConnect, final BleConnectCallback connectCallback){
        if("huawei".equalsIgnoreCase(Watch.brand)){
            this.connectCallback = connectCallback;
//        if (null == device || mBluetoothAdapter == null || bluetoothLeDevices == null) {
//            LogUtil.e(TAG, "connect device or bluetoothAdapter is null" );
//            return;
//        }
            if (null == device || mBluetoothAdapter == null) {
                LogUtil.e(TAG, "connect device or bluetoothAdapter is null" );
                return;
            }
            mBluetoothGattCallback = new mBluetoothGattCallback();

            cleanCurBluetooth();

            curBluetoothMac = device.getAddress();
            isConnected = false;
            isBluetoothReConnect = true;
            curBluetoothGatt = null;
            BluetoothGatt gatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
            //获取到当前GATT
        curBluetoothGatt = gatt;


//        int index = -1;
//        for (int i = 0; i < bluetoothLeDevices.size(); i++) {
//            if (bluetoothLeDevices.get(i).getmBluetoothGatt().getDevice().getAddress().equals(device.getAddress())) {
//                index = i;
//            }
//        }
//        if (index != -1) {
//            BluetoothGatt getmBluetoothGatt = bluetoothLeDevices.get(index).getmBluetoothGatt();
//            closeBluetoothGatt(getmBluetoothGatt);
//            removeBluetoothLe(getmBluetoothGatt);
//            LogUtil.e(TAG, "connect bluetoothGatt remove index is"+index);
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    BluetoothGatt gatt = device.connectGatt(mContext,false,mBluetoothGattCallback);
//                    bluetoothLeDevices.add(new BluetoothLeDevice(gatt,isReConnect));
//                }
//            },1000);
//        }else{
//            BluetoothGatt gatt = device.connectGatt(mContext,false,mBluetoothGattCallback);
//            bluetoothLeDevices.add(new BluetoothLeDevice(gatt,isReConnect));
//        }
            handler.postDelayed(connectTimeoutRunnable,CONNECT_TIME_OUT);
        }else {
            this.connectCallback = connectCallback;
            if (null == device || mBluetoothAdapter == null) {
                Log.e(TAG, "connect device or bluetoothAdapter is null");
            }
            mBluetoothGattCallback = new mBluetoothGattCallback();
            int index = -1;
            for (int i = 0; i < bluetoothLeDevices.size(); i++) {
                if (bluetoothLeDevices.get(i).getmBluetoothGatt().getDevice().getAddress().equals(device.getAddress())) {
                    index = i;
                }
            }
            Log.i(TAG, "connect() bluetoothLeDevices.size()==== " + bluetoothLeDevices.size());
            if (index != -1) {
                BluetoothGatt getmBluetoothGatt = bluetoothLeDevices.get(index).getmBluetoothGatt();
                closeBluetoothGatt(getmBluetoothGatt);
                removeBluetoothLe(getmBluetoothGatt);
                Log.e(TAG, "connect bluetoothGatt remove index is" + index);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothGatt gatt = device.connectGatt(mContext, false, mBluetoothGattCallback);

                        bluetoothLeDevices.add(new BluetoothLeDevice(gatt, isReConnect));
//                    connectCallback.setBluetoothGatt(gatt);
                    }
                }, 1000);
                Log.i(TAG, "connected() bluetoothLeDevices.size()==== " + bluetoothLeDevices.size());
            } else {
                BluetoothGatt gatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
//            connectCallback.setBluetoothGatt(gatt);
                bluetoothLeDevices.add(new BluetoothLeDevice(gatt, isReConnect));
                Log.i(TAG, "connected2() bluetoothLeDevices.size()==== " + bluetoothLeDevices.size());
            }
            handler.postDelayed(connectTimeoutRunnable, 10000);
        }
    }
//    BleConnectCallback connectCallback;
    Runnable connectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (connectCallback != null) {
                connectCallback.onConnectFailure(new TimeOutException());
                connectCallback = null;
            }
        }
    };
    /**
     * 断开Ble设备连接
     * @param gatt 蓝牙中央
     */
    public synchronized void disconnect(BluetoothGatt gatt){
        if (gatt != null) {
            refreshDeviceCache(gatt);
            gatt.close();
        }
    }

    BleCallback disConnectCallback;
    public synchronized void disconnect(String mac,BleCallback disConnectCallback){
        if("huawei".equalsIgnoreCase(Watch.brand)){
            if (curBluetoothGatt == null||mac == null) {
                disConnectCallback.onFailure(new OtherException("disconnect leDevice is null!"));
                return;
            }
//        BluetoothGatt gatt = leDevice.getmBluetoothGatt();
//        if (gatt == null) {
//            disConnectCallback.onFailure(new OtherException("disconnect gatt is null!"));
//            return;
//        }
            this.disConnectCallback = disConnectCallback;
            handler.postDelayed(disConnectTimeoutRunnable,DISCONNECT_TIME_OUT);
            if(mac.equals(curBluetoothGatt.getDevice().getAddress())){
                curBluetoothGatt.disconnect();
            }
        }else {
            BluetoothLeDevice leDevice = getBluetoothLeDevice(mac);
            if (leDevice == null) {
                disConnectCallback.onFailure(new OtherException("disconnect leDevice is null!"));
                return;
            }
            BluetoothGatt gatt = leDevice.getmBluetoothGatt();
            if (gatt == null) {
                disConnectCallback.onFailure(new OtherException("disconnect gatt is null!"));
                return;
            }
            this.disConnectCallback = disConnectCallback;
            handler.postDelayed(disConnectTimeoutRunnable, DISCONNECT_TIME_OUT);
            gatt.disconnect();
        }
    }

    Runnable disConnectTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (disConnectCallback != null) {
                disConnectCallback.onFailure(new TimeOutException());
                disConnectCallback = null;
            }
        }
    };

    /**
     * 重连设备
     * @param gatt 蓝牙中央
     */
    public synchronized void reConnect(BluetoothGatt gatt){
        if (gatt != null) {
            boolean status = gatt.connect();
            Log.d(TAG, "reconnect status:" + status);
            broadcastUpdate(ACTION_GATT_RECONNECT,null,gatt.getDevice().getAddress());
            Log.e(TAG, "reConnect: device is" + gatt.getDevice().getAddress() );
        }
    }

    /**
     * 设备连接状态
     * @param gatt 蓝牙中央
     * @return
     */
    public boolean isConnect(BluetoothGatt gatt){
        BluetoothLeDevice bluetoothLeDevice =  getBluetoothLeDevice(gatt);
        if (bluetoothLeDevice != null) {
            return bluetoothLeDevice.IsConnect();
        }
        throw new IllegalArgumentException("no find gatt");
    }

    public BluetoothDevice getDevice(String mac){
        if("huawei".equalsIgnoreCase(Watch.brand)) {
            if (mBluetoothAdapter != null) {
                return mBluetoothAdapter.getRemoteDevice(mac);
            }
            return null;
        }else{
            return mBluetoothAdapter.getRemoteDevice(mac);
        }
    }

    /********Notification and WriteData********/
    /**
     * 订阅蓝牙消息通知
     * @param gatt 蓝牙中央
     * @param service 蓝牙服务id
     * @param characteristic 蓝牙特征id
     * @return
     */
    public synchronized boolean enableNotification(BluetoothGatt gatt,UUID service,UUID characteristic){
        if (gatt == null) {
            Log.e(TAG, "enableNotification BluetoothGatt is null" );
            return false;
        }
        BluetoothGattService gattServer = gatt.getService(service);
        if (gattServer == null) {
            Log.e(TAG, "enableNotification BluetoothGattService is null");
            return false;
        }
        BluetoothGattCharacteristic gattCharacteristic = gattServer.getCharacteristic(characteristic);
        if (gattCharacteristic == null) {
            Log.e(TAG, "enableNotification BluetoothGattCharacteristic is null");
            return false;
        }
        boolean status = gatt.setCharacteristicNotification(gattCharacteristic,true);
        if (status) {
            List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
            for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(gattDescriptor);
            }
            broadcastUpdate(ACTION_NOTIFICATION_ENABLE,null,gatt.getDevice().getAddress());
            handler.removeCallbacks(connectTimeoutRunnable);
            if (connectCallback != null) {
                connectCallback.onConnectSuccess();
                connectCallback = null;
            }
        }else{
            if (connectCallback != null) {
                connectCallback.onConnectFailure(new GattException(3));
                connectCallback = null;
            }
            LogUtil.e(TAG, "enableNotification status is false");
            return false;
        }
        return true;
    }

    /**
     * 写入特征值数据（默认特征值）
     * @param gatt 蓝牙中央
     * @param value 值
     * @return
     */
    public boolean writeCharacteristic(BluetoothGatt gatt,byte[] value){
        return writeCharacteristic(gatt,service,write,value);
    }
    /**
     * 写入特征值数据（默认特征值）
     * @param gatt 蓝牙中央
     * @param value 值
     * @return
     */
    protected boolean writeCharacteristic(BluetoothGatt gatt, byte[] value, BleCallback bleCallback){
        return writeCharacteristic(gatt,service,write,value,bleCallback);
    }

    /**
     * 写入特征值数据
     * @param gatt 蓝牙中央
     * @param service 蓝牙服务id
     * @param characteristic 蓝牙特征值id
     * @param value 值
     * @return
     */
    BleCallback bleCallback;
    public synchronized boolean writeCharacteristic(BluetoothGatt gatt,UUID service,UUID characteristic,byte[] value, BleCallback bleCallback){
        if (gatt == null) {
            LogUtil.e(TAG, "writeCharacteristic BluetoothGatt is null" );
            bleCallback.onFailure(new OtherException("writeCharacteristic BluetoothGatt is null"));
            return false;
        }
        BluetoothGattService gattServer = gatt.getService(service);
        if (gattServer == null) {
            LogUtil.e(TAG, "writeCharacteristic BluetoothGattService is null");
            bleCallback.onFailure(new OtherException("writeCharacteristic BluetoothGattService is null"));
            return false;
        }
        BluetoothGattCharacteristic gattCharacteristic = gattServer.getCharacteristic(characteristic);
        if (gattCharacteristic == null) {
            LogUtil.e(TAG, "writeCharacteristic BluetoothGattCharacteristic is null");
            bleCallback.onFailure(new OtherException("writeCharacteristic BluetoothGattCharacteristic is null"));
            return false;
        }
        if (value.length == 0 || value.length>20) {
            LogUtil.e(TAG, "writeCharacteristic value count is error ");
            bleCallback.onFailure(new OtherException("writeCharacteristic value count is error "));
            return false;
        }
        gattCharacteristic.setValue(value);
        boolean status = gatt.writeCharacteristic(gattCharacteristic);
        if (!status) {
            LogUtil.e(TAG, "writeCharacteristic status is false");
            bleCallback.onFailure(new BleException(ERROR_CODE_WRITE,"writeCharacteristic is false"));
        }else{
            this.bleCallback = bleCallback;
        }
        return status;
    }

    /**
     * 写入特征值数据
      * @param gatt 蓝牙中央
     * @param service 蓝牙服务id
     * @param characteristic 蓝牙特征值id
     * @param value 值
     * @return
     */
    public synchronized boolean writeCharacteristic(BluetoothGatt gatt,UUID service,UUID characteristic,byte[] value){
        if (gatt == null) {
            Log.e(TAG, "writeCharacteristic BluetoothGatt is null" );
            return false;
        }
        BluetoothGattService gattServer = gatt.getService(service);
        if (gattServer == null) {
            Log.e(TAG, "writeCharacteristic BluetoothGattService is null");
            return false;
        }
        BluetoothGattCharacteristic gattCharacteristic = gattServer.getCharacteristic(characteristic);
        if (gattCharacteristic == null) {
            Log.e(TAG, "writeCharacteristic BluetoothGattCharacteristic is null");
            return false;
        }
        if (value.length == 0 || value.length>20) {
            Log.e(TAG, "writeCharacteristic value count is error ");
            return false;
        }
        gattCharacteristic.setValue(value);
        boolean status = gatt.writeCharacteristic(gattCharacteristic);
        if (!status) {
            Log.e(TAG, "writeCharacteristic status is false");
        }
        return status;
    }


    /********BluetoothGatt********/
    /**
     * 蓝牙BLE回调
     */
    class mBluetoothGattCallback extends BluetoothGattCallback{
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            if("huawei".equalsIgnoreCase(Watch.brand)){
//                if (!BluetoothLeManager.IS_RENECT) return;
                Log.i(TAG,"oldBluetoothGatts.size():"+oldBluetoothGatts.size()+":"+curBluetoothGatt);
//            if (status != BluetoothGatt.GATT_SUCCESS) {
//                String err = "Cannot connect device with error status: " + status;
//                // 当尝试连接失败的时候调用 disconnect 方法是不会引起这个方法回调的，所以这里
//                //   直接回调就可以了。
//                gatt.close();
//                Log.e(TAG, err);
//                return;
//            }
                try{
                    super.onConnectionStateChange(gatt, status, newState);
                    LogUtil.e(TAG, "onConnectionStateChange: device is "+gatt.getDevice().getAddress()+",status is "+status+", new state "+newState );
                    if (status!=BluetoothGatt.GATT_SUCCESS){
                        if(oldBluetoothGatts.size()>0&&oldBluetoothGatts.contains(gatt)){
                            for(BluetoothGatt bluetoothGatt : oldBluetoothGatts){
                                if(bluetoothGatt==gatt){
                                    oldBluetoothGatts.remove(gatt);
                                    gatt.close();
                                }
                            }
                        }else{
                            if (disConnectCallback != null) {
                                handler.removeCallbacks(disConnectTimeoutRunnable);
                                cleanCurBluetooth();
                                gatt.close();
                                curBluetoothGatt = null;
                                disConnectCallback.onSuccess(null);
                                disConnectCallback = null;
                                mBluetoothGattCallback = null;
                                broadcastUpdate(ACTION_GATT_DISCONNECTED,null,gatt.getDevice().getAddress());
                            }else{
                                broadcastUpdate(ACTION_GATT_DISCONNECTED,new byte[]{(byte) status},gatt.getDevice().getAddress());
//                                    if (bluetoothLeDevice.isReConnect()) {
//                                        LogUtil.d(TAG, "isReConnect() called ");
//                                        reConnect(bluetoothLeDevice);
//                                    }
                                gatt.close();
                                curBluetoothGatt = null;
                                reConnect(gatt.getDevice().getAddress());
                            }
                        }
                    }else {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            isConnected = false;
                            gatt.discoverServices();
                            broadcastUpdate(ACTION_GATT_CONNECT, null, gatt.getDevice().getAddress());
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            if(oldBluetoothGatts.size()>0&&oldBluetoothGatts.contains(gatt)){
                                for(BluetoothGatt bluetoothGatt : oldBluetoothGatts){
                                    if(bluetoothGatt==gatt){
                                        oldBluetoothGatts.remove(gatt);
                                        gatt.close();
                                    }
                                }
                            }else{
                                if (disConnectCallback != null) {
                                    handler.removeCallbacks(disConnectTimeoutRunnable);
                                    cleanCurBluetooth();
                                    gatt.close();
                                    curBluetoothGatt = null;
                                    disConnectCallback.onSuccess(null);
                                    disConnectCallback = null;
                                    mBluetoothGattCallback = null;
                                    broadcastUpdate(ACTION_GATT_DISCONNECTED,null,gatt.getDevice().getAddress());
                                }else{
                                    broadcastUpdate(ACTION_GATT_DISCONNECTED,new byte[]{(byte) status},gatt.getDevice().getAddress());
//                                    if (bluetoothLeDevice.isReConnect()) {
//                                        LogUtil.d(TAG, "isReConnect() called ");
//                                        reConnect(bluetoothLeDevice);
//                                    }
                                    gatt.close();
                                    curBluetoothGatt = null;
                                    reConnect(gatt.getDevice().getAddress());
                                }
                            }
                        } else {//连接异常状态处理
                            isConnected = false;
                            reConnect(gatt.getDevice().getAddress());
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else {




            try{
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    BluetoothLeDevice bluetoothLeDevice = getBluetoothLeDevice(gatt);
                    if (bluetoothLeDevice != null){
                        bluetoothLeDevice.setIsConnect(true);
                    }
                    gatt.discoverServices();
                    broadcastUpdate(ACTION_GATT_CONNECT,null,gatt.getDevice().getAddress());
                    Log.e(TAG, "onConnectionStateChange: connected device is "+gatt.getDevice().getAddress()+",status is "+status );
                }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                    BluetoothLeDevice bluetoothLeDevice = getBluetoothLeDevice(gatt);
                    if (bluetoothLeDevice != null){
                        bluetoothLeDevice.setIsConnect(false);
                        if (disConnectCallback != null) {
                            handler.removeCallbacks(disConnectTimeoutRunnable);
                            refreshDeviceCache(gatt);
                            gatt.close();
                            removeBluetoothLe(gatt);
                            disConnectCallback.onSuccess(null);
                            disConnectCallback = null;
                            mBluetoothGattCallback = null;
                            broadcastUpdate(ACTION_GATT_DISCONNECTED,null,gatt.getDevice().getAddress());
                        }else {
                            if (bluetoothLeDevice.isReConnect()) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        reConnect(gatt);
                                    }
                                }, 4000);
                            }
                        }
                    }
                    broadcastUpdate(ACTION_GATT_DISCONNECTED,null,gatt.getDevice().getAddress());
                    Log.e(TAG, "onConnectionStateChange: disconnected device is"+gatt.getDevice().getAddress());
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if("huawei".equalsIgnoreCase(Watch.brand)){
                super.onServicesDiscovered(gatt, status);
                Log.i(TAG,"onServicesDiscovered:"+gatt.getDevice()+";status:"+status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    reConnectHandler.removeMessages(2);
                    isConnected = true;
//                    curBluetoothGatt = gatt;//不能在发现服务里面获取到当前gatt
                    Log.i(TAG,"oldBluetoothGatts:onServicesDiscovered:"+gatt);
                    enableNotification(gatt,service,notify);
                    broadcastUpdate(ACTION_SERVICES_DISCOVERED,null,gatt.getDevice().getAddress());
                }else{
                    if (connectCallback != null) {
                        connectCallback.onConnectFailure(new GattException(status));
                        connectCallback = null;
                    }
                    LogUtil.e(TAG, "onServicesDiscovered: error code is"+status);
                }
            }else {

                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    enableNotification(gatt, service, notify);
                    broadcastUpdate(ACTION_SERVICES_DISCOVERED, null, gatt.getDevice().getAddress());
                }
                Log.e(TAG, "onServicesDiscovered:code is" + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
//            Log.e(TAG, "onCharacteristicWrite: data = "+characteristic.getValue()+" device= " + gatt.getDevice().getAddress() );
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            LogUtil.e(TAG, "onCharacteristicChanged: gatt is " +gatt.getDevice().getAddress() );
            LogUtil.e(TAG,"返回数据; "+ BitUtil.parseByte2HexStr(characteristic.getValue()));
            BleParse.getInstance().setBleParseData(characteristic.getValue(),bleCallback);
            bleCallback = null;
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic.getValue(),gatt.getDevice().getAddress());
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

    };

    /********bluetoothLeManager********/
    /**
     * 删除蓝牙BLE设备
     * @param gatt
     */
    public void removeBluetoothLe(BluetoothGatt gatt){
        if (gatt != null) {
            for (BluetoothLeDevice mBluetoothLeGatt : bluetoothLeDevices) {
                if (mBluetoothLeGatt.getmBluetoothGatt().getDevice().getAddress().equals(gatt.getDevice().getAddress())){
                    bluetoothLeDevices.remove(mBluetoothLeGatt);
                    Log.e(TAG, "removeBluetoothLeGatt: true"+"size is "+bluetoothLeDevices.size());
                    Log.i(TAG, "removeBluetoothLe() bluetoothLeDevices.size()==== "+ bluetoothLeDevices.size());
                    return;
                }
            }
        }
    }

    /**
     * 得到蓝牙BLE设备
     * @param gatt
     * @return
     */
    public BluetoothLeDevice getBluetoothLeDevice(BluetoothGatt gatt){
        if (gatt != null) {
            for (BluetoothLeDevice mBluetoothLeGatt : bluetoothLeDevices) {
                if (mBluetoothLeGatt.getmBluetoothGatt().getDevice().getAddress().equals(gatt.getDevice().getAddress())){
                    return mBluetoothLeGatt;
                }
            }
        }
        return null;
    }

    /**
     * 得到蓝牙BLE设备
     * @param mac
     * @return
     */
    public BluetoothLeDevice getBluetoothLeDevice(String mac){
        if (mac != null) {
            for (BluetoothLeDevice mBluetoothLeGatt : bluetoothLeDevices) {
                if (mBluetoothLeGatt.getmBluetoothGatt().getDevice().getAddress().equals(mac)){
                    return mBluetoothLeGatt;
                }
            }
        }
        return null;
    }

    /********Other********/
    /**
     * @Name:yanwen
     * @Date:18/11/21
     * */
    public void closeCurBluetoothGatt() {
        if (curBluetoothGatt != null) {
            curBluetoothGatt.disconnect();
            curBluetoothGatt.close();
        }
    }

    /**
     * 刷新蓝牙设备缓存
     * @param gatt
     * @return
     */
    public boolean refreshDeviceCache(BluetoothGatt gatt) {

        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(gatt);
                Log.i(TAG, "Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "An exception occured while refreshing device", e);
        }
        return false;
    }

    /**
     * 关闭蓝牙中央
     * @param gatt
     */
    public void closeBluetoothGatt(BluetoothGatt gatt){
        if ("huawei".equalsIgnoreCase(Watch.brand)) {
        } else {
            if (gatt != null) {
                refreshDeviceCache(gatt);
                gatt.close();
            }
        }
    }

    public void closeBluetoothGatt(String mac){
        if("huawei".equalsIgnoreCase(Watch.brand)) {
        }
        else {
            BluetoothLeDevice leDevice = getBluetoothLeDevice(mac);
            Log.i("closeBluetoothGatt", "leDevice:" + leDevice);
            if (leDevice != null) {
                BluetoothGatt bluetoothGatt = leDevice.getmBluetoothGatt();
                Log.i("closeBluetoothGatt", "getmBluetoothGatt():" + bluetoothGatt);
                closeBluetoothGatt(bluetoothGatt);
            }
        }
    }

    /**
     * 关闭所有蓝牙BLE设备
     */
    public void closeALLBluetoothLe(){
        if("huawei".equalsIgnoreCase(Watch.brand)) {
        }
        else {
            Log.i(TAG, "closeALLBluetoothLe() bluetoothLeDevices.size()==== " + bluetoothLeDevices.size());

            for (BluetoothLeDevice mBluetoothLeGatt : bluetoothLeDevices) {
                closeBluetoothGatt(mBluetoothLeGatt.getmBluetoothGatt());
            }
            bluetoothLeDevices.clear();
            bluetoothLeDevices = null;
        }
    }

    public void clearBluetoothLe(){
        if("huawei".equalsIgnoreCase(Watch.brand)) {
        }
        else {
            bluetoothLeDevices.clear();
        }

    }

    /*******广播*******/
    private void broadcastUpdate(String action,byte[] data,String mac) {
        final Intent intent = new Intent(action);
        if (data != null){
            intent.putExtra("BLUETOOTH_DATA",data);
        }
        if (mac != null){
            intent.putExtra("BLUETOOTH_MAC",mac);
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
