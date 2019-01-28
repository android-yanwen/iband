package com.manridy.iband.view.alert;

import android.os.Bundle;
import android.view.View;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.AlertBigItems4;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.EventBus;

/**
 * 血压报警
 * Created by yw on 18/12/24.
 */
public class BloodAlertActivity extends BaseActionActivity {
    private AlertBigItems4 abt_blood_alert;
    private NumDialog  numDialogBlood;
    private boolean bloodIdOpenIfg = false;
    private String strHeartValue = "150", strBloodValue = "140";

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_blood_alert_layout);
        abt_blood_alert = (AlertBigItems4) findViewById(R.id.abt_blood_alert);
    }

    @Override
    protected void initVariables() {
        setTitleAndMenu(getResources().getString(R.string.hint_hear_rate_alert), getResources().getString(R.string.hint_save));
        bloodIdOpenIfg = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_BLOOD_IFG, bloodIdOpenIfg);
        abt_blood_alert.setAlerMenuImgOnOrOff(bloodIdOpenIfg);

        strHeartValue = (String) SPUtil.get(mContext, AppGlobal.DATA_ALERT_HEART_VALUE, "150");
        strBloodValue = (String) SPUtil.get(mContext, AppGlobal.DATA_ALERT_BLOOD_VALUE, "140");
        abt_blood_alert.setAlertValue(strBloodValue);
    }

    @Override
    protected void initListener() {
        // 菜单栏保存按钮
        findViewById(R.id.tb_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int onOff = 0;
                onOff = (int) SPUtil.get(mContext, AppGlobal.DATA_ALERT_BLOOD_ON_OFF, 0x00);
                showProgress(getString(R.string.hint_saveing));
                if (bloodIdOpenIfg) {
                    onOff |= 0x02;
                } else {
                    onOff &= ~0x02;
                }

                final int finalOnOff = onOff;
                ibandApplication.service.watch.sendCmd(BleCmd.setHeartBloodAlert(onOff, Integer.parseInt(strHeartValue), Integer.parseInt(strBloodValue)), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        dismissProgress();
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_BLOOD_ON_OFF, finalOnOff);
                        if (bloodIdOpenIfg) {
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_BLOOD_VALUE, strBloodValue);
                        }
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_BLOOD_IFG, bloodIdOpenIfg);
                        showToast(getString(R.string.hint_save_success));
                        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_CHANGE_MENU));//改变上一个activity心率血压报警状态显示

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_save_success));
                            }
                        });
                        onBackPressed();
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

        abt_blood_alert.setAlertMenuSwitchOnClickListener(new AlertBigItems4.AlertMenuSwitchOnClickListener() {
            @Override
            public void switchOnClick(View v, boolean isOnOff) {
                bloodIdOpenIfg = isOnOff;
            }
        });
        abt_blood_alert.setAlertMenuValueOnClickListener(new AlertBigItems4.AlertMenuValueOnClickListener() {
            @Override
            public void valueOnClick(View v) {
                showNumDialogBlood();
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private String[] getBloodSpaces() {
        String[] spaces = new String[71];
        for (int i = 0; i <= 70; i++) {
            spaces[i] = 110 + i + "";
        }
        return spaces;
    }

    private void showNumDialogBlood() {
        if (numDialogBlood == null) {
            numDialogBlood = new NumDialog(mContext, getBloodSpaces(), strBloodValue, getString(R.string.hint_blood_pressure_alarm_selection), new NumDialog.NumDialogListener() {
                @Override
                public void getNum(String num) {
                    abt_blood_alert.setAlertValue(num);
                    strBloodValue = num;
                }
            });
        }
        numDialogBlood.show();
    }


}
