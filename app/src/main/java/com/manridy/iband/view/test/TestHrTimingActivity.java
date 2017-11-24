package com.manridy.iband.view.test;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 心率持续测量
 * Created by jarLiao on 17/5/11.
 */

public class TestHrTimingActivity extends BaseActionActivity {


    @BindView(R.id.ai_alert)
    AlertBigItems aiAlert;
    @BindView(R.id.tv_space)
    TextView tvSpace;
    @BindView(R.id.rl_space)
    RelativeLayout rlSpace;
    @BindView(R.id.tb_menu)
    TextView tbMenu;
    private boolean curOnoff;
    private int curSpace;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test_hr);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setTitleAndMenu("心率定时测量","保存");
        curOnoff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR,false);
        curSpace = (int) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR_SPACE,30);
        aiAlert.setAlertCheck(curOnoff);
        tvSpace.setText(curSpace+"分钟");

    }


    @Override
    protected void initListener() {

        aiAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curOnoff = !curOnoff;
                aiAlert.setAlertCheck(curOnoff);
            }
        });
        rlSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NumDialog(mContext, getSpaces(), curSpace+"","测量间隔", new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        curSpace = Integer.valueOf(num);
                        tvSpace.setText(num+"分钟");
                    }
                }).show();
            }
        });
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress( getString(R.string.hint_saveing));
                ibandApplication.service.watch.sendCmd(BleCmd.setTimingHrTest(curOnoff,curSpace), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
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
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast( getString(R.string.hint_save_fail));
                            }
                        });
                    }
                });
            }
        });
    }

    private String[] getSpaces() {
        return new String[]{"5","15","30","60","90","120"};
    }


}
