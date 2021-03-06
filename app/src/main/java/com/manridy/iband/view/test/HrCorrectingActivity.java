package com.manridy.iband.view.test;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.iband.R;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.toast.HrCorrectingResultToast;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.ble.BleParse;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 心率持续测量
 * Created by jarLiao on 17/5/11.
 */

public class HrCorrectingActivity extends BaseActionActivity {



    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.bt_correcting)
    Button rlCorrecting;
    @BindView(R.id.bt_reset)
    Button rlReset;

    private boolean curOnoff;
    private int curSpace;

    private int requstType = 0;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_hr_correcting);
        ButterKnife.bind(this);
        BleParse.getInstance().setHrCorrectingNotifyListener(new BleNotifyListener() {
            @Override
            public void onNotify(Object o) {
                if(requstType==1){
                    dismissProgress();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                                showToast("心率校验完成");
                            HrCorrectingResultToast.getToastEmail().ToastShow(getBaseContext(), null,getString(R.string.hr_correcting_success));
                        }
                    });
                }else if(requstType==2){
                    dismissProgress();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HrCorrectingResultToast.getToastEmail().ToastShow(getBaseContext(), null,getString(R.string.hr_reseting_success));
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void initVariables() {
//        setTitleAndMenu(getString(R.string.hint_hr_test_timing),getString(R.string.hint_save));
        setTitleBar(getString(R.string.hint_hr_correcting));
//        checkMenuVisibility();
//        curOnoff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
//        curSpace = (int) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
//        aiAlert.setAlertCheck(curOnoff);
//        tvSpace.setText(curSpace+getString(R.string.unit_min));

    }

    private void checkMenuVisibility() {
        try {
            String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST, "");
            String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
            String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
            String deviceFirm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
            if (strDeviceList == null || strDeviceList.isEmpty()) {
                return;
            }
            DeviceList filterDeviceList = new Gson().fromJson(strDeviceList, DeviceList.class);
            for (DeviceList.ResultBean resultBean : filterDeviceList.getResult()) {
                if (resultBean.getDevice_id().equals(deviceType)) {
                    if (resultBean.getHeartrate_version().compareTo(deviceFirm) <= 0){
//                        curOnoff = resultBean.getHeartrate_isopen().equals("1");
//                        curSpace = Integer.parseInt(resultBean.getHeartrate_interval());

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void initListener() {
        rlCorrecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(getString(R.string.hr_correcting));
                requstType = 1;
                ibandApplication.service.watch.setHrCorrecting(true,new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);

//                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        dismissProgress();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast( "心率校验失败");
//                            }
//                        });
                    }
                });
            }
        });

        rlReset.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showProgress(getString(R.string.hr_reseting));
                requstType = 2;
                ibandApplication.service.watch.setHrCorrecting(false,new BleCallback()  {
                    @Override
                    public void onSuccess(Object o) {


//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
//                        dismissProgress();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast( getString(R.string.hint_save_success));
//                            }
//                        });
//                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        dismissProgress();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast( getString(R.string.hint_save_fail));
//                            }
//                        });
                    }
                });
            }
        });

//
//        aiAlert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                curOnoff = !curOnoff;
//                aiAlert.setAlertCheck(curOnoff);
//            }
//        });
//        rlSpace.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new NumDialog(mContext, getSpaces(), curSpace+"",getString(R.string.hint_space), new NumDialog.NumDialogListener() {
//                    @Override
//                    public void getNum(String num) {
//                        curSpace = Integer.valueOf(num);
//                        tvSpace.setText(num+getString(R.string.unit_min));
//                    }
//                }).show();
//            }
//        });
//        tbMenu.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showProgress( getString(R.string.hint_saveing));
//                ibandApplication.service.watch.sendCmd(BleCmd.setTimingHrTest(curOnoff,curSpace), new BleCallback() {
//                    @Override
//                    public void onSuccess(Object o) {
//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
//                        SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
//                        dismissProgress();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast( getString(R.string.hint_save_success));
//                            }
//                        });
//                        finish();
//                    }
//
//                    @Override
//                    public void onFailure(BleException exception) {
//                        dismissProgress();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                showToast( getString(R.string.hint_save_fail));
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }

    private String[] getSpaces() {
//        return new String[]{"5","15","30","60","90","120"};
        return new String[]{"30"};
    }


}
