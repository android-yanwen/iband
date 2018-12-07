package com.manridy.iband.view.alert;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.applib.view.dialog.TimeDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.SedentaryModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.ui.items.AlertItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.bean.Sedentary;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 久坐提醒
 * Created by jarLiao on 17/5/4.
 */

public class SedentaryActivity extends BaseActionActivity {

    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.ai_onoff)
    AlertItems aiOnoff;
    @BindView(R.id.ai_start_time)
    AlertItems aiStartTime;
    @BindView(R.id.ai_end_time)
    AlertItems aiEndTime;
    @BindView(R.id.ai_space)
    AlertItems aiSpace;
    @BindView(R.id.ai_nap)
    AlertBigItems aiNap;
    //    @BindView(R.id.ai_nap_onoff)
//    AlertItems aiNapOnoff;
//    @BindView(R.id.ai_nap_start_time)
//    AlertItems aiNapStartTime;
//    @BindView(R.id.ai_nap_end_time)
//    AlertItems aiNapEndTime;



    SedentaryModel curSedentary;


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sedentary);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_menu_alert_sedentary), getString(R.string.hint_save));
        initSedentary();
    }

    private void initSedentary() {
        curSedentary = IbandDB.getInstance().getSedentary();
        if (curSedentary == null) {
            curSedentary = new SedentaryModel(false, false, "09:00", "21:00", "12:00", "14:00", 60);
        }
        curSedentary.setSpace(curSedentary.getSpace() == 0 ? 60 : curSedentary.getSpace());
        curSedentary.setNapStartTime(curSedentary.getNapStartTime() == null ? "12:00" : curSedentary.getNapStartTime());
        curSedentary.setNapEndTime(curSedentary.getNapEndTime() == null ? "14:00" : curSedentary.getNapEndTime());

        aiOnoff.setAlertCheck(curSedentary.isSedentaryOnOff());
        aiNap.setAlertCheck(curSedentary.isSedentaryNap());
        aiStartTime.setAlertContent(curSedentary.getStartTime());
        aiEndTime.setAlertContent(curSedentary.getEndTime());
//        aiNapStartTime.setAlertContent(curSedentary.getNapStartTime());
//        aiNapEndTime.setAlertContent(curSedentary.getNapEndTime());
        aiSpace.setAlertContent(curSedentary.getSpace() + "");
        aiStartTime.setVisibility(curSedentary.isSedentaryOnOff() ? View.VISIBLE : View.GONE);
        aiEndTime.setVisibility(curSedentary.isSedentaryOnOff() ? View.VISIBLE : View.GONE);
//        aiNapStartTime.setVisibility(curSedentary.isSedentaryNap()?View.VISIBLE:View.GONE);
//        aiNapEndTime.setVisibility(curSedentary.isSedentaryNap()?View.VISIBLE:View.GONE);
    }

    @Override
    protected void initListener() {
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(getString(R.string.hint_saveing));
                Sedentary sedentary = new Sedentary(curSedentary.isSedentaryOnOff(), curSedentary.isSedentaryNap(),
                        curSedentary.getStartTime(), curSedentary.getEndTime(),
                        curSedentary.getNapStartTime(), curSedentary.getNapEndTime(), curSedentary.getSpace());
                ibandApplication.service.watch.setSedentaryAlert(sedentary, new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        curSedentary.save();
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_SEDENTARY, curSedentary.isSedentaryOnOff());
                        eventSend(EventGlobal.MSG_SEDENTARY_TOAST, getString(R.string.hint_save_success));
                        eventSend(EventGlobal.DATA_CHANGE_MENU);
                        dismissProgress();
                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        dismissProgress();
                        eventSend(EventGlobal.MSG_SEDENTARY_TOAST, getString(R.string.hint_save_fail));
                    }
                });
            }
        });
    }


    @OnClick({R.id.ai_onoff, R.id.ai_start_time, R.id.ai_end_time, R.id.ai_nap, R.id.ai_space})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ai_onoff:
                boolean onOff = !curSedentary.isSedentaryOnOff();
                curSedentary.setSedentaryOnOff(onOff);
                aiOnoff.setAlertCheck(onOff);
                aiStartTime.setVisibility(onOff ? View.VISIBLE : View.GONE);
                aiEndTime.setVisibility(onOff ? View.VISIBLE : View.GONE);
                isChange = true;
                break;
            case R.id.ai_start_time:
                int[] startInts = getTimeInt(curSedentary.getStartTime());
                new TimeDialog(mContext, startInts, getString(R.string.hint_alert_sedentary_time_start), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String startTime = hour + ":" + minute;
                        String check = checkTime(startTime, curSedentary.getEndTime());
                        if (check != null) {
                            eventSend(EventGlobal.MSG_SEDENTARY_TOAST, check);
                            return;
                        }
                        curSedentary.setStartTime(startTime);
                        aiStartTime.setAlertContent(startTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ai_end_time:
                int[] endInts = getTimeInt(curSedentary.getEndTime());
                new TimeDialog(mContext, endInts, getString(R.string.hint_alert_sedentary_time_end), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String endTime = hour + ":" + minute;
                        String check = checkTime(curSedentary.getStartTime(), endTime);
                        if (check != null) {
                            eventSend(EventGlobal.MSG_SEDENTARY_TOAST, check);
                            return;
                        }
                        curSedentary.setEndTime(endTime);
                        aiEndTime.setAlertContent(endTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ai_nap:
                boolean nap = !curSedentary.isSedentaryNap();
                curSedentary.setSedentaryNap(nap);
                aiNap.setAlertCheck(nap);
//                aiNapStartTime.setVisibility(nap ? View.VISIBLE : View.GONE);
//                aiNapEndTime.setVisibility(nap ? View.VISIBLE : View.GONE);
                isChange = true;
                break;
//            case R.id.ai_nap_start_time:
//                int[] startNapInts = getTimeInt(curSedentary.getNapStartTime());
//                new TimeDialog(mContext, startNapInts, getString(R.string.hint_alert_sedentary_time_start), new TimeDialog.TimeDialogListener() {
//                    @Override
//                    public void getTime(String hour, String minute) {
//                        String startTime = hour + ":" + minute;
//                        curSedentary.setNapStartTime(startTime);
//                        aiNapStartTime.setAlertContent(startTime);
//                        isChange = true;
//                    }
//                }).show();
//                break;
//            case R.id.ai_nap_end_time:
//                int[] endNapInts = getTimeInt(curSedentary.getNapEndTime());
//                new TimeDialog(mContext, endNapInts, getString(R.string.hint_alert_sedentary_time_start), new TimeDialog.TimeDialogListener() {
//                    @Override
//                    public void getTime(String hour, String minute) {
//                        String time = hour + ":" + minute;
//                        curSedentary.setNapEndTime(time);
//                        aiNapEndTime.setAlertContent(time);
//                        isChange = true;
//                    }
//                }).show();
//                break;
            case R.id.ai_space:
                new NumDialog(mContext, getSpaces(), curSedentary.getSpace() + "", getString(R.string.hint_space), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        int curSpace = Integer.valueOf(num);
                        curSedentary.setSpace(curSpace);
                        aiSpace.setAlertContent(num + getString(R.string.unit_min));
                    }
                }).show();
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.MSG_SEDENTARY_TOAST) {
            showToast(event.getMsg());
        }
    }

    private int[] getTimeInt(String time) {
        String[] times = time.split(":");
        int[] ints = new int[times.length];
        for (int i = 0; i < times.length; i++) {
            ints[i] = Integer.parseInt(times[i]);
        }
        return ints;
    }

    private String checkTime(String start, String end) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String result = null;
        try {
            Date startTime = dateFormat.parse(start);
            Date endTime = dateFormat.parse(end);
            if (startTime.after(endTime)) {
                result = getString(R.string.error_start_after);
                return result;
            }
            if (endTime.before(startTime)) {
                result = getString(R.string.error_end_before);
                return result;
            }
            if (endTime.getTime() - startTime.getTime() < 60 * 60 * 1000) {
                result = getString(R.string.error_time_space);
                return result;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String[] getSpaces() {
        return new String[]{"30", "45", "60", "90", "120"};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
