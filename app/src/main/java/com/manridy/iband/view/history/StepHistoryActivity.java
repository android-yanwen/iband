package com.manridy.iband.view.history;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.applib.view.dialog.DateDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.DayBean;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 计步历史
 * Created by jarLiao on 17/5/11.
 */

public class StepHistoryActivity extends BaseActionActivity {


    @BindView(R.id.cv_history_step)
    CircularView cvHistoryStep;
    @BindView(R.id.tv_month)
    TextView tvMonth;
    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.bc_history_step)
    BarChart bcHistoryStep;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.tb_share)
    ImageView ivShare;

    private String filePath;

    private Calendar mCalendar;
    private SimpleDateFormat mDateFormat;
    private List<String> days = new ArrayList<>();
    private List<DayBean> stepList = new ArrayList<>();
    private int stepNum = 0, stepMi = 0, stepKa = 0,stepCount = 0;
    private int unit;
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_history_step);
        ButterKnife.bind(this);
        ivShare.setVisibility(View.VISIBLE);
    }

    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    ivShare.setEnabled(false);
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessageDelayed(message,1500);
                    showShare();
                    break;
                case 2:
                    ivShare.setEnabled(true);
                    break;
            }
        }
    };

    public void screenShot(){
        handler2.post(new Runnable() {
            @Override
            public void run() {
                View dView = getWindow().getDecorView();
                dView.setDrawingCacheEnabled(false);
                dView.destroyDrawingCache();
                dView.buildDrawingCache();
                Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
                if (bitmap != null) {
                    try {
                        // 获取内置SD卡路径
                        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//                        String sdCardPath = getBaseContext().getCacheDir().getPath();
                        // 图片文件路径
                        filePath = sdCardPath + File.separator + "share_screenshot_"+System.currentTimeMillis()+".png";
                        File file = new File(filePath);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
//                        if(file.exists()){
//                            file.delete();
//                        }
                        FileOutputStream os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                        os.close();
                        Message message = handler2.obtainMessage();
                        message.what = 1;
                        handler2.sendMessageDelayed(message,500);
                    } catch (Exception e) {
                    }
                }
            }
        });


    }

    private void showShare() {

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，微信、QQ和QQ空间等平台使用
//        oks.setTitle(getString(R.string.share));
//        oks.setTitle("分享");
        // titleUrl QQ和QQ空间跳转链接
//        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
//        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

//        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//        // 图片文件路径
//        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
        File file = new File(filePath);
        if(file.exists()){
            oks.setImagePath(filePath);//确保SDcard下面存在此张图片
            // url在微信、微博，Facebook等平台中使用
//        oks.setUrl("http://sharesdk.cn");
            // comment是我对这条分享的评论，仅在人人网使用
//        oks.setComment("我是测试评论文本");
            // 启动分享GUI

//            oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
//                @Override
//                public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
//                    if (platform.getName().equalsIgnoreCase(QQ.NAME)) {
//                        paramsToShare.setText(null);
//                        paramsToShare.setTitle(null);
//                        paramsToShare.setTitleUrl(null);
//
////                        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
////                        // 图片文件路径
////                        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
//                        paramsToShare.setImagePath(filePath);
//                    }
//                }
//            });
            oks.setCallback(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onCancel(Platform platform, int i) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }
            });
            oks.show(this);
        }

    }

    @Override
    protected void initVariables() {
        setTitleBar(getString(R.string.hint_step_history));
        registerEventBus();
        unit = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT,0);
        mCalendar = Calendar.getInstance();
        mDateFormat = new SimpleDateFormat("yyyy-MM");
        initChartView(bcHistoryStep);
        initChartAxis(bcHistoryStep);
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_STEP_HISTORY));
    }


    @Override
    protected void initListener() {
        ivShare.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                screenShot();
//                showShare();
            }
        });

        tvMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time;
                int[] times = new int[]{1999,07,01};
                if (tvMonth.getText().equals(getString(R.string.hint_month_current))) {
                    time = mDateFormat.format(new Date());
                }else {
                    time = tvMonth.getText().toString();
                }
                if (time.length() >= 7) {
                    int year = Integer.parseInt(time.substring(0,4));
                    int month = Integer.parseInt(time.substring(5,7));
                    times = new int[]{year,month-1};
                }

                new DateDialog(mContext,times ,getString(R.string.hint_select_month) , new DateDialog.DateDialogListener() {
                    @Override
                    public void getTime(int year, int monthOfYear, int dayOfMonth) {
                        String time = year + "-" + TimeUtil.zero(monthOfYear+1);
                        mCalendar.set(year, monthOfYear, dayOfMonth);
                        if (time.equals(mDateFormat.format(new Date()))) {
                            tvMonth.setText(getString(R.string.hint_month_current));
                        }else {
                            tvMonth.setText(time);
                        }
                        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_STEP_HISTORY));
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
        xAxis.setLabelCount(7,true);
        xAxis.setDrawGridLines(false);//取消网格线
        xAxis.setDrawAxisLine(true);//取消x轴底线
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴位置
        xAxis.setAxisMinimum(1);//设置最小点
        xAxis.setGranularity(1f);//设置间隔
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int)value);
            }

        });
        //Y轴坐标
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0);//设置y轴最小点
//        yAxis.setAxisMaximum(24*60);
//        yAxis.setLabelCount(7,false);
        yAxis.setDrawAxisLine(true);//画坐标线
        yAxis.setDrawLabels(true);//画坐标下标
        yAxis.setDrawGridLines(false);//设置网格线
