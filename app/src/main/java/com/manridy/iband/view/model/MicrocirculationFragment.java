package com.manridy.iband.view.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.manridy.applib.utils.LogUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.MicrocirculationModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.history.MicroHistoryActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 微循环
 * Created by yw on 2018/12/25.
 */

public class MicrocirculationFragment extends BaseEventFragment {
    private static final String TAG = "MicroFragment";

    @BindView(R.id.bt_microcirculation_test)
    Button btMicrocirculationTest;
    Unbinder unbinder;
    @BindView(R.id.cv_microcirculation)
    CircularView cvMicrocirculation;
    @BindView(R.id.lc_microcirculation)
    LineChart lcMicrocirculation;
    @BindView(R.id.iv_history)
    ImageView ivHistory;
    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.tv_end)
    TextView tvEnd;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    private MicrocirculationModel curMicro;
    private List<MicrocirculationModel> curMicroList;
    boolean isTestData = true;
    private static final float yAxisMaxNum = 0.115f;  //y轴最大值
    public static final float f_MicroPointNum = 10000;//4位小数

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_microcirculation, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {
        initChartView(lcMicrocirculation);
        initChartAxis(lcMicrocirculation);
    }

    @Override
    protected void initListener() {
        if (IbandApplication.getIntance().service.watch != null) {
            IbandApplication.getIntance().service.watch.setMicroNotifyListener(new BleNotifyListener() {
                @Override
                public void onNotify(Object o) {
                    curMicro = new Gson().fromJson(o.toString(), MicrocirculationModel.class);
//                    Log.d(TAG, "onNotify: " + o);
                    if (isTestData) {
                        curMicroList = new ArrayList<>();
                        EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_MICRO_TEST));
                    }
                    isTestData = false;
                    curMicroList.add(curMicro);
                    EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_MICRO));
                }
            });
        }

        btMicrocirculationTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMicroData();
                if (btMicrocirculationTest.getText().equals(getString(R.string.hint_test))) {
                    IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setMcroTest(1), new BleCallback() {
                        @Override
                        public void onSuccess(Object o) {
                            EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_MICRO_TEST, o));
                        }

                        @Override
                        public void onFailure(BleException exception) {

                        }
                    });
                } else {
                    IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setMcroTest(0));
                    EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_MICRO_TESTED));
                }
            }
        });
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_MICRO));
    }

    private void setDataItem() {
        avarMicro = maxMicro = minMicro = 0;
        if (curMicroList.size() > 0) {
            minMicro = curMicro.getMicro();
            for (MicrocirculationModel heartModel : curMicroList) {
                float hr = heartModel.getMicro();
                avarMicro += hr;
                maxMicro = maxMicro > hr ? maxMicro : hr;
                minMicro = minMicro < hr ? minMicro : hr;
            }
            avarMicro /= curMicroList.size();
            avarMicro = (float) Math.round(avarMicro * f_MicroPointNum) / f_MicroPointNum;//保留三位小数

            String start = curMicroList.get(0).getDate().substring(11, 19);
            String end = curMicroList.get(curMicroList.size() - 1).getDate().substring(11, 19);
            tvStart.setText(start);
            tvEnd.setText(end);
        }
        diData1.setItemData(getString(R.string.hint_average_cycle), avarMicro + "");
        diData2.setItemData(getString(R.string.hint_minimum_cycle), minMicro + "");
        diData3.setItemData(getString(R.string.hint_highest_cycle), maxMicro + "");
    }

    private float minMicro = 1.0f;
    private float maxMicro = 0.0f;
    private float totalMicro = 0.0f;
    private float avarMicro = 0.0f;
    private int microNum = 0;

    private void initMicroData() {
        minMicro = 1.0f;
        maxMicro = 0.0f;
        totalMicro = 0.0f;
        avarMicro = 0.0f;
        microNum = 0;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_MICRO_TEST) {
//            String sData = event.getObject().toString();
//            Log.i(TAG, "onMainEvent: " + sData);
            btMicrocirculationTest.setText(getString(R.string.hint_stop));
            cvMicrocirculation.setTitle(getString(R.string.hint_hr_testing)).invaliDate();
        } else if (event.getWhat() == EventGlobal.ACTION_MICRO_TESTED) {
            btMicrocirculationTest.setText(getString(R.string.hint_test));
            cvMicrocirculation.setTitle(getString(R.string.hint_last_hr)).invaliDate();
            isTestData = true;
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_SYNC_HISTORY));
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_MICRO) {
//            MicrocirculationModel microcirculation = (MicrocirculationModel) event.getObject();
//            float micro = microcirculation.getMicro();
//            if (maxMicro < micro) {
//                maxMicro = micro;//最大循环
//            }
//            if (minMicro > micro) {
//                minMicro = micro;//最小循环
//            }
//            microNum++;//数据条数
//            totalMicro += micro;
//            avarMicro = totalMicro / microNum;//平均循环数
//            avarMicro = (float) Math.round(avarMicro * 1000) / 1000;//保留三位小数
//            diData1.setItemData(avarMicro + "");
//            diData2.setItemData(minMicro + "");
//            diData3.setItemData(maxMicro + "");
//            cvMicrocirculation.setText(String.valueOf(micro)).invaliDate();
            setCircularView();
            updateChartView(lcMicrocirculation, curMicroList);
            tvEmpty.setVisibility(curMicroList.size() == 0 ? View.VISIBLE : View.GONE);

            setDataItem();
        }

    }

    private void setCircularView() {
        if (curMicro == null) {
            return;
        }
        float micro = (float) Math.round(curMicro.getMicro() * f_MicroPointNum) / f_MicroPointNum;//保留4位小数
        String text = micro + "";
        float progress = ((curMicro.getMicro() / yAxisMaxNum) * 100);
//        cvHr.setProgressWithAnimation(progress);
        cvMicrocirculation.setText(text)
                .setProgress(progress)
                .invaliDate();
        cvMicrocirculation.setProgressWithAnimation(progress);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_LOAD_MICRO) {
            curMicro = IbandDB.getInstance().getLastMicro();
            curMicroList = IbandDB.getInstance().getLastsMicro();
            Collections.reverse(curMicroList); // 倒序排列
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_MICRO));
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ALL) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_MICRO));
        }
    }

    private void initChartView(LineChart chart) {
        chart.getDescription().setEnabled(false);//描述设置
        chart.setNoDataText("");//无数据时描述
        chart.setTouchEnabled(true);//可接触
        chart.setDrawMarkers(true);
        chart.setDrawBorders(true);  //是否在折线图上添加边框
        chart.setDragEnabled(false);//可拖拽
        chart.setScaleEnabled(false);//可缩放
        chart.setDoubleTapToZoomEnabled(false);//双击移动
        chart.setScaleYEnabled(false);//滑动
        chart.setDrawGridBackground(false);//画网格背景
        chart.setDrawBorders(false);  //是否在折线图上添加边框
        chart.setPinchZoom(false);//设置少量移动
        chart.setOnChartValueSelectedListener(selectedListener);
        chart.getLegend().setEnabled(false);
        chart.setData(new LineData());

    }

    private void initChartAxis(LineChart chart) {
        //x轴坐标
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);//显示x轴
        xAxis.setTextColor(Color.BLACK);//x轴文字颜色
        xAxis.setTextSize(12f);//x轴文字大小
        xAxis.setDrawGridLines(false);//取消网格线
        xAxis.setDrawAxisLine(true);//取消x轴底线
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴位置
        xAxis.setAxisMinimum(0);//设置最小点
        xAxis.setGranularity(1f);//设置间隔
        //Y轴坐标
        YAxis yAxis = chart.getAxisLeft();
