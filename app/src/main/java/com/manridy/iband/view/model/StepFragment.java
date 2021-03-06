package com.manridy.iband.view.model;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.SyncData;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.bean.WeatherModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.CircularView;
import com.manridy.iband.ui.MarqueeTextView;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.main.SportActivity;
import com.manridy.iband.view.main.TrainActivity;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.history.StepHistoryActivity;
import com.manridy.sdk.Watch;
import com.manridy.sdk.callback.BleNotifyListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.manridy.applib.base.BaseActivity.isFastDoubleClick;

/**
 * 计步
 * Created by jarLiao on 2016/10/24.
 */

public class StepFragment extends BaseEventFragment {

    @BindView(R.id.cv_step)
    CircularView cvStep;
    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.bc_step)
    BarChart bcStep;
    @BindView(R.id.iv_location)
    ImageView ivLocation;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.tv_addr)
    MarqueeTextView tvAddr;
    @BindView(R.id.tv_tempetature)
    TextView tvTempetature;
    @BindView(R.id.iv_weather)
    ImageView ivWeather;
    @BindView(R.id.ll_weather)
    LinearLayout ll_weather;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;


    StepModel curStep;
    List<StepModel> curSectionSteps;
    Map<Integer,StepModel> curMap = new HashMap<>();
    SimpleDateFormat hourFormat;
    private Gson mGson;
    int unit;

    WeatherModel weatherModel;
    String city = "";
    String country = "";
    String cityCode = "";
    String weather = "";
    int weatherImg = 0;
    private boolean isWeather=false,isVisibility=false;

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_step, container, false);
        ButterKnife.bind(this,root);

        return root;
    }

    @Override
    protected void initVariables() {
        hourFormat = new SimpleDateFormat("HH");
        unit = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT,0);
        initGson();
        initChartView(bcStep);
        initChartAxis(bcStep);
    }

    private void initGson() {
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsString());
            }
        });
        mGson =  builder.create();
    }

    @Override
    protected void initListener() {
        Watch.getInstance().setSportNotifyListener(new BleNotifyListener() {
            @Override
            public void onNotify(Object o) {
                curStep = mGson.fromJson(o.toString(),StepModel.class);
                SyncData.saveCurStep(curStep);
                EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_STEP));
            }
        });
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_STEP));

    }


    public void setWeather(boolean isVisibility){
        this.isVisibility=isVisibility;
        if(ll_weather==null){
            return;
        }
        if(isVisibility){
            if(isWeather){
                ll_weather.setVisibility(View.VISIBLE);
            }
        }else{
            ll_weather.setVisibility(View.GONE);
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        isVisibility= (boolean) SPUtil.get(mContext,"is_show_weather",false);
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//        WeatherModel weatherModel = IbandDB.getInstance().getLastWeather();
        boolean isSupply = (boolean) SPUtil.get(mContext, "isSupplyWeather", false);
        if (!isSupply) {
            ll_weather.setVisibility(View.GONE);
            return;
        }
        boolean isExist = DataSupport.isExist(WeatherModel.class);//必须先执行这条否则部分手机会卡在DataSupport.findFirst这个函数
        if (isExist) {
            WeatherModel weatherModel = DataSupport.findFirst(WeatherModel.class);
            if(weatherModel!=null){
                if (weatherModel.getCity() == null || weatherModel.getCity().equals("")) {
                    ll_weather.setVisibility(View.GONE);
                    isWeather=false;
                    return;
                }
                isWeather=true;
                if(isVisibility){
                ll_weather.setVisibility(View.VISIBLE);}
                String cityName = weatherModel.getCity().replace("市", "");
                tvAddr.setText(weatherModel.getCountry()+"•"+cityName);
                tvTempetature.setText(weatherModel.getNowTemperature()+"°");
                if(!"".equals(weatherModel.getWeatherRegime())){
//                            String weatherType = getWeatherType(weatherModel.getWeatherRegime());
                    weatherImg = getWeatherImg(weatherModel.getWeatherRegime());
                    if(weatherImg!=0){
                        ivWeather.setImageResource(weatherImg);
                    }
                }
            }
        }



    }

    public void screenShot(){
        View dView = getActivity().getWindow().getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
        if (bitmap != null) {
            try {
                // 获取内置SD卡路径
                String sdCardPath = Environment.getExternalStorageDirectory().getPath();
                // 图片文件路径
                String filePath = sdCardPath + File.separator + "screenshot.png";
                File file = new File(filePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
            }
        }
    }

    private int getWeatherImg(String weather){
        int weatherImg = 0;
        if("0".equals(weather)||"sunny".equals(weather)){
            weatherImg = R.mipmap.weather_sunny;
        }else if("1".equals(weather)||"shade".equals(weather)){
            weatherImg = R.mipmap.weather_shade;
        }else if("2".equals(weather)||"rain".equals(weather)){
            weatherImg = R.mipmap.weather_rain;
        }else if("3".equals(weather)||"snow".equals(weather)){
            weatherImg = R.mipmap.weather_snow;
        }else if("4".equals(weather)||"haze".equals(weather)){
            weatherImg = R.mipmap.weather_haze;
        }else if("5".equals(weather)||"sand".equals(weather)){
            weatherImg = R.mipmap.weather_sand;
        }
        return weatherImg;
    }


    public void screenShot(View view, String fileName) throws Exception {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //上面2行必须加入，如果不加如view.getDrawingCache()返回null
        Bitmap bitmap = view.getDrawingCache();
        FileOutputStream fos = null;
        try {
            //判断sd卡是否存在
            boolean sdCardExist = Environment.getExternalStorageState()
                    .equals(android.os.Environment.MEDIA_MOUNTED);
            if(sdCardExist){
                //获取sdcard的根目录
                String sdPath = Environment.getExternalStorageDirectory().getPath();

                //创建程序自己创建的文件夹
                File tempFile= new File(sdPath+File.separator +fileName);
                if(!tempFile.exists()){
                    tempFile.mkdirs();
                }
                //创建图片文件
                File file = new File(sdPath + File.separator+fileName+File.separator+ "screen" + ".png");
                if(!file.exists()){
                    file.createNewFile();
                }

//                image.setImageBitmap(bitmap);
//                fos = new FileOutputStream(file);
//                if (fos != null) {
//
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
//                    fos.close();
//                }




                view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();
                fos = new FileOutputStream(file);
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                }

            }


        } catch (Exception e) {
            Log.e(TAG, "cause for "+e.getMessage());
        }
    }

    @OnClick({ R.id.iv_menu,R.id.iv_history,R.id.iv_location,R.id.iv_icon})
    public void onClick(View view) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_history:
                startActivity(StepHistoryActivity.class);
                break;
            case R.id.iv_menu:
                startActivity(TrainActivity.class);
                break;
            case R.id.iv_location:
                startActivity(SportActivity.class);
                break;
            case R.id.iv_icon:
                gotoSport();
                break;
        }
    }

    //动态申请权限的测试方法
    public void gotoSport() {
        // 要申请的权限 数组 可以同时申请多个权限
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};

        if (Build.VERSION.SDK_INT >= 23) {
            //如果超过6.0才需要动态权限，否则不需要动态权限
            //如果同时申请多个权限，可以for循环遍历
            int check = ContextCompat.checkSelfPermission(getActivity(),permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //写入你需要权限才能使用的方法
//                startActivity(SportActivity.class);
                openGPSSettings();
            } else {
                //手动去请求用户打开权限(可以在数组中添加多个权限) 1 为请求码 一般设置为final静态变量
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        } else {
            //写入你需要权限才能使用的方法
//            startActivity(SportActivity.class);
            openGPSSettings();
        }
    }

    /**
     * 跳转GPS设置
     */
    private void openGPSSettings() {
        if (checkGPSIsOpen()) {
//            initLocation(); //自己写的定位方法
            startActivity(SportActivity.class);
        } else {
            //没有打开则弹出对话框
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.notifyTitle)
                    .setMessage(R.string.gpsNotifyMsg)
                    // 拒绝, 退出应用
                    .setNegativeButton(R.string.hint_cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    finish();
                                }
                            })

                    .setPositiveButton(R.string.hint_set,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //跳转GPS设置界面
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent, GPS_REQUEST_CODE);
                                }
                            })

                    .setCancelable(false)
                    .show();

        }
    }

    private int GPS_REQUEST_CODE = 10;
    /**
     * 检测GPS是否打开
     *
     * @return
     */
    private boolean checkGPSIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return isOpen;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_STEP) {
            setCircularView();
            updateBarChartView(bcStep,curSectionSteps);
            tvEmpty.setVisibility(curSectionSteps.size() == 0?View.VISIBLE:View.GONE);
            setDataItem();
        } else if (event.getWhat() == EventGlobal.REFRESH_VIEW_WEATHER){
            handler.sendMessage(handler.obtainMessage(1));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //回调，判断用户到底点击是还是否。
        //如果同时申请多个权限，可以for循环遍历
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //写入你需要权限才能使用的方法
//            run();
//            startActivity(SportActivity.class);
            openGPSSettings();
        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(getActivity(),"需要获得GPS权限",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) {
            //做需要做的事情，比如再次检测是否打开GPS了 或者定位
            openGPSSettings();
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.DATA_LOAD_STEP) {
            Log.i("StepFragment","REFRESH_VIEW_ALL");
            curStep = IbandDB.getInstance().getCurStep();
            curSectionSteps = IbandDB.getInstance().getCurSectionStep();
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_STEP));
        }else if (event.getWhat() == EventGlobal.REFRESH_VIEW_ALL){
            Log.i("StepFragment","REFRESH_VIEW_ALL");
            EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_STEP));
        }else if (event.getWhat() == EventGlobal.DATA_CHANGE_UNIT){
            Log.i("StepFragment","DATA_CHANGE_UNIT");
            unit = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT,0);
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_STEP));
        }else if (event.getWhat() == EventGlobal.DATA_LOAD_WEATHER){

            IbandApplication.getIntance().country = IbandDB.getInstance().getLastWeather().getCountry();
            IbandApplication.getIntance().city = IbandDB.getInstance().getLastWeather().getCity();
            country = IbandApplication.getIntance().country;
            city = IbandApplication.getIntance().city;

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            if(country==null||city==null)return;
//            List<WeatherModel> weatherModelList = AppDB.getInstance().getCurDayWeather(df.format(new Date()),country,city);
//            if(weatherModelList.size()>0) {
//                weatherModel = weatherModelList.get(0);
//            }
            WeatherModel lastWeatherModel = IbandDB.getInstance().getCurDayWeather(df.format(new Date()),country,city);
            if(lastWeatherModel!=null){
                weatherModel = lastWeatherModel;
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_WEATHER));
        }
    }

    private void initChartView(BarChart chart) {
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
        chart.setMaxVisibleValueCount(7);
        chart.setData(new BarData());
        chart.setFitBars(true);
    }

    private void initChartAxis(BarChart chart) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);//显示x轴
        xAxis.setTextColor(Color.BLACK);//x轴文字颜色
        xAxis.setTextSize(12f);//x轴文字大小
        xAxis.setDrawGridLines(false);//取消网格线
        xAxis.setDrawAxisLine(true);//取消x轴底线
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴位置
        xAxis.setDrawLabels(false);


