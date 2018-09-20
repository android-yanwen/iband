package com.manridy.iband.view.main;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.SyncData;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DeviceUpdate;
import com.manridy.iband.common.DomXmlParse;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.R;
import com.manridy.iband.adapter.DeviceAdapter;
import com.manridy.iband.common.OnItemClickListener;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.model.BoFragment;
import com.manridy.iband.view.model.BpFragment;
import com.manridy.sdk.BluetoothLeDevice;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleConnectCallback;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.scan.TimeScanCallback;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 绑定设备
 * Created by jarLiao on 17/5/11.
 */

public class DeviceActivity extends BaseActionActivity {

    @BindView(R.id.tv_bind_state)
    TextView tvBindState;
    @BindView(R.id.iv_qrcode)
    ImageView ivQrcode;
    @BindView(R.id.tv_bind_name)
    TextView tvBindName;
    @BindView(R.id.rl_bind_state)
    RelativeLayout rlBindState;
    @BindView(R.id.rv_device)
    RecyclerView rvDevice;
    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.bt_bind)
    Button btBind;
    @BindView(R.id.rl_image)
    ImageView rlImage;

    private DeviceAdapter mDeviceAdapter;
    private List<DeviceAdapter.DeviceModel> mDeviceList = new ArrayList<>();
    private String bindName;//绑定设备名称
    private int curPosition = -1;//选中设备序号
    private String strDeviceList;
    private boolean isDebug;
    private ArrayList<String> deviceFilters = new ArrayList<>();
    public static String identifier = "iband";
    public String localFilters = "[\"HB\",\"F07Lite\",\"CB606\",\"L8\",\"HM\",\"M7\",\"CB606\",\"R11\",\"HB-M1\",\"N67\",\"watch\",\"F07\",\"F1Pro\",\"HB08\",\"Smart\",\"K2\",\"N68\",\"Smart B\",\"N109\",\"Smart-2\",\"TF1\"]";
    DeviceList filterDeviceList;
    private String url = "http://39.108.92.15:12345";


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_device);
        ButterKnife.bind(this);
        setTitleAndMenu(getString(R.string.hint_device),getString(R.string.hint_search));//初始化titlebar
    }

    @Override
    protected void initVariables() {
        registerEventBus();//注册EventBus
        initRecyclerView();//初始化搜索设备列表
        initBindView();//初始化绑定视图
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        String filterStr = (String) SPUtil.get(ibandApplication,AppGlobal.DATA_DEVICE_FILTER,localFilters);
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        deviceFilters = new Gson().fromJson(filterStr,type);
        filterDeviceList = new Gson().fromJson(strDeviceList,DeviceList.class);
    }

    //初始化搜索设备列表
    private void initRecyclerView() {
        mDeviceAdapter = new DeviceAdapter(mDeviceList);
        rvDevice.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        ((SimpleItemAnimator) rvDevice.getItemAnimator()).setSupportsChangeAnimations(false);
        rvDevice.setAdapter(mDeviceAdapter);
    }

    //初始化绑定视图
    private void initBindView() {
        bindName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
        if (null != bindName && !bindName.isEmpty()) {//判断是否绑定
            showBindView();
        }
    }

    @Override
    protected void initListener() {
        mDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {//搜索设备列表单击选中
                boolean isSelect = false;//是否选中变量
                for (int i = 0; i < mDeviceList.size(); i++) {//遍历数据
                    DeviceAdapter.DeviceModel deviceModel = mDeviceList.get(i);//得到点击的数据
                    if (i == position) {//如果是点击的数据
                        deviceModel.isSelect = !deviceModel.isSelect;//点击item取相反值
                        isSelect = deviceModel.isSelect;//接受是否选中
                    } else {//如果不是点击数据
                        deviceModel.isSelect = false;//设置不选中
                    }
                }
                mDeviceAdapter.notifyDataSetChanged();//更新列表数据显示
                btBind.setVisibility(isSelect ? View.VISIBLE : View.GONE);//绑定按钮根据是否选中显示或隐藏
                curPosition = isSelect ? position : -1;//记录当前选中序号，未选中初始化-1
            }
        });

        findViewById(R.id.tb_title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isDebug = true;
                showToast("不筛选设备模式开启");
                return true;
            }
        });

        SyncAlert.getInstance(mContext).setSyncAlertListener(new SyncAlert.OnSyncAlertListener() {
            @Override
            public void onResult(final boolean isSuccess) {
//                dismissProgress();
                SyncData.getInstance().setRun(false);
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(isSuccess?getString(R.string.hint_sync_success)
                                :getString(R.string.hint_sync_fail));
                    }
                });
