package com.manridy.iband.view.model.sport;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.iband.ConfigurationParameter;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.map.LocationUtil;
import com.manridy.iband.service.LocationService;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.main.AMapPlaybackActivity;
import com.manridy.iband.view.main.CountDownActivity;
import com.manridy.iband.view.main.GoogleMapPlaybackActivity;
import com.manridy.iband.view.main.TrainActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.BIND_AUTO_CREATE;
import static org.litepal.LitePalApplication.getContext;

public class OutdoorRunMainFragment extends BaseEventFragment {
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


    LocationService.LocationServiceBinder locationServiceBinder;
    StepModel stepModel;


    @Override
    public void onResume() {
        super.onResume();
//        updateButtonState();
        stepModel = IbandDB.getInstance().getLastRunData();
        if(stepModel!=null) {
            DecimalFormat df = new DecimalFormat("0.00");
            String str_distance_km = df.format((float) stepModel.getStepMileage() / 1000);
            tv_distance.setText(str_distance_km);

            tv_pace.setText(stepModel.getPace());

            tv_exercise_time.setText(stepModel.getRunTime());
        }
        Message message = handler.obtainMessage();
        message.what = 1;
        if(locationServiceBinder!=null){
            message.arg1 = LocationUtil.satellites;
        }
        handler.sendMessage(message);
    }

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sport_outdoor_run_main, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    protected void initVariables() {
//        setStatusBarColor(Color.parseColor("#151515"));

    }

    private ServiceConnection locationServiceConnection = new  ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationServiceBinder = (LocationService.LocationServiceBinder)service;
            locationServiceBinder.startCheckSatellites(true);
            locationServiceBinder.startLocation();
            Message message = handler.obtainMessage();
            message.what = 1;
            if(locationServiceBinder!=null){
                message.arg1 = locationServiceBinder.getNowSatellites();
            }
            handler.sendMessage(message);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
            }
        }
    };

    @Override
    protected void initListener() {
        iv_tomap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                boolean isInCn = (boolean)SPUtil.get(getContext(), AppGlobal.DATA_IS_IN_CN,true);
//                boolean isOnlyUseAmapOnOff =  (boolean) SPUtil.get(getContext(), AppGlobal.DATA_IS_ONLY_USE_AMAP,false);
//                FragmentManager manager = getFragmentManager();
//                FragmentTransaction transaction = manager.beginTransaction();
//                Fragment mFragment;
//                if(!isInCn&&!isOnlyUseAmapOnOff){
//                    mFragment = new OutdoorRunGoogleMapFragment();
//                    transaction.replace(R.id.fl_content, mFragment);
//                    transaction.commit();
//                }else {
////                    mFragment = new OutdoorRunMapFragment();
////                    transaction.replace(R.id.fl_content, mFragment);
////                    transaction.commit();
//                    startActivity(AMapRunActivity.class);
//                }
                if(stepModel!=null){
                    if("google".equals(stepModel.getMap())){
                        Intent intent = new Intent(getActivity(), GoogleMapPlaybackActivity.class);
                        intent.putExtra("StepDate", stepModel.getStepDate());
                        startActivity(intent);
                    }else if("GaoDe".equals(stepModel.getMap())){
                            Intent intent = new Intent(getActivity(), AMapPlaybackActivity.class);
                            intent.putExtra("StepDate", stepModel.getStepDate());
                            startActivity(intent);
                    }else{
                        Intent intent = new Intent(getActivity(), AMapPlaybackActivity.class);
                        intent.putExtra("StepDate", stepModel.getStepDate());
                        startActivity(intent);
                    }

                }else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_no_data);
                    builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
            }
        });
        iv_pause_run.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                runningState = runningState_pause;
//                iv_pause_run.setVisibility(View.GONE);
//                gl_run_pause.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        switch (event.getWhat()){
            case EventGlobal.REFRESH_VIEW_LOCATION_SATELLITES_NUM:
                Message message = handler.obtainMessage();
                message.what = 1;
                if(locationServiceBinder!=null){
                    message.arg1 = LocationUtil.satellites;
                }
                handler.sendMessage(message);
                break;
        }
    }




    @Override
    public void onPause() {
        super.onPause();
        handler.removeMessages(1);
    }


//    static int runningState = 0;
//    final int runningState_stop = 0;
//    final int runningState_running = 1;
//    final int runningState_pause = 2;
//
//    public void updateButtonState(){
//        switch (runningState){
//            case runningState_stop:
//                iv_start_run.setVisibility(View.VISIBLE);
//                gl_run_pause.setVisibility(View.GONE);
//                break;
//            case runningState_running:
//                iv_start_run.setVisibility(View.GONE);
//                iv_pause_run.setVisibility(View.VISIBLE);
//                break;
//            case runningState_pause:
//                iv_pause_run.setVisibility(View.GONE);
//                gl_run_pause.setVisibility(View.VISIBLE);
//                break;
//        }
//    }

    @OnClick({R.id.gl_run_pause,R.id.iv_restart_run,R.id.iv_end_run,R.id.iv_start_run,R.id.iv_pause_run,R.id.iv_ic_his})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.gl_run_pause:

                break;
            case R.id.iv_restart_run:
//                runningState = runningState_running;
//                iv_pause_run.setVisibility(View.VISIBLE);
//                gl_run_pause.setVisibility(View.GONE);
                break;
            case R.id.iv_end_run:
//                runningState = runningState_stop;
//                iv_start_run.setVisibility(View.VISIBLE);
//                gl_run_pause.setVisibility(View.GONE);
//                locationServiceBinder.stopRunLocationRecord();
                break;
            case R.id.iv_start_run:
//                runningState = runningState_running;
//                iv_start_run.setVisibility(View.GONE);
//                iv_pause_run.setVisibility(View.VISIBLE);
//                locationServiceBinder.saveRunLocationRecord();
                if(ConfigurationParameter.Is_startOutdoorsSport_accordingGpsSignal){
                    if(LocationUtil.satellites>10){
                        Intent intent = new Intent(getActivity(),CountDownActivity.class);
                        intent.putExtra("objectActivity",1);
                        startActivity(intent);
                    }else{
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_no_gps);
                        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    }
                }else{
                    Intent intent = new Intent(getActivity(),CountDownActivity.class);
                    intent.putExtra("objectActivity",1);
                    startActivity(intent);
                }

                break;
            case R.id.iv_pause_run:

                break;
            case R.id.iv_ic_his:
                startActivity(TrainActivity.class);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(getAppliaction(),LocationService.class);
        getActivity().bindService(intent,locationServiceConnection,BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(locationServiceConnection);
    }


}
