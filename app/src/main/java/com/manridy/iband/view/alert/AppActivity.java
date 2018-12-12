package com.manridy.iband.view.alert;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.manridy.applib.common.AppManage;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.adapter.AppAdapter;
import com.manridy.iband.bean.AppModel;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.OnItemClickListener;
import com.manridy.iband.service.AppNotificationListenerService;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.ui.items.AlertItems;
import com.manridy.iband.view.base.BaseActionActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.weyye.hipermission.PermissionAdapter;
import me.weyye.hipermission.PermissionItem;
import me.weyye.hipermission.PermissionView;

/**
 * 应用提醒
 * Created by jarLiao on 17/5/4.
 */

public class AppActivity extends BaseActionActivity {
    public static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    @BindView(R.id.ai_alert)
    AlertBigItems aiAlert;
    @BindView(R.id.rv_app)
    RecyclerView rvApp;
    boolean onOff;
    List<AppModel> curAppList;
    AppAdapter mAppAdapter;
    List<AppAdapter.Menu> menuList;
    boolean isAppNewShow = false;
    @BindView(R.id.ai_more)
    AlertItems aiMore;
    @BindView(R.id.rv_more)
    RecyclerView rvMore;
    AppAdapter moreAdapter;
    List<AppAdapter.Menu> moreList;
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_app);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.title_app), getString(R.string.hint_save));
        onOff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_APP, false);
        isAppNewShow = getAppNewShow();//判断app通知支持新版
        aiAlert.setAlertCheck(onOff);
        menuList = getMenuList();
        curAppList = IbandDB.getInstance().getAppList();
        if (curAppList != null && curAppList.size() > 0) {
            for (AppModel appModel : curAppList) {
                for (AppAdapter.Menu menu : menuList) {
                    if (appModel.getAppId() == menu.menuId) {
                        menu.menuCheck = appModel.isOnOff();
                    }
                }
            }
        }
        mAppAdapter = new AppAdapter(menuList);
        rvApp.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        rvApp.setAdapter(mAppAdapter);

        

    }

    private boolean getAppNewShow() {
        boolean isView = false;
        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST, "");
        String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
        String firmVersion = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
        String appVersion = "1.0.1";
        boolean isHaveDevice = false;
        if (!strDeviceList.isEmpty()) {
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                if (resultBean.getDevice_id().equals(deviceType)) {
                    appVersion = resultBean.getNotify_version();
                    isHaveDevice = true;
                }
            }
        }

        if(!isHaveDevice){
           return false;
        }

        if("0".equals(appVersion)){
            isView = false;
        }else{
            //设备自身的大于或等于网络获取的，就显示
            isView = firmVersion.compareTo(appVersion) >= 0;
        }
        return isView;
    }

    @Override
    protected void initListener() {
        aiAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectApp(!onOff);
                isChange = true;
            }
        });

        mAppAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (onOff) {
                            AppAdapter.Menu menu = menuList.get(position);
                            menu.menuCheck = !menu.menuCheck;
                            mAppAdapter.notifyDataSetChanged();
                            isChange = true;
                        }
                    }
                });
            }
        });

        findViewById(R.id.tb_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.put(mContext, AppGlobal.DATA_ALERT_APP, onOff);
                IbandDB.getInstance().saveAppList(menuList);
                showToast(getString(R.string.hint_save_success));
                eventSend(EventGlobal.DATA_CHANGE_MENU);
                finish();
            }
        });
    }

    private void selectApp(boolean isChecked) {
        if (!AppNotificationListenerService.isNotificationListenEnable(mContext)) {
            OpenNotifiactionDialog();
            return;
        }
        if (isChecked) {
            AppNotificationListenerService.startNotificationService(mContext);
            onOff = true;
        } else {
            AppNotificationListenerService.stopNotificationService(mContext);
            onOff = false;
        }
        aiAlert.setAlertCheck(onOff);

    }

    private List<AppAdapter.Menu> getMenuList() {
        List<AppAdapter.Menu> menuList = new ArrayList<>();
        menuList.add(new AppAdapter.Menu(4, getString(R.string.hint_app_wechat), R.mipmap.appremind_wechat));
        menuList.add(new AppAdapter.Menu(2, getString(R.string.hint_app_qq), R.mipmap.appremind_qq));
        menuList.add(new AppAdapter.Menu(5, getString(R.string.hint_app_whatsapp), R.mipmap.appremind_whatsapp));
        menuList.add(new AppAdapter.Menu(6, getString(R.string.hint_app_facebook), R.mipmap.appremind_facebook));
        menuList.add(new AppAdapter.Menu(7, getString(R.string.hint_app_line), R.mipmap.line));
        if (isAppNewShow) {
            menuList.add(new AppAdapter.Menu(8, "Twiteer", R.mipmap.appremind_twitter));
            menuList.add(new AppAdapter.Menu(9, "Skype", R.mipmap.appremind_skype));
            menuList.add(new AppAdapter.Menu(10, "Ins", R.mipmap.appremind_ins));
        }
        return menuList;
    }

    public void OpenNotifiactionDialog() {
        PermissionView contentView = new PermissionView(this);
        List<PermissionItem> data = new ArrayList<>();
        data.add(new PermissionItem(getString(R.string.hint_notification), getString(R.string.hint_notification), R.mipmap.permission_ic_notice));
        contentView.setGridViewColum(data.size());
        contentView.setTitle(getString(R.string.hint_alert_open));
        contentView.setMsg(getString(R.string.hint_request_alert_app));
        contentView.setGridViewAdapter(new PermissionAdapter(data));
        contentView.setStyleId(R.style.PermissionBlueStyle);
//        contentView.setFilterColor(mFilterColor);
        final AlertDialog bluetoothDialog = new AlertDialog.Builder(AppManage.getInstance().currentActivity())
                .setView(contentView)
                .create();
        contentView.setBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothDialog != null && bluetoothDialog.isShowing()) {
                    bluetoothDialog.dismiss();
                }
//                Intent intent = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivityForResult(intent,10000);
                AppNotificationListenerService.startNotificationListenSettings(mContext);
                onOff = true;
            }
        });
        bluetoothDialog.setCanceledOnTouchOutside(false);
        bluetoothDialog.setCancelable(false);
        bluetoothDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bluetoothDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isEnable = AppNotificationListenerService.isNotificationListenEnable(mContext);
        aiAlert.setAlertCheck(isEnable && onOff);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isChange) {
            showNotSaveDialog();
        }
    }



    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
//        if (requestCode == 10000) {
//
//        }
//    }
}
