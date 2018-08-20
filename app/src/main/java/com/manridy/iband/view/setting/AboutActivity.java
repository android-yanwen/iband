package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    @BindView(R.id.iv_icon)
    ImageView ivIcon;

    final static int COUNTS = 20;//点击次数
    final static long DURATION = 30 * 1000;//规定有效时间
    long[] mHits = new long[COUNTS];

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

        ivIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
//                    String tips = "您已在[" + DURATION + "]ms内连续点击【" + mHits.length + "】次了！！！";
//                    Toast.makeText(AboutActivity.this, tips, Toast.LENGTH_SHORT).show();
                    hiLangue.setVisibility(View.VISIBLE);
                }
            }
        });

    }



}
