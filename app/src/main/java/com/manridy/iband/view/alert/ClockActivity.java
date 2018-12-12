package com.manridy.iband.view.alert;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.TimeDialog;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.ClockModel;
import com.manridy.iband.ui.items.ClockItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.bean.Clock;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;
import com.manridy.sdk.type.ClockType;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 闹钟提醒
 * Created by jarLiao on 17/5/4.
 */

public class ClockActivity extends BaseActionActivity {

    @BindView(R.id.ci_clock1)
    ClockItems ciClock1;
    @BindView(R.id.ci_clock2)
    ClockItems ciClock2;
    @BindView(R.id.ci_clock3)
    ClockItems ciClock3;
    @BindView(R.id.ci_clock4)
    ClockItems ciClock4;
    @BindView(R.id.ci_clock5)
    ClockItems ciClock5;
    @BindView(R.id.ci_clock6)
    ClockItems ciClock6;
    @BindView(R.id.ci_clock7)
    ClockItems ciClock7;
    @BindView(R.id.ci_clock8)
    ClockItems ciClock8;
    @BindView(R.id.ci_clock9)
    ClockItems ciClock9;
    @BindView(R.id.ci_clock10)
    ClockItems ciClock10;
    @BindView(R.id.ci_clock11)
    ClockItems ciClock11;
    @BindView(R.id.ci_clock12)
    ClockItems ciClock12;
    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.ll_additional_clock)
    LinearLayout llAdditionalClock;


    List<ClockModel> clockList = new ArrayList<>();
    int clockNum = 3;
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_clock);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.title_clock), getString(R.string.hint_save));

        String strDeviceList = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_LIST, "");
        String deviceType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
        String deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
        String deviceFirm = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "1.0.0");
        if("K2".equals(deviceName)&&"8007".equals(deviceType)&&deviceFirm.compareTo("1.5.6")>=0){
            clockNum = 12;
        }else{
            clockNum = 3;
        }
        initClock(clockNum);
        thread.start();
    }

    private void initClock(int clockNum) {
        clockList = IbandDB.getInstance().getClock();
        if(clockNum==3) {
            llAdditionalClock.setVisibility(View.GONE);
            if (clockList == null || clockList.size() == 0) {
                clockList = new ArrayList<>();
                clockList.add(new ClockModel("08:00", false));
                clockList.add(new ClockModel("08:30", false));
                clockList.add(new ClockModel("09:00", false));
            }
            ciClock1.setClockTime(clockList.get(0).getTime())
                    .setClockOnOff(clockList.get(0).isClockOnOFF());
            ciClock2.setClockTime(clockList.get(1).getTime())
                    .setClockOnOff(clockList.get(1).isClockOnOFF());
            ciClock3.setClockTime(clockList.get(2).getTime())
                    .setClockOnOff(clockList.get(2).isClockOnOFF());
        }else if(clockNum==12){
            llAdditionalClock.setVisibility(View.VISIBLE);
            if (clockList == null || clockList.size()==0) {
                clockList = new ArrayList<>();
                clockList.add(new ClockModel("08:00",false));
                clockList.add(new ClockModel("08:30",false));
                clockList.add(new ClockModel("09:00",false));
                clockList.add(new ClockModel("09:30",false));
                clockList.add(new ClockModel("10:00",false));
                clockList.add(new ClockModel("10:30",false));
                clockList.add(new ClockModel("11:00",false));
                clockList.add(new ClockModel("11:30",false));
                clockList.add(new ClockModel("12:00",false));
                clockList.add(new ClockModel("12:30",false));
                clockList.add(new ClockModel("13:00",false));
                clockList.add(new ClockModel("13:30",false));
            }else if(clockList.size()==3){
                clockList.add(new ClockModel("09:30",false));
                clockList.add(new ClockModel("10:00",false));
                clockList.add(new ClockModel("10:30",false));
                clockList.add(new ClockModel("11:00",false));
                clockList.add(new ClockModel("11:30",false));
                clockList.add(new ClockModel("12:00",false));
                clockList.add(new ClockModel("12:30",false));
                clockList.add(new ClockModel("13:00",false));
                clockList.add(new ClockModel("13:30",false));
            }
            ciClock1.setClockTime(clockList.get(0).getTime())
                    .setClockOnOff(clockList.get(0).isClockOnOFF());
            ciClock2.setClockTime(clockList.get(1).getTime())
                    .setClockOnOff(clockList.get(1).isClockOnOFF());
            ciClock3.setClockTime(clockList.get(2).getTime())
                    .setClockOnOff(clockList.get(2).isClockOnOFF());
            ciClock4.setClockTime(clockList.get(3).getTime())
                    .setClockOnOff(clockList.get(3).isClockOnOFF());
            ciClock5.setClockTime(clockList.get(4).getTime())
                    .setClockOnOff(clockList.get(4).isClockOnOFF());
            ciClock6.setClockTime(clockList.get(5).getTime())
                    .setClockOnOff(clockList.get(5).isClockOnOFF());
            ciClock7.setClockTime(clockList.get(6).getTime())
                    .setClockOnOff(clockList.get(6).isClockOnOFF());
            ciClock8.setClockTime(clockList.get(7).getTime())
                    .setClockOnOff(clockList.get(7).isClockOnOFF());
            ciClock9.setClockTime(clockList.get(8).getTime())
                    .setClockOnOff(clockList.get(8).isClockOnOFF());
            ciClock10.setClockTime(clockList.get(9).getTime())
                    .setClockOnOff(clockList.get(9).isClockOnOFF());
            ciClock11.setClockTime(clockList.get(10).getTime())
                    .setClockOnOff(clockList.get(10).isClockOnOFF());
            ciClock12.setClockTime(clockList.get(11).getTime())
                    .setClockOnOff(clockList.get(11).isClockOnOFF());
        }
    }

    static int errorSum_clock15 = 0;

    @Override
    protected void initListener() {
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorSum_clock15 = 0;
                showProgress(getString(R.string.hint_saveing));
                List<Clock> clocks = new ArrayList<>();
//                for (ClockModel model : clockList) {
//                    clocks.add(new Clock(model.getTime(),model.isClockOnOFF()));
//                }
                for (int i = 0; i < clockNum && i < clockList.size(); i++) {
                    ClockModel model = clockList.get(i);
                    clocks.add(new Clock(model.getTime(), model.isClockOnOFF()));
                }
                if(clockNum<=3){
                ibandApplication.service.watch.setClock(ClockType.SET_CLOCK, clocks, new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        int openNum = 0;
                        for (ClockModel clockModel : clockList) {
                            if (clockModel.isClockOnOFF()) {
                                openNum++;
                            }
                            clockModel.save();
                        }
                        SPUtil.put(mContext, AppGlobal.DATA_ALERT_CLOCK, openNum > 0);
                        eventSend(EventGlobal.MSG_CLOCK_TOAST, getString(R.string.hint_save_success));
                        eventSend(EventGlobal.DATA_CHANGE_MENU);
                        dismissProgress();
                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        eventSend(EventGlobal.MSG_CLOCK_TOAST, getString(R.string.hint_save_fail));
                        dismissProgress();
                    }
                });
            }else{
                    ibandApplication.service.watch.set15Clock(ClockType.SET_CLOCK, clocks, new BleCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            errorSum_clock15 = 0;
                            int openNum = 0;
                            for (ClockModel clockModel : clockList) {
                                if (clockModel.isClockOnOFF()) {
                                    openNum++;
                                }
                                clockModel.save();
                            }
                            SPUtil.put(mContext, AppGlobal.DATA_ALERT_CLOCK, openNum > 0);
                            eventSend(EventGlobal.MSG_CLOCK_TOAST, getString(R.string.hint_save_success));
                            eventSend(EventGlobal.DATA_CHANGE_MENU);
                            dismissProgress();
                            finish();
                        }

                        @Override
                        public void onFailure(BleException exception) {
                            errorSum_clock15++;
                            if(errorSum_clock15>=3) {
                                eventSend(EventGlobal.MSG_CLOCK_TOAST, getString(R.string.hint_save_fail));
                                errorSum_clock15 = 0;
                            }
                            dismissProgress();
                        }
                    });
                }
            }
        });
        ciClock1.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(0).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock2.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(1).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock3.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(2).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock4.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(3).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock5.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(4).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock6.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(5).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock7.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(6).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock8.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(7).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock9.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(8).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock10.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(9).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock11.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(10).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
        ciClock12.setCheckClockListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                clockList.get(11).setClockOnOFF(isChecked);
                isChange = true;
            }
        });
    }

    @OnClick({R.id.ci_clock1, R.id.ci_clock2, R.id.ci_clock3,R.id.ci_clock4,R.id.ci_clock5,R.id.ci_clock6,R.id.ci_clock7,R.id.ci_clock8,R.id.ci_clock9,R.id.ci_clock10,R.id.ci_clock11,R.id.ci_clock12})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ci_clock1:
                int[] time1 = getTimeInt(clockList.get(0).getTime());
                new TimeDialog(mContext, time1, getString(R.string.hint_clock1), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock1.setClockTime(clockTime);
                        clockList.get(0).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock2:
                int[] time2 = getTimeInt(clockList.get(1).getTime());
                new TimeDialog(mContext, time2, getString(R.string.hint_clock2), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock2.setClockTime(clockTime);
                        clockList.get(1).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock3:
                int[] time3 = getTimeInt(clockList.get(2).getTime());
                new TimeDialog(mContext, time3, getString(R.string.hint_clock3), new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock3.setClockTime(clockTime);
                        clockList.get(2).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock4:
                int[] time4 = getTimeInt(clockList.get(3).getTime());
                new TimeDialog(mContext, time4, "4", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock4.setClockTime(clockTime);
                        clockList.get(3).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock5:
                int[] time5 = getTimeInt(clockList.get(4).getTime());
                new TimeDialog(mContext, time5, "5", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock5.setClockTime(clockTime);
                        clockList.get(4).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock6:
                int[] time6 = getTimeInt(clockList.get(5).getTime());
                new TimeDialog(mContext, time6, "6", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock6.setClockTime(clockTime);
                        clockList.get(5).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock7:
                int[] time7 = getTimeInt(clockList.get(6).getTime());
                new TimeDialog(mContext, time7, "7", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock7.setClockTime(clockTime);
                        clockList.get(6).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock8:
                int[] time8 = getTimeInt(clockList.get(7).getTime());
                new TimeDialog(mContext, time8, "8", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock8.setClockTime(clockTime);
                        clockList.get(7).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock9:
                int[] time9 = getTimeInt(clockList.get(8).getTime());
                new TimeDialog(mContext, time9, "9", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock9.setClockTime(clockTime);
                        clockList.get(8).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock10:
                int[] time10 = getTimeInt(clockList.get(9).getTime());
                new TimeDialog(mContext, time10, "10", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock10.setClockTime(clockTime);
                        clockList.get(9).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock11:
                int[] time11 = getTimeInt(clockList.get(10).getTime());
                new TimeDialog(mContext, time11, "11", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock11.setClockTime(clockTime);
                        clockList.get(10).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.ci_clock12:
                int[] time12 = getTimeInt(clockList.get(11).getTime());
                new TimeDialog(mContext, time12, "12", new TimeDialog.TimeDialogListener() {
                    @Override
                    public void getTime(String hour, String minute) {
                        String clockTime = hour + ":" + minute;
                        ciClock12.setClockTime(clockTime);
                        clockList.get(11).setTime(clockTime);
                        isChange = true;
                    }
                }).show();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_CHANGE_MINUTE) {
            ciClock1.setClockTime(clockList.get(0).getTime());
            ciClock2.setClockTime(clockList.get(1).getTime());
            ciClock3.setClockTime(clockList.get(2).getTime());
            ciClock4.setClockTime(clockList.get(3).getTime());
            ciClock5.setClockTime(clockList.get(4).getTime());
            ciClock6.setClockTime(clockList.get(5).getTime());
            ciClock7.setClockTime(clockList.get(6).getTime());
            ciClock8.setClockTime(clockList.get(7).getTime());
            ciClock9.setClockTime(clockList.get(8).getTime());
            ciClock10.setClockTime(clockList.get(9).getTime());
            ciClock11.setClockTime(clockList.get(10).getTime());
            ciClock12.setClockTime(clockList.get(11).getTime());
        }else if (event.getWhat() == EventGlobal.MSG_CLOCK_TOAST){
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

    boolean isExit;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!isExit){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss");
                String s = simpleDateFormat.format(new Date());
                if (s.equals("00")) {
                    eventSend(EventGlobal.DATA_CHANGE_MINUTE);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isExit = true;
        thread.interrupt();
        thread = null;
    }
}
