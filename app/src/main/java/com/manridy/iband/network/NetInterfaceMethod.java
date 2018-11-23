package com.manridy.iband.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface NetInterfaceMethod {
    //forecast?location="+longitudeAndLatitude+"&key=e778b60bd3004e309d51fe0a2d69dd39
    //forecast?location=113.988807,22.682011&key=e778b60bd3004e309d51fe0a2d69dd39
    @GET("forecast?location={longitude},{latitude}&key=e778b60bd3004e309d51fe0a2d69dd39")
    Call<ResponseBody> getCityWeather(@Path("longitude") String longitude, @Path("latitude") String latitude);


    // 微信运动数注册
    @FormUrlEncoded
    @POST("wechatRegister.php")
    Call<String> postWechatRegister(@FieldMap Map<String, String> body);


    /**
     * 用户反馈
     * yw 18/11/23
     */
    @FormUrlEncoded
    @POST("getSurveyData")
    Call<String> postGetSurveyData(@FieldMap Map<String, Object> map);


}
