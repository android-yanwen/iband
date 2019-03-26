package com.manridy.iband.view.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.baoyz.widget.PullRefreshLayout;
import com.dalimao.library.util.FloatUtil;
import com.google.gson.Gson;
import com.manridy.applib.base.BaseActivity;
import com.manridy.applib.common.AppManage;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.DeviceListDataSpare;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.IbandLoginBean;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.bean.AddressModel1;
import com.manridy.iband.bean.WeatherModel;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.SyncData;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DeviceUpdate;
import com.manridy.iband.common.DomXmlParse;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.common.Utils;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.SimpleView;
import com.manridy.iband.view.model.BoFragment;
import com.manridy.iband.view.model.BpFragment;
import com.manridy.iband.view.model.EcgFragment;
import com.manridy.iband.view.model.HrFragment;
import com.manridy.iband.view.model.MicrocirculationFragment;
import com.manridy.iband.view.model.SleepFragment;
import com.manridy.iband.view.model.StepFragment;
import com.manridy.sdk.Watch;
import com.manridy.sdk.bean.Weather;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.exception.BleException;
import com.rd.PageIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import me.weyye.hipermission.PermissionAdapter;
import me.weyye.hipermission.PermissionItem;
import me.weyye.hipermission.PermissionView;

import static com.manridy.iband.common.EventGlobal.DATA_LOAD_WEATHER;

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
    @BindView(R.id.iv_share)
    ImageView ivShare;

    private FragmentPagerAdapter viewAdapter;
    private List<Fragment> viewList = new ArrayList<>();

    private IbandApplication ibandApplication;
    private NotificationManager mNotificationManager;
    private Notification notification;
    private AlertDialog findPhone;
    private AlertDialog lostAlert;
    private AlertDialog noNetWorkDlg;
    private Vibrator vibrator;
    private MediaPlayer mp;
    private String url = "http://39.108.92.15:12345";
    private boolean isLogOnOff = true;

    private String filePath;
    WeatherModel weatherModel;

    private boolean isViewEcg = true;

    private boolean isDeviceIdInService = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
            boolean otaRun = (boolean) SPUtil.get(mContext, AppGlobal.STATE_APP_OTA_RUN, false);
            LogUtil.d(TAG, "LostHandleMessage() called with: state = [" + state + "]"+"otaRun = ["+otaRun+"]");
            if ((state != AppGlobal.DEVICE_STATE_CONNECTED && !otaRun)&& !checkLostDisturb() ) {
                playAlert(true, alertTime);
                String time = TimeUtil.getNowYMDHMSTime();
                showLostNotification(time);
                showLostAlert(time);
                LogUtil.d(TAG, "onEventMainThread() called with: event = [handleMessage]");
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    ivShare.setEnabled(false);
                    showShare();
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessageDelayed(message,1500);
                    break;
                case 2:
                    ivShare.setEnabled(true);
                    break;
                case 3:
                    if(!isConn(MainActivity.this)){
                        showNoNetWorkDlg(MainActivity.this);
                    }

                    break;
                case 4:
                    boolean isShowBp = (boolean)SPUtil.get(mContext,"isShowBp",false);
                    boolean isShowBo = (boolean)SPUtil.get(mContext,"isShowBo",false);
                    boolean isShowEcg = (boolean)SPUtil.get(mContext,"isShowEcg",false);
                    boolean isShowMicro = (boolean)SPUtil.get(mContext,"isShowMicro",false);
                    is_show_weather = (boolean)SPUtil.get(mContext,"is_show_weather",false);
                    if(stepFragment!=null){
                        stepFragment.setWeather(is_show_weather);
                    }
                    try {
                        if (!isShowBp) {
                            viewList.remove(bpFragment);
                        }
                        if (!isShowBo) {
                            viewList.remove(boFragment);
                        }
                        if(!isShowEcg){
                            viewList.remove(ecgFragment);
                        }
                        if (!isShowMicro) {
                            viewList.remove(microcirculationFragment);
                        }
                        if (!isShowBp || !isShowBo || !isShowEcg || !isShowMicro) {
                            viewAdapter.notifyDataSetChanged();
//                            handler2.sendMessage(handler2.obtainMessage(4));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    boolean isnotifyDataSetChanged = false;

                    if (isShowBp || isShowBo || isShowEcg || isShowMicro) {
                        boolean isHaveBp = false;
                        boolean isHaveBo = false;
                        boolean isHaveEcg = false;
                        boolean isHaveMicro = false;
                        Iterator it = viewList.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (BpFragment.class.equals(o.getClass())) {
                                isHaveBp = true;
                            } else if (BoFragment.class.equals(o.getClass())) {
                                isHaveBo = true;
                            } else if (EcgFragment.class.equals(o.getClass())) {
                                isHaveEcg = true;
                            } else if (MicrocirculationFragment.class.equals(o.getClass())) {
                                isHaveMicro = true;
                            }
                        }
                        if (isShowBp && (!isHaveBp)) {
                            if (bpFragment != null) {
                                viewList.add(bpFragment);
                                isnotifyDataSetChanged = true;
                            } else {
                                bpFragment = new BpFragment();
                                viewList.add(bpFragment);
                                isnotifyDataSetChanged = true;
                            }
                        }
                        if (isShowBo && (!isHaveBo)) {
                            if (boFragment != null) {
                                viewList.add(boFragment);
                                isnotifyDataSetChanged = true;
                            } else {
                                boFragment = new BoFragment();
                                viewList.add(boFragment);
                                isnotifyDataSetChanged = true;
                            }
                        }
                        if (isShowEcg && (!isHaveEcg)) {
                            if (ecgFragment != null) {
                                viewList.add(ecgFragment);
                                isnotifyDataSetChanged = true;
                            } else {
                                ecgFragment = new EcgFragment();
                                viewList.add(ecgFragment);
                                isnotifyDataSetChanged = true;
                            }
                        }
                        if (isShowMicro && (!isHaveMicro)) {
                            if (microcirculationFragment != null) {
                                viewList.add(microcirculationFragment);
                                isnotifyDataSetChanged = true;
                            } else {
                                microcirculationFragment = new MicrocirculationFragment();
                                viewList.add(microcirculationFragment);
                                isnotifyDataSetChanged = true;
                            }
                        }
                    }


                    if(isnotifyDataSetChanged){
//                    handler2.sendMessage(handler2.obtainMessage(4));
                        viewAdapter.notifyDataSetChanged();
//                    isnotifyDataSetChanged = false;
                    }
//                    viewAdapter.notifyDataSetChanged();
                    break;
                case 5:
                    initDeviceUpdate();
                    break;
                case 6:
                    if ( noNetWorkDlg!= null) {
                        noNetWorkDlg.dismiss();
                    }
                    break;
            }
        }
    };


    Runnable updatePageRunnable = new Runnable() {
        @Override
        public void run() {
            //        isShowBp = isShowBo =false;
            isShowBp = (boolean) SPUtil.get(mContext,"isShowBp",false);
            isShowBo = (boolean) SPUtil.get(mContext,"isShowBo",false);
            isShowEcg = (boolean) SPUtil.get(mContext,"isShowEcg",false);
            isShowMicro = (boolean) SPUtil.get(mContext,"isShowMicro",false);
            is_show_weather= (boolean) SPUtil.get(mContext,"is_show_weather",false);
            String strDeviceList = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_LIST,"");
            String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");

            String deviceName = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_BIND_NAME,"");
            Log.i("deviceType",deviceType);
            Log.i("deviceName",deviceName);

            isDeviceIdInService = false;
            if (strDeviceList!= null && !strDeviceList.isEmpty()) {
                DeviceList filterDeviceList = new Gson().fromJson(strDeviceList,DeviceList.class);
                for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                    if (deviceType.trim().equals(resultBean.getDevice_id().trim())){
                        isDeviceIdInService = true;
                        if ("0".equals(resultBean.getBlood_pressure())) {
                            isShowBp = false;
                        }else{
                            isShowBp = true;
                        }
                        SPUtil.put(mContext,"isShowBp",isShowBp);
                        if ("0".equals(resultBean.getOxygen_pressure())) {
                            isShowBo = false;
                        }else{
                            isShowBo = true;
                        }
                        SPUtil.put(mContext,"isShowBo",isShowBo);

                        if ("0".equals(resultBean.getEcg())) {
                            isShowEcg = false;
                        }else{
                            isShowEcg = true;
                        }
                        SPUtil.put(mContext,"isShowEcg",isShowEcg);

                        if ("0".equals(resultBean.getMicrocirculation())) {
                            isShowMicro = false;
                        }else{
                            isShowMicro = true;
                        }
                        SPUtil.put(mContext,"isShowMicro",isShowMicro);

                        if ("0".equals(resultBean.getIs_show_weather())) {
                            is_show_weather = false;
                        }else{
                            is_show_weather = true;
                        }
                        SPUtil.put(mContext,"is_show_weather",is_show_weather);


                        handler2.sendMessage(handler2.obtainMessage(4));

                    }
                }


            }
        }
    };

    /**
     * 判断网络连接是否已开
     * true 已打开  false 未打开
     * */
    public static boolean isConn(Context context){
        boolean bisConnFlag=false;
        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = conManager.getActiveNetworkInfo();
        if(network!=null){
            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
        }
        return bisConnFlag;
    }

    /**
     * 当判断当前手机没有网络时选择是否打开网络设置
     * @param context
     */
    public void showNoNetWorkDlg(final Context context) {
        if ( noNetWorkDlg!= null) {
            noNetWorkDlg.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.app_icon)         //
                .setTitle(R.string.app_name)            //
                .setMessage(R.string.hint_network_available).setPositiveButton(R.string.hint_set, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 跳转到系统的网络设置界面
                Intent intent = null;
                // 先判断当前系统版本
                if (android.os.Build.VERSION.SDK_INT > 10) {  // 3.0以上
//                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                } else {
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
                }
                context.startActivity(intent);

            }
        }).setNegativeButton(R.string.hint_cancel, null);
        noNetWorkDlg = builder.create();
        noNetWorkDlg.setCanceledOnTouchOutside(false);
        noNetWorkDlg.show();
    }

        //检测防丢免打扰时间段和开关
    private boolean checkLostDisturb() {
        String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME,"");
        String deviceVersion = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"1.0.0");
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        if (strDeviceList!= null && !strDeviceList.isEmpty()) {
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            boolean isDisturb = getLostDisturb(deviceName,deviceVersion,filterDeviceList);
            if (isDisturb) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
                String hour = simpleDateFormat.format(new Date());
                int h = Integer.valueOf(hour);
                return h >= 23 || h< 6;
            }
        }
        return false;
    }

    //防丢免打扰开关
    private boolean getLostDisturb(String deviceName,String firmVision,DeviceList filterDeviceList){
        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
            if (resultBean.getDevice_name().equals(deviceName) && null !=  resultBean.getNot_disturb()) {
                return resultBean.getNot_disturb().compareTo(firmVision)>=0;
            }
        }
        return false;
    }

    private boolean isShowBp;
    private boolean isShowBo;
    private boolean isShowEcg;
    private boolean isShowMicro;
    private boolean  is_show_weather=false;
    @Override
    protected void onResume() {
        super.onResume();
        IbandApplication.recordingLoginNumFunc();
//        String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
//        if (bindMac == null || bindMac.isEmpty()) {
        int connectState = (int) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_CONNECT_STATE,AppGlobal.DEVICE_STATE_UNCONNECT);
        if (connectState == AppGlobal.DEVICE_STATE_CONNECTED) {
            long time = (long) SPUtil.get(mContext, AppGlobal.DATA_SYNC_TIME, 0L);
            if (time != 0 && tbSync != null) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String str = format.format(new Date(time));
                tbSync.setText(getString(R.string.hint_sync_last) +" "+str);
            }
            Watch.getInstance().sendCmd(BleCmd.setTime());
        }

        updateDeviceList();
        boolean isSupply = (boolean) SPUtil.get(mContext, "isSupplyWeather", true);
        if (isSupply) {
            getLocation();
        }