//        yAxis.setValueFormatter(new HourValueFormatter());
        yAxis.setDrawZeroLine(false);
        yAxis.setEnabled(true);//显示Y轴
        chart.getAxisRight().setEnabled(false);//不显示右侧
    }

    private void updateChartView(BarChart chart,List<DayBean> dayData){
        if (chart.getData() != null) {
            chart.clearValues();
        }
        if (dayData == null) {
            return;
        }
        List<BarEntry> sumList = new ArrayList<>();
        for (int i = 0; i < dayData.size(); i++) {
            sumList.add(new BarEntry(i+1,new float[]{dayData.get(i).getDaySum()}));
        }
        BarData barData = new BarData(getInitChartDataSet(sumList));
        chart.setData(barData);
        chart.notifyDataSetChanged();
        chart.moveViewToX(barData.getEntryCount());
    }

    private BarDataSet getInitChartDataSet(List<BarEntry> entryList) {
        BarDataSet set = new BarDataSet(entryList,"");//初始化折线数据源
        set.setColor(Color.parseColor("#8aff9800"));//折线颜色
        set.setBarBorderWidth(2f);//
        set.setBarBorderColor(Color.TRANSPARENT);
        set.setValueTextSize(12f);//折线值文字大小
        set.setDrawValues(false);
        return set;
    }

    private void setCircularView(){
        if (stepList == null || stepList.size()== 0) {
            return;
        }
        int target = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_TARGET_STEP,8000);
        stepCount = stepCount == 0 ? 1:stepCount;
        String step = stepNum/stepCount+"";
        String miUnit = unit == 1 ? getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
        String state = miToKm(stepMi/stepCount,unit)+miUnit+"/" +stepKa/stepCount+getString(R.string.hint_unit_ka);
        float progress = (stepNum/stepCount / (float)target) * 100;
        cvHistoryStep.setText(step)
                .setState(state)
                .setProgress(progress)
                .invaliDate();
    }

    private void setDataItem(){
        String miUnit = unit == 1 ? getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
        diData1.setItemData(getString(R.string.hint_step_history_sum),stepNum+"");
        diData2.setItemData(getString(R.string.hint_step_history_mi),miToKm(stepMi,unit),miUnit);
        diData3.setItemData(getString(R.string.hint_step_history_ka),stepKa+"");
    }

    OnChartValueSelectedListener selectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            if (stepList != null&&stepList.size()>=e.getX()) {
                DayBean dayBean = stepList.get((int)e.getX()>0?(int)e.getX()-1:0);
                int step = dayBean.getDaySum();
                int ka = dayBean.getDayMax();
                int mi = dayBean.getDayMin();
                String day = dayBean.getDay();
                String miUnit = unit == 1 ?getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
                diData1.setItemData(day,step+"");
                diData2.setItemData(getString(R.string.hint_mi),miToKm(mi,unit),miUnit);
                diData3.setItemData(getString(R.string.hint_ka),ka+"");
            }
        }

        @Override
        public void onNothingSelected() {
            setDataItem();
        }
    };

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_LOAD_STEP_HISTORY) {
            stepNum = stepKa = stepMi = stepCount =0;
            days = TimeUtil.getMonthToDay(mCalendar);
            stepList = IbandDB.getInstance().getMonthStep(days);
            for (DayBean dayBean : stepList) {
                stepMi += dayBean.getDayMin();
                stepKa +=dayBean.getDayMax();
                stepNum += dayBean.getDaySum();
                if (dayBean.getDayCount()!= 0) {
                    stepCount++;
                }
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_STEP_HISTORY));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_STEP_HISTORY) {
            setCircularView();
            updateChartView(bcHistoryStep,stepList);
            tvEmpty.setVisibility(stepCount == 0?View.VISIBLE:View.GONE);
            setDataItem();
        }
    }

    public String miToKm(int mi,int unit){
        if (unit == 1) {
            return String .format(Locale.US,"%.1f", CheckUtil.kmToMi(mi/1000.0));
        }
        return String .format(Locale.US,"%.1f",(mi/1000.0));
    }

}
