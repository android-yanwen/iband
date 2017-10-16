package com.manridy.iband.view.model;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.SleepModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.ChartView;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.history.SleepHistoryActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.manridy.applib.base.BaseActivity.isFastDoubleClick;

/**
 * 睡眠
 * Created by jarLiao on 2016/10/24.
 */

public class SleepFragment extends BaseEventFragment {

    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.cv_sleep)
    CircularView cvSleep;
    @BindView(R.id.chart_sleep)
    ChartView chartSleep;

    List<SleepModel> curSleeps;
    int[] colors,selectColors;
    @BindView(R.id.tv_time_start)
    TextView tvTimeStart;
    @BindView(R.id.tv_time_end)
    TextView tvTimeEnd;

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sleep, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {
        colors = new int[]{Color.parseColor("#8a311b92"), Color.parseColor("#614527a0"), Color.parseColor("#8affbc00")};
        selectColors = new int[]{ Color.parseColor("#de311b92"),Color.parseColor("#ab4527a0"), Color.parseColor("#deffbc00")};
    }

    @Override
    protected void initListener() {
        chartSleep.setOnChartItemSelectListener(new ChartView.onChartItemSelectListener() {
            @Override
            public void onItemSelect(int position) {
                setSelectDataItem(position);
            }

            @Override
            public void onNoSelect() {
                setDataItem();
            }
        });
        cvSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                IbandApplication.getIntance().service.watch.sendCmd(new byte[]{(byte) 0xfc,0x0c,0x03});
            }
        });
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_SLEEP));
    }

    @OnClick({R.id.iv_history})
    public void onClick(View view) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_history:
                startActivity(SleepHistoryActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_SLEEP) {
            setCircularView();
            chartSleep.setChartData(colors,selectColors ,curSleeps).invaliDate();
            setDataItem();
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_LOAD_SLEEP) {
            curSleeps = IbandDB.getInstance().getCurSleeps();
            curSleeps = getFilterRepeatList(curSleeps);
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_SLEEP));
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ALL) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_SLEEP));
        }
    }

    @NonNull
    private List<SleepModel> getFilterRepeatList( List<SleepModel> curSleeps) {
        List<SleepModel> sleepList = new ArrayList<>();
        if (curSleeps.size()>0) {
            SleepModel curSleep = curSleeps.get(0);//获取第一条数据
            for (int i = 1; i < curSleeps.size(); i++) {//循环比较是否类型相同
                SleepModel nextSleep = curSleeps.get(i);//获取下一条数据
                if (curSleep.getSleepDataType() == nextSleep.getSleepDataType()) {//比较类型相同
                    //类型相同时长累加
                    if (curSleep.getSleepDataType() == 1) {//相同浅睡
                        nextSleep.setSleepDeep(curSleep.getSleepDeep()+nextSleep.getSleepDeep());
                    }else if (curSleep.getSleepDataType() == 2){//相同深睡
                        nextSleep.setSleepLight(curSleep.getSleepLight()+nextSleep.getSleepLight());
                    }else if (curSleep.getSleepDataType() == 3){//相同清醒
                        nextSleep.setSleepAwake(curSleep.getSleepAwake()+nextSleep.getSleepAwake());
                    }
                    nextSleep.setSleepStartTime(curSleep.getSleepStartTime());//开始时间赋给下一条
                }else {//类型不同添加到集合
                    sleepList.add(curSleep);
                }
                curSleep = nextSleep;//下一条赋给当前，继续比较
            }
            sleepList.add(curSleep);//最后一条添加到集合
        }
        return sleepList;
    }

    private void setCircularView() {
        if (curSleeps == null || curSleeps.size() == 0) {
            return;
        }
        int target = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_TARGET_SLEEP, 8);
        int sleepSum = 0, sleepLight = 0, sleepDeep = 0;
        for (SleepModel curSleep : curSleeps) {
            sleepLight += curSleep.getSleepLight();
            sleepDeep += curSleep.getSleepDeep();
        }
        sleepSum += (sleepLight + sleepDeep);
        double dou = TimeUtil.getHourDouble(sleepLight) + TimeUtil.getHourDouble(sleepDeep);
        String sum = String .format("%.1f", dou);
        String state = getString(R.string.hint_sleep_deep) + getHour(sleepDeep) + getString(R.string.hint_sleep_light1) + getHour(sleepLight);
        float progress = (sleepSum / (float) (target * 60)) * 100;
        cvSleep.setText(sum)
                .setState(state)
                .setProgress(progress)
                .invaliDate();
//        cvSleep.setProgressWithAnimation(progress);
    }

    private void setSelectDataItem(int position) {
        if (curSleeps == null || curSleeps.size() == 0) {
            return;
        }
        SleepModel sleepModel = curSleeps.get(position);
        String start = sleepModel.getSleepStartTime();
        String end = sleepModel.getSleepEndTime();
        int type = sleepModel.getSleepDataType() ;
        int min = 0;
        String title = "";
        if (type == 1) {
            min = sleepModel.getSleepDeep();
            title = getString(R.string.hint_sleep_deep);
        }else if (type == 2){
            min = sleepModel.getSleepLight();
            title = getString(R.string.hint_sleep_light);
        }else if (type == 3){
            min = sleepModel.getSleepAwake();
            title = getString(R.string.hint_sleep_awake);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-DD HH:mm");
        try {
            Date dateStart = simpleDateFormat.parse(start);
            Date dateEnd = simpleDateFormat.parse(end);
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");
            String startTime = simpleDateFormat2.format(dateStart);
            String endTime = simpleDateFormat2.format(dateEnd);
            String m = String.format("%.1f", ((double) min / 60));
            diData1.setItemData(title + getString(R.string.hint_start), startTime);
            diData2.setItemData(title + getString(R.string.hint_end), endTime);
            diData3.setItemData(title + getString(R.string.hint_times), m);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setDataItem() {
        if (curSleeps == null || curSleeps.size() == 0) {
            return;
        }
        String start = curSleeps.get(0).getSleepStartTime();
        String end = curSleeps.get(curSleeps.size() - 1).getSleepEndTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-DD HH:mm");
        int awake = getAwake(curSleeps);
        String str = String.format("%.1f", ((double) awake / 60));
        try {
            Date dateStart = simpleDateFormat.parse(start);
            Date dateEnd = simpleDateFormat.parse(end);
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("HH:mm");
            String startTime = simpleDateFormat2.format(dateStart);
            String endTime = simpleDateFormat2.format(dateEnd);
            diData1.setItemData(getString(R.string.hint_sleep_start), startTime);
            diData2.setItemData(getString(R.string.hint_sleep_end), endTime);
            diData3.setItemData(getString(R.string.hint_sleep_sober),str,getString(R.string.hint_unit_sleep));
            tvTimeStart.setText(startTime);
            tvTimeEnd.setText(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int getAwake(List<SleepModel> curSleeps) {
        int awake = 0;
        for (SleepModel curSleep : curSleeps) {
            if (curSleep.getSleepDataType() == 3) {
                awake += curSleep.getSleepAwake();
            }
        }
        return awake;
    }

    private String getHour(int time) {
        String str ;
        if (time<60) {
            str = time + getString(R.string.unit_min);
        }else {
            str =  String .format("%.1f", ((double)time/60))+getString(R.string.hint_unit_sleep);
        }
        return str;
    }

}
