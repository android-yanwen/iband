package com.manridy.iband.service;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.manridy.applib.utils.FileUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.VersionUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.DomXmlParse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    public static final String device_list = "http://120.78.138.141:8080/device_list.php";
    public static final String device_img = "http://120.78.138.141:8080/image/";
    public static final String device_ota_record = "http://120.78.138.141:8080/update/update_record.php";

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
                Log.d(TAG, "downloadXml() called with: url = [" + url + "]");
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

    public void downloadOTAFile(String url, OnResultCallBack onResultCallBack) {

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                InputStream is = response.body().byteStream();
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
        String url = isOld?wechat_old_query:wechat_query;
        Request request = new Request.Builder()
                .url(url+"?id="+mac)
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
                .url(productID.equals("35788")?wechat_old_regist:wechat_regist)
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

    public void getDeviceList(OnResultCallBack onResultCallBack){
        Request request = new Request.Builder().url(device_list).build();
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

}
