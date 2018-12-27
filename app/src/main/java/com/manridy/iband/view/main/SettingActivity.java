package com.manridy.iband.view.main;

import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.MarqueeTextView;
import com.manridy.iband.ui.items.MenuItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.setting.AboutActivity;
import com.manridy.iband.view.setting.AlertActivity;
import com.manridy.iband.view.setting.CameraActivity;
import com.manridy.iband.view.setting.DoNotDisturbActivity;
import com.manridy.iband.view.setting.FindActivity;
import com.manridy.iband.view.setting.LightActivity;
import com.manridy.iband.view.setting.TargetActivity;
import com.manridy.iband.view.setting.TimeActivity;
import com.manridy.iband.view.setting.UnitActivity;
import com.manridy.iband.view.setting.ViewActivity;
import com.manridy.iband.view.setting.WechatActivity;
import com.manridy.iband.view.setting.WristActivity;
import com.manridy.iband.view.test.TestHrTimingActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.manridy.iband.common.AppGlobal.DATA_USER_HEAD;

/**
 *
 * 设置
 * Created by jarLiao on 17/5/4.
 */

public class SettingActivity extends BaseActionActivity {

    @BindView(R.id.iv_user_icon)
    SimpleDraweeView ivUserIcon;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.rl_user_info)
    RelativeLayout rlUserInfo;
    @BindView(R.id.iv_device_icon)
    SimpleDraweeView ivDeviceIcon;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;
    @BindView(R.id.tv_device_bind_state)
    TextView tvDeviceBindState;
    @BindView(R.id.tv_device_connect_state)
    TextView tvDeviceConnectState;
    @BindView(R.id.tv_device_battery)
    MarqueeTextView tvDeviceBattery;
    @BindView(R.id.rl_un_bind)
    RelativeLayout rlUnBind;
    @BindView(R.id.sv_menu)
    ScrollView svMenu;
    @BindView(R.id.rl_device)
    RelativeLayout rlDevice;
    @BindView(R.id.rl_tab)
    LinearLayout rlTab;
    @BindView(R.id.menu_view)
    MenuItems menuView;
    @BindView(R.id.menu_camera)
    MenuItems menuCamera;
    @BindView(R.id.menu_find)
    MenuItems menuFind;
    @BindView(R.id.menu_alert)
    MenuItems menuAlert;
    @BindView(R.id.menu_wechat)
    MenuItems menuWechat;
    @BindView(R.id.menu_light)
    MenuItems menuLight;
    @BindView(R.id.menu_unit)
    MenuItems menuUnit;
    @BindView(R.id.menu_time)
    MenuItems menuTime;
    @BindView(R.id.menu_target)
    MenuItems menuTarget;
    @BindView(R.id.menu_about)
    MenuItems menuAbout;
    @BindView(R.id.menu_wrist)
    MenuItems menuWrist;
    @BindView(R.id.menu_reset)
    MenuItems menuReset;
    @BindView(R.id.menu_hr_test)
    MenuItems menuHrTest;
    @BindView(R.id.menu_clean)
    MenuItems menuClean;
    @BindView(R.id.menu_do_not_disturb)
    MenuItems menu_do_not_disturb;

    private String bindName;

    private int connectState;
    private int curBatteryNum;
    private int curBatteryState;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        registerEventBus();
    }
    //868053035518273
    //868053035518265
    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_set));
        initUser();
        bindName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
        connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
        curBatteryNum = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_NUM, -1);
        curBatteryState = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_STATE, -1);
        if (!bindName.isEmpty()) {
            showBindDevice();
            showConnectState();
        }
        loadDeviceImg();
        checkMenuVisibility();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkMenuVisibility();
