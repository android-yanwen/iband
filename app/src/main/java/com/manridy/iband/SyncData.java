package com.manridy.iband;

import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.bean.BoModel;
import com.manridy.iband.bean.BpModel;
import com.manridy.iband.bean.DoNotDisturbModel;
import com.manridy.iband.bean.HeartModel;
import com.manridy.iband.bean.MicrocirculationModel;
import com.manridy.iband.bean.SleepModel;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.sdk.Watch;
import com.manridy.sdk.bean.Microcirculation;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.ble.BleParse;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleHistoryListener;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.type.InfoType;

import java.lang.reflect.Type;
import java.util.Date;

/**
 *
 * Created by jarLiao on 17/5/25.
 */

public class SyncData {

    private Watch watch;
    private int syncIndex;//同步当前
    private int progressIndex;//进度当前
    private int progressSum;//进度总数
    private int errorNum;//错误计数器
    private int stepSum,runSum,sleepSum,hrSum,bpSum,boSum,microSum;//数据记录
    private OnSyncAlertListener syncAlertListener;
    private static SyncData instance;
    private Gson mGson;
    private boolean isRun;
    private long updateDate = 0;


    public interface OnSyncAlertListener{
        void onResult(boolean isSuccess);

        void onProgress(int progress);
    }

    public void setSyncAlertListener(OnSyncAlertListener syncAlertListener) {
        this.syncAlertListener = syncAlertListener;
    }

    private SyncData() {
        watch = Watch.getInstance();
        GsonBuilder builder = new GsonBuilder();
        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsString());
            }
        });
        mGson =  builder.create();
