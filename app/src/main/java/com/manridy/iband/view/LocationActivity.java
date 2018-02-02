package com.manridy.iband.view;

import android.os.Bundle;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 训练
 * Created by jarLiao on 17/5/11.
 */

public class LocationActivity extends BaseActionActivity {

    @BindView(R.id.bmapView)
    MapView mMapView;
    private BaiduMap mBaiduMap;

    @Override
    protected void initView(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        registerEventBus();
        mLocationClient = new LocationClient(getApplicationContext());//声明LocationClient类
        mLocationClient.registerLocationListener(myListener);//注册监听函数
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        MapStatus.Builder builder = new MapStatus.Builder();
//        builder.target(target).zoom(18f);

        //地图设置缩放状态
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        /**
         * 配置线段图层参数类： PolylineOptions
         * ooPolyline.width(13)：线宽
         * ooPolyline.color(0xAAFF0000)：线条颜色红色
         * ooPolyline.points(latLngs)：List<LatLng> latLngs位置点，将相邻点与点连成线就成了轨迹了
         */
//        OverlayOptions ooPolyline = new PolylineOptions().width(13).color(0xAAFF0000).points(latLngs);

        //在地图上画出线条图层，mPolyline：线条图层
//        mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);
//        mPolyline.setZIndex(3);

        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_stat_notify_dfu);
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true,bitmap));
        initLocation();
    }

    public LocationClient mLocationClient = null;;
    private void initLocation() {
        LocationClientOption mOption = new LocationClientOption();
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mOption.setOpenGps(true);
        mOption.setCoorType("bd09ll");
        mOption.setScanSpan(1000);
        mOption.setIsNeedAddress(true);
        mOption.setIsNeedLocationDescribe(true);
        mOption.setNeedDeviceDirect(false);
        mOption.setLocationNotify(false);
        mOption.setIgnoreKillProcess(true);
        mOption.setIsNeedLocationPoiList(true);
        mOption.SetIgnoreCacheException(false);
        mOption.setIsNeedAltitude(false);
        mLocationClient.setLocOption(mOption);//设置定位参数
        mLocationClient.start();//开始定位
    }


    private boolean isFirstLoc;
    //伪代码
    private BDAbstractLocationListener myListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //定位sdk获取位置后回调
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
//                        .direction(mCurrentDirection)
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude())
                        .build();

                mBaiduMap.setMyLocationData(locData);//给地图设置定位数据，这样地图就显示位置了

                /**
                 *当首次定位时，记得要放大地图，便于观察具体的位置
                 * LatLng是缩放的中心点，这里注意一定要和上面设置给地图的经纬度一致；
                 * MapStatus.Builder 地图状态构造器
                 */
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    //设置缩放中心点；缩放比例；
                    builder.target(ll).zoom(15f);
                    //给地图设置状态
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                location.getTime();//服务端
                location.getLocType();//定位类型
                location.getLocTypeDescription();//定位类型说明
                location.getLatitude();//维度
                location.getLongitude();//经度
                location.getRadius();//误差
                location.getCountryCode();//国家码，null代表没有信息
                location.getCountry();//国家名称
                location.getCityCode();//城市编码
                location.getCity();//城市
                location.getDistrict();//区
                location.getStreet();//街道
                location.getAddrStr();//地址信息
                location.getLocationDescribe();//位置描述信息
                location.getUserIndoorState();//判断用户是在室内，还是在室外
                location.getDirection();//获取方向
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        poi.getName();//获取位置附近的一些商场、饭店、银行等信息
                    }
                }

                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS类型定位结果
                    location.getSpeed();//速度 单位：km/h，注意：网络定位结果是没有速度的
                    location.getSatelliteNumber();//卫星数目，gps定位成功最少需要4颗卫星
                    location.getAltitude();//海拔高度 单位：米
                    location.getGpsAccuracyStatus();//gps质量判断
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//网络类型定位结果
                    if (location.hasAltitude()) {//如果有海拔高度
                        location.getAltitude();//单位：米
                    }
                    location.getOperators();//运营商信息
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    //离线定位成功，离线定位结果也是有效的;
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    //服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com;
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    //网络不同导致定位失败，请检查网络是否通畅;
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    //无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机;
                }
            }
        }

        public void onConnectHotSpotMessage(String s, int i){
        }
    };

    @Override
    protected void initListener() {

    }

    @Override
    protected void loadData() {
        super.loadData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {

    }

    //伪代码
    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.unRegisterLocationListener(myListener); //注销掉监听
        mLocationClient.stop(); //停止定位
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

}
