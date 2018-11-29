package com.manridy.iband.view.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.applib.view.dialog.TimeDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.DoNotDisturbModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.ui.items.AlertItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.bean.DoNotDisturb;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

public class DoNotDisturbActivity extends BaseActionActivity {

    private AlertItems ai_do_not_disturb, ai_do_not_disturb_start_time, ai_do_not_disturb_end_time;
    private TextView tb_menu;
    private boolean onOrOff = false;

    private DoNotDisturbModel curDoNotDisturbModel;
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_do_not_disturb);
        ai_do_not_disturb = (AlertItems) findViewById(R.id.ai_do_not_disturb);
        ai_do_not_disturb_start_time = (AlertItems) findViewById(R.id.ai_do_not_disturb_start_time);
        ai_do_not_disturb_end_time = (AlertItems) findViewById(R.id.ai_do_not_disturb_end_time);
        tb_menu = (TextView) findViewById(R.id.tb_menu);
    }

    @Override
    protected void initVariables() {
        ai_do_not_disturb.setAlertCheck(onOrOff);
        setTitleAndMenu("勿扰模式",getString(R.string.hint_save));
        curDoNotDisturbModel = IbandDB.getInstance().getDoNotDisturbModel();
        if (curDoNotDisturbModel == null) {
            curDoNotDisturbModel = new DoNotDisturbModel(0,0x19,0x10,0x7,0x30);
        }

        int onOff = curDoNotDisturbModel.getDoNotDisturbOnOff();
        if (onOff == 0) {
            onOrOff = false;
        } else {
            onOrOff = true;
        }
        initAi_do_not_disturb(
                onOrOff,
                curDoNotDisturbModel.getStartHour(),
                curDoNotDisturbModel.getStartMinute(),
                curDoNotDisturbModel.getEndHour(),
                curDoNotDisturbModel.getEndMinute()
                );
    }

    private void initAi_do_not_disturb(boolean onOrOff, int startHour, int entMinute, int endHour, int endMinute) {
        ai_do_not_disturb.setAlertCheck(onOrOff);
        ai_do_not_disturb_start_time.setVisibility(onOrOff ? View.VISIBLE : View.GONE);
        ai_do_not_disturb_end_time.setVisibility(onOrOff ? View.VISIBLE : View.GONE);
        ai_do_not_disturb_start_time.setAlertContent(Integer.toHexString(startHour) + ":" + Integer.toHexString(entMinute));
        ai_do_not_disturb_end_time.setAlertContent(Integer.toHexString(endHour) + ":" + Integer.toHexString(endMinute));
    }

    @Override
    protected void initListener() {
        ai_do_not_disturb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrOff = !onOrOff;
                ai_do_not_disturb.setAlertCheck(onOrOff);
                ai_do_not_disturb_start_time.setVisibility(onOrOff ? View.VISIBLE : View.GONE);
                ai_do_not_disturb_end_time.setVisibility(onOrOff ? View.VISIBLE : View.GONE);
//                curDoNotDisturbModel.save();
            }
        });

        ai_do_not_disturb_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimeDialog(mContext, new int[]{12,10}, getString(R.string.hint_alert_sedentary_time_start), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        ai_do_not_disturb_start_time.setAlertContent(hour + ":" + minute);
//                        curDoNotDisturbModel.setStartHour(Integer.parseInt(hour,16));
//                        curDoNotDisturbModel.setStartMinute(Integer.parseInt(minute,16));
//                        curDoNotDisturbModel.save();
                    }
                }).show();
            }
        });

        ai_do_not_disturb_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimeDialog(mContext, new int[]{12,10}, getString(R.string.hint_alert_sedentary_time_end), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        ai_do_not_disturb_end_time.setAlertContent(hour + ":" + minute);
//                        curDoNotDisturbModel.setEndHour(Integer.parseInt(hour, 16));
//                        curDoNotDisturbModel.setEndMinute(Integer.parseInt(minute, 16));
//                        curDoNotDisturbModel.save();
                    }
                }).show();
            }
        });

        tb_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curDoNotDisturbModel.setDoNotDisturbOnOff(onOrOff ? 1 : 0);
                DoNotDisturb notDisturb = new DoNotDisturb(
                        curDoNotDisturbModel.getDoNotDisturbOnOff(),
                        curDoNotDisturbModel.getStartHour(),
                        curDoNotDisturbModel.getStartMinute(),
                        curDoNotDisturbModel.getEndHour(),
                        curDoNotDisturbModel.getEndMinute()
                );

                showProgress(getString(R.string.hint_saveing));
                ibandApplication.service.watch.setDoNotDisturb(notDisturb, new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(TAG, "onSuccess: "+o);
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ibandApplication, getString(R.string.hint_save_success), Toast.LENGTH_SHORT).show();
                            }
                        });
                        curDoNotDisturbModel.save();
                        onBackPressed();
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        Log.d(TAG, "onFailure: ");
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ibandApplication, getString(R.string.hint_save_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }
}
