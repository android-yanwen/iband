package com.manridy.sdk;


import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.util.Log;

import com.manridy.sdk.bean.Clock;
import com.manridy.sdk.bean.Sedentary;
import com.manridy.sdk.bean.User;
import com.manridy.sdk.bean.Weather;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.ble.BleParse;
import com.manridy.sdk.ble.WatchApi;
import com.manridy.sdk.callback.BleActionListener;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.exception.OtherException;
import com.manridy.sdk.exception.TimeOutException;
import com.manridy.sdk.type.AlertType;
import com.manridy.sdk.type.ClockType;
import com.manridy.sdk.type.FindType;
import com.manridy.sdk.type.InfoType;
import com.manridy.sdk.type.PhoneType;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 腕表
 */
public class Watch extends BluetoothLeManager implements WatchApi {
    public static String brand = "";
    private static Watch instance;
    private static String version = "2.2.4";
    private static List<cmdMessage> messageList = new LinkedList<>();//消息集合
    public static long messageTimeOut = 3000;//消息超时时间
    public static int reCount = 0;//重发次数
    private int reSend;//重发计数器
    private String sendMac;

    class cmdMessage{
        byte[] data;
        BleCallback bleCallback;

        public cmdMessage(byte[] data, BleCallback bleCallback) {
            this.data = data;
            this.bleCallback = bleCallback;
        }
    }

