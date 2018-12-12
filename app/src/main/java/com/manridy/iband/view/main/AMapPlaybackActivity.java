package com.manridy.iband.view.main;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.CoordinateConverter;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.bean.data.RunLocationModel;
import com.manridy.iband.view.base.BaseActionActivity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AMapPlaybackActivity extends BaseActionActivity implements LocationSource,AMapLocationListener {
    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.iv_map_close)
    ImageView ivMapClose;
    @BindView(R.id.tv_exercise_time)
    TextView tv_exercise_time;
    @BindView(R.id.tv_pace)
    TextView tv_pace;
    @BindView(R.id.tv_distance)
    TextView tv_distance;


    private AMap aMap;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;

    private String stepDate;
    private List<RunLocationModel> runLocationModels;
    private List<LatLng> latLngs;

    private StepModel runData;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_amap);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        initMap();
        stepDate = getIntent().getStringExtra("StepDate");
        if(stepDate!=null&&!"".equals(stepDate)) {
           handler.post(initRunLocationModels);
        }
    }

    Runnable initRunLocationModels = new Runnable() {
        @Override
        public void run() {
            runLocationModels = IbandDB.getInstance().getRunLocationData(stepDate);
            runData = IbandDB.getInstance().getRunData(stepDate);
            Log.i("AMapPlaybackActivity","runTime:"+runData.getRunTime());
            latLngs = new ArrayList<LatLng>();
            String locationData;
            String[] locations;
            String[] dots;
            LatLng latLng;
            for(RunLocationModel runLocationModel : runLocationModels) {
                locationData = runLocationModel.getLocationData();
                locations = locationData.split(";");
                for(String location:locations){
                    dots = location.split(",");
                    latLng =new LatLng(Double.parseDouble(dots[0]),Double.parseDouble(dots[1]));
//                    CoordinateConverter converter  = new CoordinateConverter();
//                    converter.from(CoordinateConverter.CoordType.GPS);
//                    converter.coord(latLng);
//                    LatLng newLatLng = converter.convert();
                    latLngs.add(latLng);
                }

            }
            Message message = handler.obtainMessage();
            message.what = 2;
            handler.sendMessage(message);

        }
    };


    /**
     * 初始化AMap对象
     */
    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            mUiSettings = aMap.getUiSettings();
            setUpMap();
            setMapUI();
            MapsInitializer.loadWorldGridMap(true);
        }
//        basicmap = (Button)findViewById(R.id.basicmap);
//        basicmap.setOnClickListener(this);
//        rsmap = (Button)findViewById(R.id.rsmap);
//        rsmap.setOnClickListener(this);
//
//        mRadioGroup = (RadioGroup) findViewById(R.id.check_language);
//
//        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.radio_en) {
//                    aMap.setMapLanguage(AMap.ENGLISH);
//                } else {
        aMap.setMapLanguage(AMap.CHINESE);
//                }
//            }
//        });
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
//        // 自定义系统定位小蓝点
//        MyLocationStyle myLocationStyle = new MyLocationStyle();
//        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
//                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
//        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
//        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
//        aMap.setMyLocationStyle(myLocationStyle);
//        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
        aMap.setTrafficEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));

    }

