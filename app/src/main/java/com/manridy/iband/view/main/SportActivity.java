package com.manridy.iband.view.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class SportActivity extends BaseActionActivity {

    @BindView(R.id.iv_signa)
    ImageView ivSigna;
    @BindView(R.id.iv_history)
    ImageView ivHistory;
    @BindView(R.id.tv_mi)
    TextView tvMi;
    @BindView(R.id.iv_speed_icon)
    ImageView ivSpeedIcon;
    @BindView(R.id.tv_speed)
    TextView tvSpeed;
    @BindView(R.id.line)
    TextView line;
    @BindView(R.id.iv_time_icon)
    ImageView ivTimeIcon;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.iv_start)
    ImageView ivStart;
    @BindView(R.id.tv_time_num)
    TextView tvTimeNum;
    @BindView(R.id.rl_time_num)
    RelativeLayout rlTimeNum;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sport);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));

    }

    @Override
    protected void initListener() {
        ivStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivStart.setEnabled(false);
                rlTimeNum.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(3);
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what > 0) {
                tvTimeNum.setText(String.valueOf(msg.what--));
                tvTimeNum.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.out_from_big));
                handler.sendEmptyMessageDelayed(msg.what,1000);
            }else {
                rlTimeNum.setVisibility(View.GONE);
                startActivity(SportRunActivity.class);
                ivStart.setEnabled(true);
            }
        }
    };
}
