package com.manridy.iband.view.model.sport;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.manridy.applib.callback.LocationCallBack;
import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.map.LocationUtil;
import com.manridy.iband.service.LocationService;
import com.manridy.iband.view.base.BaseEventFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OutdoorRunMapFragment extends BaseEventFragment implements LocationSource,AMapLocationListener {

    @BindView(R.id.map)
    MapView mapView;

    private AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private UiSettings mUiSettings;

    @BindView(R.id.iv_map_close)
    ImageView ivMapClose;

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sport_outdoor_run_map, container, false);
        ButterKnife.bind(this, root);
        mapView.onCreate(getArguments());
        initMap();
        return root;
    }
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
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
        aMap.setTrafficEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
    }

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
//        setStatusBarColor(Color.parseColor("#151515"));

    }

    @Override
    protected void initListener() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocationUtil.getInstance(getAppliaction()).init().checkGpsStatus(true);
        LocationUtil.getInstance(getAppliaction()).setCallBack(locationCallBack);
        LocationUtil.getInstance(getAppliaction()).getLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
//        EventBus.getDefault().post(new EventMessage(EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_GONE));
        Intent intent = new Intent(getAppliaction(),LocationService.class);
        getActivity().startService(intent);
        Message message = handler.obtainMessage();
        message.what = 1;
        handler.sendMessage(message);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
        handler.removeMessages(1);
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
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getContext());
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
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                android.support.v4.app.Fragment mFragment;
                mFragment = new OutdoorRunMainFragment();
                transaction.replace(R.id.fl_content, mFragment);
                transaction.commit();
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
                .geodesic(true).color(Color.GREEN));

    }






    LocationCallBack locationCallBack = new LocationCallBack(){
        @Override
        public void onGpsStatus(int satellites) {

        }

        @Override
        public void onLocation(Location location) {


        }
    };


    Location oldLocation = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(oldLocation == null){
                        oldLocation = LocationUtil.getInstance(getAppliaction()).getNowLocation();
                    }else{
                        LatLng oldLatLng = new LatLng(oldLocation.getLatitude(),oldLocation.getLongitude());

                        Location newLocation = LocationUtil.getInstance(getAppliaction()).getNowLocation();
                        LatLng newLatLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                        setUpMap(oldLatLng,newLatLng);
                        oldLocation = LocationUtil.getInstance(getAppliaction()).getNowLocation();
                        Toast.makeText(getAppliaction(),""+newLocation.getLongitude()+":"+newLocation.getLatitude(),Toast.LENGTH_SHORT).show();
                        Log.i(TAG,""+newLocation.getLongitude()+":"+newLocation.getLatitude());
                    }
//                    Message message = handler.obtainMessage();
//                    message.what = 1;
//                    handler.sendMessageDelayed(message,8);
                    handler.sendEmptyMessageDelayed(1, 5000);
                    break;
            }
        }
    };


}
