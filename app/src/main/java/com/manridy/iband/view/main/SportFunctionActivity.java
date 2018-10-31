package com.manridy.iband.view.main;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.ble.BleCmd;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jarLiao on 17/12/12.
 */

public class SportFunctionActivity extends BaseActionActivity {

    @BindView(R.id.item_map)
    AlertBigItems itemMap;
    @BindView(R.id.btn_to_battery_saver_settings)
    Button btnToBatterySaverSettings;
    @BindView(R.id.btn_to_battery_saver_settings2)
    Button btnToBatterySaverSettings2;
    private boolean onlyUseAmapOnOff;



    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sport_function);
        ButterKnife.bind(this);

    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setTitleBar(getString(R.string.title_menu));
        onlyUseAmapOnOff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_IS_ONLY_USE_AMAP,false);
        itemMap.setAlertCheck(onlyUseAmapOnOff);
    }

    @Override
    protected void initListener() {
        itemMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlyUseAmapOnOff = !onlyUseAmapOnOff;
                Watch.getInstance().sendCmd(BleCmd.setWristOnOff(onlyUseAmapOnOff?1:0));
                SPUtil.put(mContext, AppGlobal.DATA_IS_ONLY_USE_AMAP,onlyUseAmapOnOff);
                itemMap.setAlertCheck(onlyUseAmapOnOff);
            }
        });

        btnToBatterySaverSettings.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_MAIN);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                ComponentName cn = ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity");

                intent.setComponent(cn);

                startActivity(intent);
            }
        });

        btnToBatterySaverSettings2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
