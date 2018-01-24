package com.manridy.iband.view;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.applib.utils.FileUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.service.DfuService;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.exception.BleException;

import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceController;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;


/**
 * 关于
 * Created by jarLiao on 16/11/21.
 */

public class OtaActivity extends BaseActionActivity {
    @BindView(R.id.cv_ota)
    CircularView cvOta;
    @BindView(R.id.iv_ota)
    ImageView ivOta;
    @BindView(R.id.tv_ota_result)
    TextView tvOtaResult;
    @BindView(R.id.tv_ota_ok)
    TextView tvOtaOk;
    @BindView(R.id.tv_ota_progress)
    TextView tvOtaProgress;
    private DfuServiceController controller;
    private boolean isInfiniti;

//    Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Log.d(TAG, "handleMessage() called with: msg = [" + msg.what + "]");
//            if (msg.what <=100 && msg.what >0) {
//                tvOtaProgress.setText("已完成" + msg.what + "%");
//                cvOta.setProgress((float)msg.what)
//                        .invaliDate();
//            }else {
//                tvOtaProgress.setText("");
//                cvOta.setProgress(0)
//                        .invaliDate();
//            }
//            if (msg.what < 25){
//                handler.sendEmptyMessageDelayed(progress++,600);
//            }else if (msg.what == 75 ){
//                ivOta.setVisibility(View.GONE);
//                tvOtaResult.setVisibility(View.VISIBLE);
//                tvOtaResult.setText("重连设备中");
//                handler.sendEmptyMessageDelayed(progress++,1000);
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        connect();
//                    }
//                },2500);
//            }else if (msg.what >75 &&msg.what <100){
//                handler.sendEmptyMessageDelayed(progress++,1000);
//            }
//        }
//    };

