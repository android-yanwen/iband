package com.manridy.iband.view.setting;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.applib.base.BaseActivity;
import com.manridy.applib.common.DialogManage;
import com.manridy.applib.utils.BitmapUtil;
import com.manridy.applib.view.dialog.ListDialog;
import com.manridy.iband.R;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.network.NetInterfaceMethod;
import com.manridy.iband.view.base.BaseActionActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * 意见反馈
 * Created by yanwen on 18/11/07.
 */

public class FeedbackActivity extends BaseActionActivity {


    private TextView tbTitle;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_feedback);
        tbTitle = (TextView) findViewById(R.id.tb_title);
    }

    @Override
    protected void initVariables() {
        tbTitle.setText("意见反馈");


    }

    @Override
    protected void loadData() {
        super.loadData();

    }

    @Override
    protected void initListener() {
        findViewById(R.id.tb_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 发送意见
        findViewById(R.id.bt_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl("http://112.74.54.235/product/index.php/Api/Goods/")
//                        .build();
//
//                BlogServer blogServer = retrofit.create(BlogServer.class);
//                Call<ResponseBody> call = blogServer.getBlog();
//                call.enqueue(new retrofit2.Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//                        ResponseBody result = response.body();
//                        try {
//                            String ss = result.string();
//                            Log.i(TAG, "onResponse: " + ss);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Log.d(TAG, "onFailure: ");
//
//                    }
//                });

//                Retrofit retrofit = new Retrofit.Builder()
//                        .baseUrl("http://120.78.138.141:8080/")
////                        .addConverterFactory(GsonConverterFactory.create())
////                        .baseUrl("http://112.74.54.235/product/index.php/Api/Survey/")
//                        .addConverterFactory(ScalarsConverterFactory.create())
//                        .build();
//                NetInterfaceMethod blogServer = retrofit.create(NetInterfaceMethod.class);
//                Map<String, String> map = new HashMap<>();
//                map.put("device_name", "N109");
//                map.put("device_mac", "EE6997DE995B");
//                map.put("product_id", "35788");
//                Call<String> call = blogServer.postWechatRegister(map);
//                call.enqueue(new retrofit2.Callback<String>() {
//                    @Override
//                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
//                        //                            String result = response.body().string();
//                        String result = response.body();
//                        Log.d(TAG, "onResponse: "+result);
//                        Toast.makeText(FeedbackActivity.this, result, Toast.LENGTH_SHORT).show();
//                        //                            Log.d(TAG, "onResponse: " + result);
//                    }
//
//                    @Override
//                    public void onFailure(Call<String> call, Throwable t) {
//                        Log.d(TAG, "onFailure");
//                    }
//                });


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://112.74.54.235/product/index.php/Api/Survey/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();
                NetInterfaceMethod blogServer = retrofit.create(NetInterfaceMethod.class);
                Map<String, Object> map = new HashMap<>();
                map.put("mac", "");
                map.put("app_name", "");
                map.put("phone_system_version", "");
                map.put("device_id", 35788);
                map.put("device_name", "");
                map.put("username", "");
                map.put("email", "");
                map.put("firmware_edition", "");
                map.put("soft_edition", "");
                map.put("live_city", "");
                map.put("age", "");
                map.put("sex", "");
                map.put("height", "");
                map.put("weight", "");
                map.put("step_size", "");
                map.put("question_desc", "");
                Call<String> call = blogServer.postGetSurveyData(map);
                call.enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        String result = response.body();
                        Log.d(TAG, "onResponse: "+result);
                        Toast.makeText(FeedbackActivity.this, result, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, "onFailure");
                    }
                });
            }
        });
    }



}