//        yAxis.setAxisMinimum(0);//设置y轴最小点
        yAxis.setAxisMaximum(yAxisMaxNum);
        yAxis.setDrawAxisLine(true);//画坐标线
        yAxis.setDrawLabels(true);//画坐标下标
        yAxis.setDrawGridLines(false);//设置网格线
        yAxis.setDrawZeroLine(false);
        yAxis.setEnabled(true);//显示Y轴
        chart.getAxisRight().setEnabled(false);//不显示右侧
    }

    private LineDataSet getInitChartDataSet() {
        LineDataSet set = new LineDataSet(null, "");//初始化折线数据源
        set.setColor(Color.parseColor("#deef5350"));//折线颜色
        set.setLineWidth(1.5f);//折线宽度
//        set.setValueTextColor(Color.BLACK);//折线值文字颜色
//        set.setDrawCircleHole(false);
        set.setCircleRadius(3f);
        set.setValueTextSize(12f);//折线值文字大小
        set.setCircleColorHole(Color.parseColor("#deef5350"));
        set.setCircleColor(Color.parseColor("#deef5350"));//设置圆点颜色
        set.setDrawHighlightIndicators(false);
        set.setDrawValues(false);//显示颜色值
        return set;
    }

    private void updateChartView(LineChart chart, List<MicrocirculationModel> curMicroList) {
        if (curMicroList == null || curMicroList.size() <= 0) {
            return;
        }
        LineData data = new LineData(getInitChartDataSet());
        if (chart.getData() != null) {
            chart.clearValues();
        }
        //判断数据数量
        for (int i = 0; i < curMicroList.size(); i++) {
            data.addEntry(new Entry(i, curMicroList.get(i).getMicro()), 0);
        }
        chart.setData(data);
        // 像ListView那样的通知数据更新
//        mvHeart.setHeartList(heartList);
//        chart.setMarker(mvHeart);
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMinimum(15);
        chart.setVisibleXRangeMaximum(15);
        chart.moveViewToX(data.getEntryCount());
    }


    OnChartValueSelectedListener selectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {

            int x = e.getX() > 0 ? (int) e.getX() : 0;
            LogUtil.d(TAG, "onValueSelected() called with: e = [" + e.getX() + "], h = [" + x + "]");
            if (curMicroList != null && curMicroList.size() >= x) {
                MicrocirculationModel heartModel = curMicroList.get(x > 0 ? x - 1 : 0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String times = "00:00";
                try {
                    Date date = dateFormat.parse(heartModel.getDate());
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
                    times = dateFormat2.format(date);
                } catch (Exception e1) {

                }
                diData1.setItemData(getString(R.string.hint_time), times, false);
                diData2.setItemData("", "", false);
                diData3.setItemData(getString(R.string.hint_microcirculation), String.valueOf(heartModel.getMicro()));
            }
        }

        @Override
        public void onNothingSelected() {
            setDataItem();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.iv_history)
    public void onHistoryViewClicked() {
        Intent intent = new Intent(mContext, MicroHistoryActivity.class);
        intent.putExtra("history_type", 4);
        startActivity(intent);
    }
}