//        }

        // 设备是否绑定
        String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        if (bindMac == null || bindMac.isEmpty()) {
            if (alertHandler == null) {
                alertHandler = new Handler();
                alertHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog();
                    }
                }, 2000);
            }
        } else {
            dismissAlerDialog();
        }
    }

    private Handler alertHandler;
    private Dialog dialog;
    private void dismissAlerDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    private void showAlertDialog() {
        if (MainActivity.this != null && !MainActivity.this.isFinishing()) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.dialog_alert, null);
            Button btn_sure = v.findViewById(R.id.dialog_btn_sure);
            Button btn_cancel = v.findViewById(R.id.dialog_btn_cancel);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setCancelable(false);
            dialog = builder.create();
            dialog.show();
            dialog.getWindow().setContentView(v);
            btn_sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    startActivity(new Intent(mContext, DeviceActivity.class));
                    alertHandler = null;
                }
            });
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


//        builder.setMessage("设备未绑定，前往绑定");
//        builder.setTitle(R.string.hint_warm_alert);
//        builder.setCancelable(false);
//        builder.setPositiveButton(getString(R.string.hint_ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
//        builder.setNegativeButton(getString(R.string.hint_cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//            }
//        });
//        builder.create().show();
        }
    }

    int last_date;
    AMapLocationClient mlocationClient = null;
    AMapLocationListener aMapLocationListener = null;
    private void getLocation(){
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        Log.i("amapLocationErrorCode",""+amapLocation.getErrorCode());
                        aMapLocationListener = null;
                        if (mlocationClient != null) {
                            mlocationClient.stopLocation();
                            mlocationClient.onDestroy();
                        }
                        //zh en de es fr it jp kr ru
                        String localeCode = "en";
                        String locale = Locale.getDefault().getLanguage().toLowerCase();
                        String[] locales = new String[]{"zh","en","de","es","fr","it","jp","kr","ru"};

                        if(locale!=null&&locale.length()>=2){
                            boolean isAvailableLocale = false;
                            String localeSub = locale.substring(0,2);
                            if("ko".equals(localeSub)){
                                localeSub = "kr";
                            }
                            if("ja".equals(localeSub)){
                                localeSub = "jp";
                            }
                            for (String s:locales) {
                                if(s.equals(localeSub)){
                                    isAvailableLocale = true;
                                    break;
                                }
                            }
                            if(isAvailableLocale)localeCode = localeSub;
                        }

                        // 经度，纬度
//                        Log.e(TAG, "onLocationChanged: ................." + amapLocation.getLongitude() );
//                        Log.e(TAG, "onLocationChanged: ................." + amapLocation.getLatitude() );

                        Calendar c = Calendar.getInstance();
                        final int date = c.get(Calendar.DATE);
                        last_date = (int) SPUtil.get(mContext, AppGlobal.DATA_DATE, 0);
                        if (last_date != date) {
//                        if (true) {//测试用
                            HttpService.getInstance().getCityWeather(mContext,
                                    "" + amapLocation.getLongitude() + "," + amapLocation.getLatitude(),
//                                "116.310316,39.956074",
                                    new OnResultCallBack() {
                                        @Override
                                        public void onResult(boolean result, Object o) {
                                            if (result) {
                                                AddressModel1 addressModel = (AddressModel1) o;
//                                                MainActivity.addressModel = addressModel;//保存天气数据
//                                                pushForecastWeatherToWatch();//推送天气到手环
                                                /**************************存本地数据库***************************/
                                                saveForecastWeatherInfoToDatabase(addressModel);
                                                EventBus.getDefault().post(new EventMessage(DATA_LOAD_WEATHER));
                                                SPUtil.put(mContext, AppGlobal.DATA_DATE, date);
                                                Log.i(TAG, "onLocationChanged: 当天获取天气成功");
                                            } else {
                                                Log.d(TAG, "获取失败");
                                                last_date = -1;
                                            }
                                        }
                                    }
                            );
                        } else {
                            Log.i(TAG, "onLocationChanged: 下一天在获取天气");
                        }


                        //定位成功回调信息，设置相关消息
//                        HttpService.getInstance().getHeWeather_city("" + amapLocation.getLongitude() + "," + amapLocation.getLatitude(),localeCode, new OnResultCallBack() {
//                            @Override
//                            public void onResult(boolean result, Object o) {
//                                if(result){
//                                    AddressModel addressModel = (AddressModel)o;
//                                    List<AddressModel.HeWeather6Bean> heWeather6Beans = addressModel.getHeWeather6();
//                                    if(heWeather6Beans.size()>0){
//                                        AddressModel.HeWeather6Bean heWeather6Bean = heWeather6Beans.get(0);
//                                        List<AddressModel.HeWeather6Bean.BasicBean> basicBeans = heWeather6Bean.getBasic();
//                                        if(basicBeans.size()<1)return;
//                                        final AddressModel.HeWeather6Bean.BasicBean basicBean = basicBeans.get(0);
//                                        IbandApplication.getIntance().country = basicBean.getCnty();
//                                        if(basicBean.getParent_city()!=null) {
//                                            IbandApplication.getIntance().city = basicBean.getParent_city();
//                                        }else{
//                                            IbandApplication.getIntance().city = basicBean.getLocation();
//                                        }
//                                        HttpService.getInstance().getWeather(IbandApplication.getIntance().city, new OnResultCallBack() {
//                                            @Override
//                                            public void onResult(boolean result, Object o) {
//                                                if(result){
//                                                    com.manridy.iband.bean.Weather weather = (com.manridy.iband.bean.Weather)o;
//                                                    int max = 0xFF;
//                                                    int min = 0xFF;
//                                                    int now = 0xFF;
//                                                    if(weather.getData().getNowWeather().getTmp()!=null){
//                                                        now = Integer.parseInt(weather.getData().getNowWeather().getTmp());
//                                                    }
//                                                    if(weather.getData().getNowWeather().getTmp_max()!=null){
//                                                        max = Integer.parseInt(weather.getData().getNowWeather().getTmp_max());
//                                                    }
//                                                    if(weather.getData().getNowWeather().getTmp_min()!=null){
//                                                        min = Integer.parseInt(weather.getData().getNowWeather().getTmp_min());
//                                                    }
//                                                    LinkedList<Weather> forecastWeathers = new LinkedList<>();
//                                                    Weather forecastWeather;
//                                                    if(weather.getData().getForecastWeather().size()>=4){
//                                                        for(int i = 1;i<=4;i++){
//                                                            com.manridy.iband.bean.Weather.DataBean.ForecastWeatherBean forecastWeatherBean = weather.getData().getForecastWeather().get(i);
//                                                            forecastWeather = new Weather(forecastWeatherBean.getWeather_type(),Integer.parseInt(forecastWeatherBean.getTmp_max()),Integer.parseInt(forecastWeatherBean.getTmp_min()),0xFF,null);
//                                                            forecastWeathers.add(forecastWeather);
//                                                        }
//
//                                                    }
//                                                    Weather weatherBean = new Weather(weather.getData().getNowWeather().getWeather_type(),max,min,now,forecastWeathers);
//                                                    IbandApplication.getIntance().weather =weatherBean;
//                                                    Watch.getInstance().setWeather(weatherBean, new BleCallback() {
//                                                        @Override
//                                                        public void onSuccess(Object o) {
//
//                                                        }
//
//                                                        @Override
//                                                        public void onFailure(BleException exception) {
//
//                                                        }
//                                                    });
//                                                    weatherModel = new WeatherModel(IbandApplication.getIntance().country,basicBean.getAdmin_area(),IbandApplication.getIntance().city,basicBean.getCid());
//                                                    weatherModel.setWeatherInfo(""+weatherBean.getWeatherRegime(),""+weatherBean.getNowTemperature());
//                                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//                                                    weatherModel.setDay(df.format(new Date()));
//                                                    weatherModel.save();
//                                                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_WEATHER));
//                                                }else{
//                                                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_WEATHER));
//                                                }
//                                            }
//                                        });
//                                    }
//                                }else{
//                                    Log.d(TAG, "获取失败");
//                                }
//                            }
//                        });
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                    }
                }
            }
        };

        //声明mLocationOption对象
        AMapLocationClientOption mLocationOption = null;
        mlocationClient = new AMapLocationClient(getBaseContext());
