package com.manridy.iband.view.setting;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.base.BaseActionActivity;
import com.tencent.mm.opensdk.modelbiz.JumpToBizProfile;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class WechatActivity extends BaseActionActivity {

    private static final String QR_CODE = "http://we.qq.com/d/AQAMj-aODjiAZeqaBa46Npk8sdayEsk2CudEf-P8";
    @BindView(R.id.bt_more)
    Button btMore;
    String curMac;
    private IWXAPI api;
    private String extMsg = "weiqi";

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_wechat);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_menu_wechat));
    }

    @Override
    protected void initListener() {
        btMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumbWechat();
            }
        });
        curMac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        if (!curMac.isEmpty()) {
            curMac = curMac.replace(":","");
//            showProgress(getString(R.string.hint_wechat_query));
            eventSend(EventGlobal.ACTION_WECHAT_QUERY);
        }
    }

    private void jumbWechat() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.error_not_install, Toast.LENGTH_LONG).show();
        }
    }

    private void jumb2Wechat() {
        if (api == null) {
            api = WXAPIFactory.createWXAPI(mContext, "wx8dda48129cdf2ba5", false);//AppID
        }
        if (!api.isWXAppInstalled()) {
            // 提醒用户没有安装微信
            Toast.makeText(mContext, R.string.error_not_install, Toast.LENGTH_SHORT).show();
            return;
        }
        api.registerApp("wx8dda48129cdf2ba5");//AppID
        JumpToBizProfile.Req req = new JumpToBizProfile.Req();
        req.toUserName = "gh_6bc241fa53ba";//公众号原始ID
        req.profileType = JumpToBizProfile.JUMP_TO_HARD_WARE_BIZ_PROFILE;
        req.extMsg = QR_CODE + "#" + extMsg;//二维码路径 + 自定义数据
        api.sendReq(req);
    }

    private void showRegistDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.hint_wechart_alert);
        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setPositiveButton(R.string.hint_activate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showProgress(getString(R.string.hint_activateing));
                eventSend(EventGlobal.ACTION_WECHAT_REGIST);
            }
        });
        builder.create().show();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.ACTION_WECHAT_QUERY) {
            String firmType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
            if (firmType.isEmpty()) {
                return;//没有固件型号信息
            }
            boolean isOld = firmType.equals("0001");
            HttpService.getInstance().wechatQuery(curMac,isOld,new OnResultCallBack() {
                @Override
                public void onResult(boolean result, Object o) {
                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(o.toString());
                            final int statusCode = jsonObject.getInt("status_code");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (statusCode == 10000) {
//                                        showRegistDialog();
                                        showProgress(getString(R.string.hint_activateing));
                                        eventSend(EventGlobal.ACTION_WECHAT_REGIST);
                                    }else if (statusCode == 10004){
//                                        showToast(getString(R.string.hint_device_activate));
                                    }else {
                                        showError();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgress();
                                showError();
                            }
                        });
                    }

                    Log.d(TAG, "wechatQuery() called with: result = [" + result + "], o = [" + o== null?"null":o.toString() + "]");
                }
            });
        }else if (event.getWhat() == EventGlobal.ACTION_WECHAT_REGIST){
            String firmType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE,"");
            if (firmType.isEmpty()) {
                return;//没有固件型号信息
            }
            String productId = "35788";
            String deviceName = "N109";
            if (!firmType.equals("0001")){//固件型号非0001
                productId = "40166";
                deviceName = "MRD-Sports";
            }
            HttpService.getInstance().wechatRegister(productId,deviceName,curMac, new OnResultCallBack() {
                @Override
                public void onResult(final boolean result, final Object o) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                try {
                                    JSONObject jsonObject = new JSONObject(o.toString());
                                    int statusCode = jsonObject.getInt("status_code");
                                    if (statusCode == 20000) {
                                        dismissProgress();
                                        showToast(getString(R.string.hint_install_success));
                                    }else {
                                        showError();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                showError();
                            }
                        }
                    });
                    Log.d(TAG, "wechatRegister() called with: result = [" + result + "], o = [" + o== null?"null":o.toString() + "]");

                }
            });
        }
    }

    private void showError() {
        showToast(getString(R.string.hint_net_error));
        finish();
    }
}
