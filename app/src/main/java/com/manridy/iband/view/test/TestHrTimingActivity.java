package com.manridy.iband.view.test;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.iband.R;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.ui.items.AlertBigItems2;
import com.manridy.iband.ui.items.AlertBigItems3;
import com.manridy.iband.ui.items.AlertBigItemsOnClick;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.main.HrCorrectActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.ble.BleParse;
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
    AlertBigItems3 aiAlert;
    @BindView(R.id.tv_space)
    TextView tvSpace;
    @BindView(R.id.rl_space)
    RelativeLayout rlSpace;
    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.ai_hr_correcting)
    AlertBigItems2 aiCorrecting;
    @BindView(R.id.ai_hr_correcting_baseline)
    TextView aiHrCorrectingBaseline;
    private boolean curOnoff;
    private int curSpace;



    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_test_hr);
        ButterKnife.bind(this);

        aiAlert.setAlertImgClickListen(new AlertBigItemsOnClick() {
            @Override
            public void onClick() {
                curOnoff = !curOnoff;
                aiAlert.setAlertCheck(curOnoff);
            }
        });

        aiAlert.setAlertCenterClickListen(new AlertBigItemsOnClick() {
            @Override
            public void onClick() {
                new NumDialog(mContext, getSpaces(), curSpace+"",getString(R.string.hint_space), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        curSpace = Integer.valueOf(num);
//                        tvSpace.setText(num+getString(R.string.unit_min));
                        aiAlert.setAlertCenterContent(num+getString(R.string.unit_min));
                    }
                }).show();
            }
        });

//        aiAlert.setAlertImgClickListen(new AlertBigItems3.OnClick{
//
//        });


//        BleParse.getInstance().setTimingHrTestListener(new BleCallback() {
//            @Override
//            public void onSuccess(Object o) {
//                SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
//                SPUtil.put(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
//                dismissProgress();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showToast( getString(R.string.hint_save_success));
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(BleException exception) {
//                dismissProgress();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showToast( getString(R.string.hint_save_fail));
//                    }
//                });
//                curOnoff = !curOnoff;
//                aiAlert.setAlertCheck(curOnoff);
//            }
//        });
    }

    @Override
    protected void initVariables() {
        setTitleAndMenu(getString(R.string.hint_hr_test_timing),getString(R.string.hint_save));
        setTitleBar(getString(R.string.hint_hr_set));
        aiAlert.setIvMenuCenterIsView(false);
        curOnoff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR,curOnoff);
        aiAlert.setAlertCheck(curOnoff);
        curSpace = (int) SPUtil.get(mContext, AppGlobal.DATA_TIMING_HR_SPACE,curSpace);
        aiAlert.setAlertCenterContent(curSpace+getString(R.string.unit_min));
        tvSpace.setText(curSpace+getString(R.string.unit_min));
        checkMenuVisibility();
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
                    if(!"0".equals(resultBean.getHeartrate_version())&&resultBean.getHeartrate_version().compareTo(deviceFirm)<=0){
                        aiAlert.setVisibility(View.VISIBLE);
                        curOnoff = resultBean.getHeartrate_isopen().equals("1");
                        curSpace = Integer.parseInt(resultBean.getHeartrate_interval());
                    }
                    if(!"0".equals(resultBean.getIs_chk_heart_rate())&&resultBean.getIs_chk_heart_rate().compareTo(deviceFirm) <=0 ){
                        aiCorrecting.setVisibility(View.VISIBLE);
                        aiHrCorrectingBaseline.setVisibility(View.VISIBLE);
                    }
//                    is_heart_rate_timing_chk
                    if(!"0".equals(resultBean.getIs_heart_rate_timing_chk())&&resultBean.getIs_heart_rate_timing_chk().compareTo(deviceFirm)<=0){
                        aiAlert.setIvMenuCenterIsView(true);
                    }else{
                        aiAlert.setIvMenuCenterIsView(false);
                        curSpace = 30;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void initListener() {

        aiCorrecting.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(HrCorrectingActivity.class);
            }
        });

//        aiAlert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                curOnoff = !curOnoff;
//                aiAlert.setAlertCheck(curOnoff);
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
////                        finish();
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
//
//
//
//            }
//        });

        rlSpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NumDialog(mContext, getSpaces(), curSpace+"",getString(R.string.hint_space), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        curSpace = Integer.valueOf(num);
                        tvSpace.setText(num+getString(R.string.unit_min));
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
//        return new String[]{"30"};
    }


}
