package com.manridy.iband.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NetInterfaceMethod {
    //forecast?location="+longitudeAndLatitude+"&key=e778b60bd3004e309d51fe0a2d69dd39
    //forecast?location=113.988807,22.682011&key=e778b60bd3004e309d51fe0a2d69dd39
    //116.310316,39.956074 经度，纬度

    /**
     * 和风天气接口
     * yw 18/11/24
     */
    @GET("forecast")
    Call<ResponseBody> getCityWeather(@Query("location") String longitude, @Query("key") String latitude);
    @GET("forecast")
    Call<ResponseBody> getCityWeather(@Query("location") String longitude, @Query("key") String latitude, @Query("lang") String lang);


    // 微信运动数注册
    @FormUrlEncoded
    @POST("wechatRegister.php")
    Call<String> postWechatRegister(@FieldMap Map<String, String> body);


    /**
     * 用户反馈
     * yw 18/11/13
     */
    @FormUrlEncoded
    @POST("getSurveyData")
    Call<String> postGetSurveyData(@FieldMap Map<String, Object> map);


}