//                String firmVersion = (String) SPUtil.get(mContext,AppGlobal.DATA_FIRMWARE_VERSION,"1.0.0");
//                boolean isLostDisturb = getLostDisturb(bindName,firmVersion ,filterDeviceList);
//                SPUtil.put(mContext,AppGlobal.DATA_ALERT_LOST_NAP,isLostDisturb);
            }
        });
    }

    @Override
    protected void loadData() {
        super.loadData();
        scanDevice(false);

    }

    @OnClick({R.id.iv_qrcode, R.id.iv_refresh, R.id.bt_bind, R.id.tb_menu})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_qrcode://二维码点击
                ibandApplication.service.watch.startScan(new TimeScanCallback(5000,null) {
                    @Override
                    public void onScanEnd() {

                    }

                    @Override
                    public void onFilterLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

                    }
                });
                Intent intent = new Intent(DeviceActivity.this, QrActivity.class);
                startActivityForResult(intent,10000);
                break;
            case R.id.iv_refresh://刷新按钮点击
                scanDevice(true);
                break;
            case R.id.tb_menu://刷新按钮点击
                scanDevice(true);
                break;
            case R.id.bt_bind://绑定按钮点击
                if (null == bindName || bindName.isEmpty()) {//判断是绑定还是解除绑定
                    SyncData.getInstance().setRun(true);//正在绑定关闭同步数据流程,同步设置后开启
                    bindDevice(null);//绑定设备
                } else {
                    unBindDevice();//解绑设备
                }
                break;
        }
    }

    //绑定设备
    private void bindDevice(BluetoothDevice device) {
        BluetoothDevice bindDevice = null;//绑定设备蓝牙对象
        if (curPosition != -1  && mDeviceList.size()>0){//选中不为-1
            bindDevice = mDeviceList.get(curPosition).leDevice;//取出选中对象赋值
        }
        if (device != null) {//传入蓝牙对象不为空
            bindDevice = device;//赋值
        }
        if (bindDevice == null) {
            return;//设备蓝牙对象不为空
        }
        showProgress(getString(R.string.hint_device_binding));
        bindName = bindDevice.getName();
        final String mac = bindDevice.getAddress();
        Watch.getInstance().connect(bindDevice, true, new BleConnectCallback() {
            @Override
            public void onConnectSuccess() {
                dismissProgress();//取消弹窗，保存设备名称和地址
                BluetoothLeDevice leDevice = Watch.getInstance().getBluetoothLeDevice(mac);
                SPUtil.put(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, mac);
                if (leDevice != null) {
                    bindName = leDevice.getmBluetoothGatt().getDevice().getName();
                }
                SPUtil.put(mContext, AppGlobal.DATA_DEVICE_BIND_NAME,bindName == null ? "UNKONW":bindName);
                String deviceImgRes = getDeviceImgRes(bindName,filterDeviceList);
                SPUtil.put(mContext,AppGlobal.DATA_DEVICE_BIND_IMG,deviceImgRes);
                eventSend(EventGlobal.STATE_DEVICE_BIND);//发送绑定成功广播
            }

            @Override
            public void onConnectFailure(BleException exception) {
                dismissProgress();
                Watch.getInstance().closeBluetoothGatt(mac);
                curPosition = -1;
                bindName = "";
                eventSend(EventGlobal.STATE_DEVICE_BIND_FAIL);
            }
        });
    }

    //解绑设备
    private void unBindDevice() {
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        ibandApplication.service.watch.disconnect(mac, new BleCallback() {
            @Override
            public void onSuccess(Object o) {
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
        SPUtil.remove(mContext, AppGlobal.DATA_DEVICE_BIND_NAME);
        SPUtil.remove(mContext, AppGlobal.DATA_DEVICE_BIND_MAC);
        SPUtil.remove(mContext, AppGlobal.DATA_DEVICE_BIND_IMG);
        SPUtil.remove(mContext, AppGlobal.DATA_FIRMWARE_TYPE);
        SPUtil.remove(mContext, AppGlobal.DATA_FIRMWARE_VERSION);
        SPUtil.remove(mContext, AppGlobal.DATA_TIMING_HR);
        SPUtil.remove(mContext, AppGlobal.DATA_TIMING_HR_SPACE);

        curPosition = -1;
        bindName = "";
        eventSend(EventGlobal.STATE_DEVICE_UNBIND);
    }

    //显示已绑定视图
    private void showBindView() {
        rlImage.setImageResource(R.mipmap.device_connect);
        tvBindState.setText(R.string.hint_device_binded);
        tvBindName.setText(bindName);
        btBind.setText(R.string.hint_deivce_un_bind);
        ivRefresh.setVisibility(View.GONE);
        ivQrcode.setVisibility(View.GONE);
        tvBindName.setVisibility(View.VISIBLE);
        btBind.setVisibility(View.VISIBLE);

    }

    //显示未绑定视图
    private void showUnBindView() {
        rlImage.setImageResource(R.mipmap.device_disconnect);
        tvBindState.setText(R.string.hint_device_unbind);
        ivQrcode.setVisibility(View.VISIBLE);
        ivRefresh.setVisibility(View.VISIBLE);
        tvBindName.setVisibility(View.GONE);
        btBind.setText(R.string.hint_device_bind);
        btBind.setVisibility(View.GONE);
    }

    //扫描设备
    private void scanDevice(boolean isCheckBind) {
        if (!ibandApplication.service.watch.isBluetoothEnable()) {
            ibandApplication.service.watch.BluetoothEnable(mContext);
            return;
        }
        if (null != bindName && !bindName.isEmpty()) {
            if (isCheckBind) {
                showToast(getString(R.string.hint_alert_bind));
            }
            return;
        }
        if (ibandApplication.service.watch.isScaning()) {
            showToast(getString(R.string.hint_device_searching));
            return;
        }
//        showProgress(getString(R.string.hint_device_searching), new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                ibandApplication.service.watch.stopScan(mTimeScanCallback);
//            }
//        });
        showToast(getString(R.string.hint_device_searching));
        ivRefresh.setVisibility(View.GONE);
        mDeviceList.clear();
        mDeviceAdapter.notifyDataSetChanged();
        dataIndex = 0;
        ibandApplication.service.watch.startScan(mTimeScanCallback);
    }

    //扫描设备回调
    TimeScanCallback mTimeScanCallback = new TimeScanCallback(5000, null) {
        @Override
        public void onScanEnd() {
            showToast(getString(R.string.hint_searched));
        }

        @Override
        public void onFilterLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            LogUtil.d(TAG, "onFilterLeScan() called with: device = [" + device.getName() + "], rssi = [" + rssi + "], mac = [" + device.getAddress() + "]");
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_DEVICE,device));
        }
    };

    /**
     * 筛选过滤设备
     * @param deviceName
     * @return
     */
    private boolean checkFilter(String deviceName, ArrayList<String> filterList) {
        for (String filter : filterList) {//本地设备数据判断
            if (deviceName.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    private String getDeviceImgRes(String deviceName,DeviceList filterDeviceList){
        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
            if (resultBean.getDevice_name().equals(deviceName)) {
                return resultBean.getImageName() == null ? "unknown" : resultBean.getImageName();
            }
        }
        return "unknown";
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event){
        if (event.getWhat() == EventGlobal.STATE_DEVICE_BIND) {
            //绑定成功显示已绑定视图，清空列表，弹出提示
            showBindView();
            mDeviceList.clear();
            dataIndex = 0;
            mDeviceAdapter.notifyDataSetChanged();
            showToast(getString(R.string.hint_bind_success));
            showToast(getString(R.string.hint_sync_data));
            tvBindName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SyncAlert.getInstance(mContext).sync();
                    SyncAlert.getInstance(mContext).isGetCallbackSetTimingHrTest = true;
//                    SyncAlert.getInstance(mContext).setTimingHrTest();

                }
            },1000);
//            handler.post(SetTimingHrTest);

            ibandApplication.isNeedRefresh = true;
//            HttpService.getInstance().getDeviceList(new OnResultCallBack() {
//                @Override
//                public void onResult(boolean result, Object o) {
//                    if (result) {
//                        String strDeviceList = o.toString();
//                        //解析服务器设备列表数据
//                        SPUtil.put(ibandApplication,AppGlobal.DATA_DEVICE_LIST, strDeviceList);
//                        DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
//                        //筛选iband设备数据
//                        ArrayList<String> nameList = new ArrayList<>();
//                        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
//                            if (resultBean.getIdentifier().equals("iband")) {
//                                nameList.add(resultBean.getDevice_name());
//                            }
//                        }
//                        String str =new Gson().toJson(nameList);
//                        SPUtil.put(ibandApplication,AppGlobal.DATA_DEVICE_FILTER,str);
//                        handler2.sendMessage(handler2.obtainMessage(4));
//                    }else{
//                        Message message = handler2.obtainMessage(3);
//                        handler2.sendMessage(message);
//                    }
//                }
//            });

        }else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNBIND) {
            //解绑成功显示未绑定视图，弹出提示
            showUnBindView();
            ibandApplication.service.stopNotification();
            showToast(getString(R.string.hint_un_bind_success));
        }else if (event.getWhat() == EventGlobal.STATE_DEVICE_BIND_FAIL){
            //绑定失败清空列表，弹出提示
            mDeviceList.clear();
            dataIndex = 0;
            mDeviceAdapter.notifyDataSetChanged();
            showUnBindView();
            showToast(getString(R.string.hint_un_bind_fail));
        }else if (event.getWhat() == EventGlobal.REFRESH_VIEW_DEVICE){
            BluetoothDevice device = (BluetoothDevice) event.getObject();
            String deviceName = device.getName();
            if ((deviceName != null && checkFilter(deviceName,deviceFilters))||isDebug) {
                mDeviceList.add(dataIndex,new DeviceAdapter.DeviceModel(device,deviceName));
                Log.d(TAG, "onEventMainThread() called with: event = [" + dataIndex + "]");
                mDeviceAdapter.notifyItemInserted(dataIndex);
                dataIndex++;
            }
        }
    }

    private int dataIndex = 0;

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventBackroundThread(EventMessage event){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //二维码扫描结果，取出返回字符串，校验是否蓝牙地址，取得蓝牙设备对象，调用绑定设备方法
        if (requestCode == 10000){
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    if (!CheckUtil.checkBluetoothAddress(result)) {
                        showToast(getString(R.string.error_mac));
                        return;
                    }
                    BluetoothDevice device = ibandApplication.service.watch.getDevice(result);
                    SyncData.getInstance().setRun(true);//正在绑定关闭同步数据流程,同步设置后开启
                    bindDevice(device);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {

                }
            }
        }
    }

