package com.manridy.iband.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.manridy.applib.utils.FileUtil;
import com.manridy.applib.utils.LogUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.bean.AddressModel;
import com.manridy.iband.bean.DeviceList;
import com.manridy.iband.bean.Weather;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DomXmlParse;
import com.manridy.iband.network.NetInterfaceMethod;
import com.manridy.iband.view.setting.FeedbackActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 *
 * Created by jarLiao on 17/4/19.
 */

public class HttpService {
    private static final String TAG = "HttpService";
    private static HttpService httpService;
    private static OkHttpClient client = new OkHttpClient();
    public static final String wechat_query = "http://39.108.92.15:12348/wx/device_query";
    public static final String wechat_regist ="http://39.108.92.15:12348/wx/device_authorize";
    public static final String wechat_old_query = "http://39.108.92.15:12347/wx/device_query";
    public static final String wechat_old_regist = "http://39.108.92.15:12347/wx/device_authorize";
    public static final String device_list2 = "http://120.78.138.141:8080/device_list.php";
//    public static final String device_list2 = "http://120.78.138.141:8080/device_list.php";
    public static final String device_list = "http://112.74.54.235/devicelist/index.php/Home/Index/device_list_test";
    public static final String device_img = "http://120.78.138.141:8080/image/";
    public static final String device_ota_record = "http://120.78.138.141:8080/update/update_record.php";
    public static final String device_wechat_query = "http://120.78.138.141:8080/deviceRegisterQuery.php";
    public static final String device_wechat_regist = "http://120.78.138.141:8080/wechatRegister.php";

//    public static final String heweather_city = "https://search.heweather.com/find?key=e778b60bd3004e309d51fe0a2d69dd39&location=";
    public static final String heweather_city = "https://api.heweather.com/s6/weather/";
    public static final String weather = "http://112.74.54.235/product/index.php/Api/weather/requestByKey/city/";

    /*******获取用户反馈信息******/
    public static final String URL_GetSurveyData = "http://112.74.54.235/product/index.php/Api/Survey/";

    private HttpService() {
    }

    public static HttpService getInstance(){
        if (httpService == null) {
            httpService = new HttpService();
        }
        return httpService;
    }

