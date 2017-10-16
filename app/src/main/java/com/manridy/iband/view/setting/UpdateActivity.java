package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.VersionUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DeviceUpdate;
import com.manridy.iband.common.DomXmlParse;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.items.HelpItems;
import com.manridy.iband.view.OtaActivity;
import com.manridy.iband.view.base.BaseActionActivity;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

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
    String url = "http://39.108.92.15:12345";
    String version = "/version.xml";
    String firm;
    String deviceType;
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

    }

    private void updateFirmView() {
        firm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"");
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");

        if (!firm.isEmpty() && !mac.isEmpty()) {
            hiUpdateFirm.setMenuContent("v"+firm);
            hiUpdateFirm.setVisibility(View.VISIBLE);
        }
//        if (deviceType.equals("2")){
//            hiUpdateFirm.setMenuUnit("");
//            hiUpdateFirm.setEnabled(false);
//        }
    }

    @Override
    protected void initListener() {
        hiUpdateFirm.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(OtaActivity.class);
                return true;
            }
        });
    }

    @OnClick({R.id.hi_update_soft, R.id.hi_update_firm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hi_update_soft:
                Beta.checkUpgrade(true,false);
                Beta.upgradeListener = new UpgradeListener() {
                    @Override
                    public void onUpgrade(int i, UpgradeInfo upgradeInfo, boolean b, boolean b1) {
                        if (upgradeInfo == null) {
                            showToast(getString(R.string.hint_ota_newest));
                        }
                    }
                };

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
                new DeviceUpdate(mContext).getOTAVersion(deviceType,firm);
                break;
        }
    }





    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.MSG_OTA_TOAST) {
            showToast(event.getMsg());
        }
    }
}