//    private void setMarker() {
//        LatLng startPoint = new LatLng(latLngs.get(0).latitude,
//                latLngs.get(0).longitude);
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions
//        aMap.addMarker()
//        LatLng endPoint = new LatLng(latLngs.get(latLngs.size() - 1).latitude,
//                latLngs.get(latLngs.size() - 1).longitude);
//        addMarker(startPoint, R.drawable.ic_marker_start);
//        addMarker(endPoint, R.drawable.ic_marker_end);
////        addBgMarker(endPoint,R.drawable.ic_marker_bg);
//
////        mAMap.animateCamera(CameraUpdateFactory
////                .newCameraPosition(new CameraPosition(new LatLng(list_latLatLonPoints.get(list_latLatLonPoints.size() - 1).getLatitude(),
////                        list_latLatLonPoints.get(list_latLatLonPoints.size() - 1).getLongitude()), 15,
////                        0, 0)));
//    }

    private void setMapUI(){
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(true);
//        mUiSettings.setMyLocationButtonEnabled(true);
//        mUiSettings.setScrollGesturesEnabled(true);
//        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setAllGesturesEnabled(true);

    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
//        EventBus.getDefault().post(new EventMessage(EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_GONE));
//        Intent intent = new Intent(getApplication(),LocationService.class);
//        startService(intent);
//        Message message = handler.obtainMessage();
//        message.what = 1;
//        handler.sendMessage(message);
    }

    @Override
    public void onStop() {
        super.onStop();
//        LocationUtil.getInstance(getApplication()).init().checkGpsStatus(true);
//        LocationUtil.getInstance(getApplication()).setCallBack(locationCallBack);
//        LocationUtil.getInstance(getApplication()).getLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
//        handler.removeMessages(1);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getApplication());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    @OnClick({R.id.iv_map_close})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_map_close:
                finish();
                break;
        }
    }

    /**绘制两个坐标点之间的线段,从以前位置到现在位置*/
    private void setUpMap(LatLng oldData, LatLng newData ) {
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(oldData);
        LatLng oldDesLatLng = converter.convert();

        CoordinateConverter converter1  = new CoordinateConverter();
        converter1.from(CoordinateConverter.CoordType.GPS);
        converter1.coord(newData);
        LatLng newDesLatLng = converter1.convert();

        // 绘制一个大地曲线
        aMap.addPolyline((new PolylineOptions())
                .add(oldDesLatLng, newDesLatLng)
                .geodesic(true).color(Color.RED));

    }


    private void setUpMap(List<LatLng> latLngs, boolean isInCN) {
//        CoordinateConverter converter  = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(oldData);
//        LatLng oldDesLatLng = converter.convert();

//        CoordinateConverter converter1  = new CoordinateConverter();
//        converter1.from(CoordinateConverter.CoordType.GPS);
//        converter1.coord(newData);
//        LatLng newDesLatLng = converter1.convert();

        if(isInCN){
            List<LatLng> converterLatLngs = new ArrayList<>();

            for(LatLng latLng:latLngs){

                CoordinateConverter converter  = new CoordinateConverter();
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(latLng);
                LatLng converterLatLng = converter.convert();
                converterLatLngs.add(converterLatLng);
            }
//            if(converterLatLngs.size()>0){
//                aMap.moveCamera(CameraUpdateFactory.changeLatLng(converterLatLngs.get(0)));
//            }
            if(aMap!=null){
                aMap.addPolyline((new PolylineOptions()).addAll(converterLatLngs)
                        .geodesic(true).color(Color.RED));

            }
            if(latLngs.size()>0){
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(converterLatLngs.get(0)));
            }

        }else{
            if(aMap!=null){
                aMap.addPolyline((new PolylineOptions()).addAll(latLngs)
                        .geodesic(true).color(Color.RED));
                if(latLngs.size()>0){
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLngs.get(0)));
                }
            }
        }
        if(latLngs.size()>0) {
            MarkerOptions markerOption = new MarkerOptions();


            CoordinateConverter converter  = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(latLngs.get(0));
            LatLng converterLatLng = converter.convert();
            markerOption.position(converterLatLng);
//            markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");
//            markerOption.draggable(true);//设置Marker可拖动
            markerOption.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.mipmap.map_ic_start)));
            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//            markerOption.setFlat(true);//设置marker平贴地图效果
            aMap.addMarker(markerOption);
            MarkerOptions markerOption2 = new MarkerOptions();
            CoordinateConverter converter2  = new CoordinateConverter();
            converter2.from(CoordinateConverter.CoordType.GPS);
            converter2.coord(latLngs.get(latLngs.size()-1));
            LatLng converterLatLng2 = converter2.convert();
            markerOption2.position(converterLatLng2);
//            markerOption.title("西安市").snippet("西安市：34.341568, 108.940174");
//            markerOption.draggable(true);//设置Marker可拖动
            markerOption2.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.mipmap.map_ic_end)));
            // 将Marker设置为贴地显示，可以双指下拉地图查看效果
//            markerOption2.setFlat(true);//设置marker平贴地图效果
            aMap.addMarker(markerOption2);
        }
    }

    DecimalFormat df=new DecimalFormat("0.00");
    Location oldLocation = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    break;
                case 2:
                    if(runData!=null) {
                        setUpMap(latLngs, runData.isInCN());

                        tv_exercise_time.setText(runData.getRunTime());
                        String str_distance_km = df.format((float) runData.getStepMileage() / 1000);
                        tv_distance.setText(str_distance_km);
                        tv_pace.setText(runData.getPace());
                    }
                    break;
            }
        }
    };


    @Override
    public void scrollToFinishActivity() {
    }

    @Override
    protected void initBack() {
    }

}
