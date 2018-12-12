package com.manridy.iband.view.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class HelpItemActivity extends BaseActionActivity {

    @BindView(R.id.tv_content)
    TextView tvContent;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_img)
    ImageView ivImg;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_help_item);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        int position = getStartType();
        String[] titleStrs = getResources().getStringArray(R.array.helpTitleList);
        String[] contentStrs = getResources().getStringArray(R.array.helpContentList);
        setTitleBar(getString(R.string.hint_menu_help));
        tvTitle.setText(titleStrs[position]);
        tvContent.setText(contentStrs[position]);
        if (position == 8) {
            ivImg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
