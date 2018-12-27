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
 * 心率血压报警
 * Created by yw on 18/12/19.
 */
public class HearBloodAlertActivity extends BaseActionActivity {
    private AlertBigItems4 abt_heart_alert, abt_blood_alert;
    private NumDialog numDialogHeart, numDialogBlood;
    private boolean heartIsOpenIfg = false, bloodIdOpenIfg = false;
    private String strHeartValue = "150", strBloodValue = "140";

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_heart_blood_alert);
        abt_heart_alert = (AlertBigItems4) findViewById(R.id.abt_heart_alert);
        abt_blood_alert = (AlertBigItems4) findViewById(R.id.abt_blood_alert);
    }

    @Override
    protected void initVariables() {
        setTitleAndMenu(getResources().getString(R.string.hint_hear_rate_alert), getResources().getString(R.string.hint_save));
        heartIsOpenIfg = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_HEART, heartIsOpenIfg);
        abt_heart_alert.setAlerMenuImgOnOrOff(heartIsOpenIfg);
        bloodIdOpenIfg = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_BLOOD, bloodIdOpenIfg);
        abt_blood_alert.setAlerMenuImgOnOrOff(bloodIdOpenIfg);

        strHeartValue = (String) SPUtil.get(mContext, AppGlobal.DATA_ALERT_HEART_VALUE, "150");
        abt_heart_alert.setAlertValue(strHeartValue);
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
                showProgress(getString(R.string.hint_saveing));
                if (heartIsOpenIfg) {
                    onOff = 1;
                }
                if (bloodIdOpenIfg) {
                    onOff = 2;
                }
                if (heartIsOpenIfg && bloodIdOpenIfg) {
                    onOff = 3;
                }

                ibandApplication.service.watch.sendCmd(BleCmd.setHeartBloodAlert(onOff, Integer.parseInt(strHeartValue), Integer.parseInt(strBloodValue)), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        dismissProgress();
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_HEART, heartIsOpenIfg);
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_BLOOD, bloodIdOpenIfg);
                        if (heartIsOpenIfg) {
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_HEART_VALUE, strHeartValue);
                        }
                        if (bloodIdOpenIfg) {
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_BLOOD_VALUE, strBloodValue);
                        }
                        if (heartIsOpenIfg || bloodIdOpenIfg) {
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_HEART_BLOOD, true);
                        } else if (!heartIsOpenIfg && !bloodIdOpenIfg) {
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_HEART_BLOOD, false);
                        }
                        onBackPressed();
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

        abt_heart_alert.setAlertMenuSwitchOnClickListener(new AlertBigItems4.AlertMenuSwitchOnClickListener() {
            @Override
            public void switchOnClick(View v, boolean isOnOff) {
                heartIsOpenIfg = isOnOff;
            }
        });
        abt_heart_alert.setAlertMenuValueOnClickListener(new AlertBigItems4.AlertMenuValueOnClickListener() {
            @Override
            public void valueOnClick(View v) {
                showNumDialogHeart();
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

    private String[] getHeartSpaces() {
        String[] spaces = new String[121];
        for (int i = 0; i <= 120; i++) {
            spaces[i] = 80 + i + "";
        }
        return spaces;
    }

    private String[] getBloodSpaces() {
        String[] spaces = new String[71];
        for (int i = 0; i <= 70; i++) {
            spaces[i] = 110 + i + "";
        }
        return spaces;
    }
    private void showNumDialogHeart() {
        if (numDialogHeart == null) {
            numDialogHeart = new NumDialog(mContext, getHeartSpaces(), strHeartValue, getString(R.string.hint_heart_rate_alert_selection), new NumDialog.NumDialogListener() {
                @Override
                public void getNum(String num) {
                    abt_heart_alert.setAlertValue(num);
                    strHeartValue = num;
                }
            });
        }
        numDialogHeart.show();
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