//        xAxis.setAxisMinimum(1);//设置最小点
//        xAxis.setCenterAxisLabels(true);
//        xAxis.setLabelCount(7, false);
        xAxis.setGranularity(1f);//设置间隔

        YAxis yAxis = chart.getAxisLeft();
//        yAxis.setAxisMaximum(220f);
        yAxis.setAxisMinimum(0);//设置y轴最小点
//        int tag_step = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_TARGET_STEP, 2000);
        yAxis.setAxisMaximum(100);
        yAxis.setDrawAxisLine(true);//画坐标线
        yAxis.setDrawLabels(true);//画坐标下标
        yAxis.setDrawGridLines(true);//设置网格线
        yAxis.setGridColor(R.color.color_chart_line);
        yAxis.setDrawZeroLine(false);
        yAxis.setEnabled(true);//显示Y轴

        chart.getAxisRight().setEnabled(false);//不显示右侧
    }

    private void updateBarChartView(BarChart chart, List<StepModel> stepList) {
        curMap = new HashMap<>();
//        StepModel step = curStep;
//        int hour = Integer.parseInt(hourFormat.format(new Date()));
        if (stepList == null ){
            stepList = new ArrayList<>();
        }
        List<BarEntry> barList = new ArrayList<>();
        int size = stepList.size();
        for (int i = 0; i < size; i++) {
            StepModel stepModel = stepList.get(i);
            String stepDate = stepModel.getStepDate();
            String format = hourFormat.format(new Date(stepDate));
            int h = Integer.valueOf(format);
//            step.setStepNum((step.getStepNum() - stepModel.getStepNum()));
//            step.setStepMileage(step.getStepMileage() - stepModel.getStepMileage());
//            step.setStepCalorie(step.getStepCalorie() - stepModel.getStepCalorie());
            curMap.put(h,stepModel);
        }
//        if (step != null) {
//            curMap.put(hour,step);
//            curSteps = step.getStepNum();
//            curKa = step.getStepCalorie();
//            curMi = step.getStepMileage();
//        }
        for (int i = 0; i < 24; i++) {
            int value = 0;
            if (curMap.containsKey( (i) )) {
                value = curMap.get((i)).getStepNum();
            }
            barList.add( new BarEntry(i,value));
        }

        BarData data = new BarData(getInitChartDataSet(barList, Color.parseColor("#8aff9800"), "set1"));
//        BarData data = new BarData(getInitChartDataSet(barList, Color.parseColor("#808080"), "set1"));
        if (chart.getData() != null) {
            chart.clearValues();
        }
//        data.setBarWidth(0.35f);
//        data.groupBars(0,0.4f,0.00f);

        if (size > 0) {
            YAxis yAxis = chart.getAxisLeft();
            yAxis.resetAxisMaximum();
        }
        chart.setData(data);
        //判断数据数量
        chart.notifyDataSetChanged();
        chart.setVisibleXRangeMinimum(24);
//        chart.setVisibleXRangeMaximum(7);
        chart.moveViewToX(data.getEntryCount() - 24);
    }

    private BarDataSet getInitChartDataSet(List<BarEntry> entryList, int color, String label) {
        BarDataSet set = new BarDataSet(entryList, label);//初始化折线数据源
        set.setColor(color);//折线颜色
        set.setBarBorderWidth(0.4f);//
        set.setValueTextColor(Color.BLACK);//折线值文字颜色
        set.setBarBorderColor(Color.TRANSPARENT);
        set.setValueTextSize(12f);//折线值文字大小
        set.setDrawValues(false);
        return set;
    }

    private void setDataItem(){
        if (curStep == null) {
            return;
        }
        int curSteps,curMi,curKa;
        curSteps =curStep.getStepNum();
        curMi = curStep.getStepMileage();
        curKa = curStep.getStepCalorie();
        diData1.setItemData(getString(R.string.hint_steps),curSteps+"");
        String miUnit = unit == 1 ?  getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
        diData2.setItemData(getString(R.string.hint_mi),miToKm(curMi,unit),miUnit);
        diData3.setItemData(getString(R.string.hint_ka),curKa+"");
    }


    private void setCircularView(){
        if (curStep == null) {
            return;
        }
        int target = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_TARGET_STEP,8000);
        String step = curStep.getStepNum()+"";
        String miUnit = unit == 1 ?  getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
        String state = miToKm(curStep.getStepMileage(),unit)+miUnit+"/" +curStep.getStepCalorie()+getString(R.string.hint_unit_ka);
        float progress = (curStep.getStepNum() / (float)target) * 100;
