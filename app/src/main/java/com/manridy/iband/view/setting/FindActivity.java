package com.manridy.iband.view.setting;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.manridy.applib.base.BaseHandler;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class FindActivity extends BaseActionActivity {

    @BindView(R.id.iv_find_device)
    ImageView ivFindDevice;
    private AlertDialog findWatch;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_find);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.title_find));
        registerEventBus();
    }

    @Override
    protected void initListener() {
        ivFindDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFindWatchDialog();
            }
        });
    }


    private void showFindWatchDialog() {
        AlertDialog.Builder  builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getString(R.string.title_find));
        builder.setMessage(R.string.hint_device_finding);

        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                handler.removeCallbacksAndMessages(null);
                ibandApplication.service.watch.sendCmd(BleCmd.findDevice(0), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onFailure(BleException exception) {

                    }
                });
            }
        });

        ibandApplication.service.watch.sendCmd(BleCmd.findDevice(3), new BleCallback() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
        findWatch = builder.create();
        findWatch.setCanceledOnTouchOutside(false);
        findWatch.show();
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(7);
    }


    BaseHandler<FindActivity> handler = new BaseHandler<FindActivity>(this) {
        @Override
        public void handleMessage(Message msg, FindActivity mainActivity) {
            if (findWatch != null && findWatch.isShowing()) {
                findWatch.setMessage(getString(R.string.hint_device_findings)+"("+msg.what+")");
                if (msg.what == 0 ) {
                    findWatch.dismiss();
                }else {
                    if (findWatch.isShowing()) {
                        msg.what--;
                        handler.sendEmptyMessageDelayed(msg.what,1000);
                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_FIND_WATCH_STOP) {
            showToast(getString(R.string.hint_device_find_cancel));
            if (findWatch != null && findWatch.isShowing()) {
                findWatch.dismiss();
            }
        }
    }


}
