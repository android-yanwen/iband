package com.manridy.iband.view.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.manridy.iband.bean.EcgDataBean;
import com.manridy.iband.bean.EcgHistoryModel;
import com.manridy.iband.bean.HeartModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.ui.chars.SuperCharts;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.history.EcgHistoryActivity;
import com.manridy.iband.view.history.HrHistoryActivity;
import com.manridy.iband.view.test.TestHrTimingActivity;
import com.manridy.sdk.bean.Ecg;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.callback.BleNotifyListener;
import com.manridy.sdk.exception.BleException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.manridy.applib.base.BaseActivity.isFastDoubleClick;

/**
 * 心率
 * Created by jarLiao on 2016/10/24.
 */

public class EcgFragment extends BaseEventFragment {

    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.iv_test)
    TextView ivTest;
    @BindView(R.id.cv_hr)
    CircularView cvHr;
//    @BindView(R.id.lc_hr)
//    LineChart lcHr;
    @BindView(R.id.chart_ecg)
    SuperCharts chart_ecg;
    @BindView(R.id.tv_start)
    TextView tvStart;
    @BindView(R.id.tv_end)
    TextView tvEnd;
//    @BindView(R.id.tv_empty)
//    TextView tvEmpty;

    HeartModel curHeart;
    List<HeartModel> curHeartList;
    int avgHr, maxHr, minHr;
    boolean isTestData = true;
    private Gson gson;

    //本测心电测试中的生成的心率数据
    HeartModel curEcgHeart;
    private int curEcgHr;
    private List<HeartModel> ecgHrList;
    private int avgEcgHr, maxEcgHr, minEcgHr;
    private String lastEcgHrDate;

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_ecg, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {
        gson = new Gson();
        try {
            EcgHistoryModel ecgHistoryModel = IbandDB.getInstance().getLastEcgHistoryModel();
            if(ecgHistoryModel!=null) {
                String text = ecgHistoryModel.getLastHr() + "";
                float progress = (float) ((ecgHistoryModel.getLastHr() / 220.0) * 100);
//        cvHr.setProgressWithAnimation(progress);
                cvHr.setText(text)
                        .setProgress(progress)
                        .invaliDate();
                cvHr.setProgressWithAnimation(progress);
                diData1.setItemData(getString(R.string.hint_hr_avg), ecgHistoryModel.getAvgHr() + "");
                diData2.setItemData(getString(R.string.hint_hr_min), ecgHistoryModel.getMinHr() + "");
                diData3.setItemData(getString(R.string.hint_hr_max), ecgHistoryModel.getMaxHr() + "");
                String startTime = ecgHistoryModel.getEcgStartDate().substring(11);
                String stopTime = ecgHistoryModel.getEcgEndDate().substring(11);
                tvStart.setText(startTime);
                tvEnd.setText(stopTime);
            }
        }catch (Exception e){

        }


//        initChartView(lcHr);
//        initChartAxis(lcHr);
    }

    private ArrayList<Integer> curEcgList = new ArrayList<>();
    private ArrayList<Integer> nextEcgList = new ArrayList<>();
    int index = 0;
    int count = 0;
    int dataPackage = 0;
    EcgHistoryModel ecgHistoryModel;
    SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat format2 = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat format3 = new SimpleDateFormat("yyyyMMddHHmmss");
    String tamp;
    EcgDataBean ecgDataBean;
    private int hrBaseLine;
    @Override
    protected void initListener() {

        IbandApplication.getIntance().service.watch.setHrBaseLineListener(new BleNotifyListener() {
            @Override
            public void onNotify(Object o) {
                String hrBaseLineListener = o.toString();
                Log.i("parseHrBaseLine:Notify",hrBaseLineListener);
                hrBaseLine= Integer.parseInt(hrBaseLineListener);
            }
        });




        IbandApplication.getIntance().service.watch.setEcgNotifyListener(new BleNotifyListener() {
            @Override
            public void onNotify(Object o) {
//                Log.i("BleNotifyListener","BleNotifyListener:"+o.toString());
                Ecg ecg = gson.fromJson(o.toString(), Ecg.class);
//                Log.i("BleNotifyListener","BleNotifyListener:"+ecg);
                count++;
                if(dataPackage!=ecg.getDataPackage()){
                    ecgHrList = new ArrayList<>();
                    maxEcgHr = minEcgHr = avgEcgHr = curEcgHr =0;
//                    String text =  "0";
//                    float progress = (float) ((220.0 / 220.0) * 100);
////        cvHr.setProgressWithAnimation(progress);
//                    cvHr.setText(text)
//                            .setProgress(progress)
//                            .invaliDate();
//                    cvHr.setProgressWithAnimation(progress);
//                    diData1.setItemData(getString(R.string.hint_hr_avg),  "0");
//                    diData2.setItemData(getString(R.string.hint_hr_min), "0");
//                    diData3.setItemData(getString(R.string.hint_hr_max), "0");
                    lastEcgHrDate = "";
                    curEcgHeart = new HeartModel();

                    handler.sendMessage(handler.obtainMessage(1));

                    dataPackage = ecg.getDataPackage();
                    ecgHistoryModel = new EcgHistoryModel();
                    ecgHistoryModel.setUserId(ecg.getUserId());
                    ecgHistoryModel.setDataPackage(ecg.getDataPackage());
                    Date date = new Date();
                    long date_l = date.getTime();
                    String time = format0.format(date_l);
                    String day = format1.format(date_l);
                    String hms = format2.format(date_l);
                    tamp = format3.format(date_l);
                    ecgHistoryModel.setEcgStartDate(time);
                    ecgHistoryModel.setEcgEndDate(time);
                    ecgHistoryModel.setEcgDate(time);
                    ecgHistoryModel.setEcgDay(day);
//                    String startTime = time.substring(11);
//                    tvStart.setText(startTime);
//                    ecgHistoryModel.setEcgDay("2018-06-10");
                    List<Integer> list = ecg.getList();
//                    if(list!=null){
//                        str_ecg = "";
//                        str_ecg_time = "";
//                        for(Integer ecg_int :list)
//                        {
//                            str_ecg = str_ecg+ecg_int+",";
//                            str_ecg_time = str_ecg_time+(ecg_int-10000)+" "+hms+",";
//                        }
//                        ecgHistoryModel.setEcg(str_ecg);
//                        ecgHistoryModel.setEcg_time(str_ecg_time);
//                    }


                    if(list!=null){
                        ecgHistoryModel.setEcg_data_id(tamp);
                        ecgHistoryModel.save();
                        String str_ecg = "";
                        for(Integer ecg_int :list)
                        {
                            str_ecg = str_ecg+ecg_int+",";
                        }
                        ecgDataBean = new EcgDataBean();
                        ecgDataBean.setEcg_time(time);
                        ecgDataBean.setEcg_data_id(tamp);
                        ecgDataBean.setEcg(str_ecg);
                        ecgDataBean.setRate_aided_signal(hrBaseLine);
                        Log.i("setRate_aided_signal:",""+hrBaseLine);
                        ecgDataBean.save();
                    }

                }else if(dataPackage==ecg.getDataPackage()){
                    Date date = new Date();
                    long date_l = date.getTime();
                    String time = format0.format(date_l);
                    String hms = format2.format(date_l);
                    ecgHistoryModel.setEcgEndDate(time);
//                    String stopTime = time.substring(11);
//                    tvEnd.setText(stopTime);
                    if(avgHr!=0) {
                        ecgHistoryModel.setAvgHr(avgHr);

                    }else{
                        ecgHistoryModel.setAvgHr(65);
                    }
                    ecgHistoryModel.setAvgHr(avgEcgHr);
                    ecgHistoryModel.setMinHr(minEcgHr);
                    ecgHistoryModel.setMaxHr(maxEcgHr);
                    ecgHistoryModel.setLastHr(curEcgHr);
                    ecgHistoryModel.setLastHrDate(lastEcgHrDate);
                    List<Integer> list = ecg.getList();
                    if(list!=null){
                        String str_ecg = "";
                        for(Integer ecg_int :list)
                        {
                            str_ecg = str_ecg+ecg_int+",";
                        }
                        ecgDataBean = new EcgDataBean();
                        ecgDataBean.setEcg_time(time);
                        ecgDataBean.setEcg_data_id(tamp);
                        ecgDataBean.setEcg(str_ecg);
                        ecgDataBean.setRate_aided_signal(hrBaseLine);
                        ecgDataBean.save();
                    }
                    ecgHistoryModel.save();
                }


//                EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_DATA_COUNT));
                if (index++ > 2) {//如果大于180个数据刷新一次

                    curEcgList = (ArrayList<Integer>) nextEcgList.clone();
                    nextEcgList.clear();
                    index = 0;
                    EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_ECG));
                }
                nextEcgList.addAll(ecg.getList());
            }
        });