//        menuHrTest.setVisibility(View.VISIBLE);
    }

    private void checkMenuVisibility() {
        try {
            menuLight.setVisibility(View.VISIBLE);
            String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST, "");
            String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
            String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
            String deviceFirm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
            if (strDeviceList == null || strDeviceList.isEmpty()) {
                return;
            }
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
//                if (resultBean.getDevice_name().equals(deviceName) || resultBean.getDevice_id().equals(deviceType)) {
                if (resultBean.getDevice_id().equals(deviceType)) {
                    if (resultBean.getBrightness().equals("0")) {
                        menuLight.setVisibility(View.GONE);
                    }
                    if (resultBean.getClear_away().compareTo(deviceFirm) <= 0) {
                        menuClean.setVisibility(View.VISIBLE);
                    }

                    boolean isViewMenuHeartrate = false;
                    if (!"0".equals(resultBean.getHeartrate_version())&&resultBean.getHeartrate_version().compareTo(deviceFirm) <= 0){
                        isViewMenuHeartrate = true;
                    }

                    if(!"0".equals(resultBean.getIs_chk_heart_rate())&&resultBean.getIs_chk_heart_rate().compareTo(deviceFirm) <= 0) {
                        isViewMenuHeartrate = true;
                        }
                    if(isViewMenuHeartrate) {
                        menuHrTest.setVisibility(View.VISIBLE);
                    }else{
                        menuHrTest.setVisibility(View.GONE);
                    }


                    String no_exec = resultBean.getNo_exec();
                    // 判断固件版本是否有支持勿扰模式
                    if (!"0".equals(no_exec) && no_exec.compareTo(deviceFirm) <= 0) {
//                        Log.d(TAG, "checkMenuVisibility: " + no_exec);

                        menu_do_not_disturb.setVisibility(View.VISIBLE);
                        SPUtil.get(mContext, AppGlobal.DATA_DO_NOT_DISTURB_IFG, true);
                    } else {
                        menu_do_not_disturb.setVisibility(View.GONE);
                        SPUtil.get(mContext, AppGlobal.DATA_DO_NOT_DISTURB_IFG, false);
                    }

                        return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDeviceImg() {
        String deviceImgPath = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_IMG, "");
        if (!deviceImgPath.isEmpty() && !deviceImgPath.equals("unknown")) {
            Uri loadImgUri = Uri.parse(HttpService.device_img + deviceImgPath);
            ivDeviceIcon.setImageURI(loadImgUri);
        }
    }

    private void initUser() {
        UserModel curUser = IbandDB.getInstance().getUser();
        if (curUser != null) {
            tvUserName.setText(curUser.getUserName());
        }
        String path = (String) SPUtil.get(mContext, DATA_USER_HEAD, "");
        File file = new File(Environment.getExternalStorageDirectory() + "/iband" + path);
        if (file.exists()) {
            ivUserIcon.setImageResource(R.mipmap.set_head);
            ivUserIcon.setImageURI("file://" + file.getPath());
        }
    }

    @Override
    protected void initListener() {
        Watch.getInstance().getBatteryInfo(new BleCallback() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    @OnClick({R.id.menu_view, R.id.menu_camera, R.id.menu_find,
            R.id.menu_alert, R.id.menu_wechat, R.id.menu_light,
            R.id.menu_unit, R.id.menu_time, R.id.menu_target,
            R.id.menu_about, R.id.rl_user_info, R.id.rl_device,
            R.id.iv_user_icon, R.id.menu_wrist, R.id.menu_reset,
            R.id.menu_hr_test,R.id.menu_clean, R.id.menu_do_not_disturb
    })
    public void onClick(View view) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_user_icon:
                startActivity(UserActivity.class);
                break;
            case R.id.rl_user_info:
                startActivity(UserActivity.class);
                break;
            case R.id.rl_device:
                eventSend(EventGlobal.ACTION_HIDE_SIMPVIEW);
                startActivity(DeviceActivity.class);
                break;
            case R.id.menu_view:
                startActivity(ViewActivity.class);
                break;
            case R.id.menu_camera:
                startActivity(CameraActivity.class);
                break;
            case R.id.menu_find:
                startActivity(FindActivity.class);
                break;
            case R.id.menu_alert:
                startActivity(AlertActivity.class);
                break;
            case R.id.menu_wechat:
                if (!CheckUtil.isNetworkAvailable(mContext)) {
                    showToast(getString(R.string.hint_network_available));
                    return;
                }
                connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                if (connectState != 1) {
                    showToast(getString(R.string.hintUnConnect));
                    return;
                }
                startActivity(WechatActivity.class);
                break;
            case R.id.menu_light:
                startActivity(LightActivity.class);
                break;
            case R.id.menu_unit:
                startActivity(UnitActivity.class);
                break;
            case R.id.menu_time:
                startActivity(TimeActivity.class);
                break;
            case R.id.menu_target:
                startActivity(TargetActivity.class);
                break;
            case R.id.menu_about:
                startActivity(AboutActivity.class);
                break;
            case R.id.menu_wrist:
                startActivity(WristActivity.class);
                break;
            case R.id.menu_reset:
                connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                if (connectState != 1) {
                    showToast(getString(R.string.hintUnConnect));
                    return;
                }
                showResetDialog();
                break;
            case R.id.menu_clean:
                connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
                if (connectState != 1) {
                    showToast(getString(R.string.hintUnConnect));
                    return;
                }
                showCleanDialog();
                break;
            case R.id.menu_hr_test:
                startActivity(TestHrTimingActivity.class);
                break;
            case R.id.menu_do_not_disturb:    // 勿扰模式
                startActivity(DoNotDisturbActivity.class);
                break;
            default:
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_CHANGE_USER) {
            initUser();
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_BIND) {
            bindName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
            showBindDevice();
            loadDeviceImg();
            checkMenuVisibility();
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_UNBIND) {
            rlUnBind.setVisibility(View.VISIBLE);
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_CONNECT) {
            tvDeviceConnectState.setText(R.string.hint_state_connected);
            curBatteryNum = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_NUM, -1);
            curBatteryState = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_STATE, -1);
            showBattery();
            LogUtil.d(TAG, "onEventMainThread() called with: event = [  已连接  ]");
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_DISCONNECT) {
            connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
            tvDeviceConnectState.setText(R.string.hint_state_unconnect);
            tvDeviceBattery.setText("");
            LogUtil.d(TAG, "onEventMainThread() called with: event = [  未连接  ]");
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_CONNECTING) {
            tvDeviceConnectState.setText(R.string.hint_state_connecting);
            tvDeviceBattery.setText("");
            LogUtil.d(TAG, "onEventMainThread() called with: event = [  连接中  ]");
        } else if (event.getWhat() == EventGlobal.ACTION_BATTERY_NOTIFICATION) {
            connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE, AppGlobal.DEVICE_STATE_UNCONNECT);
            curBatteryNum = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_NUM, -1);
            curBatteryState = (int) SPUtil.get(mContext, AppGlobal.DATA_BATTERY_STATE, -1);
            showBattery();
        } else if (event.getWhat() == EventGlobal.STATE_DEVICE_SEARCHING){
            tvDeviceConnectState.setText(R.string.hint_state_unconnect);
            tvDeviceBattery.setText("");
        }
    }

    private void showCleanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.hint_reset_text);
        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Watch.getInstance().sendCmd(BleCmd.deviceClean(), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        try {
                            IbandDB.getInstance().resetAppData();
                            removeSetting();
                        } catch (Exception e) {
                            e.toString();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_reset_success));
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        showToast(getString(R.string.hint_reset_failure));
                    }
                });

            }
        });
        builder.create().show();
    }

    private void showResetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.hint_restart);
        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Watch.getInstance().sendCmd(BleCmd.deviceRestart(), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        try {
                        } catch (Exception e) {
                            e.toString();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_reset_success));
                            }
                        });
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        showToast(getString(R.string.hint_reset_failure));
                    }
                });

            }
        });
        builder.create().show();
    }

    private void removeSetting() {
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_PHONE);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_SMS);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_SEDENTARY);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_CLOCK);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_LOST);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_APP);
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_WRIST);
        SPUtil.remove(mContext, AppGlobal.DATA_SETTING_LIGHT);
        SPUtil.remove(mContext, AppGlobal.DATA_SETTING_UNIT);
        SPUtil.remove(mContext, AppGlobal.DATA_SETTING_UNIT_TIME);
        SPUtil.remove(mContext, AppGlobal.DATA_SETTING_TARGET_STEP);
        SPUtil.remove(mContext, AppGlobal.DATA_SETTING_TARGET_SLEEP);
    }


    private void showBindDevice() {
        tvDeviceName.setText(bindName);
        tvDeviceBindState.setText(R.string.hint_state_bind);
        rlUnBind.setVisibility(View.GONE);

    }

    private void showBattery() {
        String battery = "";
        if (curBatteryState == 1 && connectState == 1) {
            battery = getString(R.string.hint_state_charge);
        } else if (curBatteryNum != -1 && connectState == 1) {
            battery = getString(R.string.hint_state_battery) + curBatteryNum + "%";
        }
        tvDeviceBattery.setText(battery);
    }

    private void showConnectState() {
        String state = getString(R.string.hint_state_unconnect);
        if (connectState == 1) {
            state = getString(R.string.hint_state_connected);
        } else if (connectState == 2) {
            state = getString(R.string.hint_state_connecting);
        }
        tvDeviceConnectState.setText(state);
        showBattery();
    }


}