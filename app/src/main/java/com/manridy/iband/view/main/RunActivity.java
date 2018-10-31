package com.manridy.iband.view.main;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.progressbar.*;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.ConfigurationParameter;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.map.LocationUtil;
import com.manridy.iband.service.LocationService;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.manridy.iband.R;

public class RunActivity  extends BaseActionActivity {

    @BindView(R.id.iv_to_map)
    ImageView iv_tomap;
    @BindView(R.id.iv_gps_signal)
    ImageView iv_gps_signal;
    @BindView(R.id.gl_run_pause)
    GridLayout gl_run_pause;
    @BindView(R.id.iv_restart_run)
    ImageView iv_restart_run;
    @BindView(R.id.iv_end_run)
    ImageView iv_end_run;
    @BindView(R.id.iv_start_run)
    ImageView iv_start_run;
    @BindView(R.id.iv_pause_run)
    ImageView iv_pause_run;
    @BindView(R.id.iv_ic_his)
    ImageView iv_ic_his;
    @BindView(R.id.tv_exercise_time)
    TextView tv_exercise_time;
    @BindView(R.id.tv_pace)
    TextView tv_pace;
    @BindView(R.id.tv_distance)
    TextView tv_distance;
    @BindView(R.id.line_progress)
    CircleProgressBar timeProgress;
    @BindView(R.id.tv_run_state)
    TextView tv_run_state;

    LocationService.LocationServiceBinder locationServiceBinder;

//    Timeutils timeutils;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_run);
        ButterKnife.bind(this);
//        setSwipeBackEnable(false);
//        timeutils=new Timeutils(tv_exercise_time);
    }

    @Override
    public void scrollToFinishActivity() {
    }

    @Override
    protected void initVariables() {
    }


    @Override
    protected void initListener() {
        iv_tomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInCn = (boolean) SPUtil.get(getBaseContext(), AppGlobal.DATA_IS_IN_CN,true);
                boolean isOnlyUseAmapOnOff =  (boolean) SPUtil.get(getBaseContext(), AppGlobal.DATA_IS_ONLY_USE_AMAP,false);
//                FragmentManager manager = getFragmentManager();
//                FragmentTransaction transaction = manager.beginTransaction();
//                Fragment mFragment;


                if(ConfigurationParameter.Is_appoint_map){
                    if("google".equals(ConfigurationParameter.Appoint_map)){
                        startActivity(GoogleMapRunActivity.class);
                    }else if("GaoDe".equals(ConfigurationParameter.Appoint_map)){
                        startActivity(AMapRunActivity.class);
                    }
                }else{
                    if(isInCn){
//                    startActivity(AMapRunActivity.class);
                        startActivity(AMapRunActivity.class);
                    }else{
                        if(isOnlyUseAmapOnOff) {
                            startActivity(AMapRunActivity.class);
                        }else{
                            startActivity(GoogleMapRunActivity.class);
                        }
                    }
                }


//                if(!isInCn&&!isOnlyUseAmapOnOff){
////                    mFragment = new OutdoorRunGoogleMapFragment();
////                    transaction.replace(R.id.fl_content, mFragment);
////                    transaction.commit();
//                    startActivity(AMapRunActivity.class);
//                }else {
////                    mFragment = new OutdoorRunMapFragment();
////                    transaction.replace(R.id.fl_content, mFragment);
////                    transaction.commit();
//                    startActivity(GoogleMapRunActivity.class);
//                }

            }
        });
        iv_pause_run.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        timeProgress.setVisibility(View.GONE);
                        animator.cancel();
                        break;
                    case MotionEvent.ACTION_DOWN:
                        timeProgress.setVisibility(View.VISIBLE);
                        simulateProgress(2);
                        break;
                }

                return false;
            }
        });
//        iv_pause_run.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                runningState = runningState_pause;
//                iv_pause_run.setVisibility(View.GONE);
//                gl_run_pause.setVisibility(View.VISIBLE);
//                locationServiceBinder.pauseTimer();
////                timeutils.puseTimer();
//                tv_run_state.setText("运动暂停");
//                return true;
//            }
//        });
    }

    private ServiceConnection locationServiceConnection = new  ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationServiceBinder = (LocationService.LocationServiceBinder)service;

            locationServiceBinder.startTimer();
            Message message2 = handler.obtainMessage();
            message2.what = 2;
            handler.sendMessage(message2);

            locationServiceBinder.startCheckSatellites(true);


            locationServiceBinder.startLocation();
            Message message = handler.obtainMessage();
            message.what = 1;
            if(locationServiceBinder!=null){
                message.arg1 = locationServiceBinder.getNowSatellites();
            }
            handler.sendMessage(message);
//            timeutils.startTimer();



            runningState = runningState_running;
            iv_start_run.setVisibility(View.GONE);
            iv_pause_run.setVisibility(View.VISIBLE);
            locationServiceBinder.saveRunLocationRecord(1001);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    DecimalFormat df=new DecimalFormat("0.00");
    SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    int satellites = msg.arg1;
                    if(satellites<5){
                        iv_gps_signal.setImageResource(R.mipmap.sp_ic_signal_0);
                    }else if(satellites>=5&&satellites<10){
                        iv_gps_signal.setImageResource(R.mipmap.sp_ic_signal_1);
                    }else if(satellites>=10&&satellites<20){
                        iv_gps_signal.setImageResource(R.mipmap.sp_ic_signal_2);
                    }else if(satellites>=20&&satellites<30){
                        iv_gps_signal.setImageResource(R.mipmap.sp_ic_signal_3);
                    }else if(satellites>=30){
                        iv_gps_signal.setImageResource(R.mipmap.sp_ic_signal_4);
                    }
                    Message message = handler.obtainMessage();
                    message.what = 1;