//        IbandApplication.getIntance().service.watch.setHrNotifyListener(new BleNotifyListener() {
//            @Override
//            public void onNotify(Object o) {//上报不做保存处理
//                curHeart = new Gson().fromJson(o.toString(), HeartModel.class);
//                if (isTestData) {
//                    curHeartList = new ArrayList<>();
//                }
//                isTestData = false;
//                curHeartList.add(curHeart);
//                EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_HR_TEST));
//                EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_HR));
//            }
//        });

        IbandApplication.getIntance().service.watch.setEcgHrNotifyListener(new BleNotifyListener() {
            @Override
            public void onNotify(Object o) {
                curEcgHeart = new Gson().fromJson(o.toString(),HeartModel.class);
                ecgHrList.add(curEcgHeart);
                Date date = new Date();
                long date_l = date.getTime();
                SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm:ss");
                String time = format.format(date_l);
                lastEcgHrDate = time;
                curEcgHr = curEcgHeart.getHeartRate();
                EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_ECG_HR));
            }
        });

//        btTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (btTest.getText().equals(getString(R.string.hint_test))) {
//                    IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setHrTest(2), new BleCallback() {
//                        @Override
//                        public void onSuccess(Object o) {
//                            EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_HR_TEST));
//                        }
//
//                        @Override
//                        public void onFailure(BleException exception) {
//
//                        }
//                    });
//                } else {
//                    IbandApplication.getIntance().service.watch.sendCmd(BleCmd.setHrTest(0));
//                    EventBus.getDefault().post(new EventMessage(EventGlobal.ACTION_HR_TESTED));
//                }
//            }
//        });
    }

    @Override
    public void initData(Bundle savedInstanceState) {
//        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_HR));
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_ECG));
    }


    @OnClick({R.id.iv_test, R.id.iv_history})
    public void onClick(View view) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_history:
                Intent intent = new Intent(mContext, EcgHistoryActivity.class);
                intent.putExtra("history_type", 3);
                startActivity(intent);
                break;
            case R.id.iv_test:
                startActivity(TestHrTimingActivity.class);
                break;
        }
    }

    int i = 0;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_HR) {
//            setCircularView();
//            updateChartView(lcHr, curHeartList);
//            tvEmpty.setVisibility(curHeartList.size() == 0?View.VISIBLE:View.GONE);

//            setDataItem();
        } else if(event.getWhat() == EventGlobal.REFRESH_VIEW_ECG_HR){
            setCircularView();
            setDataItem();
        } else if (event.getWhat() == EventGlobal.ACTION_HR_TEST) {
//            btTest.setText(R.string.hint_stop);
            cvHr.setTitle(getString(R.string.hint_hr_testing)).invaliDate();
        } else if (event.getWhat() == EventGlobal.ACTION_HR_TESTED) {
//            btTest.setText(R.string.hint_test);
            cvHr.setTitle(getString(R.string.hint_last_hr)).invaliDate();
            isTestData = true;
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ECG) {
            setEcgView();
            try {
                String startTime = ecgHistoryModel.getEcgStartDate().substring(11);
                tvStart.setText(startTime);
                String stopTime = ecgHistoryModel.getEcgEndDate().substring(11);
                tvEnd.setText(stopTime);
            }catch (Exception e){

            }

            i++;
            if(i>6) {
                i = 0;
                IbandApplication.getIntance().service.watch.getHrBaseLineInfo(new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
//                Log.i("parseHrBaseLine()","EcgFragment:"+o.toString());
                    }

                    @Override
                    public void onFailure(BleException exception) {
                    }
                });
            }
        }
    }


    private void setEcgView() {
//        for(Integer i : curEcgList){
//            Log.i(TAG,"setEcgView():curEcgList:"+i);
//        }
        chart_ecg.setmData(curEcgList,hrBaseLine);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_LOAD_ECG) {
//            curHeart = IbandDB.getInstance().getLastHeart();
//            curHeartList = IbandDB.getInstance().getLastsHeart();
//            Collections.reverse(curHeartList); // 倒序排列
//            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_HR));

        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ALL) {
//            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_ECG));
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

    private void setDataItem() {
        if(ecgHrList==null)return;
        avgEcgHr = maxEcgHr = minEcgHr = 0;
        if (ecgHrList.size() > 0) {
            minEcgHr = curEcgHeart.getHeartRate();
            for (HeartModel heartModel : ecgHrList) {
                int hr = heartModel.getHeartRate();
                avgEcgHr += hr;
                maxEcgHr = maxEcgHr > hr ? maxEcgHr : hr;
                minEcgHr = minEcgHr < hr ? minEcgHr : hr;
            }
            avgEcgHr /= ecgHrList.size();

            String start = ecgHrList.get(0).getHeartDate().substring(11, 19);
            String end = ecgHrList.get(ecgHrList.size() - 1).getHeartDate().substring(11, 19);
//            tvStart.setText(start);
//            tvEnd.setText(end);
        }
        diData1.setItemData(getString(R.string.hint_hr_avg), avgEcgHr + "");
        diData2.setItemData(getString(R.string.hint_hr_min), minEcgHr + "");
        diData3.setItemData(getString(R.string.hint_hr_max), maxEcgHr + "");
    }

    private void setCircularView() {
        if (curEcgHeart == null) {
            return;
        }
        String text = curEcgHeart.getHeartRate() + "";
        float progress = (float) ((curEcgHeart.getHeartRate() / 220.0) * 100);
//        cvHr.setProgressWithAnimation(progress);
        cvHr.setText(text)
                .setProgress(progress)
                .invaliDate();
        cvHr.setProgressWithAnimation(progress);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    String text = "--";
                    float progress = (float) ((220.0 / 220.0) * 100);
//        cvHr.setProgressWithAnimation(progress);
                    cvHr.setText(text)
                            .setProgress(progress)
                            .invaliDate();
                    cvHr.setProgressWithAnimation(progress);
                    diData1.setItemData(getString(R.string.hint_hr_avg), "--");
                    diData2.setItemData(getString(R.string.hint_hr_min), "--");
                    diData3.setItemData(getString(R.string.hint_hr_max), "--");
                    break;
            }

        }
    };

    OnChartValueSelectedListener selectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
            int x = e.getX() >0 ? (int) e.getX() : 0;
            LogUtil.d(TAG, "onValueSelected() called with: e = [" + e.getX() + "], h = [" + x + "]");
            if (curHeartList != null && curHeartList.size() >= x) {
                HeartModel heartModel = curHeartList.get(x > 0 ? x - 1 : 0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String times = "00:00";
                try {
                    Date date = dateFormat.parse(heartModel.getHeartDate());
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
                    times = dateFormat2.format(date);
                } catch (Exception e1) {

                }
                diData1.setItemData(getString(R.string.hint_time), times,false);
                diData2.setItemData("","",false);
                diData3.setItemData(getString(R.string.hint_view_hr), String.valueOf(heartModel.getHeartRate()));
            }
        }

        @Override
        public void onNothingSelected() {
            setDataItem();
        }
    };

}