    Handler handler = new Handler();
    AtomicBoolean isRun = new AtomicBoolean(false);
    BleCallback cmdCallback;
    int sleepIndex = 2;
    //消息队列
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            for (;;){//循环
                if (!isRun.get()) {//运行状态
                    synchronized (messageList) {
                        Integer integer = messageList.size();
                        if (integer > 0) {//判断消息队列是否存在消息
                            cmdMessage cmdMessage = messageList.get(0);//拿到消息
                            isRun.set(true);//运行状态改变运行中
//                        if (sleepIndex-- < 0) {
//                            sleepIndex = 2;
                            try {//休眠100毫秒
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
//                        }
                            cmdCallback = cmdMessage.bleCallback;//得到回调
                            writeCharacteristic(cmdMessage.data, new BleCallback() {//执行发送

                                @Override
                                public void onSuccess(Object o) {//执行成功
                                    if (cmdCallback != null) {
                                        cmdCallback.onSuccess(o);//返回执行结果
                                        handler.removeCallbacks(timeOutRunnable);//删除超时任务
                                        if (messageList.size() > 0) {
                                            messageList.remove(0);//删除消息队列消息
                                        }
                                        isRun.set(false);//运行状态改变未运行
                                        reSend = 0;//重发计数归零
                                    }
                                }

                                @Override
                                public void onFailure(BleException exception) {//执行失败
                                    if (cmdCallback != null) {
                                        cmdCallback.onFailure(exception);//返回执行结果
                                        handler.removeCallbacks(timeOutRunnable);//删除超时任务
                                        if (messageList.size() > 0) {
                                            messageList.remove(0);//删除消息队列消息
                                        }
                                        isRun.set(false);//运行状态改变未运行
                                        reSend = 0;//重发计数归零
                                    }
                                }
                            });
                            handler.postDelayed(timeOutRunnable, messageTimeOut);//开始超时计时任务
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
        }
    });

    Runnable timeOutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRun.get()) {//判断是否完成
                if (reCount > reSend) {//判断是否重发
                    reSend++;//超时计数加一
                    isRun.set(false);//运行状态改变未运行
                }else {//返回超时异常
                    if (cmdCallback != null) {
                        cmdCallback.onFailure(new TimeOutException());//返回结果
                        if (messageList.size()>0) {
                            messageList.remove(0);//删除消息队列消息
                        }
                        isRun.set(false);//运行状态改变未运行
                    }
                }
            }
        }
    };


    public static synchronized Watch getInstance(){
        if (instance == null) {
            instance = new Watch();
        }
        return instance;
    }

    private Watch() {
        thread.start();
    }

    public void sendCmd(byte[] data){
        sendCmd(data, new BleCallback() {

            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    //发送命令
    public synchronized void sendCmd(byte[] data, BleCallback bleCallback){
        String datas = "";
        for (int i = 0;i<data.length;i++){
            datas+="["+i+"]:"+data[i];
        }
        Log.i("sendCmd","sendCmd:"+datas);
        messageList.add(new cmdMessage(data, bleCallback));
        synchronized (thread) {
            thread.notify();
        }
    }



    //写入特征值
    private synchronized void writeCharacteristic(byte[] data, BleCallback bleCallback){
        if("huawei".equalsIgnoreCase(Watch.brand)){
//        BluetoothLeDevice leDevice;
            BluetoothGatt bluetoothGatt;
            if (data == null) {
                bleCallback.onFailure(new OtherException("sendCmd data is null!"));
                return;
            }
            if (sendMac == null || sendMac.isEmpty()) {//mac地址传空拿最后一个否则拿指定地址
//            if (bluetoothLeDevices != null && bluetoothLeDevices.size()>0) {//判断设备集合数据
//                leDevice = bluetoothLeDevices.get(bluetoothLeDevices.size()-1);//拿取最后一个
//            }else{
//                bleCallback.onFailure(new OtherException("sendCmd leDevice is null!"));
//                return;
//            }
                if (curBluetoothGatt != null) {//判断设备集合数据
                    bluetoothGatt = curBluetoothGatt;
                }else{
                    bleCallback.onFailure(new OtherException("sendCmd leDevice is null!"));
                    return;
                }
            }else {
                bluetoothGatt = curBluetoothGatt;
            }
            if (bluetoothGatt == null) {
                bleCallback.onFailure(new OtherException("sendCmd leDevice is null!"));
                return;
            }
            writeCharacteristic(bluetoothGatt,data,bleCallback);
        }else {
            BluetoothLeDevice leDevice;
            if (data == null) {
                bleCallback.onFailure(new OtherException("sendCmd data is null!"));
                return;
            }
            if (sendMac == null || sendMac.isEmpty()) {//mac地址传空拿最后一个否则拿指定地址
                if (bluetoothLeDevices != null && bluetoothLeDevices.size() > 0) {//判断设备集合数据
                    leDevice = bluetoothLeDevices.get(bluetoothLeDevices.size() - 1);//拿取最后一个
                } else {
                    bleCallback.onFailure(new OtherException("sendCmd leDevice is null!"));
                    return;
                }
            } else {
                leDevice = getBluetoothLeDevice(sendMac);
            }
            if (leDevice == null) {
                bleCallback.onFailure(new OtherException("sendCmd leDevice is null!"));
                return;
            }
            ;
            writeCharacteristic(leDevice.getmBluetoothGatt(), data, bleCallback);
        }
    }

    //=============================蓝牙操作=============================



    //=============================腕表操作=============================

    /**
     * 同步当前时间
     */
    @Override
    public void setTimeToNew(BleCallback bleCallback) {
        sendCmd(BleCmd.setTime(),bleCallback);
    }

    /**
     * 设置闹钟
     * @param clocks 闹钟集合
     */
    @Override
    public void setClock(ClockType clockType, List<Clock> clocks, BleCallback bleCallback) {
        if (clocks == null || clocks.size() > 3) {
            throw new IllegalArgumentException("clocks data is null or size > 3 !");
        }
        int size = clocks.size();
        String[] times = new String[size];
        int[] onOffs =new int[size];
        for (int i = 0; i < clocks.size(); i++) {
            times[i] = clocks.get(i).getTime();
            onOffs[i] = clocks.get(i).getOnOff() ? 1 : 2;
        }
        sendCmd(BleCmd.setAlarm(times,onOffs),bleCallback);
    }

    /**
     * 设置15个闹钟
     * @param clocks 闹钟集合
     */
    @Override
    public void set15Clock(ClockType clockType, List<Clock> clocks,BleCallback bleCallback) {
//        if (clocks == null || clocks.size() > 5) {
//            throw new IllegalArgumentException("clocks data is null or size > 5 !");
//        }
        int size = clocks.size();
        String[] times1 = new String[5];
        int[] onOffs1 =new int[5];
        for (int i = 0; i<5; i++) {
            times1[i] = clocks.get(i).getTime();
            onOffs1[i] = clocks.get(i).getOnOff() ? 1 : 2;
        }
        sendCmd(BleCmd.set15Alarm(times1, onOffs1, (byte) 0x80),bleCallback);

        String[] times2 = new String[5];
        int[] onOffs2 =new int[5];
        for (int i = 0; i<5; i++) {
            times2[i] = clocks.get(i+5).getTime();
            onOffs2[i] = clocks.get(i+5).getOnOff() ? 1 : 2;
        }
        sendCmd(BleCmd.set15Alarm(times2,onOffs2,(byte)0x81),bleCallback);

        String[] times3 = new String[2];
        int[] onOffs3 =new int[2];
        for (int i = 0; i<2; i++) {
            times3[i] = clocks.get(i+10).getTime();
            onOffs3[i] = clocks.get(i+10).getOnOff() ? 1 : 2;
        }
        sendCmd(BleCmd.set15Alarm(times3,onOffs3,(byte)0x82),bleCallback);
    }

    @Override
    public void setHrCorrecting(boolean onOff, BleCallback bleCallback) {
        sendCmd(BleCmd.setHrCorrecting(onOff),bleCallback);
    }

    /**
     * 获取腕表闹钟数据
     */
    @Override
    public void getClockInfo(BleCallback bleCallback) {
        sendCmd(BleCmd.getAlarm(),bleCallback);
    }

    /**
     * 设置腕表震动开关
     * @param onOff
     */
    @Override
    public void setShakeOnOff(boolean onOff,BleCallback bleCallback) {
        sendCmd(BleCmd.setShake(onOff ? 1: 0),bleCallback);
    }

    /**
     * 获取运动信息
     * @param infoType CURRENT_INFO 当前数据，HISTORY_INFO 历史数据，HISTORY_NUM 历史数量
     */
    @Override
    public void getSportInfo(InfoType infoType, BleCallback bleCallback) {
        if (infoType == null) {
            throw new IllegalArgumentException("getSportInfo infoType is null !");
        }

        if (infoType == InfoType.CURRENT_INFO) {
            sendCmd(BleCmd.getSport(3),bleCallback);
        }else if (infoType == InfoType.HISTORY_INFO){
            sendCmd(BleCmd.getSportHistoryData(),bleCallback);
        }else if (infoType == InfoType.HISTORY_NUM){
            sendCmd(BleCmd.getSportHistoryNum(),bleCallback);
        }
    }

    /**
     * 设置运动目标
     * @param target 目标步数
     */
    public void setSportTarget(int target,BleCallback bleCallback){
        sendCmd(BleCmd.setSportTarget(target),bleCallback);
    }

    /**
     * 清除腕表运动数据
     */
    @Override
    public void clearSportInfo(BleCallback bleCallback) {
        sendCmd(BleCmd.clearSport(),bleCallback);
    }

    /**
     * 获取Gps数据
     */
    @Override
    public void getGpsInfo(BleCallback bleCallback) {
        sendCmd(BleCmd.getGps(),bleCallback);
    }

    /**
     * 设置用户信息
     * @param user 用户
     */
    @Override
    public void setUserInfo(User user, BleCallback bleCallback) {
        if (user == null) {
            throw new IllegalArgumentException("setUserInfo user is null !");
        }
        sendCmd(BleCmd.setUserInfo(Integer.valueOf(user.getUserHeight()),Integer.valueOf(user.getUserWeight())),bleCallback);
    }

    /**
     * 消息提醒（无字库简单版）
     * @param alertType 提醒类型
     * @param bleCallback 结果回调
     */
    @Override
    public void setInfoAlert(AlertType alertType, BleCallback bleCallback) {
        switch (alertType) {
            case PHONE_ALERT:
                sendCmd(BleCmd.setInfoAlert(0),bleCallback);
                break;
            case SMS_ALERT:
                sendCmd(BleCmd.setInfoAlert(1),bleCallback);
                break;
            case APP_ALERT:
                sendCmd(BleCmd.setInfoAlert(2),bleCallback);
                break;
        }
    }

    /**
     * 来电提醒 (增加内容版)
     * @param type PHONE_NAME 来电名称，PHONE_NUMBER 来电号码
     * @param phoneStr 来电字符串
     */
    @Override
    public void setPhoneAlert(PhoneType type, String phoneStr,BleCallback bleCallback) {
        if (type == null) {
            throw new IllegalArgumentException("setPhoneAlert phoneType is null !");
        }
        if (phoneStr == null) {
            throw new IllegalArgumentException("setPhoneAlert alertStr is null !");
        }
        if (type == PhoneType.PHONE_NAME) {
            sendCmd(BleCmd.setPhoneAlert(1,phoneStr),bleCallback);
        }else if (type == PhoneType.PHONE_NUMBER){
            sendCmd(BleCmd.setPhoneAlert(2,phoneStr),bleCallback);
        }
    }

    /**
     * 短信提醒名称
     * @param type PHONE_NAME 来电名称，PHONE_NUMBER 来电号码
     * @param smsId 短信id
     * @param phoneStr 名称或号码字符串
     */
    @Override
    public void setSmsAlertName(PhoneType type,int smsId, String phoneStr,BleCallback bleCallback) {
        if (type == null) {
            throw new IllegalArgumentException("setSmsAlert phoneType is null !");
        }
        if (phoneStr == null) {
            throw new IllegalArgumentException("setSmsAlert alertStr is null !");
        }
        if (type == PhoneType.PHONE_NAME) {
            sendCmd(BleCmd.setSmsAlertName(smsId,1,phoneStr),bleCallback);
        }else if (type == PhoneType.PHONE_NUMBER){
            sendCmd(BleCmd.setSmsAlertName(smsId,0,phoneStr),bleCallback);
        }
    }

    /**
     * 短信提醒内容
     * @param smsId 短信id
     * @param smsContent 短信内容
     * @param bleCallback 结果回调
     */
    @Override
    public void setSmsAlertContent(int smsId,byte[] smsContent,BleCallback bleCallback){
        sendCmd(BleCmd.setSmsAlertContext(smsId,smsContent),bleCallback);
    }

    /**
     * 应用提醒
     */
    @Override
    public void setAppAlert(BleCallback bleCallback) {
        sendCmd(BleCmd.setInfoAlert(2),bleCallback);
    }

    /**
     * 防丢提醒
     * @param onOff
     * @param bleCallback
     */
    @Override
    public void setLostAlert(boolean onOff,int time,BleCallback bleCallback) {
        sendCmd(BleCmd.setLostDeviceAlert(onOff? 1 : 0,time),bleCallback);
    }

    /**
     * 设置久坐提醒
     * @param sedentary 久坐提醒
     */
    @Override
    public void setSedentaryAlert(Sedentary sedentary, BleCallback bleCallback) {
        if (sedentary == null) {
            throw new IllegalArgumentException("setSedentatyAlert is null !");
        }
        String[] times = new String[4];
        times[0] = sedentary.getNapStartTime() == null ? "12:00":sedentary.getNapStartTime();
        times[1] = sedentary.getNapEndTime() == null ? "14:00":sedentary.getNapEndTime() ;
        times[2] = sedentary.getStartTime();
        times[3] = sedentary.getEndTime();
        int space = sedentary.getSpace();
        int target = (int) (space / 60.0 * 100.0);
        sendCmd(BleCmd.setSedentaryAlert(sedentary.isSedentaryOnOff() ? 1 : 0,sedentary.isSedentaryNap() ? 1 : 0 ,space ,target ,times ),bleCallback);
    }

    /**
     * 心率测量
     * @param onOff 开始/停止
     */
    @Override
    public void setHeartRateTestOnOff(boolean onOff,BleCallback bleCallback) {
        sendCmd(BleCmd.setHrTest(onOff ? 1 : 0),bleCallback);
    }

    /**
     * 心率自动测量
     * @param onOff
     */
    @Override
    public void setHeartRateAutoTestOnOff(boolean onOff,BleCallback bleCallback) {
        sendCmd(BleCmd.setHrAuto(onOff ? 1 : 0),bleCallback);
    }

    /**
     * 动态心率
     * @param onOff 开关
     */
    @Override
    public void setHeartRateDynamicOnOff(boolean onOff,BleCallback bleCallback) {
        sendCmd(BleCmd.setHrTest2(onOff ? 1 : 0),bleCallback);
    }

    /**
     * 获取心率
     * @param infoType CURRENT_INFO 当前数据，HISTORY_INFO 历史数据，HISTORY_NUM 历史数量
     */
    @Override
    public void getHeartRateInfo(InfoType infoType, BleCallback bleCallback) {
        if (infoType == null) {
            throw new IllegalArgumentException("getHeartRateInfo infoType is null !");
        }
        if (infoType == InfoType.CURRENT_INFO) {
            sendCmd(BleCmd.getHrData(0),bleCallback);
        }else if (infoType == InfoType.HISTORY_INFO){
            sendCmd(BleCmd.getHrData(1),bleCallback);
        }else if (infoType == InfoType.HISTORY_NUM){
            sendCmd(BleCmd.getHrData(2),bleCallback);
        }
    }

    /**
     * 获取睡眠
     * @param infoType CURRENT_INFO 当前数据，HISTORY_INFO 历史数据，HISTORY_NUM 历史数量
     */
    @Override
    public void getSleepInfo(InfoType infoType, BleCallback bleCallback) {
        if (infoType == null) {
            throw new IllegalArgumentException("getSleepInfo infoType is null !");
        }
        if (infoType == InfoType.CURRENT_INFO) {
            sendCmd(BleCmd.getSleep(0),bleCallback);
        }else if (infoType == InfoType.HISTORY_INFO){
            sendCmd(BleCmd.getSleep(1),bleCallback);
        }else if (infoType == InfoType.HISTORY_NUM){
            sendCmd(BleCmd.getSleep(2),bleCallback);
        }
    }

    /**
     * 获取血压
     * @param infoType CURRENT_INFO 当前数据，HISTORY_INFO 历史数据，HISTORY_NUM 历史数量
     */
    @Override
    public void getBloodPressureInfo(InfoType infoType, BleCallback bleCallback) {
        if (infoType == null) {
            throw new IllegalArgumentException("getBloodPressureInfo infoType is null !");
        }
        if (infoType == InfoType.CURRENT_INFO) {
            sendCmd(BleCmd.getBloodpPressure(0),bleCallback);
        }else if (infoType == InfoType.HISTORY_INFO){
            sendCmd(BleCmd.getBloodpPressure(1),bleCallback);
        }else if (infoType == InfoType.HISTORY_NUM){
            sendCmd(BleCmd.getBloodpPressure(2),bleCallback);
        }
    }

    /**
     * 获取血氧
     * @param infoType CURRENT_INFO 当前数据，HISTORY_INFO 历史数据，HISTORY_NUM 历史数量
     */
    @Override
    public void getBloodOxygenInfo(InfoType infoType, BleCallback bleCallback) {
        if (infoType == null) {
            throw new IllegalArgumentException("getBloodOxygenInfo infoType is null !");
        }
        if (infoType == InfoType.CURRENT_INFO) {
            sendCmd(BleCmd.getBloodOxygen(0),bleCallback);
        }else if (infoType == InfoType.HISTORY_INFO){
            sendCmd(BleCmd.getBloodOxygen(1),bleCallback);
        }else if (infoType == InfoType.HISTORY_NUM){
            sendCmd(BleCmd.getBloodOxygen(2),bleCallback);
        }
    }

    /**
     * 获取设备心电波形基线获取
     */
    public void getHrBaseLineInfo(BleCallback bleCallback){
        sendCmd(BleCmd.getHrBaseLine(),bleCallback);
    }

    /**
     * 获取固件版本
     */
    @Override
    public void getFirmwareVersion(BleCallback bleCallback) {
        sendCmd(BleCmd.getFirmware(),bleCallback);
    }

    /**
     * 获取电池电量
     */
    @Override
    public void getBatteryInfo(BleCallback bleCallback) {
        sendCmd(BleCmd.getBattery(),bleCallback);
    }

    /**
     * 查找腕表
     */
    @Override
    public void findDevice(FindType findType, BleCallback bleCallback) {
        switch (findType) {
            case FIND_DEVICE:
                sendCmd(BleCmd.findDevice(3),bleCallback);
                break;
            case CANCLE_FIND_DEVICE:
                sendCmd(BleCmd.findDevice(0),bleCallback);
                break;
            case AFFIRM_FIND:
                sendCmd(BleCmd.affirmFind(),bleCallback);
                break;
        }
    }

    /**
     * 设置设备蓝牙名称
     * @param name 名称 15字节utf-8编码
     * @param bleCallback
     */
    @Override
    public void setDeviceName(String name, BleCallback bleCallback) {
        if (name == null ||name.isEmpty()) {
            throw new IllegalArgumentException("setDeviceName name is null or length = 0!");
        }
        sendCmd(BleCmd.setDeviceName(name),bleCallback);
    }
    //=============================监听接口=============================

    /**
     * 运动上报数据监听
     * @param sportNotifyListener
     */
    public void setSportNotifyListener(BleNotifyListener sportNotifyListener) {
        BleParse.getInstance().setSportNotifyListener(sportNotifyListener);
    }

    public void setStepNotifyListener(BleNotifyListener setStepNotifyListener) {
        BleParse.getInstance().setStepNotifyListener(setStepNotifyListener);
    }

    public void setRunNotifyListener(BleNotifyListener runNotifyListener) {
        BleParse.getInstance().setRunNotifyListener(runNotifyListener);
    }

    /**
     * 睡眠上报数据监听
     * @param sleepNotifyListener
     */
    public void setSleepNotifyListener(BleNotifyListener sleepNotifyListener) {
        BleParse.getInstance().setSleepNotifyListener(sleepNotifyListener);
    }

    /**
     * 心率上报数据监听
     * @param hrNotifyListener
     */
    public void setHrNotifyListener(BleNotifyListener hrNotifyListener) {
        BleParse.getInstance().setHrNotifyListener(hrNotifyListener);
    }

    /**
     * 血压上报数据监听
     * @param bpNotifyListener
     */
    public void setBpNotifyListener(BleNotifyListener bpNotifyListener) {
        BleParse.getInstance().setBpNotifyListener(bpNotifyListener);
    }

    /**
     * 血氧上报数据监听
     * @param boNotifyListener
     */
    public void setBoNotifyListener(BleNotifyListener boNotifyListener) {
        BleParse.getInstance().setBoNotifyListener(boNotifyListener);
    }

    /**
     * 行为上报监听
     * @param actionListener
     */
    public void setActionListener(BleActionListener actionListener) {
        BleParse.getInstance().setActionListener(actionListener);
    }

    public void setEcgNotifyListener(BleNotifyListener boNotifyListener) {
        BleParse.getInstance().setEcgNotifyListener(boNotifyListener);
    }

    public void setHrBaseLineListener(BleNotifyListener hrBaseLinebleNotifyListener) {
        BleParse.getInstance().setHrBaseLineListener(hrBaseLinebleNotifyListener);
    }

    //=============================SDK接口=============================

    /**
     * 返回当前版本信息
     * @return
     */
    public static String getVersion() {
        return version;
    }


    public String getSendMac() {
        return sendMac;
    }

    public void setSendMac(String sendMac) {
        this.sendMac = sendMac;
    }

    @Override
    public void setWeather(Weather weather, BleCallback bleCallback) {
        if(weather!=null){
            int T0 = weather.getWeatherRegime();
            int H0 = weather.getMaxTemperature();
            if(H0<0){
                H0 = -H0+128;
            }
            int L0 = weather.getMinTemperature();
            if(L0<0){
                L0 = -L0+128;
            }
            int C0 = weather.getNowTemperature();
            if(C0<0){
                C0 = -C0+128;
            }
            if(weather.getForecastWeathers().size()>=3){
                int T01 = weather.getForecastWeathers().get(0).getWeatherRegime();
                int H01 = weather.getForecastWeathers().get(0).getMaxTemperature();
                if(H01<0){
                    H01 = -H01+128;
                }
                int L01 = weather.getForecastWeathers().get(0).getMinTemperature();
                if(L01<0){

                    L01 = -L01+128;
                }

                int T02 = weather.getForecastWeathers().get(1).getWeatherRegime();
                int H02 = weather.getForecastWeathers().get(1).getMaxTemperature();
                if(H02<0){
                    H02 = -H02+128;
                }
                int L02 = weather.getForecastWeathers().get(1).getMinTemperature();
                if(L02<0){

                    L02 = -L02+128;
                }

                int T03 = weather.getForecastWeathers().get(2).getWeatherRegime();
                int H03 = weather.getForecastWeathers().get(2).getMaxTemperature();
                if(H03<0){
                    H03 = -H03+128;
                }
                int L03 = weather.getForecastWeathers().get(2).getMinTemperature();
                if(L03<0){

                    L03 = -L03+128;
                }
                byte[] data = new byte[]{(byte) T0,(byte) H0,(byte) L0,(byte) C0,(byte) T01,(byte) H01,(byte) L01,(byte)0xff,(byte) T02,(byte) H02,(byte) L02,(byte)0xff,(byte) T03,(byte) H03,(byte) L03,(byte)0xff};
                Log.i("setWeather","T0:"+T0+",H0:"+H0+",L0:"+L0+",C0:"+C0+",T01:"+T01+",H01:"+H01+",L01:"+L01+",T02:"+T02+",H02:"+H02+",L02:"+L02+",T03:"+T03+",H03:"+H03+",L03:"+L03);
                sendCmd(BleCmd.setWeather(data),bleCallback);
            }else {
                byte[] data = new byte[]{(byte) T0,(byte) H0,(byte) L0,(byte) C0};
                Log.i("setWeather","T0:"+T0+",H0:"+H0+",L0:"+L0+",C0:"+C0);
                sendCmd(BleCmd.setWeather(data),bleCallback);
            }
//                byte[] data = new byte[]{(byte) T0,(byte) H0,(byte) L0,(byte) C0};
//                Log.i("setWeather","T0:"+T0+",H0:"+H0+",L0:"+L0+",C0:"+C0);
//                sendCmd(BleCmd.setWeather(data),bleCallback);

        }

    }

}