    public void downloadXml(String url, OnResultCallBack onResultCallBack) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                List<DomXmlParse.Image> imageList = DomXmlParse.parseXml(inputStream);
                onResultCallBack.onResult(true,imageList);
                if (inputStream != null) {
                    inputStream.close();
                }
            } else {
                LogUtil.d(TAG, "downloadXml() called with: url = [" + url + "]");
                onResultCallBack.onResult(false,null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            onResultCallBack.onResult(  false,null);
        } catch (Exception e) {
            onResultCallBack.onResult(false,null);
            e.printStackTrace();
        }
    }

    private String getWeatherCityUrl(String longitudeAndLatitude) {
        String heweather_city = "https://api.heweather.com/s6/weather/forecast?location="+longitudeAndLatitude+"&key=e778b60bd3004e309d51fe0a2d69dd39";
        return heweather_city;
    }

    public void getHeWeather_city(String location,String lang ,final OnResultCallBack onResultCallBack){
//        Request request = new Request.Builder().url(heweather_city+location+"&lang="+lang).build();
        Request request = new Request.Builder().url(getWeatherCityUrl(location)).build();
//        Request request = new Request.Builder().url("https://api.heweather.com/s6/weather/forecast?location=116.310316,39.956074&key=e778b60bd3004e309d51fe0a2d69dd39").build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onResultCallBack.onResult(false,null);
//                Log.d(TAG, "onFailure: ......................");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Log.d(TAG, "onResponse: ........................");
                try {
                    String result = response.body().string();
                    Log.d(TAG, "onResponse: " + result);
//                    AddressModel addressModel = new Gson().fromJson(result, AddressModel.class);
                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(result);
                    AddressModel addressModel = new AddressModel(3);//json返回未来3天的天气状况
                    addressModel.setCnty(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("basic").getAsJsonObject().get("cnty").getAsString());
                    addressModel.setParent_city(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("basic").getAsJsonObject().get("parent_city").getAsString());
                    for (int i = 0; i < 3; i++) {
                        addressModel.getForecastWeather().get(i).setCond_txt_d(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("daily_forecast").getAsJsonArray().get(0).getAsJsonObject().get("cond_txt_d").getAsString());
                        addressModel.getForecastWeather().get(i).setTmp_max(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("daily_forecast").getAsJsonArray().get(0).getAsJsonObject().get("tmp_max").getAsString());
                        addressModel.getForecastWeather().get(i).setTmp_min(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("daily_forecast").getAsJsonArray().get(0).getAsJsonObject().get("tmp_min").getAsString());
                        addressModel.getForecastWeather().get(i).setTmp_now(jsonObject.get("HeWeather6").getAsJsonArray().get(0).getAsJsonObject().get("daily_forecast").getAsJsonArray().get(0).getAsJsonObject().get("tmp_min").getAsString());
                    }
                    onResultCallBack.onResult(true, addressModel);
                } catch (Exception e) {
                    onResultCallBack.onResult(false, null);
                }
            }
        });
    }

    public void getWeather(String city,final OnResultCallBack onResultCallBack){
        Request request = new Request.Builder().url(weather+city).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onResultCallBack.onResult(false,null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    try {
                    String result = response.body().string();
                        Weather weather = new Gson().fromJson(result,Weather.class);
                        onResultCallBack.onResult(true,weather);
                    }catch (Exception e){
                        onResultCallBack.onResult(false,null);
                    }
//                AddressModel addressModel = new Gson().fromJson(result, AddressModel.class);
//                onResultCallBack.onResult(true,addressModel);
                }catch (Exception e){
                    onResultCallBack.onResult(false,null);
                }

            }
        });
    }

    public void downloadOTAFile(String url, OnResultCallBack onResultCallBack) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                if (FileUtil.getSdCardPath() == null) {
                    return;
                }
                String path = FileUtil.getSdCardPath()+"/ota.zip";
                FileOutputStream fos = new FileOutputStream(new File(path));
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer,0,len);
                }
                fos.flush();
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
                onResultCallBack.onResult(true,null);
            } else {
                onResultCallBack.onResult(false,null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            onResultCallBack.onResult(false,null);
        } catch (Exception e) {
            onResultCallBack.onResult(false,null);
            e.printStackTrace();
        }
    }

    public void wechatQuery(String mac,boolean isOld ,OnResultCallBack onResultCallBack){
//        String url = isOld?wechat_old_query:wechat_query;
        Request request = new Request.Builder()
                .url(device_wechat_query+"?device mac="+mac)
//                .addHeader("Connection", "close")
                .get()
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                onResultCallBack.onResult(true,result);
            } else {
                onResultCallBack.onResult(false,null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            onResultCallBack.onResult(false,null);
        } catch (Exception e) {
            onResultCallBack.onResult(false,null);
            e.printStackTrace();
        }
    }

    /**
     * {“product_id”:”33052”,”device_name”:”X9Pro”, “device_mac”:”xx:xx:xx:xx:xx:xx”}
     * @param mac
     * @param onResultCallBack
     */
    public void wechatRegister(String productID, String deviceName, String mac, OnResultCallBack onResultCallBack){
        RequestBody formBody = new FormBody.Builder()
                .add("product_id", productID)
                .add("device_name", deviceName)
                .add("device_mac", mac)
                .build();
        Request request = new Request.Builder()
//                .url(productID.equals("35788")?wechat_old_regist:wechat_regist)
                .url(device_wechat_regist)
                .post(formBody)
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                onResultCallBack.onResult(true,result);
            } else {
                onResultCallBack.onResult(false,null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            onResultCallBack.onResult(false,null);
        } catch (Exception e) {
            onResultCallBack.onResult(false,null);
            e.printStackTrace();
        }
    }

