package com.manridy.iband.view.test;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.ui.items.MenuItems;
import com.manridy.iband.view.HrCorrectActivity;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.setting.LightActivity;
import com.manridy.iband.view.setting.ViewActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jarLiao on 17/6/13.
 */

public class TestMenuActivity extends BaseActionActivity {


    @BindView(R.id.menu_view)
    MenuItems menuView;
    @BindView(R.id.menu_db)
    MenuItems menuDb;
    @BindView(R.id.menu_hr_test)
    MenuItems menuHrTest;
    @BindView(R.id.menu_hr_correct)
    MenuItems menuHrCorrect;

    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.menu_step)
    MenuItems menuStep;


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test_menu);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setTitleBar("实验室");
        tbMenu.setText("隐藏");
        tbMenu.setTextColor(Color.TRANSPARENT);
    }

    int clickNum;

    @Override
    protected void initListener() {
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNum++;
                if (clickNum > 5) {
                    menuDb.setVisibility(View.VISIBLE);
                }
            }
        });
        menuDb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                menuStep.setVisibility(View.VISIBLE);
                return true;
            }
        });
        menuView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ibandApplication.service.watch.sendCmd(BleCmd.getPair(), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        showToastOnMain("发送配对请求成功");
                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
                return true;
            }
        });

    }

    @OnClick({R.id.menu_view, R.id.menu_db, R.id.menu_hr_test,R.id.menu_step,R.id.menu_reset,R.id.menu_light,R.id.menu_hr_correct})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_view:
                startActivity(ViewActivity.class);
                break;
            case R.id.menu_db:
                startActivity(TestDatabaseActivity.class);
                break;
            case R.id.menu_hr_test:
                startActivity(TestHrTimingActivity.class);
                break;
            case R.id.menu_light:
                startActivity(LightActivity.class);
                break;
            case R.id.menu_reset:
                int connectState = (int) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_CONNECT_STATE,AppGlobal.DEVICE_STATE_UNCONNECT);
                if (connectState != 1) {
                    showToast(getString(R.string.hintUnConnect));
                    return;
                }
                showRegistDialog();
                break;
            case R.id.menu_step:
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                View layout = LayoutInflater.from(mContext).inflate(R.layout.dialog_edittext,null);
                final EditText editText = (EditText) layout.findViewById(R.id.et_step_num);
                builder.setView(layout);
                final AlertDialog dialog = builder.create();
                layout.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                layout.findViewById(R.id.bt_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        int num = Integer.parseInt(editText.getText().toString());
                        if (num < 99999) {
                            ibandApplication.service.watch.sendCmd(BleCmd.setDeviceStepNum(num));
                        }else {
                            showToast("数值超出！");
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.menu_hr_correct:
                startActivity(HrCorrectActivity.class);
                break;
        }
    }

    private void showRegistDialog(){
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
                IbandApplication.getIntance().service.watch.sendCmd(BleCmd.deviceReset(), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        try {
                            IbandDB.getInstance().resetAppData();
                            removeSetting();
                        }catch (Exception e){
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

    private void removeSetting() {
        SPUtil.remove(mContext, AppGlobal.DATA_ALERT_PHONE);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_SMS);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_SEDENTARY);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_CLOCK);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_LOST);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_APP);
        SPUtil.remove(mContext,AppGlobal.DATA_ALERT_WRIST);
        SPUtil.remove(mContext,AppGlobal.DATA_SETTING_LIGHT);
        SPUtil.remove(mContext,AppGlobal.DATA_SETTING_UNIT);
        SPUtil.remove(mContext,AppGlobal.DATA_SETTING_UNIT_TIME);
        SPUtil.remove(mContext,AppGlobal.DATA_SETTING_TARGET_STEP);
        SPUtil.remove(mContext,AppGlobal.DATA_SETTING_TARGET_SLEEP);
    }

}
