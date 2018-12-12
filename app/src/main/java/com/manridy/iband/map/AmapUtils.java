package com.manridy.iband.map;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AmapUtils {
    final public static int AmapUtils_getCoordinates = 1;
    final public static int AmapUtils_getAddress = 2;
    private volatile static AmapUtils uniqueInstance;
    AMapLocationClient getCoordinates_mlocationClient = null;
    AMapLocationClient getAddress_mlocationClient = null;
    AMapLocationListener aMapLocationListener = null;
    private Context mContext;

    private AmapUtils(Context context) {
        mContext = context;
    }

    //采用Double CheckLock(DCL)实现单例
    public static AmapUtils getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (LocationUtil.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new AmapUtils( context );
                }
            }
        }
        return uniqueInstance;
    }

    private void getAddress_stopLocation(){
        if(aMapLocationListener!=null) {
            getAddress_mlocationClient.unRegisterLocationListener(aMapLocationListener);
        }
        aMapLocationListener = null;
        if (getAddress_mlocationClient != null) {
            getAddress_mlocationClient.stopLocation();
            getAddress_mlocationClient.onDestroy();
        }
        getAddress_mlocationClient = null;
    }

    private void getCoordinates_stopLocation(){
        if(aMapLocationListener!=null) {
            getCoordinates_mlocationClient.unRegisterLocationListener(aMapLocationListener);
        }
        aMapLocationListener = null;
        if (getCoordinates_mlocationClient != null) {
            getCoordinates_mlocationClient.stopLocation();
            getCoordinates_mlocationClient.onDestroy();
        }
        getCoordinates_mlocationClient = null;
    }

    public interface AMapLocationCallback<AMapLocation> {
        void onSuccess(int type, AMapLocation t);
        void onFailure(AMapLocation exception);
    }
    AMapLocationCallback getCoordinates_mapLocationCallback;
    public void getCoordinates(AMapLocationCallback aMapLocationCallback){
        getCoordinates_mapLocationCallback = aMapLocationCallback;
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.getLatitude();//获取纬度
                        amapLocation.getLongitude();//获取经度
                        amapLocation.getAccuracy();//获取精度信息
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(amapLocation.getTime());
                        df.format(date);//定位时间
                        amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                        String country = amapLocation.getCountry();//国家信息
                        String province = amapLocation.getProvince();//省信息
                        String city = amapLocation.getCity();//城市信息
                        amapLocation.getDistrict();//城区信息
                        amapLocation.getStreet();//街道信息
                        amapLocation.getStreetNum();//街道门牌号信息
                        String cityCode = amapLocation.getCityCode();//城市编码
                        amapLocation.getAdCode();//地区编码
//                  amapLocation.getAOIName();//获取当前定位点的AOI信息
                        if(amapLocation.getLatitude()!=0.0D||amapLocation.getLongitude()!=0.0D) {
                            getCoordinates_mapLocationCallback.onSuccess(AmapUtils.AmapUtils_getCoordinates,amapLocation);
                            getCoordinates_stopLocation();
                        }
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                        getCoordinates_mapLocationCallback.onFailure(amapLocation);
                    }
                }
            }
        };

        //声明mLocationOption对象
        AMapLocationClientOption mLocationOption = null;
        getCoordinates_mlocationClient = new AMapLocationClient(mContext);
//初始化定位参数
        mLocationOption = new AMapLocationClientOption();
//设置返回地址信息，默认为true
        mLocationOption.setNeedAddress(true);
//设置定位监听
        getCoordinates_mlocationClient.setLocationListener(aMapLocationListener);
//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
//设置定位参数
        getCoordinates_mlocationClient.setLocationOption(mLocationOption);
// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
// 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
// 在定位结束后，在合适的生命周期调用onDestroy()方法
// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
//启动定位
        getCoordinates_mlocationClient.startLocation();

//        String ha = sHA1(getContext());
//        Log.i(TAG,"sHA1:"+ha);

    }
    AMapLocationCallback getAddress_mapLocationCallback;
    public void getAddress(AMapLocationCallback aMapLocationCallback){
        getAddress_mapLocationCallback = aMapLocationCallback;
        aMapLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //定位成功回调信息，设置相关消息
                        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                        amapLocation.getLatitude();//获取纬度
                        amapLocation.getLongitude();//获取经度
                        amapLocation.getAccuracy();//获取精度信息
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(amapLocation.getTime());
                        df.format(date);//定位时间
                        amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                        String country = amapLocation.getCountry();//国家信息
                        String province = amapLocation.getProvince();//省信息
                        String city = amapLocation.getCity();//城市信息
                        amapLocation.getDistrict();//城区信息
                        amapLocation.getStreet();//街道信息
                        amapLocation.getStreetNum();//街道门牌号信息
                        String cityCode = amapLocation.getCityCode();//城市编码
                        amapLocation.getAdCode();//地区编码
//                  amapLocation.getAOIName();//获取当前定位点的AOI信息
                        if(amapLocation.getCity()!=null&&!"".equals(amapLocation.getCity())) {
                            getAddress_mapLocationCallback.onSuccess(AmapUtils.AmapUtils_getAddress,amapLocation);
                            getAddress_stopLocation();
                        }
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                        getAddress_mapLocationCallback.onFailure(amapLocation);
                    }
                }
            }
        };

        //声明mLocationOption对象
        AMapLocationClientOption mLocationOption = null;
        getAddress_mlocationClient = new AMapLocationClient(mContext);
//初始化定位参数
        mLocationOption = new AMapLocationClientOption();
//设置返回地址信息，默认为true
        mLocationOption.setNeedAddress(true);
//设置定位监听
        getAddress_mlocationClient.setLocationListener(aMapLocationListener);
//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
//设置定位参数
        getAddress_mlocationClient.setLocationOption(mLocationOption);
// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
// 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
// 在定位结束后，在合适的生命周期调用onDestroy()方法
// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
//启动定位
        getAddress_mlocationClient.startLocation();

//        String ha = sHA1(getContext());
//        Log.i(TAG,"sHA1:"+ha);

    }

}
