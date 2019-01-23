package com.manridy.iband.view.main;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.applib.utils.FileUtil;
import com.manridy.applib.utils.ToastUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.common.HexUtil;
import com.manridy.iband.common.OtaDfuInitiator;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.BluetoothLeManager;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.manridy.sdk.BluetoothLeManager.ACTION_DATA_AVAILABLE;
import static com.manridy.sdk.BluetoothLeManager.ACTION_DATA_WRITE_SUCCESS;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_CONNECT;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_DISCONNECTED;
import static com.manridy.sdk.BluetoothLeManager.ACTION_GATT_RECONNECT;
import static com.manridy.sdk.BluetoothLeManager.ACTION_NOTIFICATION_ENABLE;
import static com.manridy.sdk.BluetoothLeManager.ACTION_SERVICES_DISCOVERED;


public class OtaActivity1 extends BaseActionActivity {

    private static final String tag = "OtaActivity1";

    private CircularView cv_ota;
    private TextView tv_ota_ok;
    private TextView tv_ota_result;
    private TextView tv_ota_progress;
    private ImageView iv_ota;

    private OtaDfuInitiator otaDfuInitiator;
    private BluetoothLeManager mBluetoothLeService;

    private Context mContext;


    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            String s_recvPro = intent.getStringExtra(BluetoothLeManager.EXTRA_DATA);
//            if (s_recvPro == null) return;
//            byte[] b_recvPro = HexUtil.hexStringToBytes(s_recvPro);
            byte[] b_recvPro = intent.getByteArrayExtra("BLUETOOTH_DATA");
            if (b_recvPro == null) return;
            if (BluetoothLeManager.ACTION_DATA_AVAILABLE.equals(action)) {
                if ((b_recvPro[0] & 0xff) == 0xfb) { //固件升级返回头
                    if ((b_recvPro[1] & 0xff) == 0x80) {    //设备ready,并开始请求数据块
                        otaDfuInitiator.resetPackCnt();
                        bleSendCmd(otaDfuInitiator.getOtaNextPack());
                    } else if ((b_recvPro[1] & 0xff) == 0x81) {
                        //0xFB 81 RS(0成功1失败) 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
                        if (b_recvPro[2] == 0) {
                            EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_UPDATE_SUCCESS));
                        } else {
                            EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_UPDATE_FAIL));
                        }
                    } else if ((b_recvPro[1] & 0xff) == 0x83) {//App 终止DFU
                        //0xFB 83 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
//                        Log.d(tag, "onReceive: " + s_recvPro);
                    }
                }
            } else if (ACTION_DATA_WRITE_SUCCESS.equals(action)) {
                if ((b_recvPro[0] & 0xff) == 0xfa) {
                    if ((b_recvPro[1] & 0xff) == 0x01) {
                        if (otaDfuInitiator.otaPackCntAccumulate() < 256) {
                            bleSendCmd(otaDfuInitiator.getOtaNextPack());
                            EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_UPDATE_PROGRESS, otaDfuInitiator.progress()));
                        }
                    }
                }
            }
        }
    };
//
//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeManager.LocalBinder) service).getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };

    @Override
    protected void initView(Bundle savedInstanceState) {
        mContext = OtaActivity1.this;
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_device_ota);
        initView();

    }

    @Override
    protected void initVariables() {
        otaDfuInitiator = new OtaDfuInitiator(FileUtil.getSdCardPath()+"/ota.bin");
        initListener();
        start();

    }

    private void initView() {
        cv_ota = (CircularView) findViewById(R.id.cv_ota);
        iv_ota = (ImageView) findViewById(R.id.iv_ota);
        tv_ota_ok = (TextView) findViewById(R.id.tv_ota_ok);
        tv_ota_result = (TextView) findViewById(R.id.tv_ota_result);
        tv_ota_progress = (TextView) findViewById(R.id.tv_ota_progress);
    }

    public void initListener() {
        tv_ota_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DATA_WRITE_SUCCESS);
        filter.addAction(ACTION_DATA_AVAILABLE);
        /*LocalBroadcastManager.getInstance(getApplicationContext()).*/
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mGattUpdateReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(connection);
//        connection = null;
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bleSendCmd(otaDfuInitiator.getOtaEndDfu());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMsgEvent(EventMessage mMessageEvent) {
        switch (mMessageEvent.getWhat()) {
            case EventMessage.MSG_WHAT_UPDATE_PROGRESS:
                dismissProgress();
                int i_progress = (Integer) mMessageEvent.getObject();
                float progress = ((float) i_progress / (float) otaDfuInitiator.getOtaFilePackTotalNum()) * 100;
                cv_ota.setProgress(progress).invaliDate();
                progress = (float) Math.round(progress * 10) / 10;//保留1位小数
                tv_ota_progress.setText("已完成" + progress + "%");
                break;
            case EventMessage.MSG_WHAT_UPDATE_SUCCESS:
                iv_ota.setVisibility(View.GONE);
                tv_ota_result.setVisibility(View.VISIBLE);
                tv_ota_ok.setVisibility(View.VISIBLE);
                break;
            case EventMessage.MSG_WHAT_UPDATE_FAIL:
                tv_ota_result.setVisibility(View.VISIBLE);
                tv_ota_result.setTextColor(Color.parseColor("#e64a19"));
                tv_ota_result.setText(R.string.error_ota_fail);
                break;
            default:
                break;
        }
    }


    public void start() {
        showProgress("請勿操作，正在加載文件...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (otaDfuInitiator.loadImageFile()) {
//                    EventBus.getDefault().post(new EventMessage(EventMessage.MSG_WHAT_FILE_TOTAL_SIZE));
                    bleSendCmd(otaDfuInitiator.otaDfuCmd());
                }
            }
        }).start();
    }



    private void bleSendCmd(byte[] cmds) {
        if (ibandApplication == null || ibandApplication.service == null || ibandApplication.service.watch == null) {
            return;
        }
        ibandApplication.service.watch.bleDfuSendCmd(cmds);
    }

}