//初始化定位参数
        mLocationOption = new AMapLocationClientOption();
//设置返回地址信息，默认为true
        mLocationOption.setNeedAddress(true);
//设置定位监听
        mlocationClient.setLocationListener(aMapLocationListener);
//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
//设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
// 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
// 在定位结束后，在合适的生命周期调用onDestroy()方法
// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
//启动定位
        mlocationClient.startLocation();

//        String ha = sHA1(getContext());
//        Log.i(TAG,"sHA1:"+ha);

    }

    private static boolean isNotificationListenerServiceEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(MainActivity.this);

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
        final String deviceName = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_BIND_NAME,"");

        //20180620
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        DeviceList filterDeviceList = new Gson().fromJson(strDeviceList,DeviceList.class);

        boolean isShow = false;

        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
//            if(resultBean.getDevice_id().trim().equals(deviceType.trim())){
            if(resultBean.getDevice_id().trim().equals(deviceType.trim())){
                if("0".equals(resultBean.getNeed_autoUpdate())){
                    isShow = false;
                }else if("1".equals(resultBean.getNeed_autoUpdate())){
                    if("0".equals(resultBean.getNeed_update())){
                        String firm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"");
                        if(!"".equals(firm)&&firm.compareTo(resultBean.getSupport_software())<0){
                            isShow = true;
                        }else{
                            isShow = false;
                        }
                    }else if("1".equals(resultBean.getNeed_update())){
                        isShow = true;
                    }
                }
            }
        }
        if(!isShow){
            return;
        }
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
                                    boolean isShow = true;
                                    if(SPUtil.get(mContext,AppGlobal.DATA_DEVICEUPDATE_DELAY_FILEURL,"").equals(fileUrl)){
                                        long date = (long)SPUtil.get(mContext,AppGlobal.DATA_DEVICEUPDATE_DELAY_DATE,0L);
                                        if(System.currentTimeMillis()<date){
                                            isShow = false;
                                        }
                                    }
                                    if(isShow) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new DeviceUpdate(mContext).show_delay(fileUrl);
                                            }
                                        });
                                    }
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

    private boolean isViewListHaveFregment(Fragment fragment) {
        if (viewList.size() > 0) {
            for (int i = 0; i < viewList.size(); i++) {
                Fragment fragment1 = viewList.get(i);
                if (fragment.equals(fragment1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeOtherFragment() {

        int size = viewList.size();
        vpModel.setCurrentItem(0);

        for (int i = size; i > 3;) {
            viewList.remove(i-1);//移除list元素建议调用remove(index)
            i = viewList.size();
        }
//        Iterator it = viewList.iterator();
//        int index=0;
//        while (it.hasNext()) {
//            Object o = it.next();
//            if (StepFragment.class.equals(o.getClass()) || SleepFragment.class.equals(o.getClass()) || HrFragment.class.equals(o.getClass())) {
//                continue;
//            } else {
//                viewList.remove(0);
//                break;
//            }
//        }
        viewAdapter.notifyDataSetChanged();
    }

    private StepFragment stepFragment = new StepFragment();
    private SleepFragment sleepFragment = new SleepFragment();
    private HrFragment hrFragment = new HrFragment();
    BpFragment bpFragment;
    BoFragment boFragment;
    EcgFragment ecgFragment;
    private MicrocirculationFragment microcirculationFragment;
    private void initViewPager() {
        if (bpFragment == null) {
            bpFragment = new BpFragment();
        }
        if (boFragment == null) {
            boFragment = new BoFragment();
        }
        if (ecgFragment == null) {
            ecgFragment = new EcgFragment();
        }
        if (microcirculationFragment == null) {
            microcirculationFragment = new MicrocirculationFragment();
        }

        if (!isViewListHaveFregment(stepFragment)) {
            viewList.add(stepFragment);
        }
        if (!isViewListHaveFregment(sleepFragment)) {
            viewList.add(sleepFragment);
        }
        if (!isViewListHaveFregment(hrFragment)) {
            viewList.add(hrFragment);
        }
//        viewList.add(bpFragment);
//        viewList.add(boFragment);
//        viewList.add(ecgFragment);
//        viewList.add(microcirculationFragment);

        if (viewAdapter == null) {
            viewAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    return viewList.get(position);
                }

                @Override
                public int getCount() {
//                    Log.i("MainActivity", "viewList.size():" + viewList.size());
                    return viewList.size();
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    super.destroyItem(container, position, object);
                }
            };
        }
        vpModel.setAdapter(viewAdapter);
        pivDots.setViewPager(vpModel);
        vpModel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
//                        prlRefresh.setEnabled(false);
//                        LogUtil.d(TAG, "onTouch() called with: ACTION_MOVE ");
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
//                        prlRefresh.setEnabled(true);
//                        LogUtil.d(TAG, "onTouch() called with: ACTION_CANCEL ");
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onAttachFragment(android.app.Fragment fragment) {
        super.onAttachFragment(fragment);
    }


    public void screenShot(){
        handler2.post(new Runnable() {
            @Override
            public void run() {
                View dView = getWindow().getDecorView();
                dView.setDrawingCacheEnabled(false);
                dView.destroyDrawingCache();
                dView.buildDrawingCache();
                Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
                if (bitmap != null) {
                    try {
                        // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//                        String sdCardPath = getBaseContext().getCacheDir().getPath();
                        // 图片文件路径
                        filePath = sdCardPath + File.separator + "share_screenshot_"+System.currentTimeMillis()+".png";
                        File file = new File(filePath);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
//                        if(file.exists()){
//                            file.delete();
//                        }
                        FileOutputStream os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                        os.close();
                        Message message = handler2.obtainMessage();
                        message.what = 1;
                        handler2.sendMessageDelayed(message,500);
                    } catch (Exception e) {
                    }
                }
            }
        });


    }

    private void showShare() {

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，微信、QQ和QQ空间等平台使用
//        oks.setTitle(getString(R.string.share));
//        oks.setTitle("分享");
        // titleUrl QQ和QQ空间跳转链接
//        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
//        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

//        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//        // 图片文件路径
//        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
        File file = new File(filePath);
        if(file.exists()){
            oks.setImagePath(filePath);//确保SDcard下面存在此张图片
            // url在微信、微博，Facebook等平台中使用
//        oks.setUrl("http://sharesdk.cn");
            // comment是我对这条分享的评论，仅在人人网使用
//        oks.setComment("我是测试评论文本");
            // 启动分享GUI

//            oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
//                @Override
//                public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
//                    if (platform.getName().equalsIgnoreCase(QQ.NAME)) {
//                        paramsToShare.setText(null);
//                        paramsToShare.setTitle(null);
//                        paramsToShare.setTitleUrl(null);
//
////                        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
////                        // 图片文件路径
////                        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
//                        paramsToShare.setImagePath(filePath);
//                    }
//                }
//            });
            oks.setCallback(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onCancel(Platform platform, int i) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }
            });
            oks.show(this);
        }

    }


    private static final String TAG = "MainActivity";
    @Override
    protected void initListener() {
        ivShare.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                screenShot();
//                showShare();
            }
        });




        tbSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, SettingActivity.class));
            }
        });

        vpModel.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("onPageScrolled","position:"+position);
                if (position > viewList.size()) {
                    return;
                }
                float alpha;
                int index = position;
                if (positionOffset <= 0.5) {
                    alpha = 1 - (positionOffset * 2);
                } else {
                    alpha = (positionOffset * 2) - 1;
                    index++;
                }
                rlTitle.setAlpha(alpha);
                if (index >= 0 && index <= 3) {
                    selectTitle(index);
                } else {
                    if (boFragment != null && boFragment.equals(viewList.get(index))) {
                        tbTitle.setText(getResources().getString(R.string.hint_view_bo));
                    }
                    if (bpFragment != null && bpFragment.equals(viewList.get(index))){
                        tbTitle.setText(getResources().getString(R.string.hint_view_hp));
                    }
                    if (ecgFragment != null && ecgFragment.equals(viewList.get(index))) {
                        tbTitle.setText(getResources().getString(R.string.hint_view_ecg));
                    }
                    if (microcirculationFragment != null && microcirculationFragment.equals(viewList.get(index))) {
                        tbTitle.setText(getResources().getString(R.string.hint_microcirculation));
//                        view.setBackgroundColor(Color.parseColor("#3949ab"));
                    }
                }


                Log.i("pagenum",String.valueOf(viewList.size()));
            }

            @Override
            public void onPageSelected(int position) {
                view.refreshDrawableState();
                Log.i("onPageSelected","position:"+position);
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
                    case 5:
                        if (microcirculationFragment != null && microcirculationFragment.equals(viewList.get(5))) {
                            tbTitle.setText(getResources().getString(R.string.hint_microcirculation));
                            view.setBackgroundColor(Color.parseColor("#3949ab"));
                        } else {
                            view.setBackgroundColor(Color.parseColor("#00897b"));
                        }
                        break;
//                    case 6:
//                        view.setBackgroundColor(Color.parseColor("#3949ab"));
//                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                Log.i("onPageScrollStateChanged","state:"+state);
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
                            boolean isSupply = (boolean) SPUtil.get(mContext, "isSupplyWeather", true);
//                            if (isSupply) {
//                                pushForecastWeatherToWatch();//推送天气信息到手环
//                            }
                            if (is_show_weather) {
                                pushForecastWeatherToWatch();//推送天气信息到手环
                            }
                            start5sTimer();//弥补同步显示错误，因为时间关系不得已采取的临时办法，根本解决问题需要优化同步数据那块
                        } else {
                            setHintState(AppGlobal.DEVICE_STATE_SYNC_NO);
                        }
                    }
                });
                LogUtil.d(TAG, "onResult() called with: isSuccess = [" + isSuccess + "]");
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
                LogUtil.d(TAG, "onProgress() called with: progress = [" + progress + "]");
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
                if (state != AppGlobal.DEVICE_STATE_CONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ibandApplication.service != null && ibandApplication.service.watch != null) {
                                setHintState(AppGlobal.DEVICE_STATE_CONNECTING);
                                ibandApplication.service.watch.closeBluetoothGatt(mac);
                                connectDevice();
                            }
                        }
                    });
                }else{
                    if (SyncData.getInstance().isRun()){
                        prlRefresh.setRefreshing(false);
                        return;
                    }
                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
                }
