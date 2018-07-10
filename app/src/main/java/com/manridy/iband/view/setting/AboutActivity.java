package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.manridy.applib.utils.VersionUtil;
import com.manridy.iband.R;
import com.manridy.iband.ui.items.HelpItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.view.test.TestMenuActivity;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 关于页面
 * Created by jarLiao on 17/5/4.
 */

public class AboutActivity extends BaseActionActivity {

    @BindView(R.id.hi_update)
    HelpItems hiUpdate;
    @BindView(R.id.hi_langue)
    HelpItems hiLangue;
    @BindView(R.id.hi_help)
    HelpItems hiHelp;
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.hi_data)
    HelpItems hiData;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_title_about));
        tvVersion.setText(getString(R.string.hint_current_version)+VersionUtil.getVersionName(mContext));
    }

    @Override
    protected void initListener() {
        hiUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(UpdateActivity.class);
            }
        });
        hiLangue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(LangueActivity.class);
            }
        });

        hiHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(UpdateActivity.isGoogle){
                    startActivity(HelpWebViewActivity.class);
//                }else {
//                    startActivity(HelpActivity.class);
//                }
            }
        });

        hiHelp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                IbandApplication.getIntance().service.watch.sendCmd(new byte[]{(byte) 0xfc,0x0c,0x03});
                return true;
            }
        });
        hiData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(TestMenuActivity.class);
                return true;
            }
        });

        hiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DataExportActivity.class);
            }
        });

    }



}