//    public void getDeviceList(OnResultCallBack onResultCallBack){
//        Log.i("strDeviceList","strDeviceList:getDeviceList");
//        Request request = new Request.Builder().url(device_list).build();
//        OkHttpClient client = new OkHttpClient();
//        try {
//            Response response = client.newCall(request).execute();
//            Log.i("strDeviceList","strDeviceList:response:"+response.isSuccessful());
//            if (response.isSuccessful()) {
//                String result = response.body().string();
//                onResultCallBack.onResult(true,result);
//            }else {
//                onResultCallBack.onResult(false,"");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
    public void getDeviceList(final OnResultCallBack onResultCallBack){
        Request request = new Request.Builder().url(device_list).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Log.i("strDeviceList","strDeviceList:onFailure");
//                onResultCallBack.onResult(false,"");
                Request request = new Request.Builder().url(device_list2).build();
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                Log.i("strDeviceList","strDeviceList:onFailure");
                onResultCallBack.onResult(false,"");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try{
                            Log.i("strDeviceList","strDeviceList:onResponse");
                            String result = response.body().string();
                            DeviceList filterDeviceList = new Gson().fromJson(result, DeviceList.class);
                            onResultCallBack.onResult(true,result);
                        }catch(Exception exception){
                            onResultCallBack.onResult(false,"");
                        }

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("strDeviceList","strDeviceList:onResponse");
                try{
                    String result = response.body().string();
                    DeviceList filterDeviceList = new Gson().fromJson(result, DeviceList.class);
                    onResultCallBack.onResult(true,result);
                }catch(Exception exception){
                    Request request = new Request.Builder().url(device_list2).build();
                    OkHttpClient client = new OkHttpClient();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i("strDeviceList","strDeviceList:onFailure");
                            onResultCallBack.onResult(false,"");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try{
                                Log.i("strDeviceList","strDeviceList:onResponse");
                                String result = response.body().string();
                                DeviceList filterDeviceList = new Gson().fromJson(result, DeviceList.class);
                                onResultCallBack.onResult(true,result);
                            }catch(Exception exception){
                                onResultCallBack.onResult(false,"");
                            }
                        }
                    });
                }
            }
        });
    }


    public void sendOtaData(Context mContext,OnResultCallBack onResultCallBack){
        String name = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME,"");
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC,"");
        String version = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION,"");
        String versionNew = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION_NEW,"");
        String phone = android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL;
        RequestBody formBody = new FormBody.Builder()
                .add("device_name", name)
                .add("device_mac", mac)
                .add("device_version", version)
                .add("app_name","iband")
                .add("update_version",versionNew)
                .add("app_type",phone)
                .build();
        Request request = new Request.Builder()
                .url(device_ota_record)
                .post(formBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                onResultCallBack.onResult(true,result);
            }else {
                onResultCallBack.onResult(false,"");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /***
     * @Name getSurveyData
     * @Func 发送用户反馈的信息，
     *        http://112.74.54.235/product/index.php/Api/Survey/服务器获取用户反馈信息接口
     * @Author Yanwen
     * @Data 18.11.14
     */
    public void getSurveyData(Map<String, Object> map, final OnResultCallBack onResultCallBack) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_GetSurveyData)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        NetInterfaceMethod blogServer = retrofit.create(NetInterfaceMethod.class);
        retrofit2.Call<String> call = blogServer.postGetSurveyData(map);
        call.enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                String result = response.body();
//                Log.d(TAG, "onResponse: "+result);
                onResultCallBack.onResult(true, result);
            }

            @Override
            public void onFailure(retrofit2.Call<String> call, Throwable t) {
//                Log.d(TAG, "发送失败，请确认网络正常.");
                onResultCallBack.onResult(false, call);
            }
        });
    }


    public void getCityWeather(String longitude, String latitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(heweather_city)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        NetInterfaceMethod netInterfaceMethod = retrofit.create(NetInterfaceMethod.class);
        retrofit2.Call call = netInterfaceMethod.getCityWeather(longitude, latitude);
        call.enqueue(new retrofit2.Callback() {
            @Override
            public void onResponse(retrofit2.Call call, retrofit2.Response response) {
                Log.d(TAG, "onResponse: ................");
            }

            @Override
            public void onFailure(retrofit2.Call call, Throwable t) {
                Log.d(TAG, "onFailure:................. ");
            }
        });
    }


}
