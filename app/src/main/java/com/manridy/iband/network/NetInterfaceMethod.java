package com.manridy.iband.network;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NetInterfaceMethod {
    @GET("getGoodsData")
    Call<ResponseBody> getBlog();

    // 微信运动数注册
    @FormUrlEncoded
    @POST("wechatRegister.php")
    Call<String> postWechatRegister(@FieldMap Map<String, String> body);


    // 用户反馈
    @FormUrlEncoded
    @POST("getSurveyData")
    Call<String> postGetSurveyData(@FieldMap Map<String, Object> map);


}