//                updateDeviceList();
            }
        });

        tbTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LogUtil.setLogOnOff(isLogOnOff);
                com.manridy.sdk.common.LogUtil.setLogOnOff(isLogOnOff);
                Log.d(TAG, "LOG日志开关 = [" + isLogOnOff + "]");
                isLogOnOff = !isLogOnOff;
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private boolean checkBindDevice(String mac) {
        if (mac == null ) { return true;
        }else if(mac.isEmpty()){
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
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void loadData() {
        super.loadData();
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
//        updateDeviceList();
        int state = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
        if (!Watch.getInstance().isBluetoothEnable()) {
            OpenBluetoothDialog();
        } else if (mac.isEmpty()) {
            showFloatView(getString(R.string.hint_device_unbind), getString(R.string.hint_bind));
        } else if (state == AppGlobal.DEVICE_STATE_UNCONNECT) {
            Log.i(TAG,"AppGlobal.DEVICE_STATE_UNCONNECT");
            if (ibandApplication.service != null) {
                ibandApplication.service.initConnect(false);
                setHintState(AppGlobal.DEVICE_STATE_CONNECTING);
            }
            return;
        } else if (state == AppGlobal.DEVICE_STATE_CONNECTED) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
        }
        setHintState(state);
    }

    private void updateDeviceList(){
        if ( noNetWorkDlg!= null) {
            noNetWorkDlg.dismiss();
        }
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        if("".equals(strDeviceList)) {
            strDeviceList = DeviceListDataSpare.strDeviceList;
            SPUtil.put(ibandApplication, AppGlobal.DATA_DEVICE_LIST, strDeviceList);
        }else{
            try{
                DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            }catch (Exception e){
                strDeviceList = DeviceListDataSpare.strDeviceList;
                SPUtil.put(ibandApplication, AppGlobal.DATA_DEVICE_LIST, strDeviceList);
            }
        }
        handler2.post(updatePageRunnable);
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
                    handler2.post(updatePageRunnable);
                    handler2.sendMessageDelayed(handler2.obtainMessage(5),200);
                }else{
                    EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_LOAD_DEVICE_LIST));
                    handler2.post(updatePageRunnable);
                    Message message = handler2.obtainMessage(3);
                    handler2.sendMessage(message);
                    handler2.sendMessageDelayed(handler2.obtainMessage(5),200);
                }
            }
        });
    }



    private void selectTitle(int position) {
        switch (position) {
            case 0:
                tbTitle.setText(getResources().getString(R.string.hint_view_step));
                break;
            case 1:
                tbTitle.setText(getResources().getString(R.string.hint_view_sleep));
                break;
            case 2:
                tbTitle.setText(getResources().getString(R.string.hint_view_hr));
                break;
            case 3:
                String deviceType = (String) SPUtil.get(mContext,AppGlobal.DATA_FIRMWARE_TYPE,"");
                if(deviceType.equals("")||isShowBp||!isDeviceIdInService){
                    tbTitle.setText(getString(R.string.hint_view_hp));
                }else if(!isShowBp&&isShowBo){
                    tbTitle.setText(getString(R.string.hint_view_bo));
                }else if(!isShowBp&&!isShowBo){
                    tbTitle.setText(getString(R.string.hint_view_ecg));
                }
//                if(isShowBp||deviceType.equals("")){
//                    tbTitle.setText(R.string.hint_view_hp);
//                }else{
//                    tbTitle.setText(R.string.hint_view_bo);
//                }
                break;
            case 4:
                String deviceType1 = (String) SPUtil.get(mContext,AppGlobal.DATA_FIRMWARE_TYPE,"");
                if(isShowBo||deviceType1.equals("")||!isDeviceIdInService){
                    tbTitle.setText(getResources().getString(R.string.hint_view_bo));
                }else{
                    tbTitle.setText(getResources().getString(R.string.hint_view_ecg));
                }
                break;
            case 5:
                tbTitle.setText(getResources().getString(R.string.hint_view_ecg));
                break;
//            case 6:
//                tbTitle.setText(getResources().getString(R.string.hint_microcirculation));
//                break;
        }
    }

    private void setHintState(int state){
        String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        if (bindMac == null || bindMac.isEmpty()) {
            removeOtherFragment();
            SPUtil.remove(mContext,"isShowMicro");
            tbSync.setText(getResources().getString(R.string.hint_un_bind));
            return;
        }else if (!Watch.getInstance().isBluetoothEnable()){
            tbSync.setText(getResources().getString(R.string.hint_bluetooth_close));
            return;
        }
        switch (state) {
            case AppGlobal.DEVICE_STATE_UNCONNECT:
                tbSync.setText(getResources().getString(R.string.hint_un_connect));
                break;
            case AppGlobal.DEVICE_STATE_CONNECTED:
                tbSync.setText(getResources().getString(R.string.hint_connected));
                break;
            case AppGlobal.DEVICE_STATE_CONNECTING:
                tbSync.setText(getResources().getString(R.string.hint_connecting));
                break;
            case AppGlobal.DEVICE_STATE_CONNECT_FAIL:
                tbSync.setText(getResources().getString(R.string.hint_connect_fail));
                break;
            case AppGlobal.DEVICE_STATE_CONNECT_SUCCESS:
                tbSync.setText(getResources().getString(R.string.hint_connect_success));
                break;
            case AppGlobal.DEVICE_STATE_UNFIND:
                tbSync.setText(getResources().getString(R.string.hint_un_find));
                break;
            case AppGlobal.DEVICE_STATE_SYNC_OK:
                tbSync.setText(getResources().getString(R.string.hint_sync_ok));
                handler2.post(updatePageRunnable);
                break;
            case AppGlobal.DEVICE_STATE_SYNC_NO:
                tbSync.setText(getResources().getString(R.string.hint_sync_no));
                break;
            case AppGlobal.DEVICE_STATE_BLUETOOTH_DISENABLE:
                tbSync.setText(getResources().getString(R.string.hint_bluetooth_close));
                break;
            case AppGlobal.DEVICE_STATE_BLUETOOTH_ENABLEING:
                tbSync.setText(getResources().getString(R.string.hint_bluetooth_opening));
                break;
//            case AppGlobal.DEVICE_STATE_UNBIND://取消绑定
////                initViewPager();
//                break;
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
            LogUtil.d(TAG, "STATE_DEVICE_DISCONNECT() called with: bytes = [" + bytes[0] + "]");
            if (bindMac.isEmpty()) {
                tbSync.setText(R.string.hint_device_unbind);
            }else {
                tbSync.setText(R.string.hint_un_connect);
            }
            if (isLostOn && !bindMac.isEmpty() && (0 == bytes[0] || 19 == bytes[0]|| 8==bytes[0])) {
                String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME,"");
                int time = 20;
                boolean isOn = true;
                String devices[]={"F07","F07A","F10","F10A"};
                for(int i=0;i<devices.length;i++){
                    if(deviceName!=null&&devices[i].equals(deviceName.trim())){
                        time = 120;
                    }
                }
                String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
                String deviceIDs[] = {"8077","8078","8079","8080","8092"};
                for(int i = 0;i<deviceIDs.length;i++){
                    if(deviceType!=null&&deviceIDs[i].equals(deviceType.trim())){
                        isOn = false;
                    }
                }
                if(isOn){
                    handler.sendEmptyMessageDelayed(0, time * 1000);
                }
                LogUtil.d(TAG, "onLostThread() called with: event = [STATE_DEVICE_DISCONNECT]");
            }
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_CONNECTING) {
//            boolean isLostOn = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_LOST, false);
//            String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
//            if (isLostOn && !bindMac.isEmpty()) {
//                handler.sendEmptyMessageDelayed(0, 20 * 1000);
//                LogUtil.d(TAG, "onLostThread() called with: event = [STATE_DEVICE_CONNECTING]");
//            }
            //蓝牙重连
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
                    ibandApplication.service.initConnect(true);
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
            Log.i(TAG,"EventGlobal.ACTION_DEVICE_CONNECT");
            ibandApplication.service.initConnect(false);
        } else if (event.getWhat() == EventGlobal.ACTION_HIDE_SIMPVIEW){
            hideFloatView();
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNBIND){
            setHintState(AppGlobal.DEVICE_STATE_UNBIND);
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_SEARCHING){
            tbSync.setText(getString(R.string.hint_device_searching));
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNFIND) {
            tbSync.setText(getString(R.string.hint_un_find));
        } else if (event.getWhat() == EventGlobal.STATE_CHANGE_NETWOOK_ON){
            Message message = handler2.obtainMessage(6);
            handler2.sendMessage(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_SYNC_HISTORY) {
            SyncData.getInstance().sync();

        }else  if (event.getWhat() == EventGlobal.ACTION_LOAD_DEVICE_LIST) {


        } else if (event.getWhat() == EventGlobal.DATA_LOAD_WEATHER) {
//            ibandApplication.recordingLoginNumFunc();//和天气获取一样一天一次
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
                if (IbandApplication.getIntance().service != null){
                    if (IbandApplication.getIntance().service.watch != null) {
                        IbandApplication.getIntance().service.watch.BluetoothEnable(AppManage.getInstance().currentActivity());
                    }
                }

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
        if (ibandApplication != null && ibandApplication.service != null && ibandApplication.service.watch != null) {
//            ibandApplication.service.watch.closeCurBluetoothGatt();
            String bindMac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
            ibandApplication.service.watch.closeBluetoothGatt(bindMac);
        }
    }

    @Override
    public void onBackPressed() {
        if ("huawei".equalsIgnoreCase(Watch.brand)) {
//            super.onBackPressed();
//            BluetoothLeManager.IS_RENECT = false;
//            ActivityManager manager = (ActivityManager)mContext.getSystemService(ACTIVITY_SERVICE); //获取应用程序管理器
//            manager.killBackgroundProcesses(getPackageName()); //强制结束当前应用程序
//            ibandApplication.stopBleService();
            moveTaskToBack(true);//Activity活动于后台
        }
        else {
            moveTaskToBack(true);//Activity活动于后台
        }
    }


    /**
     *  多国语言切换华为手机支持不稳定，后面有时间更改
     * */
/*
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences preferences = newBase.getSharedPreferences("language", Context.MODE_PRIVATE);
        String selectedLanguage = preferences.getString("language", "");
        super.attachBaseContext(LanguageUtil.attachBaseContext(newBase, selectedLanguage));
    }
*/


    private void pushForecastWeatherToWatch() {
        /*if (addressModel == null) {
            return;
        }
        int max = 0xFF;
        int min = 0xFF;
        int now = 0xFF;
        if (addressModel.getForecastWeather().get(0).getTmp_now() != null) {
            now = 0xff;
        }
        if (addressModel.getForecastWeather().get(0).getTmp_max() != null) {
            max = Integer.parseInt(addressModel.getForecastWeather().get(0).getTmp_max());
        }
        if (addressModel.getForecastWeather().get(0).getTmp_min() != null) {
            min = Integer.parseInt(addressModel.getForecastWeather().get(0).getTmp_min());
        }
        LinkedList<Weather> forecastWeathers = new LinkedList<>();
        Weather forecastWeather;
        if (addressModel.getForecastWeather().size() == 3) {
            for (int i = 1; i < 3; i++) {
                com.manridy.iband.bean.Weather.DataBean.ForecastWeatherBean forecastWeatherBean = new com.manridy.iband.bean.Weather.DataBean.ForecastWeatherBean();
                forecastWeatherBean.setWeather_type(addressModel.getForecastWeather().get(i).getWeater_type());
                forecastWeatherBean.setTmp_max(addressModel.getForecastWeather().get(i).getTmp_max());
                forecastWeatherBean.setTmp_min(addressModel.getForecastWeather().get(i).getTmp_min());
                forecastWeather = new Weather(forecastWeatherBean.getWeather_type(), Integer.parseInt(forecastWeatherBean.getTmp_max()), Integer.parseInt(forecastWeatherBean.getTmp_min()), 0xFF, null);
                forecastWeathers.add(forecastWeather);
            }
        }*/

        WeatherModel currentDayWeather = DataSupport.findFirst(WeatherModel.class);
        if (currentDayWeather == null) return;
        String weatherRegime = currentDayWeather.getWeatherRegime();
        if (weatherRegime == null || weatherRegime == "") return;
        int s_tempType = Integer.parseInt(weatherRegime);
        String s_tempMax = currentDayWeather.getMaxTemperature();
        if (s_tempMax == null || s_tempMax == "") return;
        String s_tempMin = currentDayWeather.getMinTemperature();
        if (s_tempMin == null || s_tempMin == "") return;
        int max = Integer.parseInt(s_tempMax);
        int min = Integer.parseInt(s_tempMin);
        int now = 0xff;
        LinkedList<Weather> forecastWeathers = new LinkedList<>();
        List<WeatherModel> weatherModelList = DataSupport.findAll(WeatherModel.class, 2, 3);
        for (int i = 0; i < weatherModelList.size();i++) {
            WeatherModel weatherModel = weatherModelList.get(i);
            String s_type = weatherModel.getWeatherRegime();
            int i_type = Integer.parseInt(s_type);
            int i_tempMax = Integer.parseInt(weatherModel.getMaxTemperature());
            int i_tempMin = Integer.parseInt(weatherModel.getMinTemperature());
            Weather weather = new Weather(i_type, i_tempMax, i_tempMin, 0xff, null);
            forecastWeathers.add(weather);
        }
        Weather weatherBean = new Weather(s_tempType, max, min, now, forecastWeathers);
        IbandApplication.getIntance().weather = weatherBean;
        Watch.getInstance().setWeather(weatherBean, new BleCallback() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "onSuccess: 推送天气信息成功");
            }

            @Override
            public void onFailure(BleException exception) {
                Log.d(TAG, "onSuccess: 推送天气信息失败");
            }
        });
    }

    private String getWeatherType(AddressModel1 addressModel, int forecastDay) {
        int weater_type=0;
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.DailyForecastBean dailyForecastBean;
        if (forecastDay >= heWeather6Bean.getDaily_forecast().size()) {
            forecastDay = heWeather6Bean.getDaily_forecast().size() - 1;
        }
        dailyForecastBean = heWeather6Bean.getDaily_forecast().get(forecastDay);
        String cond_code_d = dailyForecastBean.getCond_code_d();
        int code_d = Integer.parseInt(cond_code_d);
        if (code_d == 100 || code_d == 900 || code_d == 901 || code_d == 999) { //晴
            weater_type = 0;
        } else if ((code_d >= 101 && code_d <= 104) || (code_d >= 200 && code_d <= 213)) {//阴
            weater_type = 1;
        } else if ((code_d >= 300 && code_d <= 318) || code_d == 399) { //雨
            weater_type = 2;
        } else if ((code_d >= 400 && code_d <= 410) || code_d == 499) { //雪
            weater_type = 3;
        } else if ((code_d >= 500 && code_d <= 502) || (code_d >= 509 && code_d <= 515)) {//雾霾
            weater_type = 4;
        } else if (code_d == 503 || code_d == 504 || code_d == 507 || code_d == 508) {//沙尘
            weater_type = 5;
        }
        return Integer.toString(weater_type);
    }

    private String getCountry(AddressModel1 addressModel) {
        String cnty;
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.BasicBean basicBean = heWeather6Bean.getBasic();
        cnty = basicBean.getCnty();
        return cnty;
    }
    private String getCity(AddressModel1 addressModel) {
        String city;
        AddressModel1.HeWeather6Bean heWeather6Bean;
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.BasicBean basicBean = heWeather6Bean.getBasic();
//        city = basicBean.getParent_city();
        city = basicBean.getLocation();
        return city;
    }

    private String getTempNow(AddressModel1 addressModel, int forecastDay) {
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.DailyForecastBean dailyForecastBean;
        if (forecastDay >= heWeather6Bean.getDaily_forecast().size()) {
            forecastDay = heWeather6Bean.getDaily_forecast().size() - 1;
        }
        dailyForecastBean = heWeather6Bean.getDaily_forecast().get(forecastDay);
        String tmp_max = dailyForecastBean.getTmp_max();
        String tmp_min = dailyForecastBean.getTmp_min();
        String tmpNow = tmp_max + "°-" + tmp_min;
        return tmpNow;
    }

    private String getMaxTemp(AddressModel1 addressModel, int forecastDay) {
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.DailyForecastBean dailyForecastBean;
        if (forecastDay >= heWeather6Bean.getDaily_forecast().size()) {
            forecastDay = heWeather6Bean.getDaily_forecast().size() - 1;
        }
        dailyForecastBean = heWeather6Bean.getDaily_forecast().get(forecastDay);
        String tmp_max = dailyForecastBean.getTmp_max();
        return tmp_max;
    }
    private String getMinTemp(AddressModel1 addressModel, int forecastDay) {
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.DailyForecastBean dailyForecastBean;
        if (forecastDay >= heWeather6Bean.getDaily_forecast().size()) {
            forecastDay = heWeather6Bean.getDaily_forecast().size() - 1;
        }
        dailyForecastBean = heWeather6Bean.getDaily_forecast().get(forecastDay);
        String tmp_min = dailyForecastBean.getTmp_min();
        return tmp_min;
    }


    private String getDate(AddressModel1 addressModel, int forecastDay) {
        if (addressModel.getHeWeather6().size() == 0) {
            return "";
        }
        AddressModel1.HeWeather6Bean heWeather6Bean;
        heWeather6Bean = addressModel.getHeWeather6().get(0);
        AddressModel1.HeWeather6Bean.DailyForecastBean dailyForecastBean;
        if (forecastDay >= heWeather6Bean.getDaily_forecast().size()) {
            forecastDay = heWeather6Bean.getDaily_forecast().size() - 1;
        }
        dailyForecastBean = heWeather6Bean.getDaily_forecast().get(forecastDay);
        String date = dailyForecastBean.getDate();
        return date;
    }

    private void saveForecastWeatherInfoToDatabase(AddressModel1 addressModel) {
        List<WeatherModel> modelList = DataSupport.findAll(WeatherModel.class);
        if (modelList == null) {
            modelList = new ArrayList<>();
        }
        for (int i = 0; i < 3; i++) {//保存当天和未来3天的天气状况
            WeatherModel weatherModel = DataSupport.find(WeatherModel.class, i + 1);
            if (weatherModel == null) {
                weatherModel = new WeatherModel();
            }
            weatherModel.setWeatherRegime(getWeatherType(addressModel,i));
            weatherModel.setCountry(getCountry(addressModel));
            weatherModel.setCity(getCity(addressModel));
            weatherModel.setMinTemperature(getMinTemp(addressModel,i));
            weatherModel.setMaxTemperature(getMaxTemp(addressModel,i));
            weatherModel.setNowTemperature(getTempNow(addressModel,i));
//                                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//                                                    weatherModel.setDay(df.format(new Date()));
            weatherModel.setDay(getDate(addressModel, i));
            modelList.add(weatherModel);
//                                                    weatherModel.update(i + 1);
        }
//                                                weatherModel.save();
        DataSupport.saveAll(modelList);
    }

    /**
     *  启动一个5s的定时器刷新同步完成显示，
     *  填补同步数据错位漏洞，鬼知道同步的最后哪里来了一条同步中信号导致同步显示错误
     */
    private Timer timer_5s;
    private void start5sTimer() {
        if (timer_5s == null) {
            timer_5s = new Timer();
        }
        timer_5s.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setHintState(AppGlobal.DEVICE_STATE_SYNC_OK);
                    }
                });
            }
        }, 5000);
    }


}
