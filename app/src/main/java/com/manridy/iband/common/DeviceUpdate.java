package com.manridy.iband.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.ToastUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.OtaActivity;

import java.util.List;

/**
 * Created by jarLiao on 17/8/16.
 */

public class DeviceUpdate {
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
        checkDeviceUpdate(new OnResultCallBack() {
            @Override
            public void onResult(boolean result, Object o) {
                if (result) {
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
                }
            }
        });
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
                            showToast(R.string.hint_ota_file_fail);
                        }
                    }
                });
            }
        }).start();
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
