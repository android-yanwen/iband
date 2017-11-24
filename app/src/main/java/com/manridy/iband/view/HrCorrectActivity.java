package com.manridy.iband.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jaygoo.widget.RangeSeekbar;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class HrCorrectActivity extends BaseActionActivity implements View.OnClickListener {

    int curLight;
    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.rs_light)
    RangeSeekbar rsLight;
    @BindView(R.id.tv_reduce)
    TextView tvReduce;
    @BindView(R.id.tv_add)
    TextView tvAdd;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_hr_correct);
        ButterKnife.bind(this);

    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_menu_light), getString(R.string.hint_save));
        curLight = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_HRCORRECT, 1);
        rsLight.setValue(curLight);
    }

    @Override
    protected void initListener() {
        tvReduce.setOnClickListener(this);
        tvAdd.setOnClickListener(this);
        findViewById(R.id.tb_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        rsLight.setOnRangeChangedListener(new RangeSeekbar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekbar rangeSeekbar, float v, float v1, boolean b) {
                Log.d(TAG, "onRangeChanged() called with: rangeSeekbar = [" + rangeSeekbar + "], v = [" + v + "], v1 = [" + v1 + "], b = [" + b + "]");
                if (b) {
                    curLight = (int) v;
                    isChange = true;
                }
            }
        });

        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress( getString(R.string.hint_saveing));
                int correctNum = getCorrectNum(curLight);
                ibandApplication.service.watch.sendCmd(BleCmd.setHrCorrect(correctNum), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        SPUtil.put(mContext, AppGlobal.DATA_SETTING_HRCORRECT, curLight);
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast( getString(R.string.hint_save_success));
                            }
                        });
                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
                Log.d(TAG, "onClick() called with: correctNum = [" + correctNum + "]");

            }
        });
    }

    public int getCorrectNum(int curLight) {
        int correctNum = 1700;
        switch (curLight) {
            case 0:
                correctNum = 2500;
                break;
            case 1:
                correctNum = 1700;
                break;
            case 2:
                correctNum = 800;
                break;
        }
        return correctNum;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_reduce:
                curLight = curLight <= 0 ? 0 : curLight - 1;
                rsLight.setValue(curLight);
                isChange = true;
                break;
            case R.id.tv_add:
                curLight = curLight >= 2 ? 2 : curLight + 1;
                rsLight.setValue(curLight);
                isChange = true;
                break;
        }
    }

}
