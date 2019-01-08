package com.manridy.iband.view.setting;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.applib.base.BaseActivity;
import com.manridy.applib.common.DialogManage;
import com.manridy.applib.utils.BitmapUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.utils.VersionUtil;
import com.manridy.applib.view.dialog.ListDialog;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.bean.WeatherModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.network.NetInterfaceMethod;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.ui.EditItem;
import com.manridy.iband.view.base.BaseActionActivity;

import org.litepal.crud.DataSupport;

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
    private EditItem eiAge;
    private EditText et_question;

    private UserModel curUser;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_feedback);
        tbTitle = (TextView) findViewById(R.id.tb_title);
        eiAge = (EditItem) findViewById(R.id.ei_age);
        eiAge.setEtTextInputTypeIsEmail(); //输入Email地址
        et_question = (EditText) findViewById(R.id.et_question);
    }

    @Override
    protected void initVariables() {
        tbTitle.setText(R.string.activity_feedback);


    }

    @Override
    protected void loadData() {
        super.loadData();
        curUser = IbandDB.getInstance().getUser();
        if (curUser == null) {
            curUser = new UserModel();
        }
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
                UploadInfo uploadInfo = obtainInfo();
                if (uploadInfo == null) {
                    return;
                }

                Map<String, Object> map = new HashMap<>();
                map.put("mac", uploadInfo.deviceMac);//
                map.put("app_name", uploadInfo.appName);//
                map.put("phone_system_version", uploadInfo.phoneSysVersion);//
                map.put("device_id", uploadInfo.deviceId);//
                map.put("device_name", uploadInfo.deviceName);//
                map.put("username", uploadInfo.userName);//
                map.put("email", uploadInfo.email);//
                map.put("firmware_edition", uploadInfo.deviceVersion);//
                map.put("soft_edition", uploadInfo.softVersion);//
                map.put("live_city", uploadInfo.liveCity);
                map.put("age", uploadInfo.userAge);//
                map.put("sex", uploadInfo.userSex);//
                map.put("height", uploadInfo.userHeight);//
                map.put("weight", uploadInfo.userWeight);//
                map.put("step_size", uploadInfo.stepSize);//
                map.put("question_desc", uploadInfo.question);//
                showProgress(getString(R.string.activity_sending));
                HttpService.getInstance().getSurveyData(map, new OnResultCallBack() {
                    @Override
                    public void onResult(boolean isSuccess, Object result) {
                        if (isSuccess == true) {
                            Toast.makeText(FeedbackActivity.this, getString(R.string.activity_send_success), Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        } else {
                            Toast.makeText(FeedbackActivity.this, getString(R.string.activity_send_fail), Toast.LENGTH_SHORT).show();
                        }
                        dismissProgress();
                        Log.d(TAG, "onResult: "+result);
                    }
                });
                /*Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://112.74.54.235/product/index.php/Api/Survey/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();
                NetInterfaceMethod blogServer = retrofit.create(NetInterfaceMethod.class);
                Call<String> call = blogServer.postGetSurveyData(map);
                showProgress("发送中...");
                call.enqueue(new retrofit2.Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        String result = response.body();
                        Log.d(TAG, "onResponse: "+result);
                        Toast.makeText(FeedbackActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                        dismissProgress();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, "发送失败，请确认网络正常.");
                        dismissProgress();
                    }
                });*/
            }
        });
    }


    /**
     * @Name obtainInfo
     * @Func 获得反馈信息页面需要的信息
     * @Author yanwen
     * @Date 18.11.12
     */
    private UploadInfo obtainInfo() {
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.question = et_question.getText().toString();
        if (uploadInfo.question.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.activity_input_question), Toast.LENGTH_SHORT).show();
            return null;
        }
        uploadInfo.email = eiAge.getContent();
        if (uploadInfo.email.isEmpty()) {
            Toast.makeText(mContext, getString(R.string.activity_input_email), Toast.LENGTH_SHORT).show();
            return null;
        }
        uploadInfo.deviceMac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        uploadInfo.appName = "iband";
        uploadInfo.deviceName = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_NAME, "");
        uploadInfo.deviceVersion = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_VERSION, "");
        uploadInfo.softVersion = "v" + VersionUtil.getVersionName(mContext);
        String sId = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
        if (!sId.equals("") && sId != null) {
            uploadInfo.deviceId = Integer.parseInt(sId);
        }
        uploadInfo.phoneSysVersion = /*Build.BRAND + */"Android "+Build.MODEL + " "+android.os.Build.VERSION.RELEASE;
        if (curUser == null) {
            return null;
        }
        uploadInfo.userName = curUser.getUserName();
        WeatherModel weatherModel = DataSupport.findFirst(WeatherModel.class);
        if (weatherModel != null) {
//        uploadInfo.liveCity = ibandApplication.city;
            uploadInfo.liveCity = weatherModel.getCity();
        }
        uploadInfo.userAge = Integer.parseInt(curUser.getUserAge());
        uploadInfo.userSex = curUser.getUserSex();
        uploadInfo.userHeight = Integer.parseInt(curUser.getUserHeight());
        uploadInfo.userWeight = Integer.parseInt(curUser.getUserWeight());
        return uploadInfo;
    }


    /**
     * 需要上传给服务器的一些信息
     */
    private class UploadInfo{
        public String deviceMac;
        public String appName;
        public String deviceName;
        public String deviceVersion;
        public String softVersion;
        public int deviceId;
        public String phoneSysVersion;
        public String email;
        public String userName;
        public String liveCity="未知";
        public int userAge;
        public int userSex;
        public int userHeight;
        public int userWeight;
        public int stepSize = 80;
        public String question;
    }

}
