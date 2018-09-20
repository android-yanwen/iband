package com.manridy.iband.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.ToastUtil;
import com.manridy.iband.R;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.main.OtaActivity;

import java.util.List;
import java.util.logging.Handler;

/**
 * Created by jarLiao on 17/8/16.
 */

public class DeviceUpdate {

    private static final String TAG = "DeviceUpdate";
    String url = "http://39.108.92.15:12345";
    String version = "/version.xml";
    Context mContext;

    public DeviceUpdate(Context mContext) {
        this.mContext = mContext;
    }

    public void checkDeviceUpdate(final OnResultCallBack onResultCallBack){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpService.getInstance().downloadXml(url+version,onResultCallBack);
            }
        }).start();
    }


    public void getOTAVersion(final String deviceType, final String deviceVersion, final boolean isForce){

        LogUtil.d(TAG, "getOTAVersion() called with: deviceType = [" + deviceType + "], deviceVersion = [" + deviceVersion + "], isForce = [" + isForce + "]");
        checkDeviceUpdate(new OnResultCallBack() {
            @Override
            public void onResult(boolean result, Object o) {
                if (result) {
                    LogUtil.d(TAG, "onResult() called with: result = [" + result + "], o = [" + o.toString() + "]");
                    if (o != null) {
                        List<DomXmlParse.Image> imageList = (List<DomXmlParse.Image>) o;
                        boolean isShow = false;
                        for (DomXmlParse.Image image : imageList) {
                            if (image.id.equals(deviceType)) {
                                if (image.least.compareTo(deviceVersion) > 0 || isForce) {
                                    SPUtil.put(mContext,AppGlobal.DATA_FIRMWARE_VERSION_NEW,image.least);
                                    isShow = true;
                                    final String fileUrl = url+"/"+image.id+"/"+image.file;
                                    ((Activity)mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            show(fileUrl);
                                        }
                                    });
                                }
                            }
                        }
                        if (!isShow) {
                            showToast(R.string.hint_ota_newest);
                        }
                    }
                }else{
                    showToast(R.string.error_update_fail);
//                    showNoNetWorkDlg(mContext);
                }
            }
        });
    }


    public void show_delay(final String fileUrl){
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_normal_delay,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false).setView(view);
        final AlertDialog dialog = builder.create();

//        boolean isDelayReminder = false;

//        final CheckBox cb_isDelayReminder = (CheckBox)view.findViewById(R.id.isDelayReminder);
//        cb_isDelayReminder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(cb_isDelayReminder.isChecked()){
//                    view.findViewById(R.id.goto_now).setVisibility(View.INVISIBLE);
//                }else{
//                    view.findViewById(R.id.goto_now).setVisibility(View.VISIBLE);
//                }
//            }
//        });

        view.findViewById(R.id.goto_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null &&  !((Activity)mContext).isFinishing()) {
//                    boolean isDelayReminder = cb_isDelayReminder.isChecked();
//                    if(isDelayReminder){
                        SPUtil.put(mContext,AppGlobal.DATA_DEVICEUPDATE_DELAY_FILEURL,fileUrl);
                        long timeStamp = System.currentTimeMillis()+24*60*60*1000*7;
                        SPUtil.put(mContext,AppGlobal.DATA_DEVICEUPDATE_DELAY_DATE,timeStamp);
//                    }
                    dialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.goto_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOTAFile(fileUrl);
                showToast(R.string.hint_ota_file_download);
                if (dialog != null && !((Activity)mContext).isFinishing()) {
                    dialog.dismiss();
                }
            }
        });
        if (!((Activity)mContext).isFinishing()) {
            dialog.show();
        }
    }

    public void show(final String fileUrl){
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_normal,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        view.findViewById(R.id.goto_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null &&  !((Activity)mContext).isFinishing()) {
                    dialog.dismiss();
                }
            }
        });
        view.findViewById(R.id.goto_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOTAFile(fileUrl);
                showToast(R.string.hint_ota_file_download);
                if (dialog != null && !((Activity)mContext).isFinishing()) {
                    dialog.dismiss();
                }
            }
        });
        if (!((Activity)mContext).isFinishing()) {
            dialog.show();
        }
    }

    private void getOTAFile(final String fileUrl){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpService.getInstance().downloadOTAFile(fileUrl, new OnResultCallBack() {
                    @Override
                    public void onResult(boolean result, Object o) {
                        if (result) {
                            showToast(R.string.hint_ota_file_success);
                            mContext.startActivity(new Intent(mContext,OtaActivity.class));
                        }else{
//                            showNoNetWorkDlg(mContext);
                            showToast(R.string.hint_ota_file_fail);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 当判断当前手机没有网络时选择是否打开网络设置
     * @param context
     */
    public static void showNoNetWorkDlg(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false).setIcon(R.mipmap.app_icon)         //
                .setTitle(R.string.app_name)            //
                .setMessage(R.string.hint_network_available).setPositiveButton(R.string.hint_set, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 跳转到系统的网络设置界面
                Intent intent = null;
                // 先判断当前系统版本
                if (android.os.Build.VERSION.SDK_INT > 10) {  // 3.0以上
//                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                } else {
                    intent = new Intent();
                    intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");
                }
                context.startActivity(intent);

            }
        }).setNegativeButton(R.string.hint_cancel, null).show();
    }

    private void showToast(final int res){
        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(mContext,res);
            }
        });
    }
}