//                if(locationServiceBinder!=null){
//                    message.arg1 = locationServiceBinder.getNowSatellites();
//                }
                    message.arg1 = LocationUtil.satellites;
                    handler.removeMessages(1);
                    handler.sendMessageDelayed(message,10*1000);
                    break;
                case 2:
                    tv_exercise_time.setText(locationServiceBinder.getRunningTime());

                    String str_distance_km = df.format((float)locationServiceBinder.getRunDistance()/1000);
                    tv_distance.setText(str_distance_km);

//                    try {
//                        if(locationServiceBinder.getRunDistance()>0) {
//                            Date runningTime = sdf.parse(locationServiceBinder.getRunningTime());
//                            Date zeroTime = sdf.parse("00:00:00");
//                            long runningTime_s = (runningTime.getTime() - zeroTime.getTime()) / (1000);
//                            double d_pace = runningTime_s / (locationServiceBinder.getRunDistance() / 1000);
//
//                            int minutes = ((int) d_pace) / 60;
//                            int remainingSeconds = ((int) d_pace) % 60;
//
//                            String str_pace;
//                            if(remainingSeconds<10){
//                                str_pace = "" + minutes + ":0" + remainingSeconds;
//                            }else{
//                                str_pace = "" + minutes + ":" + remainingSeconds;
//                            }
//
//                            tv_pace.setText(str_pace);
//                        }
//
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }

                    tv_pace.setText(locationServiceBinder.getRunPace());

                    handler.removeMessages(2);
                    Message message2 = handler.obtainMessage();
                    message2.what = 2;
                    handler.sendMessageDelayed(message2,1000);
                    break;
                case 3:
                    tv_exercise_time.setText("00:00:01");
                    break;
                case 4:
                    runningState = runningState_pause;
                    iv_pause_run.setVisibility(View.GONE);
                    gl_run_pause.setVisibility(View.VISIBLE);
                    locationServiceBinder.pauseTimer();
//                timeutils.puseTimer();
                    tv_run_state.setText(R.string.hint_pausesport);
                    break;
            }
        }
    };




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        switch (event.getWhat()){
            case EventGlobal.REFRESH_VIEW_LOCATION_SATELLITES_NUM:

                Message message = handler.obtainMessage();
                message.what = 1;
//                if(locationServiceBinder!=null){
//                    message.arg1 = locationServiceBinder.getNowSatellites();
//                }
                message.arg1 = LocationUtil.satellites;
                handler.sendMessage(message);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonState();
        Message message = handler.obtainMessage();
        message.what = 1;
        message.arg1 = LocationUtil.satellites;
        handler.sendMessage(message);
    }

    static int runningState = 0;
    final int runningState_stop = 0;
    final int runningState_running = 1;
    final int runningState_pause = 2;

    public void updateButtonState(){
        switch (runningState){
            case runningState_stop:
                iv_start_run.setVisibility(View.VISIBLE);
                gl_run_pause.setVisibility(View.GONE);
                break;
            case runningState_running:
                iv_start_run.setVisibility(View.GONE);
                iv_pause_run.setVisibility(View.VISIBLE);
                break;
            case runningState_pause:
                iv_pause_run.setVisibility(View.GONE);
                gl_run_pause.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({R.id.gl_run_pause,R.id.iv_restart_run,R.id.iv_end_run,R.id.iv_start_run,R.id.iv_pause_run,R.id.iv_ic_his})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gl_run_pause:

                break;
            case R.id.iv_restart_run:
                runningState = runningState_running;
                iv_pause_run.setVisibility(View.VISIBLE);
                gl_run_pause.setVisibility(View.GONE);
                locationServiceBinder.restartTimer();
                tv_run_state.setText(R.string.hint_sporting);
//                timeutils.puseTimer();
                break;
            case R.id.iv_end_run:
                final AlertDialog.Builder builder = new AlertDialog.Builder(RunActivity.this);
                builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_stopsport);
                builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runningState = runningState_stop;
                        iv_start_run.setVisibility(View.VISIBLE);
                        gl_run_pause.setVisibility(View.GONE);
                        locationServiceBinder.stopRunLocationRecord();
                        locationServiceBinder.stopTimer();
//                timeutils.stopTimer();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();


                break;
            case R.id.iv_start_run:
//                runningState = runningState_running;
//                iv_start_run.setVisibility(View.GONE);
//                iv_pause_run.setVisibility(View.VISIBLE);
//                locationServiceBinder.saveRunLocationRecord();
//                startActivity(CountDownActivity.class);

                break;
            case R.id.iv_pause_run:

                break;
            case R.id.iv_ic_his:
                startActivity(TrainActivity.class);
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(RunActivity.this,LocationService.class);
        bindService(intent,locationServiceConnection,BIND_AUTO_CREATE);
        Message message3 = handler.obtainMessage();
        message3.what = 3;
        handler.sendMessageDelayed(message3,1000);
        tv_run_state.setText(R.string.hint_sporting);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initBack() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeMessages(1);
        handler.removeMessages(2);
        unbindService(locationServiceConnection);
    }


    ValueAnimator animator = ValueAnimator.ofInt(0, 100);
    private void simulateProgress(int second) {
        animator.cancel();
//        timeProgress.setVisibility(View.VISIBLE);
//        timeProgress.bringToFront();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                if(progress==0){
                    timeProgress.setProgress(0);
                }else if(progress==99){
                    timeProgress.setProgress(100);
                }else{
                    timeProgress.setProgress(progress+2);
                }
                if(progress>99){
                    timeProgress.setVisibility(View.GONE);
                    Message message = handler.obtainMessage(4);
                    handler.sendMessage(message);
                }
            }
        });
//        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(second*1000);
        animator.start();
    }

}
