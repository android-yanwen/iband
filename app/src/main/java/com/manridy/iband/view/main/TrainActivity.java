package com.manridy.iband.view.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.applib.view.dialog.DateDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.adapter.TrainAdapter;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 训练
 * Created by jarLiao on 17/5/11.
 */

public class TrainActivity extends BaseActionActivity {
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.iv_left)
    ImageView ivLeft;
    @BindView(R.id.iv_right)
    ImageView ivRight;
    @BindView(R.id.iv_cal)
    ImageView ivCal;
    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.tv_end)
    TextView tvEnd;
    @BindView(R.id.rv_train)
    RecyclerView rvTrain;
    @BindView(R.id.bc_run)
    BarChart bcRun;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    List<StepModel> curRunList = new ArrayList<>();
    TrainAdapter trainAdapter;
    String day;
    SimpleDateFormat hourFormat;
    SimpleDateFormat dayFormat;
    Calendar mCalendar;


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_train);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        hourFormat = new SimpleDateFormat("HH:mm");
        dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        mCalendar = Calendar.getInstance();
        setTitleBar(getString(R.string.hint_tarin), Color.parseColor("#009688"));
        setStatusBarColor(Color.parseColor("#009688"));
        trainAdapter = new TrainAdapter(curRunList);
        if(curRunList!=null&&curRunList.size()>0){
            Date date = curRunList.get(0).getStepDate();
            if(date!=null) {
                tvStart.setText(hourFormat.format(date));
                String endTime = hourFormat.format(curRunList.get(curRunList.size() - 1).getStepDate().getTime() + curRunList.get(curRunList.size() - 1).getStepTime() * 60 * 1000);
                tvEnd.setText(endTime);
            }
        }
        trainAdapter.setOnItemClickListener(new TrainAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(curRunList!=null&&curRunList.size()>position){
                    List<StepModel> RunList = new ArrayList<>(curRunList);
                    Collections.reverse(RunList);
                    StepModel stepModel = RunList.get(position);
                    if(stepModel.getSportMode()==1001||stepModel.getSportMode()==1003){
                        if("GaoDe".equals(stepModel.getMap())) {
                            Intent intent = new Intent(TrainActivity.this, AMapPlaybackActivity.class);
                            intent.putExtra("StepDate", stepModel.getStepDate());
                            startActivity(intent);
                        }else if("google".equals(stepModel.getMap())){
                            Intent intent = new Intent(TrainActivity.this, GoogleMapPlaybackActivity.class);
                            intent.putExtra("StepDate", stepModel.getStepDate());
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(TrainActivity.this, AMapPlaybackActivity.class);
                            intent.putExtra("StepDate", stepModel.getStepDate());
                            startActivity(intent);
                        }
                    }
                }
            }
        });
        rvTrain.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        rvTrain.setAdapter(trainAdapter);
        initChartView(bcRun);
        initChartAxis(bcRun);
    }

    @Override
    protected void loadData() {
        super.loadData();
        day = TimeUtil.getNowYMD();
        tvTime.setText(day);
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_RUN));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_RUN) {
            List<StepModel> RunList = new ArrayList<>(curRunList);
            Collections.reverse(RunList); // 倒序排列
            trainAdapter.setItemList(RunList);
            tvEmpty.setVisibility(RunList.size() == 0?View.VISIBLE:View.GONE);
            updateChartView(bcRun, curRunList);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_LOAD_RUN) {
            curRunList = IbandDB.getInstance().getCurRunStep(day);
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_RUN));
            hourFormat = new SimpleDateFormat("HH:mm");
            dayFormat = new SimpleDateFormat("yyyy-MM-dd");
//            trainAdapter = new TrainAdapter(curRunList);
            if(curRunList!=null&&curRunList.size()>0){
                Date date = curRunList.get(0).getStepDate();
                if(date!=null) {
                    startTime = hourFormat.format(date);
//                    tvStart.setText(hourFormat.format(date));
                    endTime = hourFormat.format(curRunList.get(curRunList.size() - 1).getStepDate().getTime() + curRunList.get(curRunList.size() - 1).getStepTime() * 60 * 1000);
//                    tvEnd.setText(endTime);
                    Message message = handler.obtainMessage();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ALL) {
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_RUN));
        }
    }

    String startTime;
    String endTime;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(startTime!=null){
                        tvStart.setText(startTime);
                    }
                    if(endTime!=null){
                        tvEnd.setText(endTime);
                    }
                    break;
            }
        }
    };

    @Override
    protected void initListener() {
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.DAY_OF_MONTH, -1);//取当前日期的前一天.
                day = dayFormat.format(mCalendar.getTime());
                tvTime.setText(day);
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_RUN));
            }
        });

        ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendar.add(Calendar.DAY_OF_MONTH, +1);//取当前日期的前一天.
                day = dayFormat.format(mCalendar.getTime());
                if (!TimeUtil.compareNowYMD(mCalendar.getTime())) {
                    tvTime.setText(day);
                    EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_RUN));
                } else {
                    mCalendar.add(Calendar.DAY_OF_MONTH, -1);
                    showToast(getString(R.string.hint_day_max));
                }
            }
        });

        ivCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] times = TimeUtil.getYMDtoInt(day);
                new DateDialog(mContext, times, getString(R.string.hint_select_day), new DateDialog.DateDialogListener() {
                    @Override
                    public void getTime(int year, int monthOfYear, int dayOfMonth) {
                        String time = year + "-" + TimeUtil.zero(monthOfYear + 1) + "-" + TimeUtil.zero(dayOfMonth);
                        mCalendar.set(year, monthOfYear, dayOfMonth);
                        tvTime.setText(time);
                        day = time;
                        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_RUN));
                    }
                }).show();
            }
        });
    }

    private void initChartView(BarChart chart) {
        chart.getDescription().setEnabled(false);//描述设置
        chart.setNoDataText("无数据");//无数据时描述
        chart.setTouchEnabled(true);//可接触
        chart.setDrawMarkers(true);
        chart.setDrawBorders(true);  //是否在折线图上添加边框
        chart.setDragEnabled(false);//可拖拽
        chart.setScaleEnabled(false);//可缩放
        chart.setDoubleTapToZoomEnabled(false);//双击移动
        chart.setScaleYEnabled(true);//滑动
        chart.setDrawGridBackground(false);//画网格背景
        chart.setDrawBorders(false);  //是否在折线图上添加边框
        chart.setPinchZoom(false);//设置少量移动
        chart.setOnChartValueSelectedListener(selectedListener);
        chart.getLegend().setEnabled(false);
        chart.setData(new BarData());
    }


    private void initChartAxis(BarChart chart) {
        //x轴坐标
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);//显示x轴
        xAxis.setTextColor(Color.BLACK);//x轴文字颜色
        xAxis.setTextSize(12f);//x轴文字大小
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);//取消网格线
        xAxis.setDrawAxisLine(false);//取消x轴底线
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴位置
        xAxis.setAxisMinimum(1);//设置最小点
        xAxis.setGranularity(1f);//设置间隔
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) value);
            }

        });
        //Y轴坐标
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0);//设置y轴最小点
//        yAxis.setAxisMaximum(24*60);
//        yAxis.setLabelCount(7,false);
        yAxis.setDrawAxisLine(false);//画坐标线
        yAxis.setDrawLabels(false);//画坐标下标
        yAxis.setDrawGridLines(false);//设置网格线
