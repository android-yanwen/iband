package com.manridy.iband;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.ToastUtil;
import com.manridy.iband.bean.ClockModel;
import com.manridy.iband.bean.SedentaryModel;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.bean.ViewModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.sdk.Watch;
import com.manridy.sdk.bean.Clock;
import com.manridy.sdk.bean.Sedentary;
import com.manridy.sdk.bean.User;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.type.ClockType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by jarLiao on 17/5/25.
 */

public class SyncAlert {

    private Context mContext;
    private Watch watch;
    private int syncIndex;//同步序号
    private int errorNum;//错误计数
    private OnSyncAlertListener syncAlertListener;
    private static SyncAlert instance;

    public interface OnSyncAlertListener{
        void onResult(boolean isSuccess);
    }

    public void setSyncAlertListener(OnSyncAlertListener syncAlertListener) {
        this.syncAlertListener = syncAlertListener;
    }


    private SyncAlert(Context context) {
        mContext = context.getApplicationContext();
        watch = Watch.getInstance();
    }

    public static SyncAlert getInstance(Context context) {
        if (instance == null) {
            instance = new SyncAlert(context);
        }
        return instance;
    }

    public void sync(){
        syncIndex = errorNum = 0;
        send();
    }
//    同步时间>>获取版本号>>获取电量>>用户信息>>计步目标>>界面选择>>久坐提醒>>防丢提醒>>闹钟提醒>>亮度调节>>单位设置>>时间格式>>翻腕亮屏
    BleCallback bleCallback = new BleCallback() {
        @Override
        public void onSuccess(Object o) {
            parse(o);
            next();
        }

        @Override
        public void onFailure(BleException exception) {
            if (errorNum < 5) {
                send();
                errorNum++;
            }else {
                if (syncAlertListener != null) {
                    syncAlertListener.onResult(false);
                }
            }
            LogUtil.d("syncAlert", "onFailure() called with: errorNum = [" + errorNum + "]");
        }
    };

    public boolean isGetCallbackSetTimingHrTest=false;

    private synchronized void next(){
        if (syncIndex < 13) {
            syncIndex++;
            send();
        }else {
            if (syncAlertListener != null) {
                syncAlertListener.onResult(true);
                LogUtil.d("syncAlert", "next() called onResult true");
                if(isGetCallbackSetTimingHrTest){
                    setTimingHrTest();
                }
            }
        }
        LogUtil.d("syncAlert", "next() called syncIndex == "+syncIndex);
    }

