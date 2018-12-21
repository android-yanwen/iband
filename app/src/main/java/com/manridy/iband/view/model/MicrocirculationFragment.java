package com.manridy.iband.view.model;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.manridy.iband.R;
import com.manridy.iband.bean.HeartModel;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseEventFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.ButterKnife;

/**
 * 微循环
 * Created by jarLiao on 2016/10/24.
 */

public class MicrocirculationFragment extends BaseEventFragment {


    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_microcirculation, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    public void initData(Bundle savedInstanceState) {
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {

    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
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
        yAxis.setAxisMaximum(220f);
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

    private void updateChartView(LineChart chart, List<HeartModel> heartList) {
        if (heartList == null || heartList.size() <= 0) {
            return;
        }
        LineData data = new LineData(getInitChartDataSet());
        if (chart.getData() != null) {
            chart.clearValues();
        }
        //判断数据数量
        for (int i = 0; i < heartList.size(); i++) {
            data.addEntry(new Entry(i, heartList.get(i).getHeartRate()), 0);
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

        }

        @Override
        public void onNothingSelected() {
        }
    };

}