//        yAxis.setValueFormatter(new HourValueFormatter());
        yAxis.setDrawZeroLine(false);
        yAxis.setEnabled(true);//显示Y轴
        chart.getAxisRight().setEnabled(false);//不显示右侧
    }

    private void updateChartView(BarChart chart, List<StepModel> dayData) {
        if (chart.getData() != null) {
            chart.clearValues();
        }
        if (dayData == null) {
            return;
        }
//        List<BarEntry> sumList = new ArrayList<>();
        BarDataSet set = getInitChartDataSet();
        int[] colors = new int[dayData.size()];
        for (int i = 0; i < dayData.size(); i++) {
            BarEntry barEntry = new BarEntry(i + 1, new float[]{dayData.get(i).getStepCalorie()});
            if (dayData.get(i).getSportMode() == 0) {
                colors[i] = Color.parseColor("#ab00d6f9");
            } else if (dayData.get(i).getSportMode() == 1) {
                colors[i] = Color.parseColor("#abf45334");
            } else if (dayData.get(i).getSportMode() == 2) {
                colors[i] = Color.parseColor("#ab0736e2");
            } else if (dayData.get(i).getSportMode() == 3) {
                colors[i] = Color.parseColor("#abd83232");
            } else if (dayData.get(i).getSportMode() == 4) {
                colors[i] = Color.parseColor("#abc65e18");
            } else if (dayData.get(i).getSportMode() == 1001){
                colors[i] = Color.parseColor("#1CA196");
            } else if(dayData.get(i).getSportMode() == 1002){
                colors[i] = Color.parseColor("#1E877C");
            } else if(dayData.get(i).getSportMode() == 1003){
                colors[i] = Color.parseColor("#E55F37");
            }
            set.addEntry(barEntry);
        }
        set.setColors(colors);
        BarData barData = new BarData(set);
        chart.setData(barData);
        chart.setVisibleXRangeMinimum(15);
        chart.notifyDataSetChanged();
        chart.moveViewToX(barData.getEntryCount() - 15);
    }

    private BarDataSet getInitChartDataSet() {
        BarDataSet set = new BarDataSet(new ArrayList<BarEntry>(), "");//初始化折线数据源
//        set.setColor(Color.parseColor("#8affffff"));//折线颜色
        set.setBarBorderWidth(2f);//
        set.setBarBorderColor(Color.TRANSPARENT);
        set.setValueTextSize(12f);//折线值文字大小
        set.setDrawValues(false);
        return set;
    }


    OnChartValueSelectedListener selectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            if (curRunList != null && curRunList.size() >= e.getX()) {
                StepModel stepModel = curRunList.get((int) e.getX() > 0 ? (int) e.getX() - 1 : 0);
                String start = hourFormat.format(stepModel.getStepDate());
                String end = hourFormat.format((stepModel.getStepDate().getTime() + stepModel.getStepTime() * 60 * 1000));

                String num = stepModel.getStepNum() + getString(R.string.hint_unit_step);
                String mode = getString(R.string.hint_run);
                if (stepModel.getSportMode() == 1) {
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_cycling);
                } else if (stepModel.getSportMode() == 2) {
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_swim);
                } else if (stepModel.getSportMode() == 2) {
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_swim);
                } else if(stepModel.getSportMode() == 1001){
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_outdoors_run);
                } else if(stepModel.getSportMode() == 1002){
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_indoors_run);
                } else if(stepModel.getSportMode() == 1003){
                    num = stepModel.getStepCalorie() + getString(R.string.hint_unit_ka);
                    mode = getString(R.string.hint_cycling);
                }
                tvHint.setText(mode + " " + start + "~" + end + " " + num);
            }
        }

        @Override
        public void onNothingSelected() {
            tvHint.setText("");
        }
    };

}