    private void send(){
        switch (syncIndex) {
            case 0:
                watch.setTimeToNew(bleCallback);
                break;
            case 1:
                watch.getFirmwareVersion(bleCallback);
                break;
            case 2:
                watch.getBatteryInfo(bleCallback);
                break;
            case 3:
                UserModel userModel = IbandDB.getInstance().getUser();
                if (userModel == null || userModel.getUserHeight() == null || userModel.getUserHeight() == null) {
                    userModel = new UserModel("170","65");
                }
                watch.setUserInfo(new User(userModel.getUserHeight(),userModel.getUserWeight()),bleCallback);
                break;
            case 4:
                int target = (int) SPUtil.get(mContext,AppGlobal.DATA_SETTING_TARGET_STEP,0);
                watch.setSportTarget(target == 0 ? 8000:target,bleCallback);
                break;
            case 5:
//                List<ViewModel> viewList = IbandDB.getInstance().getView();
//                if (viewList == null || viewList.size() == 0){
//                    viewList = new ArrayList<>();
//                    viewList.add(new ViewModel(0,"待机", R.mipmap.selection_standby,true,false));
//                    viewList.add(new ViewModel(1,"计步", R.mipmap.selection_step,true));
//                    viewList.add(new ViewModel(2,"运动", R.mipmap.selection_sport,true));
//                    viewList.add(new ViewModel(3,"心率", R.mipmap.selection_heartrate,true));
//                    viewList.add(new ViewModel(4,"睡眠", R.mipmap.selection_sleep,true));
//                    viewList.add(new ViewModel(9,"闹钟", R.mipmap.selection_alarmclock,true));
//                    viewList.add(new ViewModel(7,"查找", R.mipmap.selection_find,true));
//                    viewList.add(new ViewModel(6,"信息", R.mipmap.selection_about,true));
//                    viewList.add(new ViewModel(5,"关机", R.mipmap.selection_turnoff,true));
//                }
//                int size = viewList.size();
//                int[] onOffs = new int[size];
//                int[] ids = new int[size];
//                for (int i = 0; i < viewList.size(); i++) {
//                    ids[i] = viewList.get(i).getViewId();
//                    onOffs[i] = viewList.get(i).isSelect()? 1:0;
//                }
//                 watch.sendCmd(BleCmd.getWindowsSet(ids, onOffs),bleCallback);
                next();
                break;
            case 6:
                SedentaryModel sedentaryModel = IbandDB.getInstance().getSedentary();
                if (sedentaryModel == null) {
                    sedentaryModel = new SedentaryModel(false, false, "09:00", "21:00");
                }
                Sedentary sedentary = new Sedentary(sedentaryModel.isSedentaryOnOff(), sedentaryModel.isSedentaryNap()
                        , sedentaryModel.getStartTime(), sedentaryModel.getEndTime(),sedentaryModel.getNapStartTime(),
                        sedentaryModel.getNapEndTime(),sedentaryModel.getSpace());
                watch.setSedentaryAlert(sedentary,bleCallback);
                break;
            case 7:
                boolean lostOn = (boolean) SPUtil.get(mContext,AppGlobal.DATA_ALERT_LOST,false);
                String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME,"");
                String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
                int time = 20;
                String devices[]={"F07","F07A","F10","F10A"};
                for(int i=0;i<devices.length;i++){
                    if(deviceName!=null&&devices[i].equals(deviceName.trim())){
                        time = 120;
                    }
                }
                String deviceIDs[] = {"8077","8078","8079","8080"};
                for(int i = 0;i<deviceIDs.length;i++){
                    if(deviceType!=null&&deviceIDs[i].equals(deviceType.trim())){
                        lostOn = false;
                    }
                }
                    watch.setLostAlert(lostOn,time,bleCallback);
                break;
            case 8:
                int clockNum = 0;
                String deviceType1 = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
                String deviceName1 = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
                String deviceFirm1 = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
                if("K2".equals(deviceName1)&&"8007".equals(deviceType1)&&deviceFirm1.compareTo("1.5.6")>=0){
                    clockNum = 12;
                }else{
                    clockNum = 3;
                }

                List<ClockModel> clockList = IbandDB.getInstance().getClock();
//                if (clockList == null || clockList.size()==0) {
//                    clockList = new ArrayList<>();
//                    clockList.add(new ClockModel("08:00",false));
//                    clockList.add(new ClockModel("08:30",false));
//                    clockList.add(new ClockModel("09:00",false));
//                }

                if(clockNum==3) {
                    if (clockList == null || clockList.size() == 0) {
                        clockList = new ArrayList<>();
                        clockList.add(new ClockModel("08:00", false));
                        clockList.add(new ClockModel("08:30", false));
                        clockList.add(new ClockModel("09:00", false));
                    }
                }else if(clockNum==12){
                    if (clockList == null || clockList.size()==0) {
                        clockList = new ArrayList<>();
                        clockList.add(new ClockModel("08:00",false));
                        clockList.add(new ClockModel("08:30",false));
                        clockList.add(new ClockModel("09:00",false));
                        clockList.add(new ClockModel("09:30",false));
                        clockList.add(new ClockModel("10:00",false));
                        clockList.add(new ClockModel("10:30",false));
                        clockList.add(new ClockModel("11:00",false));
                        clockList.add(new ClockModel("11:30",false));
                        clockList.add(new ClockModel("12:00",false));
                        clockList.add(new ClockModel("12:30",false));
                        clockList.add(new ClockModel("13:00",false));
                        clockList.add(new ClockModel("13:30",false));
                    }else if(clockList.size()==3){
                        clockList.add(new ClockModel("09:30",false));
                        clockList.add(new ClockModel("10:00",false));
                        clockList.add(new ClockModel("10:30",false));
                        clockList.add(new ClockModel("11:00",false));
                        clockList.add(new ClockModel("11:30",false));
                        clockList.add(new ClockModel("12:00",false));
                        clockList.add(new ClockModel("12:30",false));
                        clockList.add(new ClockModel("13:00",false));
                        clockList.add(new ClockModel("13:30",false));
                    }
                }




                List<Clock> clocks = new ArrayList<>();


                for(int i = 0 ; i<clockNum&&i<clockList.size() ;i++){
                    ClockModel model = clockList.get(i);
                    clocks.add(new Clock(model.getTime(),model.isClockOnOFF()));
                }

//                for (ClockModel model : clockList) {
//                    clocks.add(new Clock(model.getTime(),model.isClockOnOFF()));
//                }
//                watch.setClock(ClockType.SET_CLOCK,clocks,bleCallback);



                if(clockNum<=3){
                    watch.setClock(ClockType.SET_CLOCK, clocks, bleCallback);
                }else{
                    watch.set15Clock(ClockType.SET_CLOCK, clocks, bleCallback);
                }



                break;
            case 9:
                int light = (int) SPUtil.get(mContext,AppGlobal.DATA_SETTING_LIGHT,2);
                watch.sendCmd(BleCmd.setLight(light),bleCallback);
                break;
            case 10:
                int unitLength = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT,0);
                watch.sendCmd(BleCmd.setUnit(unitLength),bleCallback);
                break;
            case 11:
                int unitTime = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT_TIME,0);
                watch.sendCmd(BleCmd.setHourSelect(unitTime),bleCallback);
                break;
            case 12:
               boolean onOff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_WRIST, true);
                watch.sendCmd(BleCmd.setWristOnOff(onOff ? 1 : 0),bleCallback);
                Log.i("SetTimingHrTest","1");
                break;
            case 13:
                if(IbandApplication.getIntance().weather!=null){
                    Watch.getInstance().setWeather(IbandApplication.getIntance().weather,null);
                    next();
                }else{
                    next();
                }
                break;
        }
    }

    private void parse(Object o){
        switch (syncIndex) {
            case 1:
                String version = parseJsonString(o,"firmwareVersion");
                String type = parseJsonString(o,"firmwareType");
                SPUtil.put(mContext, AppGlobal.DATA_FIRMWARE_VERSION,version);
                SPUtil.put(mContext, AppGlobal.DATA_FIRMWARE_TYPE,type);
                break;
            case 2:
                parseBattery(o);
                break;
        }
    }

    public void parseBattery(Object o) {
        int battery = parseJsonInt(o,"battery");
        int batteryState = parseJsonInt(o,"batteryState");
        SPUtil.put(mContext, AppGlobal.DATA_BATTERY_NUM,battery);
        SPUtil.put(mContext, AppGlobal.DATA_BATTERY_STATE,batteryState);
    }

    public static String parseJsonString(Object o,String key){
        String str = o.toString();
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(str);
            result = (String) jsonObject.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int parseJsonInt(Object o,String key){
        String str = o.toString();
        int result = 0;
        try {
            JSONObject jsonObject = new JSONObject(str);
            result = (int) jsonObject.get(key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void setTimingHrTest(){
        watch.sendCmd(BleCmd.setTimingHrTest(true, 30),new BleCallback(){
            @Override
            public void onFailure(BleException exception) {
                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,true);
                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,30);
                    Log.i("SetTimingHrTest","SetTimingHrTest:onFailure");
                watch.sendCmd(BleCmd.setTimingHrTest(true, 30),new BleCallback(){
                    @Override
                    public void onFailure(BleException exception) {
                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,true);
                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,30);
                        Log.i("SetTimingHrTest","SetTimingHrTest:onFailure");

                    }
                    @Override
                    public void onSuccess(Object o) {
                        isGetCallbackSetTimingHrTest = false;
//                ToastUtil.showToast("设置心率定时测量失败，请手动设置！");
                        Log.i("SetTimingHrTest","SetTimingHrTest:onSuccess");
                    }
                });
            }
            @Override
            public void onSuccess(Object o) {
                    isGetCallbackSetTimingHrTest = false;
//                ToastUtil.showToast("设置心率定时测量失败，请手动设置！");
                    Log.i("SetTimingHrTest","SetTimingHrTest:onSuccess");
            }
        });
    }
//    boolean isGetCallbackSetTimingHrTest = false;
//
//    Runnable SetTimingHrTest = new Runnable() {
//        @Override
//        public void run() {
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(!isGetCallbackSetTimingHrTest){
//                        showToast( "设置心率定时测量失败，请手动设置！");
//                        Log.i(TAG,"SetTimingHrTest:timeout");
//                    }
//                }
//            },10*1000 );
//            Log.i(TAG,"SetTimingHrTest:sendCmd");
//            ibandApplication.service.watch.sendCmd(BleCmd.setTimingHrTest(true, 30), new BleCallback() {
//                @Override
//                public void onSuccess(Object o) {
//                    isGetCallbackSetTimingHrTest = true;
//                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,true);
//                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,30);
//                    Log.i(TAG,"SetTimingHrTest:onSuccess");
//                }
//
//                @Override
//                public void onFailure(BleException exception) {
//                    isGetCallbackSetTimingHrTest = true;
//                    showToast( "设置心率定时测量失败，请手动设置！");
//                    Log.i(TAG,"SetTimingHrTest:onFailure");
//                }
//            });
//        }
//    };


}
