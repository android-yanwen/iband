package com.manridy.iband.view;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.SyncData;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.R;
import com.manridy.iband.adapter.DeviceAdapter;
import com.manridy.iband.common.OnItemClickListener;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.BluetoothLeDevice;
import com.manridy.sdk.Watch;
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
                dismissProgress();
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
                SPUtil.put(mContext,AppGlobal.DATA_DEVICE_BIND_IMG,getDeviceImgRes(bindName,filterDeviceList));
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
        showProgress(getString(R.string.hint_device_searching), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ibandApplication.service.watch.stopScan(mTimeScanCallback);
            }
        });
        ivRefresh.setVisibility(View.GONE);
        mDeviceList.clear();
        ibandApplication.service.watch.startScan(mTimeScanCallback);
    }

    //扫描设备回调
    TimeScanCallback mTimeScanCallback = new TimeScanCallback(5000, null) {
        @Override
        public void onScanEnd() {
            dismissProgress();
        }

        @Override
        public void onFilterLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            Log.d(TAG, "onFilterLeScan() called with: device = [" + device.getName() + "], rssi = [" + rssi + "], mac = [" + device.getAddress() + "]");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String deviceName = device.getName();
//                    Log.d(TAG, "mTimeScanCallback() deviceName ==== "+deviceName==null?"null":deviceName);
                    if ((deviceName != null && checkFilter(deviceName,deviceFilters))||isDebug) {
                        mDeviceList.add(new DeviceAdapter.DeviceModel(device, rssi, scanRecord));
                        mDeviceAdapter.notifyDataSetChanged();
                    }
                }
            });

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
            mDeviceAdapter.notifyDataSetChanged();
            showToast(getString(R.string.hint_bind_success));

            showProgress(getString(R.string.hint_sync_data));
            tvBindName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SyncAlert.getInstance(mContext).sync();
                }
            },1000);
        }else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNBIND) {
            //解绑成功显示未绑定视图，弹出提示
            showUnBindView();
            ibandApplication.service.stopNotification();
            showToast(getString(R.string.hint_un_bind_success));
        }else if (event.getWhat() == EventGlobal.STATE_DEVICE_BIND_FAIL){
            //绑定失败清空列表，弹出提示
            mDeviceList.clear();
            mDeviceAdapter.notifyDataSetChanged();
            showUnBindView();
            showToast(getString(R.string.hint_un_bind_fail));
        }
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgress();
    }
}