//    boolean isGetCallbackSetTimingHrTest = false;
//
//    Runnable SetTimingHrTest = new Runnable() {
//        @Override
//        public void run() {
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if(!isGetCallbackSetTimingHrTest){
//                        showToast( "设置心率定时测量失败，请手动设置！");
//                        Log.i(TAG,"SetTimingHrTest:timeout");
//                    }
//                }
//            },10*1000 );
//            Log.i(TAG,"SetTimingHrTest:sendCmd");
//            ibandApplication.service.watch.sendCmd(BleCmd.setTimingHrTest(true, 30), new BleCallback() {
//                @Override
//                public void onSuccess(Object o) {
//                    isGetCallbackSetTimingHrTest = true;
//                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,true);
//                    SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,30);
//                    Log.i(TAG,"SetTimingHrTest:onSuccess");
//                }
//
//                @Override
//                public void onFailure(BleException exception) {
//                    isGetCallbackSetTimingHrTest = true;
//                    showToast( "设置心率定时测量失败，请手动设置！");
//                    Log.i(TAG,"SetTimingHrTest:onFailure");
//                }
//            });
//        }
//    };

    Handler handler = new Handler(){};

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }

    /**
     * 当判断当前手机没有网络时选择是否打开网络设置
     * @param context
     */
    public static void showNoNetWorkDlg(final Context context) {
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
        }).setNegativeButton(R.string.hint_cancel, null).show();
    }

    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 3:
                    showNoNetWorkDlg(DeviceActivity.this);
                    break;
                case 4:
                    initDeviceUpdate();
                    break;
            }
        }
    };


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


}
