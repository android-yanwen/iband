package com.manridy.iband.view;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.service.carrier.CarrierService;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.dalimao.library.util.FloatUtil;
import com.google.gson.Gson;
import com.manridy.applib.base.BaseActivity;
import com.manridy.applib.common.AppManage;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.SyncData;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DeviceUpdate;
import com.manridy.iband.common.DomXmlParse;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.common.Utils;
import com.manridy.iband.service.AppNotificationListenerService;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.SimpleView;
import com.manridy.iband.view.model.BoFragment;
import com.manridy.iband.view.model.BpFragment;
import com.manridy.iband.view.model.HrFragment;
import com.manridy.iband.view.model.SleepFragment;
import com.manridy.iband.view.model.StepFragment;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.exception.BleException;
import com.rd.PageIndicatorView;

import org.bouncycastle.crypto.engines.CAST5Engine;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.weyye.hipermission.PermissionAdapter;
import me.weyye.hipermission.PermissionItem;
import me.weyye.hipermission.PermissionView;

/**
 * 主页
 * Created by jarLiao on 17/5/4.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.vp_model)
    ViewPager vpModel;
    @BindView(R.id.piv_dots)
    PageIndicatorView pivDots;
    @BindView(R.id.tb_title)
    TextView tbTitle;
    @BindView(R.id.tb_set)
    ImageView tbSet;
    @BindView(R.id.rl_title)
    RelativeLayout rlTitle;
    @BindView(R.id.tb_sync)
    TextView tbSync;
    @BindView(R.id.prl_refresh)
    PullRefreshLayout prlRefresh;
    @BindView(R.id.view)
    TextView view;

    private FragmentPagerAdapter viewAdapter;
    private List<Fragment> viewList = new ArrayList<>();

    private IbandApplication ibandApplication;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private AlertDialog findPhone;
    private AlertDialog lostAlert;
    private Vibrator vibrator;
    private MediaPlayer mp;
    String url = "http://39.108.92.15:12345";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            if (!ibandApplication.service.watch.isBluetoothEnable()) {
            int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
            boolean otaRun = (boolean) SPUtil.get(mContext, AppGlobal.STATE_APP_OTA_RUN, false);
            Log.d(TAG, "LostHandleMessage() called with: state = [" + state + "]"+"otaRun = ["+otaRun+"]");
            if (state != AppGlobal.DEVICE_STATE_CONNECTED && !otaRun) {
                playAlert(true, alertTime);
                String time = TimeUtil.getNowYMDHMSTime();
                showLostNotification(time);
                showLostAlert(time);
                Log.d(TAG, "onEventMainThread() called with: event = [handleMessage]");
            }

//            }
        }
    };
    private boolean isShowBp;
    private boolean isShowBo;

    @Override
    protected void onResume() {
        super.onResume();
        int connectState = (int) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_CONNECT_STATE,AppGlobal.DEVICE_STATE_UNCONNECT);
        if (connectState == AppGlobal.DEVICE_STATE_CONNECTED) {
            long time = (long) SPUtil.get(mContext, AppGlobal.DATA_SYNC_TIME, 0L);
            if (time != 0 && tbSync != null) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String str = format.format(new Date(time));
                tbSync.setText(getString(R.string.hint_sync_last) + str);
            }
            ibandApplication.service.watch.sendCmd(BleCmd.setTime());
        }
        isShowBp = isShowBo =true;
        String strDeviceList = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_LIST,"");
        String deviceType = (String) SPUtil.get(mContext,AppGlobal.DATA_FIRMWARE_TYPE,"");
        if (!strDeviceList.isEmpty()) {
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList,DeviceList.class);
            for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                if (deviceType.equals(resultBean.getDevice_id())){
                    if ("0".equals(resultBean.getBlood_pressure())) {
                        isShowBp = false;
                    }
                    if ("0".equals(resultBean.getOxygen_pressure())) {
                        isShowBo = false;
                    }
                }
            }
            try {
                if (!isShowBp) {
                    viewList.remove(bpFragment);
                }
                if (!isShowBo) {
                    viewList.remove(boFragment);
                }
                if (!isShowBp || !isShowBo){
                    viewAdapter.notifyDataSetChanged();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        EventBus.getDefault().register(this);
        ibandApplication = (IbandApplication) getApplication();
        setStatusBar();
        initViewPager();
        initNotification();
        mSimpleView = new SimpleView(mContext.getApplicationContext());
//        initDeviceUpdate();
    }

    private void initDeviceUpdate() {
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        if (mac.isEmpty()) {
            return;
        }
        final String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
        final String deviceVersion = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"1.0.0");
        new DeviceUpdate(mContext).checkDeviceUpdate(new OnResultCallBack() {
            @Override
            public void onResult(boolean result, Object o) {
                if (result) {
                    if (o != null) {
                        List<DomXmlParse.Image> imageList = (List<DomXmlParse.Image>) o;
                        for (DomXmlParse.Image image : imageList) {
                            if (image.id.equals(deviceType)) {
                                if (image.least.compareTo(deviceVersion) > 0) {
                                    final String fileUrl = url + "/" + image.id + "/" + image.file;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new DeviceUpdate(mContext).show(fileUrl);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void initNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mp = MediaPlayer.create(this, R.raw.alert);
        mp.setLooping(true);
    }

    BpFragment bpFragment;
    BoFragment boFragment;
    private void initViewPager() {
        bpFragment = new BpFragment();
        boFragment = new BoFragment();

        viewList.add(new StepFragment());
        viewList.add(new SleepFragment());
        viewList.add(new HrFragment());
        viewList.add(bpFragment);
        viewList.add(boFragment);
        viewAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return viewList.get(position);
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
//              super.destroyItem(container, position, object);
            }
        };
        vpModel.setAdapter(viewAdapter);
        pivDots.setViewPager(vpModel);
        vpModel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
//                        prlRefresh.setEnabled(false);
//                        Log.d(TAG, "onTouch() called with: ACTION_MOVE ");
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
//                        prlRefresh.setEnabled(true);
//                        Log.d(TAG, "onTouch() called with: ACTION_CANCEL ");
                        break;
                }
                return false;
            }
        });
    }

    @Override
    protected void initListener() {
        tbSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingActivity.class));
            }
        });

        vpModel.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float alpha;
                int index = position;
                if (positionOffset <= 0.5) {
                    alpha = 1 - (positionOffset * 2);
                } else {
                    alpha = (positionOffset * 2) - 1;
                    index++;
                }
                rlTitle.setAlpha(alpha);
                selectTitle(index);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        view.setBackgroundColor(Color.parseColor("#2196f3"));
                        break;
                    case 1:
                        view.setBackgroundColor(Color.parseColor("#673ab7"));
                        break;
                    case 2:
                        view.setBackgroundColor(Color.parseColor("#ef5350"));
                        break;
                    case 3:
                        view.setBackgroundColor(Color.parseColor("#43a047"));
                        break;
                    case 4:
                        view.setBackgroundColor(Color.parseColor("#ff4081"));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        SyncData.getInstance().setSyncAlertListener(new SyncData.OnSyncAlertListener() {
            @Override
            public void onResult(final boolean isSuccess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prlRefresh.setRefreshing(false);
                        if (isSuccess) {
                            SPUtil.put(mContext,AppGlobal.DATA_SYNC_TIME,System.currentTimeMillis());
                            setHintState(AppGlobal.DEVICE_STATE_SYNC_OK);
                            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_ALL));
                        } else {
                            setHintState(AppGlobal.DEVICE_STATE_SYNC_NO);
                        }
                    }
                });
                Log.d(TAG, "onResult() called with: isSuccess = [" + isSuccess + "]");
            }

            @Override
            public void onProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String str = progress == 0 ? "" : (progress + "%");
                        tbSync.setText(getString(R.string.hint_syncing) + str);
                    }
                });
                Log.d(TAG, "onProgress() called with: progress = [" + progress + "]");
            }
        });

        prlRefresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
                if (checkBindDevice(mac)){
                    prlRefresh.setRefreshing(false);
                    return;
                }

                int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                if (state != 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setHintState(AppGlobal.DEVICE_STATE_CONNECTING);
                            ibandApplication.service.watch.closeBluetoothGatt(mac);
                            connectDevice();
                        }
                    });
                }else{
                    if (SyncData.getInstance().isRun()){
                        prlRefresh.setRefreshing(false);
                        return;
                    }
                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
                }
            }
        });
    }

    private boolean checkBindDevice(String mac) {
        if (mac == null || mac.isEmpty()) {
            return true;
        }
        return false;
    }

    private void connectDevice(){
        if (ibandApplication.service == null) {
            return;
        }
        ibandApplication.service.initConnect(true,new BleConnectCallback() {
            @Override
            public void onConnectSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setHintState(AppGlobal.DEVICE_STATE_CONNECT_SUCCESS);
                        prlRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onConnectFailure(final BleException exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prlRefresh.setRefreshing(false);
                        if (exception.getCode() ==  999) {
                            setHintState(AppGlobal.DEVICE_STATE_UNBIND);
                        }else if (exception.getCode() ==  1000){
                            setHintState(AppGlobal.DEVICE_STATE_UNFIND);
                        }else{
                            setHintState(AppGlobal.DEVICE_STATE_CONNECT_FAIL);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void loadData() {
        super.loadData();
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
        if (!Watch.getInstance().isBluetoothEnable()) {
            OpenBluetoothDialog();
        } else if (mac.isEmpty()) {
            showFloatView(getString(R.string.hint_device_unbind), getString(R.string.hint_bind));
        } else if (state == AppGlobal.DEVICE_STATE_UNCONNECT) {
            ibandApplication.service.initConnect(false);
            setHintState(AppGlobal.DEVICE_STATE_CONNECTING);
            return;
        } else if (state == AppGlobal.DEVICE_STATE_CONNECTED) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
        }
        setHintState(state);
        EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_LOAD_DEVICE_LIST));

    }

    private void selectTitle(int position) {
        switch (position) {
            case 0:
                tbTitle.setText(R.string.hint_view_step);
                break;
            case 1:
                tbTitle.setText(R.string.hint_view_sleep);
                break;
            case 2:
                tbTitle.setText(R.string.hint_view_hr);
                break;
            case 3:
                tbTitle.setText(R.string.hint_view_hp);
                break;
            case 4:
                tbTitle.setText(R.string.hint_view_bo);
                break;
        }
    }

    private void setHintState(int state){
        String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        if (bindMac == null || bindMac.isEmpty()) {
            tbSync.setText(R.string.hint_un_bind);
            return;
        }else if (!ibandApplication.service.watch.isBluetoothEnable()){
            tbSync.setText("蓝牙已关闭");
            return;
        }
        switch (state) {
            case AppGlobal.DEVICE_STATE_UNCONNECT:
                tbSync.setText(R.string.hint_un_connect);
                break;
            case AppGlobal.DEVICE_STATE_CONNECTED:
                tbSync.setText(R.string.hint_connected);
                break;
            case AppGlobal.DEVICE_STATE_CONNECTING:
                tbSync.setText(R.string.hint_connecting);
                break;
            case AppGlobal.DEVICE_STATE_CONNECT_FAIL:
                tbSync.setText(R.string.hint_connect_fail);
                break;
            case AppGlobal.DEVICE_STATE_CONNECT_SUCCESS:
                tbSync.setText(R.string.hint_connect_success);
                break;
            case AppGlobal.DEVICE_STATE_UNFIND:
                tbSync.setText(R.string.hint_un_find);
                break;
            case AppGlobal.DEVICE_STATE_SYNC_OK:
                tbSync.setText(R.string.hint_sync_ok);
                break;
            case AppGlobal.DEVICE_STATE_SYNC_NO:
                tbSync.setText(R.string.hint_sync_no);
                break;
            case AppGlobal.DEVICE_STATE_BLUETOOTH_DISENABLE:
                tbSync.setText("蓝牙已关闭");
                break;
            case AppGlobal.DEVICE_STATE_BLUETOOTH_ENABLEING:
                tbSync.setText("蓝牙开启中");
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_FIND_PHONE_START) {
            //开启震动和铃声
            playAlert(true, alertTime);
            showFindPhoneNotification();
            showFindPhoneDialog();
        } else if (event.getWhat() == EventGlobal.ACTION_FIND_PHONE_STOP) {
            if (findPhone != null) {//隐藏窗口
                findPhone.dismiss();
            }
            if (notification != null) {//取消通知
                mNotificationManager.cancel(EventGlobal.ACTION_FIND_PHONE_START);
            }
            playAlert(false, alertTime);
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_DISCONNECT) {
            byte[] bytes = (byte[]) (null == event.getObject()? new byte[1] : event.getObject()) ;//得到异常信息,空为新建
            boolean isLostOn = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_LOST, false);
            String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
            Log.d(TAG, "STATE_DEVICE_DISCONNECT() called with: bytes = [" + bytes[0] + "]");
            if (bindMac.isEmpty()) {
                tbSync.setText(R.string.hint_device_unbind);
            }else {
                tbSync.setText(R.string.hint_un_connect);
            }
            if (isLostOn && !bindMac.isEmpty() && 0 == bytes[0] || 19 == bytes[0]) {
                handler.sendEmptyMessageDelayed(0, 20 * 1000);
                Log.d(TAG, "onLostThread() called with: event = [STATE_DEVICE_DISCONNECT]");
            }
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_CONNECTING) {
//            boolean isLostOn = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_LOST, false);
//            String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
//            if (isLostOn && !bindMac.isEmpty()) {
//                handler.sendEmptyMessageDelayed(0, 20 * 1000);
//                Log.d(TAG, "onLostThread() called with: event = [STATE_DEVICE_CONNECTING]");
//            }
            tbSync.setText(getString(R.string.hint_connecting));
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_CONNECT) {
            handler.removeMessages(0);
            cancelLostAlert();
            tbSync.setText(getString(R.string.hint_connected));
//            if (isFirstConnect) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
//            }
//            isFirstConnect = false;
        } else if (event.getWhat() == EventGlobal.STATE_CHANGE_BLUETOOTH_ON) {
            if (bluetoothDialog != null && bluetoothDialog.isShowing()) {
                bluetoothDialog.dismiss();
            }
            ibandApplication.service.watch.clearBluetoothLe();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ibandApplication.service.initConnect(false);
                }
            }, 3000);
            hideFloatView();
            setHintState(AppGlobal.DEVICE_STATE_CONNECTING);
        } else if (event.getWhat() == EventGlobal.STATE_CHANGE_BLUETOOTH_OFF) {
            ibandApplication.service.watch.clearBluetoothLe();
            OpenBluetoothDialog();
            setHintState(AppGlobal.DEVICE_STATE_BLUETOOTH_DISENABLE);
        } else if (event.getWhat() == EventGlobal.STATE_CHANGE_BLUETOOTH_ON_RUNING) {
            setHintState(AppGlobal.DEVICE_STATE_BLUETOOTH_ENABLEING);
        } else if (event.getWhat() == EventGlobal.ACTION_BLUETOOTH_OPEN) {
            ibandApplication.service.watch.BluetoothEnable(AppManage.getInstance().currentActivity());
        } else if (event.getWhat() == EventGlobal.ACTION_DEVICE_CONNECT) {
            ibandApplication.service.initConnect(false);
        } else if (event.getWhat() == EventGlobal.ACTION_HIDE_SIMPVIEW){
            hideFloatView();
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNBIND){
            setHintState(AppGlobal.DEVICE_STATE_UNBIND);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_SYNC_HISTORY) {
            SyncData.getInstance().sync();
        }else  if (event.getWhat() == EventGlobal.ACTION_LOAD_DEVICE_LIST) {
            HttpService.getInstance().getDeviceList(new OnResultCallBack() {
                @Override
                public void onResult(boolean result, Object o) {
                    if (result) {
                        String strDeviceList = o.toString();
                        //解析服务器设备列表数据
                        SPUtil.put(ibandApplication,AppGlobal.DATA_DEVICE_LIST, strDeviceList);
                        DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
                        //筛选iband设备数据
                        ArrayList<String> nameList = new ArrayList<>();
                        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                            if (resultBean.getIdentifier().equals("iband")) {
                                nameList.add(resultBean.getDevice_name());
                            }
                        }
                        String str =new Gson().toJson(nameList);
                        SPUtil.put(ibandApplication,AppGlobal.DATA_DEVICE_FILTER,str);
                    }
                }
            });
        }
    }

    SimpleView mSimpleView;

    private void showFloatView(String str, String bt) {
        showFloatView(str, bt, false);
    }

    private void showFloatView(String str, String bt, boolean isEnd) {
        if (mSimpleView.isShow()) {
            mSimpleView.setContent(str, bt, isEnd);
        } else {
            mSimpleView = new SimpleView(mContext.getApplicationContext());
            Point point = new Point();
            point.x = 0;
            if (Utils.checkDeviceHasNavigationBar(mContext)) {
                point.y = Utils.getNavigationBarHeight(mContext);
            } else {
                point.y = 0;
            }
            mSimpleView.setContent(str, bt, isEnd);
            FloatUtil.showFloatView(mSimpleView, Gravity.BOTTOM, WindowManager.LayoutParams.TYPE_TOAST, point, null);
        }
    }

    private void hideFloatView() {
        mSimpleView.hideFloatView();
    }

    private void cancelLostAlert() {
        if (lostAlert != null) {//隐藏窗口
            lostAlert.dismiss();
        }
        if (notification != null) {//取消通知
            mNotificationManager.cancel(EventGlobal.STATE_DEVICE_DISCONNECT);
        }
        playAlert(false, alertTime);
    }


    long alertTime = 10000;
    boolean isPlayAlert;

    public synchronized void playAlert(boolean enable, long time) {
        try {
            if (enable) {
                long[] pattern = {100, 400, 100, 400}; // 停止 开启 停止 开启
                vibrator.vibrate(pattern, 0); //重复两次上面的pattern 如果只想震动一次，index设为-1
                mp.start();
                isPlayAlert = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playAlert(false, alertTime);
                    }
                }, time);
            } else {
                vibrator.cancel();
                if (mp.isPlaying()) {
                    mp.pause();
                }
                isPlayAlert = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFindPhoneDialog() {
        if (findPhone != null) {
            findPhone.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AppManage.getInstance().currentActivity());
        builder.setTitle(R.string.hint_find_phone);
        builder.setMessage(R.string.hint_finding_phone);
        builder.setNegativeButton(getString(R.string.hint_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ibandApplication.service.watch.sendCmd(BleCmd.affirmFind(), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
                playAlert(false, alertTime);
                dialog.dismiss();
            }
        });
        findPhone = builder.create();
        findPhone.setCanceledOnTouchOutside(false);
        findPhone.show();
    }

    private void showFindPhoneNotification() {
        if (CheckUtil.isAppBackground(mContext)) {
            if (notification != null) {
                mNotificationManager.cancel(EventGlobal.ACTION_FIND_PHONE_START);
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getString(R.string.hint_finding_phone))
                    .setTicker(getString(R.string.hint_finding_phone)) //通知首次出现在通知栏，带上升动画效果的
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                    .setLights(0xff0000ff, 300, 0);

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClass(this, AppManage.getInstance().currentActivity().getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            notification = mBuilder.build();
            mNotificationManager.notify(EventGlobal.ACTION_FIND_PHONE_START, notification);
        }
    }

    private void showLostNotification(String time) {
        if (CheckUtil.isAppBackground(mContext)) {//判断是否在后台
            if (notification != null) {//判断通知是否存在
                mNotificationManager.cancel(EventGlobal.STATE_DEVICE_DISCONNECT);
            }
            //创建通知
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
            mBuilder.setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(getString(R.string.hint_menu_alert_lost))
                    .setContentText(getString(R.string.hint_alert_lost1) + time + getString(R.string.hint_alert_lost2))
                    .setTicker(getString(R.string.hint_menu_alert_lost)) //通知首次出现在通知栏，带上升动画效果的
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setLights(0xff0000ff, 300, 0);
            //点击意图
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClass(this, AppManage.getInstance().currentActivity().getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            notification = mBuilder.build();
            mNotificationManager.notify(EventGlobal.STATE_DEVICE_DISCONNECT, notification);
        }
    }

    private void showLostAlert(String time) {
        //如果窗口存在先释放掉
        if (lostAlert != null) {
            lostAlert.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(AppManage.getInstance().currentActivity());
        builder.setTitle(getString(R.string.hint_menu_alert_lost));
        builder.setMessage(getString(R.string.hint_alert_lost1) + time + getString(R.string.hint_alert_lost2));
        builder.setNegativeButton(R.string.hint_alert_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playAlert(false, alertTime);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.hint_alert_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SPUtil.put(mContext, AppGlobal.DATA_ALERT_LOST, false);
                EventBus.getDefault().post(EventGlobal.DATA_CHANGE_MENU);
                playAlert(false, alertTime);
                dialog.dismiss();
            }
        });
        lostAlert = builder.create();
        lostAlert.setCanceledOnTouchOutside(false);
        lostAlert.show();
    }

    AlertDialog bluetoothDialog;
    public void OpenBluetoothDialog(){
        PermissionView contentView = new PermissionView(this);
        List<PermissionItem> data = new ArrayList<>();
        data.add(new PermissionItem(getString(R.string.hint_bluetooth),getString(R.string.hint_bluetooth),R.mipmap.permission_ic_bluetooth));
        contentView.setGridViewColum(data.size());
        contentView.setTitle(getString(R.string.hint_bluetooth_open));
        contentView.setMsg(getString(R.string.hint_bluetooth_open_alert));
        contentView.setGridViewAdapter(new PermissionAdapter(data));
        contentView.setStyleId(R.style.PermissionBlueStyle);
//        contentView.setFilterColor(mFilterColor);
        contentView.setBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothDialog != null && bluetoothDialog.isShowing()) {
                    bluetoothDialog.dismiss();
                }
                IbandApplication.getIntance().service.watch.BluetoothEnable(AppManage.getInstance().currentActivity());
            }
        });
        if (bluetoothDialog != null && bluetoothDialog.isShowing()) {
            return;
        }
        bluetoothDialog = new AlertDialog.Builder(AppManage.getInstance().currentActivity())
                .setView(contentView)
                .create();
        bluetoothDialog.setCanceledOnTouchOutside(false);
        bluetoothDialog.setCancelable(false);
        bluetoothDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bluetoothDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }
}
