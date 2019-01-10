package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.VersionUtil;
import com.manridy.iband.ConfigurationParameter;
import com.manridy.iband.R;
import com.manridy.iband.SyncAlert;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DeviceUpdate;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.HelpItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class UpdateActivity extends BaseActionActivity {

    @BindView(R.id.hi_update_soft)
    HelpItems hiUpdateSoft;
    @BindView(R.id.hi_update_firm)
    HelpItems hiUpdateFirm;
    @BindView(R.id.hi_device_id)
    HelpItems hiDeviceID;
    String url = "http://39.108.92.15:12345";
    String version = "/version.xml";
    String firm;
    String deviceType;
    String deviceName;
    boolean is_Force_ViewBtUpdate = false;

    public static boolean isGoogle = false;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_update);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFirmView();
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_menu_update));
        hiUpdateSoft.setMenuContent("v"+ VersionUtil.getVersionName(mContext));
        if(isGoogle) {
            hiUpdateSoft.getMenuBt().setVisibility(View.INVISIBLE);
        }
    }

    private void updateFirmView() {
        firm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"");
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
        deviceName = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_BIND_NAME,"");
        if (!firm.isEmpty() && !mac.isEmpty()) {
            hiUpdateFirm.setMenuContent("v"+firm);
            hiUpdateFirm.setVisibility(View.VISIBLE);
        }
        if(!deviceType.isEmpty()){
            hiDeviceID.setMenuContent(deviceType);
            hiDeviceID.setVisibility(View.VISIBLE);
            hiDeviceID.getMenuArrows().setVisibility(View.INVISIBLE);
        }
        //20180505
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        DeviceList filterDeviceList = new Gson().fromJson(strDeviceList,DeviceList.class);
        boolean isViewBtUpdate = false;
        for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
            if(resultBean.getDevice_id().trim().equals(deviceType.trim())){
//            if(resultBean.getDevice_id().trim().equals(deviceType.trim())){
                if("0".equals(resultBean.getNeed_update())){
                    String firm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"");
                    if(!"".equals(firm)&&firm.compareTo(resultBean.getSupport_software())<0){
                        isViewBtUpdate = true;
                    }else{
                        isViewBtUpdate = false;
                    }
                }else if("1".equals(resultBean.getNeed_update())){
                    isViewBtUpdate = true;
                } else if ("1.0.0".compareTo(resultBean.getNeed_update()) <= 0) {
                    isViewBtUpdate = true;
                }
//                String need_update = resultBean.getNeed_update();
//                int a = "1.0.1".compareTo(need_update);
//                int b = "1.0.0".compareTo(need_update);
//                int c = "1.0".compareTo(need_update);

            }
        }

        if(is_Force_ViewBtUpdate){
            isViewBtUpdate = true;
        }

        if(hiUpdateFirm.getMenuBt()!=null){
            if(isViewBtUpdate){
                hiUpdateFirm.getMenuBt().setVisibility(View.VISIBLE);
                hiUpdateFirm.setClickable(true);
            }else {
                hiUpdateFirm.getMenuBt().setVisibility(View.INVISIBLE);
                hiUpdateFirm.setClickable(false);
            }
        }

//        if (deviceType.equals("2")){
//            hiUpdateFirm.setMenuUnit("");
//            hiUpdateFirm.setEnabled(false);
//        }
    }

    boolean isForce;
    @Override
    protected void initListener() {
//        hiUpdateFirm.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                startActivity(OtaActivity.class);
//                return true;
//            }
//        });

        findViewById(R.id.tb_title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showToast("开启强制升级模式");
                isForce = true;
                return true;
            }
        });
    }

    @Override
    protected void loadData() {
        super.loadData();
        if (ibandApplication == null || ibandApplication.service == null || ibandApplication.service.watch == null) {
            return;
        }
        ibandApplication.service.watch.getFirmwareVersion(new BleCallback() {
            @Override
            public void onSuccess(Object o) {
                firm = SyncAlert.parseJsonString(o,"firmwareVersion");
                deviceType = SyncAlert.parseJsonString(o,"firmwareType");
                SPUtil.put(mContext, AppGlobal.DATA_FIRMWARE_VERSION,firm);
                SPUtil.put(mContext, AppGlobal.DATA_FIRMWARE_TYPE,deviceType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFirmView();
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });


    }

    private DeviceUpdate deviceUpdate = null;
    @OnClick({R.id.hi_update_soft, R.id.hi_update_firm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hi_update_soft:
                if(isGoogle){
                    break;
                }
//                Beta.checkUpgrade(true,false);
//                Beta.upgradeListener = new UpgradeListener() {
//                    @Override
//                    public void onUpgrade(int i, UpgradeInfo upgradeInfo, boolean b, boolean b1) {
//                        if (upgradeInfo == null) {
////                            showToast(getString(R.string.hint_ota_newest));
//                            showWarmDialog(getString(R.string.hint_ota_newest));
//                        }
//                    }
//                };

//                UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
//                StringBuilder info = new StringBuilder();
//                info.append("id: ").append(upgradeInfo.id).append("\n");
//                info.append("标题: ").append(upgradeInfo.title).append("\n");
//                info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
//                info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
//                info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
//                info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
//                info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
//                info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
//                info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
//                info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
//                info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
//                info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
//                info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
//                info.append("图片地址：").append(upgradeInfo.imageUrl);
                break;
            case R.id.hi_update_firm:
                if (!checkEditBluetoothName()) {//判断蓝牙名称是否修改过，修改名称的不支持ota升级，以免恢复默认
                    if (deviceUpdate == null) {
                        deviceUpdate = new DeviceUpdate(mContext);
                    }
                    deviceUpdate.getOTAVersion(deviceType, firm, isForce, new DeviceUpdate.UpdateListener() {
                        @Override
                        public void prompt() {
                            showWarmDialog(getString(R.string.hint_ota_newest));
                        }
                    });
                }else {
//                        showToast(getString(R.string.hint_ota_newest));
                    showWarmDialog(getString(R.string.hint_ota_newest));
                }
                break;
        }
    }

    private boolean checkEditBluetoothName() {
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST,"");
        String deviceType = (String) SPUtil.get(mContext,AppGlobal.DATA_FIRMWARE_TYPE,"");
        String deviceName = (String) SPUtil.get(mContext,AppGlobal.DATA_DEVICE_BIND_NAME,"");
        if (!strDeviceList.isEmpty()) {
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                String configName = resultBean.getDevice_name();
                String configId = resultBean.getDevice_id();
                if (configId.equals(deviceType) &&configName.equals(deviceName)){
                    return resultBean.getEdit_bluetooth_name().equals(1);
                }
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.MSG_OTA_TOAST) {
            showToast(event.getMsg());
        }
    }

}