    private void connect() {
        ibandApplication.service.initConnect(false,new BleConnectCallback() {
            @Override
            public void onConnectSuccess() {
                SyncAlert.getInstance(mContext).sync();
            }

            @Override
            public void onConnectFailure(BleException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.error_connect_fail));
                    }
                });
            }
        });
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_device_ota);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
    }


    @Override
    protected void initVariables() {
        isInfiniti = getStartType() == 1;
        int battery = (int) SPUtil.get(mContext,AppGlobal.DATA_BATTERY_NUM,0);
        int connect = (int) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_CONNECT_STATE,AppGlobal.DEVICE_STATE_UNCONNECT);

        if (battery < 30){//检测设备电量
            showToast(getString(R.string.toast_ota_battery_low));
            finish();
            return;
        }
        if (connect != AppGlobal.DEVICE_STATE_CONNECTED) {//检测设备连接状态
            showToast(getString(R.string.hintUnConnect));
            finish();
            return;
        }
        start();
        SPUtil.put(mContext, AppGlobal.STATE_APP_OTA_RUN, true);
    }

    private void sendOtaData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpService.getInstance().sendOtaData(getApplicationContext(), new OnResultCallBack() {
                    @Override
                    public void onResult(boolean result, Object o) {
                        Log.d(TAG, "onResult() called with: result = [" + result + "], o = [" + o + "]");
                    }
                });
            }
        }).start();
    }

    @Override
    protected void initListener() {
        tvOtaOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvOtaResult.getText().toString().equals(getString(R.string.error_ota_fail))) {
                    finish();
                }else {
                    int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                    if (state == 1) {
                        showProgress(getString(R.string.hint_sync_data));
                        SyncAlert.getInstance(mContext).sync();
                    }else {
                        showProgress(getString(R.string.hint_connecting));
                        connect();
                    }
                }
            }
        });

        tvOtaProgress.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isInfiniti = true;
                showToast("开启无限OTA模式");
                return true;
            }
        });

        SyncAlert.getInstance(mContext).setSyncAlertListener(new SyncAlert.OnSyncAlertListener() {
            @Override
            public void onResult(final boolean isSuccess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        showToast(isSuccess?getString(R.string.hint_sync_success):getString(R.string.hint_sync_fail));
                        finish();
                    }
                });
            }
        });
    }


    private void start() {
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        String name = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_BIND_NAME,"");
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac)
                .setDeviceName(name)
                .setDisableNotification(true)
                .setKeepBond(true);
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        if (FileUtil.getSdCardPath() == null) {
            return;
        }
        starter.setZip(null, FileUtil.getSdCardPath()+"/ota.zip");
        controller = starter.start(this,DfuService.class);
        sendOtaData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            Log.d(TAG, "onDeviceConnecting() called with: deviceAddress = [" + deviceAddress + "]");
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            tvOtaProgress.setVisibility(View.VISIBLE);
            Log.d(TAG, "onDfuProcessStarting() called with: deviceAddress = [" + deviceAddress + "]");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            super.onProgressChanged(deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal);
            cvOta.setProgress((float) percent)
                    .invaliDate();
            tvOtaProgress.setText(getString(R.string.hint_ota_completed) + percent + "%");
            if (percent == 100) {
                cvOta.setProgress(0);
                ivOta.setVisibility(View.GONE);
                tvOtaProgress.setVisibility(View.GONE);
                tvOtaResult.setVisibility(View.VISIBLE);
                tvOtaResult.setText(R.string.hint_ota_success);
                tvOtaOk.setVisibility(isInfiniti ?View.GONE : View.VISIBLE);
                resetTime = isInfiniti ? 10:5;
                progressHandler.sendEmptyMessage(0);
            }
            Log.d(TAG, "onProgressChanged() called with: deviceAddress = [" + deviceAddress + "], percent = [" + percent + "], speed = [" + speed + "], avgSpeed = [" + avgSpeed + "], currentPart = [" + currentPart + "], partsTotal = [" + partsTotal + "]");
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            super.onError(deviceAddress, error, errorType, message);
            cvOta.setProgress(0f).invaliDate();
            ivOta.setVisibility(View.GONE);
            tvOtaProgress.setVisibility(View.GONE);
            tvOtaOk.setVisibility(View.VISIBLE);
            tvOtaResult.setVisibility(View.VISIBLE);
            tvOtaResult.setTextColor(Color.parseColor("#e64a19"));
            tvOtaResult.setText(R.string.error_ota_fail);
            Log.d(TAG, "onError() called with: deviceAddress = [" + deviceAddress + "], error = [" + error + "], errorType = [" + errorType + "], message = [" + message + "]");
        }
    };

    @Override
    public void showProgress(String msg) {
        if (!this.isFinishing()) {
            ProgressDialog dialog = new ProgressDialog(mContext);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage(msg);
            dialog.show();
        }
    }


    ProgressDialog progressDialog;
    int resetTime = 5;
    Handler progressHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                progressDialog = new ProgressDialog(mContext);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("设备正在重启中,请稍后("+resetTime+")");
                progressDialog.show();
            }else if (msg.what<=resetTime){
                if (progressDialog != null) {
                    progressDialog.setMessage("设备正在重启中,请稍后("+(resetTime-msg.what)+")");
                }
            }else {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                if (isInfiniti) {
                    int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                    if (state == AppGlobal.DEVICE_STATE_CONNECTED) {
                        start();
                    }else {
                        ibandApplication.service.initConnect(false, connectCallback);
                    }
                }
            }
            if (msg.what<=resetTime) {
                progressHandler.sendEmptyMessageDelayed(msg.what+1,1000);
            }

        }
    };

    BleConnectCallback connectCallback = new BleConnectCallback() {
        @Override
        public void onConnectSuccess() {
            start();
        }

        @Override
        public void onConnectFailure(BleException exception) {
            ibandApplication.service.initConnect(false, connectCallback);
        }
    };

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        if (controller != null) {
//            controller.pause();
//        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        SPUtil.put(ibandApplication, AppGlobal.STATE_APP_OTA_RUN,false);
        super.onDestroy();
    }
}
