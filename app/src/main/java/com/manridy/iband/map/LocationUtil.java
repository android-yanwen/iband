package com.manridy.iband.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.manridy.applib.callback.LocationCallBack;

import java.util.Iterator;
import java.util.List;

public class LocationUtil {
    String TAG = "LocationUtil";
    private volatile static LocationUtil uniqueInstance;
    private LocationManager locationManager;
    private String locationProvider;
    private Location location;
    private Context mContext;
    private LocationCallBack locationCallBack;
    public static int satellites;

//    public static int markSatellites = 0;
//    public static Location location;

    private LocationUtil(Context context) {
        mContext = context;
//        getLocation();
    }

    public void setCallBack(LocationCallBack locationCallBack){
        this.locationCallBack = locationCallBack;
    }

    //采用Double CheckLock(DCL)实现单例
    public static LocationUtil getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (LocationUtil.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LocationUtil(context);
                }
            }
        }
        return uniqueInstance;
    }

    public LocationUtil init() {
        //1.获取位置管理器
        if (locationManager == null) {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        return this;
    }

    public void checkGpsStatus(boolean isCheck) {
        if (locationManager == null) {
            init();
        }
        if (isCheck) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.addGpsStatusListener(gpsStatusListener);

        }else{
            locationManager.removeGpsStatusListener(gpsStatusListener);
        }
    }



    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    //获取当前状态

                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //获取所有的卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    //卫星颗数统计
                    int count = 0;
                    StringBuilder sb = new StringBuilder();
                    while (iters.hasNext() && count <= maxSatellites) {
                        count++;
                        GpsSatellite s = iters.next();
                    }

                    if(locationCallBack!=null) {
                        locationCallBack.onGpsStatus(count);
                    }
                    satellites = count;
                    Log.i(TAG,"satellites:"+satellites);
                    break;
                default:
                    break;
            }
        }
    };


    //获取位置提供器，GPS或是NetWork
    private void judgeProvider(LocationManager locationManager) {
        if(locationManager==null){
            init();
        }
        locationProvider = "";
        List<String> prodiverlist = locationManager.getProviders(true);
        if(prodiverlist.contains(LocationManager.NETWORK_PROVIDER)){
            Log.d(TAG, "网络定位");
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }

//        &&satellites>4
        if(prodiverlist.contains(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "GPS定位");
            locationProvider = LocationManager.GPS_PROVIDER;
        }
//        if(prodiverlist.size()<1){
//            Log.d(TAG, "没有可用的位置提供器");
//            locationProvider = "";
//        }
    }

    public void getLocation() {
        // 需要检查权限,否则编译报错,想抽取成方法都不行,还是会报错。只能这样重复 code 了。
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext,"没有GPS权限！",Toast.LENGTH_SHORT).show();
            return;
        }
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        //3.获取上次的位置，一般第一次运行，此值为null
        judgeProvider(locationManager);
        if("".equals(locationProvider)){
            Toast.makeText(mContext,"没有GPS信号！",Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            setLocation(location);
        }
        // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
        locationManager.requestLocationUpdates(locationProvider, 3000, 0, locationListener);
    }

    private void setLocation(Location location) {
        this.location = location;
        String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
        Log.d(TAG, address);
    }

    //获取经纬度
    public Location getNowLocation() {
        return location;
    }

    public int getSatellites(){
        return satellites;
    }


    // 移除定位监听
    public void removeLocationUpdatesListener() {
        // 需要检查权限,否则编译不过
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext,"没有GPS权限！",Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationManager != null) {
            uniqueInstance = null;
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {
        /**
         * 当某个位置提供者的状态发生改变时
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        /**
         * 某个设备打开时
         */
        @Override
        public void onProviderEnabled(String provider) {

        }

        /**
         * 某个设备关闭时
         */
        @Override
        public void onProviderDisabled(String provider) {

        }

        /**
         * 手机位置发生变动
         */
        @Override
        public void onLocationChanged(Location location) {
            location.getAccuracy();//精确度
            setLocation(location);
            if(locationCallBack!=null) {
                locationCallBack.onLocation(location);
            }
            Log.i(TAG,"locationCallBack:"+location.getLatitude()+":"+location.getLongitude()+":"+location.getProvider());
//            Toast.makeText(mContext,""+location.getLatitude()+":"+location.getLongitude()+":"+locationProvider,Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 粗略判断当前屏幕显示的地图中心点是否在国内
     * @param latitude 纬度
     * @param longtitude 经度
     * @return 屏幕中心点是否在国内
     */
    public boolean isInArea(double latitude, double longtitude) {
        if ((latitude > 3.837031) && (latitude < 53.563624)
                && (longtitude < 135.095670) && (longtitude > 73.502355)) {
            return true;
        }
        return false;
    }

//    //位置管理器
//    private LocationManager manager;
//
//    /**
//     * 初始化定位管理
//     */
//    public void initLocation() {
//        manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//        //判断GPS是否正常启动
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(mContext, "请开启GPS导航", Toast.LENGTH_SHORT).show();
//            //返回开启GPS导航设置界面
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////            mContext.startActivityForResult(intent, 0);
//            mContext.startActivity(intent);
//            return;
//        }
//        //添加卫星状态改变监听
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        manager.addGpsStatusListener(gpsStatusListener);
//        //1000位最小的时间间隔，1为最小位移变化；也就是说每隔1000ms会回调一次位置信息
//        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
//    }


//    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
//        @Override
//        public void onGpsStatusChanged(int event) {
//            switch (event) {
//                //卫星状态改变
//                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//                    //获取当前状态
//
//                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // TODO: Consider calling
//                        //    ActivityCompat#requestPermissions
//                        // here to request the missing permissions, and then overriding
//                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                        //                                          int[] grantResults)
//                        // to handle the case where the user grants the permission. See the documentation
//                        // for ActivityCompat#requestPermissions for more details.
//                        return;
//                    }
//                    GpsStatus gpsStatus = manager.getGpsStatus(null);
//                    //获取卫星颗数的默认最大值
//                    int maxSatellites = gpsStatus.getMaxSatellites();
//                    //获取所有的卫星
//                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
//                    //卫星颗数统计
//                    int count = 0;
//                    StringBuilder sb = new StringBuilder();
//                    while (iters.hasNext() && count <= maxSatellites) {
//                        count++;
//                        GpsSatellite s = iters.next();
//                        //卫星的信噪比
//                        float snr = s.getSnr();
//                        sb.append("第").append(count).append("颗").append("：").append(snr).append("\n");
//                    }
//                    Log.e("TAG", sb.toString());
//                    Toast.makeText(mContext,sb.toString(),Toast.LENGTH_SHORT).show();
//                    break;
//                default:
//                    break;
//            }
//        }
//    };



    private static final double EARTH_RADIUS = 6378137.0;
    // 返回单位是米
    public static double getDistance(double longitude1, double latitude1,
                                     double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

}

