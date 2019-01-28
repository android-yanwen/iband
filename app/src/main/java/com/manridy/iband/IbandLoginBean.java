package com.manridy.iband;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.bean.MRDServerRequestBean;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.bean.WeatherModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.OnResultCallBack;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.setting.FeedbackActivity;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IbandLoginBean {
    private static final String TAG = "IbandLoginBean";
    private Context mContext;
    private String country;
    private String login_app;
    private int login_num;
    private String logintime;
    private int sex;
    private int age;
    private String city;

    public IbandLoginBean(Context mContext) {
        this.mContext = mContext;
    }

    public void pushLoginNumToServer(final OnPushResultCallback onPushResultCallback) {
        //推送iband打开次数到服务器
        Map<String, Object> map = new HashMap<>();
        map.put("country", country);
        map.put("login_app", login_app);
        map.put("login_num", login_num);
        map.put("logintime", logintime);
        map.put("sex", sex);
        map.put("age", age);
        map.put("city", city);
        HttpService.getInstance().postSaveLoginData(map, new OnResultCallBack() {
            @Override
            public void onResult(boolean isSuccess, Object result) {
                if (isSuccess == true) {
                    String s_result = result.toString();
                    if (s_result != null && s_result != "") {
                        MRDServerRequestBean bean = new Gson().fromJson(s_result, MRDServerRequestBean.class);
                        if (onPushResultCallback != null) {
                            onPushResultCallback.onResult(bean.getStatus());
                        }
                    }
                } else {
                    if (onPushResultCallback != null) {
                        onPushResultCallback.onResult(0);
                    }
                }
                Log.d(TAG, "onResult: "+result);
            }
        });
    }

    public interface OnPushResultCallback{
        void onResult(int result);
    }


    public IbandLoginBean setLoginInfoToThisObj() {
        if (DataSupport.isExist(WeatherModel.class)) {
            WeatherModel weatherModel = DataSupport.findFirst(WeatherModel.class);
            if (weatherModel != null) {
                country = weatherModel.getCountry();
                city = weatherModel.getCity();
            }
        }
        login_app = "iband_Android";
        login_num = (int) SPUtil.get(this.mContext, AppGlobal.KEY_RECORDING_LOGIN_NUM, 0);
        logintime = (String) SPUtil.get(mContext, AppGlobal.KEY_RECORDING_LOGIN_YMD, "");
        UserModel curUser = IbandDB.getInstance().getUser();
        if (curUser == null) {
            curUser = new UserModel();
        }
        sex = curUser.getUserSex();
        age = Integer.parseInt(curUser.getUserAge());
        return this;
    }

    public void saveLoginDay() {
        //保存当天日期day
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SPUtil.put(mContext, AppGlobal.KEY_RECORDING_LOGIN_DAY, day);
        //保存當天日期yyyy-MM-dd
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//获取日期时间
        String s_ymd = simpleDateFormat.format(new Date());
        SPUtil.put(mContext, AppGlobal.KEY_RECORDING_LOGIN_YMD, s_ymd);
    }
    public int obtainLoginDay() {
        int day = (int) SPUtil.get(mContext, AppGlobal.KEY_RECORDING_LOGIN_DAY, 0);
        return day;
    }

    public String getCountry() {
        return country;
    }


    public String getLogin_app() {
        return login_app;
    }


    public int getLogin_num() {
        return login_num;
    }


    public String getLogintime() {
        return logintime;
    }

    public int getSex() {
        return sex;
    }


    public int getAge() {
        return age;
    }


    public String getCity() {
        return city;
    }

}
