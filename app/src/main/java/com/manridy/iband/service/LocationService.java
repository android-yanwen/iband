package com.manridy.iband.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.manridy.applib.callback.LocationCallBack;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.ConfigurationParameter;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.bean.data.RunLocationModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.map.LocationUtil;

import com.manridy.sdk.common.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LocationService extends Service {

    String TAG = LocationService.class.getSimpleName();

    private int nowSatellites = 0;
    private Location nowLocation;
    private Date runRecordDate;
    private StepModel stepModel;
    private String curMinute = "";
    private RunLocationModel runLocationModel;
    private boolean isRunning;

    private boolean isTimeRunning = false;
    private boolean isTimePause = false;
    private long pauseTime = 0;
//    private long runningTime = 0;
    private long curTime = 0;
    private long beginTime = 0;

    private double runDistance = (double)0 ;
    private Location lastLocation;

    private String runPace;

    private boolean isInCN;

    public LocationService() {
    }


    public class LocationServiceBinder extends Binder {

        public void startCheckSatellites(boolean isOn){
            LocationUtil.getInstance(getApplication()).setCallBack(locationCallBack);
            LocationUtil.getInstance(getApplication()).init().checkGpsStatus(isOn);
        }

        public void startLocation(){
            LocationUtil.getInstance(getApplication()).setCallBack(locationCallBack);
            LocationUtil.getInstance(getApplication()).getLocation();
        }

        public void stopCheckSatellites(){
            LocationUtil.getInstance(getApplication()).checkGpsStatus(false);
        }

        public void stopLocation(){
            LocationUtil.getInstance(getApplication()).removeLocationUpdatesListener();
            handler.removeCallbacks(saveLocationRunnable);
        }

        public int getNowSatellites(){
            return nowSatellites;
        }

        public double getRunDistance(){
            return runDistance;
        }

        public Location getNowLocation(){
            return nowLocation;
        }


        public void saveRunLocationRecord(int sportMode){
            isRunning = true;
            runRecordDate = new Date();
            stepModel = new StepModel();

            runDistance = (double)0;
            lastLocation = nowLocation;
            runPace = "00:00";
//            stepModel.setStepNum(step);
//            stepModel.setStepMileage(mi);
//            stepModel.setStepCalorie(ka);
//            stepModel.setHisLength(packageLength);
//            stepModel.setHisCount(packageNum);
            stepModel.setStepDate(runRecordDate);
            stepModel.setStepDay(TimeUtil.getYMD(runRecordDate));
//            stepModel.setStepTime(time);
            stepModel.setStepType(2);//0代表当前 1代表分段计步
            stepModel.setSportMode(sportMode);
            stepModel.setPace("00:00");
            stepModel.setRunTime("00:00:00");
            stepModel.setStepMileage(0);
            boolean inCn = (boolean) SPUtil.get(getBaseContext(), AppGlobal.DATA_IS_IN_CN,true);
            boolean isOnlyUseAmapOnOff =  (boolean) SPUtil.get(getBaseContext(), AppGlobal.DATA_IS_ONLY_USE_AMAP,false);
            if(ConfigurationParameter.Is_appoint_map){
                if("google".equals(ConfigurationParameter.Appoint_map)){
                    stepModel.setMap("google");
                    stepModel.setInCN(inCn);
                    isInCN = inCn;
                }else if("GaoDe".equals(ConfigurationParameter.Appoint_map)){
                    stepModel.setMap("GaoDe");
                    stepModel.setInCN(inCn);
                    isInCN = inCn;
                }
            }else{
                if(inCn){
                    stepModel.setMap("GaoDe");
                    stepModel.setInCN(inCn);
                    isInCN = inCn;
                }else{
                    if(isOnlyUseAmapOnOff) {
                        stepModel.setMap("GaoDe");
                        stepModel.setInCN(inCn);
                        isInCN = inCn;
                    }else{
                        stepModel.setMap("google");
                        stepModel.setInCN(inCn);
                        isInCN = inCn;
                    }
                }
            }
            stepModel.save();
            handler.post(saveLocationRunnable);
        }

        public void stopRunLocationRecord(){
            isRunning = false;
            handler.removeCallbacks(saveLocationRunnable);
        }

        public boolean getIsRunning(){
            return isRunning;
        }

        public Date getRunRecordDate(){
            return runRecordDate;
        }

        public void restartTimer(){
            isTimePause = false;
        }

        public void pauseTimer(){
            isTimePause = true;
        }

        public void stopTimer(){
            isTimeRunning = false;
            isTimePause = false;
            beginTime = curTime = 0;
            pauseTime = 0;
        }

        public void startTimer(){
            isTimeRunning = true;
            isTimePause = false;
            beginTime = System.currentTimeMillis()-1000;
            curTime = System.currentTimeMillis();
            pauseTime = 0;
        }

        public String getRunningTime() {
            String runningTime = "00:00:00";
            long l_runningTime = curTime - beginTime - pauseTime*1000;
            runningTime = stampToDate(l_runningTime);
            return runningTime;
        }


        public String getRunPace(){
            return runPace;
        }

        public boolean getIsInCN(){
            return isInCN;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        LocationServiceBinder locationServiceBinder = new LocationServiceBinder();
        return locationServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    DecimalFormat df=new DecimalFormat("0.00");
    SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
    private int times = 3;
    Runnable saveLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if(nowLocation!=null&&times>=3){
                if(lastLocation==null){
                    lastLocation =  nowLocation;
                }
                if(!isTimePause) {
                    runDistance += LocationUtil.getDistance(lastLocation.getLongitude(), lastLocation.getLatitude(), nowLocation.getLongitude(), nowLocation.getLatitude());
                }
                lastLocation = nowLocation;
            }

            try {
                if(runDistance>10) {
                    String runningTime = "00:00:00";
                    long l_runningTime = curTime - beginTime - pauseTime*1000;
                    runningTime = stampToDate(l_runningTime);

                    Date d_runningTime = sdf.parse(runningTime);

                    Date zeroTime = sdf.parse("00:00:00");
                    long runningTime_s = (d_runningTime.getTime() - zeroTime.getTime()) / (1000);
                    double d_pace = runningTime_s / (runDistance / 1000);

                    int minutes = ((int) d_pace) / 60;
                    int remainingSeconds = ((int) d_pace) % 60;

                    String str_pace;
                    if(remainingSeconds<10){
                        str_pace = "" + minutes + ":0" + remainingSeconds;
                    }else{
                        str_pace = "" + minutes + ":" + remainingSeconds;
                    }

                    runPace = str_pace;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            if(nowLocation == null){
                String runningTime = "00:00:00";
                long l_runningTime = curTime - beginTime - pauseTime*1000;
                runningTime = stampToDate(l_runningTime);
                stepModel.setRunTime(runningTime);
                stepModel.save();
            }

            if(nowLocation!=null&&times>=3&&!isTimePause){
                times = 0;
                String nowYMDHM = TimeUtil.getNowYMDHM();

                if(curMinute!=null&&curMinute.equals(nowYMDHM)&&runLocationModel!=null){
                    runLocationModel.setLocationData(runLocationModel.getLocationData()+";"+nowLocation.getLatitude()+","+nowLocation.getLongitude());
                    runLocationModel.save();
                    stepModel.setUpdateDate(System.currentTimeMillis());
                    stepModel.setStepMileage((int)runDistance);
                    stepModel.setStepTime((int)((curTime - beginTime - pauseTime*1000)/1000/60));
                    stepModel.setPace(runPace);
                    String runningTime = "00:00:00";
                    long l_runningTime = curTime - beginTime - pauseTime*1000;
                    runningTime = stampToDate(l_runningTime);
                    stepModel.setRunTime(runningTime);
                    stepModel.setStepCalorie((int)(60*(runDistance/1000)*1.036));
                    stepModel.save();
                    Log.i(TAG,"1");
                    Log.i(TAG,runLocationModel.getCurMinute());
                    Log.i(TAG,runLocationModel.getLocationData());
                }else{
                    runLocationModel = new RunLocationModel();
                    curMinute = nowYMDHM;
                    runLocationModel.setCurMinute(nowYMDHM);
                    runLocationModel.setLocationDataPackageId(TimeUtil.getYMDHMSTime(runRecordDate));
                    runLocationModel.setLocationData(""+nowLocation.getLatitude()+","+nowLocation.getLongitude());
                    runLocationModel.save();
                    stepModel.setUpdateDate(System.currentTimeMillis());
                    stepModel.setStepMileage((int)runDistance);
                    stepModel.setStepTime((int)((curTime - beginTime - pauseTime*1000)/1000/60));
                    stepModel.setPace(runPace);
                    String runningTime = "00:00:00";
                    long l_runningTime = curTime - beginTime - pauseTime*1000;
                    runningTime = stampToDate(l_runningTime);
                    stepModel.setRunTime(runningTime);
                    stepModel.setStepCalorie((int)(60*(runDistance/1000)*1.036));
                    boolean is_inCN=LocationUtil.getInstance(getApplicationContext()).isInArea(nowLocation.getLatitude(),nowLocation.getLongitude());
                    isInCN = is_inCN;
                    stepModel.setInCN(is_inCN);
                    stepModel.save();
                    Log.i(TAG,"2");
                    Log.i(TAG,runLocationModel.getCurMinute());
                    Log.i(TAG,runLocationModel.getLocationData());

                }
            }




            if(isTimeRunning){
                if(isTimePause){
                    curTime = System.currentTimeMillis();
                    pauseTime++;
                }else{
                    curTime = System.currentTimeMillis();
                }

            }
            times++;
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    LocationCallBack locationCallBack = new LocationCallBack(){
        @Override
        public void onGpsStatus(int satellites) {
            Log.i(TAG,"satellites:"+satellites);
//            Toast.makeText(getApplication(),"satellites:"+satellites,Toast.LENGTH_SHORT).show();
            if(nowSatellites!=satellites){
                nowSatellites=satellites;
                EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_LOCATION_SATELLITES_NUM));
            }
        }

        @Override
        public void onLocation(Location location) {
            nowLocation = location;
            Log.i(TAG,""+location.getLongitude()+":"+location.getLatitude());
//            Toast.makeText(getApplication(),""+location.getLongitude()+":"+location.getLatitude(),Toast.LENGTH_SHORT).show();
        }
    };


    Handler handler=new Handler();


    public static String stampToDate(long s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        TimeZone timeZone = new TimeZone() {
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }

            @Override
            public void setRawOffset(int offsetMillis) {

            }

            @Override
            public int getRawOffset() {
                return 0;
            }

            @Override
            public boolean useDaylightTime() {
                return false;
            }

            @Override
            public boolean inDaylightTime(Date date) {
                return false;
            }
        };
        simpleDateFormat.setTimeZone(timeZone);
//        long lt = new Long(s);
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }



}
