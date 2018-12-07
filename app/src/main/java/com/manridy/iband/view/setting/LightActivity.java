package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jaygoo.widget.RangeSeekbar;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class LightActivity extends BaseActionActivity {

    @BindView(R.id.tv_light_num)
    TextView tvLightNum;
    @BindView(R.id.rs_light)
    RangeSeekbar rsLight;
    @BindView(R.id.tv_reduce)
    TextView tvReduce;
    @BindView(R.id.tv_add)
    TextView tvAdd;
    @BindView(R.id.tb_back)
    ImageView tbBack;
    @BindView(R.id.tb_title)
    TextView tbTitle;
    int curLight;
    int oldLight = -1;
    @BindView(R.id.rg_light)
    RadioGroup rgLight;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_light);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_menu_light));
        curLight = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_LIGHT, 2);
        curLight = curLight > 2 ? 1 : curLight;
        rgLight.check(getLightRes(curLight));
    }

    @Override
    protected void initListener() {
        rgLight.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_left:
                        curLight = 0;
                        break;
                    case R.id.rb_center:
                        curLight = 1;
                        break;
                    case R.id.rb_right:
                        curLight = 2;
                        break;
                }
                SPUtil.put(mContext, AppGlobal.DATA_SETTING_LIGHT, curLight);
                ibandApplication.service.watch.sendCmd(BleCmd.setLight(curLight));
            }
        });
    }

    private int getLightRes(int curLight) {
        int res = R.id.rb_right;
        switch (curLight) {
            case 0:
                res = R.id.rb_left;
                break;
            case 1:
                res = R.id.rb_center;
                break;
        }
        return res;
    }


}
