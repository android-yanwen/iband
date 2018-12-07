package com.manridy.iband.view.main;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

//import com.amap.api.maps2d.AMap;
//import com.amap.api.maps2d.CameraUpdateFactory;
//import com.amap.api.maps2d.MapView;
//import com.amap.api.maps2d.model.MyLocationStyle;
//import com.amap.api.services.core.LatLonPoint;
//import com.amap.api.services.geocoder.GeocodeResult;
//import com.amap.api.services.geocoder.GeocodeSearch;
//import com.amap.api.services.geocoder.RegeocodeQuery;
//import com.amap.api.services.geocoder.RegeocodeResult;
import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 训练
 * Created by jarLiao on 17/5/11.
 */

public class Location2Activity extends BaseActionActivity {
//    @BindView(R.id.map)
//    MapView map;
//    @BindView(R.id.tv_mi)
//    TextView tvMi;
//    @BindView(R.id.tv_time)
//    TextView tvTime;
//    @BindView(R.id.tv_step)
//    TextView tvStep;
//    @BindView(R.id.tv_ka)
//    TextView tvKa;
//    @BindView(R.id.tv_address)
//    TextView tvAddress;
//    @BindView(R.id.iv_exit)
//    ImageView ivExit;
//    @BindView(R.id.iv_location)
//    ImageView ivLocation;
//    @BindView(R.id.iv_code)
//    ImageView ivCode;
//    @BindView(R.id.iv_nav)
//    ImageView ivNav;
//    private MapView mMapView;
//    private AMap aMap;
//    private MyLocationStyle myLocationStyle;
//    private GeocodeSearch geocoderSearch;
//    private List<LatLonPoint> latLonPoints = new ArrayList<>();

    @Override
    protected void initView(Bundle savedInstanceState) {
//        setContentView(R.layout.activity_location2);
//        mMapView = (MapView) findViewById(R.id.map);
//        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
//        //初始化地图控制器对象
//        if (aMap == null) {
//            aMap = mMapView.getMap();
//        }
    }

    @Override
    protected void initVariables() {
//        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
//         myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//        aMap.getUiSettings().setZoomControlsEnabled(false);//设置默认定位按钮是否显示，非必需设置。
//        aMap.getUiSettings().setZoomGesturesEnabled(true);//设置默认定位按钮是否显示，非必需设置。
//        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//        aMap.setOnMyLocationChangeListener(onMyLocationChangeListener);
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
//        //        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 设置卫星地图模式，aMap是地图控制器对象。
//        geocoderSearch = new GeocodeSearch(this);
//        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
//            @Override
//            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
//                tvAddress.setText(regeocodeResult.getRegeocodeAddress().getFormatAddress());
//            }
//
//            @Override
//            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
//
//            }
//        });
    }

    @Override
    protected void initListener() {

    }

//    AMap.OnMyLocationChangeListener onMyLocationChangeListener = new AMap.OnMyLocationChangeListener() {
//        @Override
//        public void onMyLocationChange(Location location) {
//            Log.d(TAG, "onMyLocationChange() called with: location = [" + location.toString() + "]");
//            LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(), location.getLongitude());
//            latLonPoints.add(latLonPoint);
//            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
//            geocoderSearch.getFromLocationAsyn(query);
//        }
//    };

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
//        mMapView.onDestroy();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
//        mMapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
//        mMapView.onPause();
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
//        mMapView.onSaveInstanceState(outState);
//    }

}
