package com.manridy.iband.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.manridy.iband.bean.StepModel;
import com.manridy.sdk.common.TimeUtil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PedometerService extends Service {

    String TAG = PedometerService.class.getSimpleName();

    private Date runRecordDate;
    private StepModel stepModel;
    private String curMinute = "";
    private boolean isRunning;

    private boolean isTimeRunning = false;
    private boolean isTimePause = false;
    private long pauseTime = 0;
//    private long runningTime = 0;
    private long curTime = 0;
    private long beginTime = 0;

    private double runDistance = (double)0 ;

    private int calorie = 0;

    private String runPace;

    private SharedPreferences mySharedPreferences;

    public PedometerService() {
    }


    public class LocationServiceBinder extends Binder {
        public double getRunDistance(){
            return runDistance;
        }

        public void saveRunRecord(){
            isRunning = true;
            runRecordDate = new Date();
            stepModel = new StepModel();

            runDistance = (double)0;

            runPace = "00:00";

            stepModel.setStepDate(runRecordDate);
            stepModel.setStepDay(TimeUtil.getYMD(runRecordDate));

            stepModel.setStepType(2);//0代表当前 1代表分段计步
            stepModel.setSportMode(1002);
            stepModel.setPace("00:00");
            stepModel.setRunTime("00:00:00");
            stepModel.setStepMileage(0);

            stepModel.save();
            handler.post(saveRunnable);
        }

        public void stopRunRecord(){
            isRunning = false;
            handler.removeCallbacks(saveRunnable);
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

        public int getCalorie(){
            return calorie;
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
        mySharedPreferences = getSharedPreferences("relevant_data", Activity.MODE_PRIVATE);
    }

    DecimalFormat df=new DecimalFormat("0.00");
    SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");

    Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
                if(mySharedPreferences==null){
                    mySharedPreferences =  getSharedPreferences("relevant_data", Activity.MODE_PRIVATE);;
                }
                String step = mySharedPreferences.getString("steps","0");
                    if(step!=null) {
                        stepModel.setStepNum(Integer.parseInt(step));
                        runDistance = Integer.parseInt(step) * 0.75;
                    }

            try {
                if(runDistance>0) {
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


//            if(nowLocation == null){
//                String runningTime = "00:00:00";
//                long l_runningTime = curTime - beginTime - pauseTime*1000;
//                runningTime = stampToDate(l_runningTime);
//                stepModel.setRunTime(runningTime);
//                stepModel.save();
//            }

//            if(nowLocation!=null&&times>=3&&!isTimePause){
            if(!isTimePause){
                    stepModel.setUpdateDate(System.currentTimeMillis());
                    stepModel.setStepMileage((int)runDistance);
                    stepModel.setStepTime((int)((curTime - beginTime - pauseTime*1000)/1000/60));
                    stepModel.setPace(runPace);
                    String runningTime = "00:00:00";
                    long l_runningTime = curTime - beginTime - pauseTime*1000;
                    runningTime = stampToDate(l_runningTime);
                    stepModel.setRunTime(runningTime);
                    calorie = (int)(60*(runDistance/1000)*1.036);
                    stepModel.setStepCalorie(calorie);
                    stepModel.save();

            }


            if(isTimeRunning){
                if(isTimePause){
                    curTime = System.currentTimeMillis();
                    pauseTime++;
                }else{
                    curTime = System.currentTimeMillis();
                }

            }
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
    }



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
