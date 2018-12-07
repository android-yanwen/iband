package com.manridy.iband.view.main;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

import com.dinuscxj.progressbar.CircleProgressBar;
import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.common.step.StepService;
import com.manridy.iband.common.step.UpdateUiCallBack;
import com.manridy.iband.service.PedometerService;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IndoorRunActivity extends BaseActionActivity {
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
    @BindView(R.id.tv_step)
    TextView tv_step;
    @BindView(R.id.tv_kcal)
    TextView tv_kcal;


    private StepService mService;
    private boolean mIsRunning;
    private SharedPreferences mySharedPreferences;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                tv_step.setText(mySharedPreferences.getString("steps","0"));
            }
        }
    };

    PedometerService.LocationServiceBinder locationServiceBinder;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_indoor_run);
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
    }

    private ServiceConnection locationServiceConnection = new  ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationServiceBinder = (PedometerService.LocationServiceBinder)service;

            locationServiceBinder.startTimer();
            Message message2 = handler.obtainMessage();
            message2.what = 2;
            handler.sendMessage(message2);



            runningState = runningState_running;
            iv_pause_run.setVisibility(View.VISIBLE);
            locationServiceBinder.saveRunRecord();
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
                    break;
                case 2:
                    tv_exercise_time.setText(locationServiceBinder.getRunningTime());

                    String str_distance_km = df.format((float)locationServiceBinder.getRunDistance()/1000);
                    tv_distance.setText(str_distance_km);

                    tv_pace.setText(locationServiceBinder.getRunPace());

                    tv_kcal.setText(""+locationServiceBinder.getCalorie());

                    handler.removeMessages(2);
                    Message message = handler.obtainMessage();
                    message.what = 2;
                    handler.sendMessageDelayed(message,1000);

                    break;
                case 3:
                    tv_exercise_time.setText("00:00:01");
                    break;
                case 4:
                    runningState = runningState_pause;
                    iv_pause_run.setVisibility(View.GONE);
                    gl_run_pause.setVisibility(View.VISIBLE);
                    locationServiceBinder.pauseTimer();
                    mService.pauseValues();
                    tv_run_state.setText(R.string.hint_pausesport);
                    break;
            }
        }
    };




    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        switch (event.getWhat()){
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtonState();
        tv_step.setText(mySharedPreferences.getString("steps", "0"));
        if (this.mIsRunning){
            bindStepService();
        }
    }

    static int runningState = 0;
    final int runningState_stop = 0;
    final int runningState_running = 1;
    final int runningState_pause = 2;

    public void updateButtonState(){
        switch (runningState){
            case runningState_stop:
                gl_run_pause.setVisibility(View.GONE);
                break;
            case runningState_running:
                iv_pause_run.setVisibility(View.VISIBLE);
                break;
            case runningState_pause:
                iv_pause_run.setVisibility(View.GONE);
                gl_run_pause.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({R.id.gl_run_pause,R.id.iv_restart_run,R.id.iv_end_run,R.id.iv_pause_run,R.id.iv_ic_his})
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
                mService.reStartValues();
//                timeutils.puseTimer();
                break;
            case R.id.iv_end_run:
                final AlertDialog.Builder builder = new AlertDialog.Builder(IndoorRunActivity.this);
                builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_stopsport);
                builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runningState = runningState_stop;
                        gl_run_pause.setVisibility(View.GONE);
                        locationServiceBinder.stopRunRecord();
                        locationServiceBinder.stopTimer();
//                timeutils.stopTimer();
                        stopStepService();
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
        Intent intent = new Intent(IndoorRunActivity.this,PedometerService.class);
        bindService(intent,locationServiceConnection,BIND_AUTO_CREATE);
        Message message3 = handler.obtainMessage();
        message3.what = 3;
        handler.sendMessageDelayed(message3,1000);
        tv_run_state.setText(R.string.hint_sporting);
        mySharedPreferences = getSharedPreferences("relevant_data", Activity.MODE_PRIVATE);
        startStepService();

    }

    private UpdateUiCallBack mUiCallback = new UpdateUiCallBack() {
        @Override
        public void updateUi() {
            Message message = mHandler.obtainMessage();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService.StepBinder binder = (StepService.StepBinder) service;
            mService = binder.getService();
            mService.registerCallback(mUiCallback);
            mService.resetValues();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void bindStepService() {
        bindService(new Intent(this, StepService.class), this.mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStepService() {
        unbindService(this.mConnection);
    }

    private void startStepService() {
        this.mIsRunning = true;
        startService(new Intent(this, StepService.class));
    }

    private void stopStepService() {
        this.mIsRunning = false;
        if (this.mService != null)
            mService.resetValues();
            stopService(new Intent(this, StepService.class));
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
        handler.removeMessages(2);
        unbindService(locationServiceConnection);
        unbindStepService();
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
