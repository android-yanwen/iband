package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.R;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.UnitItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;
import com.tencent.tinker.android.dex.EncodedValue;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class UnitActivity extends BaseActionActivity {

    @BindView(R.id.tb_menu)
    TextView tvMenu;
    @BindView(R.id.unit_metric)
    UnitItems unitMetric;
    @BindView(R.id.unit_inch)
    UnitItems unitInch;

    int unit;
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_unit);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_menu_unit),getString(R.string.hint_save));
        unit = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT,0);
        showUnit();
    }

    @Override
    protected void initListener() {
        tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(getString(R.string.hint_saveing));
                mIwaerApplication.service.watch.sendCmd(BleCmd.setUnit(unit), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        SPUtil.put(mContext, AppGlobal.DATA_SETTING_UNIT,unit);
                        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_CHANGE_UNIT));
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_save_success));
                            }
                        });
                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_save_fail));
                            }
                        });
                    }
                });


            }
        });
    }

    @OnClick({R.id.unit_metric, R.id.unit_inch})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.unit_metric:
                selectUnit();
                break;
            case R.id.unit_inch:
                selectUnit();
                break;
        }
    }

    private void selectUnit() {
        unit = (unit == 0) ? 1:0;
        showUnit();
    }

    private void showUnit() {
        boolean isSelect = unit == 0;
        unitMetric.selectView(isSelect);
        unitInch.selectView(!isSelect);
    }


}