//        thread.start();
    }

    public static SyncData getInstance() {
        if (instance == null) {
            instance = new SyncData();
        }
        return instance;
    }

    public synchronized void sync(){
        if (isRun) {
            return;
        }
        isRun = true;
        progressSum = progressIndex = syncIndex = errorNum = 0;
        stepSum =runSum = sleepSum = hrSum = bpSum = boSum = 0;


        BleParse.getInstance().setStepHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                StepModel historyStep = mGson.fromJson(o.toString(), StepModel.class);
                boolean isErrorData = historyStep.getStepType() == 1 && historyStep.getStepNum()>99999;
                if (historyStep.getHisLength() != 0 &&!isErrorData) {
                    historyStep.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historyStep.getHisLength() == (historyStep.getHisCount()+1);
                if (is|| historyStep.getHisLength() == 0) {
                    next();
                }
            }
        });
        BleParse.getInstance().setRunHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                StepModel historyStep = mGson.fromJson(o.toString(), StepModel.class);
                if (historyStep.getHisLength() != 0 ) {
                    historyStep.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historyStep.getHisLength() == (historyStep.getHisCount()+1);
                if (is|| historyStep.getHisLength() == 0) {
                    next();
                }
            }
        });
        BleParse.getInstance().setSleepHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                SleepModel historySleep = mGson.fromJson(o.toString(), SleepModel.class);
                if (historySleep.getSleepLength() != 0) {
                    historySleep.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historySleep.getSleepLength() == (historySleep.getSleepNum()+1);
                if (is || historySleep.getSleepLength() == 0) {
                    next();
                }
            }
        });
        BleParse.getInstance().setHrHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                HeartModel historyHr = mGson.fromJson(o.toString(), HeartModel.class);
                if (historyHr.getHeartLength() != 0 && historyHr.getHeartDate().compareTo(TimeUtil.getNowYMDHMSTime())<=0) {
                    historyHr.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historyHr.getHeartLength() == (historyHr.getHeartNum()+1);
                if (is || historyHr.getHeartLength() == 0) {
                    next();
                }

            }
        });
        BleParse.getInstance().setBpHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                BpModel historyBp = mGson.fromJson(o.toString(), BpModel.class);
                if (historyBp.getBpLength() != 0 && historyBp.getBpDate().compareTo(TimeUtil.getNowYMDHMSTime()) <=0) {
                    historyBp.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historyBp.getBpLength() == (historyBp.getBpNum()+1);
                if (is || historyBp.getBpLength() == 0) {
                    next();
                }

            }
        });
        BleParse.getInstance().setBoHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                BoModel historyBo = mGson.fromJson(o.toString(), BoModel.class);
                if (historyBo.getboLength() != 0 && historyBo.getboDate().compareTo(TimeUtil.getNowYMDHMSTime()) <=0) {
                    historyBo.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = historyBo.getboLength() == (historyBo.getboNum()+1);
                if (is || historyBo.getboLength() == 0) {
                    next();
                    progressSum = -1;
                }
            }
        });

        BleParse.getInstance().setMicroHistoryListener(new BleHistoryListener() {
            @Override
            public void onHistory(Object o) {
                Log.d(TAG, "onHistory: " + o.toString());
                MicrocirculationModel microcirculation = mGson.fromJson(o.toString(), MicrocirculationModel.class);
                if (microcirculation.getMicroNum() != 0 && microcirculation.getDate().compareTo(TimeUtil.getNowYMDHMSTime()) <= 0) {
                    microcirculation.saveToDate();
                    timeOutIndex = 0;
                    progress();
                }
                boolean is = microcirculation.getMicroLength() == (microcirculation.getMicroNum()+1);
                if (is || microcirculation.getMicroLength() == 0) {
                    next();
                }
            }
        });
        send();
    }

    private void progress() {
        progressIndex++;
        int progress = (int) (((double)progressIndex / progressSum)*100);
        syncAlertListener.onProgress(progress);
        if (progress >= 100) {
            if (syncAlertListener != null) {
                syncAlertListener.onResult(true);
                isRun = false;
                LogUtil.d("SyncData", "next() called onResult true");
            }
        }
    }

    BleCallback bleCallback = new BleCallback() {
        @Override
        public void onSuccess(Object o) {
            if (syncAlertListener != null) {
                if (progressSum == 0) {
                    syncAlertListener.onProgress(0);
                }
            }
            parse(o);
        }

        @Override
        public void onFailure(BleException exception) {
            if (errorNum < 5) {
                send();
                errorNum++;
            }else {
                if (syncAlertListener != null) {
                    syncAlertListener.onResult(false);
                    isRun = false;
                    LogUtil.d("SyncData", "next() called onResult false");
                }
            }
            LogUtil.d("SyncData", "onFailure() called with: errorNum = [" + errorNum + "]");
        }
    };

    private synchronized void next(){
        syncIndex++;
        LogUtil.d("SyncData", "next() called syncIndex == "+syncIndex);
//        if (syncIndex < 14) {
        if (syncIndex <= 17) {
            send();
        }else {
            if (syncAlertListener != null) {
                syncAlertListener.onResult(true);
                watch.sendCmd(BleCmd.getSleepStats());
                isRun = false;
                LogUtil.d("SyncData", "next() called onResult true");
            }
        }
    }

    private boolean isH1F1(){
        String deviceName = (String) SPUtil.get(IbandApplication.getIntance().getApplicationContext(), AppGlobal.DATA_DEVICE_BIND_NAME,"");
        if("H1-F1".equals(deviceName)){
            next();
            return true;
        }else{
            return false;
        }
    }

    private boolean isSupportMicroFunction(){
        boolean isShowMicro = (boolean) SPUtil.get(IbandApplication.getIntance().getApplicationContext(),"isShowMicro",false);
        if(isShowMicro){
            return true;
        }else{
            return false;
        }
    }
    private boolean isSupportDoNotDisturbFunction(){
        boolean isSupportDoNotDisturb = (boolean) SPUtil.get(IbandApplication.getIntance().getApplicationContext(),AppGlobal.DATA_DO_NOT_DISTURB_IFG,false);
        if(isSupportDoNotDisturb){
            return true;
        }else{
            return false;
        }
    }



    //计步历史条数>>睡眠历史条数>>心率历史条数>>血压历史条数>>血氧历史条数
    //计步当前>>计步历史>>睡眠历史>>心率历史>>血压历史>>血氧历史
    private synchronized void send(){
        switch (syncIndex) {
            case 0:
                watch.setTimeToNew(bleCallback);
                break;
            case 1:
                if(isH1F1())break;
                watch.sendCmd(BleCmd.getStepSectionNum(),bleCallback);
                break;
            case 2:
                if(isH1F1())break;
                watch.sendCmd(BleCmd.getRunHistoryNum(),bleCallback);
                break;
            case 3:
                if(isH1F1())break;
                watch.getSleepInfo(InfoType.HISTORY_NUM,bleCallback);
                break;
            case 4:
                watch.getHeartRateInfo(InfoType.HISTORY_NUM,bleCallback);
                break;
            case 5:
                if(isH1F1())break;
                watch.getBloodPressureInfo(InfoType.HISTORY_NUM,bleCallback);
                break;
            case 6:
                if(isH1F1())break;
                watch.getBloodOxygenInfo(InfoType.HISTORY_NUM,bleCallback);
                break;
            case 7:
                if(isH1F1())break;
                watch.getSportInfo(InfoType.CURRENT_INFO,bleCallback);
                break;
            case 8:
                if(isH1F1())break;
                if (stepSum != 0) {
                    watch.sendCmd(BleCmd.getStepSectionHistroy(),bleCallback);
                }else {
                    next();
                }
                break;
            case 9:
                if(isH1F1())break;
                if (runSum != 0){
                    watch.sendCmd(BleCmd.getRunHistoryData(),bleCallback);
                }else {
                    next();
                }
                break;
            case 10:
                if(isH1F1())break;
                if (sleepSum != 0) {
                    watch.getSleepInfo(InfoType.HISTORY_INFO,bleCallback);
                }else {
                    next();
                }
                break;
            case 11:
                if (hrSum != 0){
                    watch.getHeartRateInfo(InfoType.HISTORY_INFO,bleCallback);
                }else {
                    next();
                }
                break;
            case 12:
                if (bpSum != 0) {
                    watch.getBloodPressureInfo(InfoType.HISTORY_INFO,bleCallback);
                }else {
                    next();
                }
                break;
            case 13:
                if(isH1F1())break;
                if (boSum != 0) {
                    watch.getBloodOxygenInfo(InfoType.HISTORY_INFO,bleCallback);
                }else {
                    next();
                }
                break;
//            case 14:
//                if(IbandApplication.getIntance().weather!=null){
//                    Watch.getInstance().setWeather(IbandApplication.getIntance().weather,null);
////                    next();
//                }else{
//                    next();
//                }
//                break;
            case 14:  //发送勿打扰模式设置
                if(isH1F1())break;//
                if (isSupportDoNotDisturbFunction()) { //支持勿扰模式功能才发送命令，否则不发送此命令给设备
                    DoNotDisturbModel curDoNotDisturbModel = IbandDB.getInstance().getDoNotDisturbModel();
                    if (curDoNotDisturbModel == null) {
                        curDoNotDisturbModel = new DoNotDisturbModel(0,0x19,0x10,0x7,0x30);
                    }
                    watch.sendCmd(BleCmd.setDoNotDisturbCmd(curDoNotDisturbModel.getDoNotDisturbOnOff(),
                            curDoNotDisturbModel.getStartHour(),
                            curDoNotDisturbModel.getStartMinute(),
                            curDoNotDisturbModel.getEndHour(),
                            curDoNotDisturbModel.getEndMinute())
                    );
                }
                next();
                break;
            case 15:
                if(isH1F1()) break;
                if (isSupportMicroFunction()) {  //判断是否有微循环功能，有则发送这条命令
                    watch.sendCmd(BleCmd.getFatigueCmd(), bleCallback);
                } else {
                    next();
                }
                break;
            case 16:
                if(isH1F1()) break;
                if (isSupportMicroFunction()) {  //判断是否有微循环功能，有则发送这条命令
                    watch.getMicroInfo(InfoType.HISTORY_NUM,bleCallback);
                } else {
                    next();
                }
                break;
            case 17:
                if(isH1F1()) break;
                if (isSupportMicroFunction()) {
                    if (microSum != 0) {
                        watch.getMicroInfo(InfoType.HISTORY_INFO, bleCallback);
                    } else {
                        next();
                    }
                } else {
                    next();
                }
                break;
        }
        timeOutIndex = 0;
    }

    private static final String TAG = "SyncData";
    private synchronized void parse(Object o){
        switch (syncIndex) {
            case 0:
                next();
                break;
            case 1:
                StepModel stepLength = mGson.fromJson(o.toString(), StepModel.class);
                stepSum = stepLength.getHisLength();
                progressSum += stepSum;
                next();
                break;
            case 2:
                StepModel runLength = mGson.fromJson(o.toString(), StepModel.class);
                runSum = runLength.getHisLength();
                progressSum += runSum;
                next();
                break;
            case 3:
                SleepModel sleepLength = mGson.fromJson(o.toString(), SleepModel.class);
                sleepSum = sleepLength.getSleepLength();
                progressSum += sleepSum;
                next();
                break;
            case 4:
                HeartModel hrLength = mGson.fromJson(o.toString(), HeartModel.class);
                hrSum = hrLength.getHeartLength();
                progressSum += hrSum;
                next();
                break;
            case 5:
                BpModel bpLength = mGson.fromJson(o.toString(), BpModel.class);
                bpSum = bpLength.getBpLength();
                progressSum += bpSum;
                next();
                break;
            case 6:
                BoModel boLength = mGson.fromJson(o.toString(), BoModel.class);
                boSum = boLength.getboLength();
                progressSum += boSum;
                next();
                break;
            case 7:
                StepModel curStep = mGson.fromJson(o.toString(), StepModel.class);
                saveCurStep(curStep);
                next();
                break;
            case 15://疲劳状态
//                Log.i(TAG, "parse: " + o.toString());
                next();
                break;
            case 16: //微循环
                Microcirculation microcirculation = mGson.fromJson(o.toString(), Microcirculation.class);
                microSum = microcirculation.getMicroLength();
                progressSum += microSum;
                next();
//                Log.d(TAG, "parse: " + o.toString());
                break;
        }
    }

    public void setRun(boolean run) {
        isRun = run;
    }

    public boolean isRun() {
        return isRun;
    }

    public static void saveCurStep(StepModel curStep) {
        StepModel dbStep = IbandDB.getInstance().getCurStep();
        if (dbStep == null) {
            curStep.saveToDate();
        }else{
            dbStep.setStepNum(curStep.getStepNum());
            dbStep.setStepMileage(curStep.getStepMileage());
            dbStep.setStepCalorie(curStep.getStepCalorie());
            dbStep.setStepDate(curStep.getStepDate());
            dbStep.saveToDate();
        }
    }


    int timeOutIndex = 0;
//    Thread thread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            while (true){
//                try {
//                    thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (isRun) {
//                    if (timeOutIndex == 5) {
//                        next();
//                    }
//                    timeOutIndex++;
//                    Log.d("syncData","timeOutIndex == "+ timeOutIndex);
//                }
//            }
//        }
//    });



}