//        cvStep.setProgressWithAnimation(progress);
        cvStep.setText(step)
                .setState(state)
                .setProgress(progress)
                .invaliDate();
    }

    OnChartValueSelectedListener selectedListener = new OnChartValueSelectedListener() {
        @Override
        public void onValueSelected(Entry e, Highlight h) {
                int hour = e.getX() >0 ? (int) e.getX() : 0;
//                Log.d(TAG, "onValueSelected() called with: e = [" + e.getX() + "], h = [" + hour + "]");
                if (curMap.containsKey(hour)) {
                    StepModel stepModel = curMap.get(hour);
                    int step = stepModel.getStepNum();
                    int ka = stepModel.getStepCalorie();
                    int mi = stepModel.getStepMileage();
                    String miUnit = unit == 1 ?  getString(R.string.hint_unit_inch_mi): getString(R.string.hint_unit_mi);
                    String timeTitle = TimeUtil.zero(hour)+":00~" + TimeUtil.zero(hour+1)+":00";
                    diData1.setItemData(timeTitle,step+"");
                    diData2.setItemData(getString(R.string.hint_mi),miToKm(mi,unit),miUnit);
                    diData3.setItemData(getString(R.string.hint_ka),ka+"");
                }
            }


        @Override
        public void onNothingSelected() {
            setDataItem();
        }
    };


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    public String miToKm(int mi, int unit){
        if (unit == 1) {
            return String .format(Locale.US,"%.1f",CheckUtil.kmToMi(mi/1000.0));
        }
        return String .format(Locale.US,"%.1f",(mi/1000.0));
    }


    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(weatherModel!=null){
                        String city = IbandApplication.getIntance().city;
                        if (city.equals("")) {
                            ll_weather.setVisibility(View.GONE);
                            break;
                        }
                        ll_weather.setVisibility(View.VISIBLE);
                        String cityName = ""+city.replace("市", "");
                        tvAddr.setText(""+IbandApplication.getIntance().country+"•"+cityName);
                        tvTempetature.setText(weatherModel.getNowTemperature()+"°");
                        if(!"".equals(weatherModel.getWeatherRegime())){
//                            String weatherType = getWeatherType(weatherModel.getWeatherRegime());
                            weatherImg = getWeatherImg(weatherModel.getWeatherRegime());
                            if(weatherImg!=0){
                                ivWeather.setImageResource(weatherImg);
                            }
                        }

                    }
                    break;
            }



        }
    };

}
