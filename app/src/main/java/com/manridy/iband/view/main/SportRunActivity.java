package com.manridy.iband.view.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.view.base.BaseActionActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class SportRunActivity extends BaseActionActivity {

    @BindView(R.id.tb_title)
    TextView tbTitle;
    @BindView(R.id.rl_tab)
    LinearLayout rlTab;
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
    @BindView(R.id.iv_pause)
    ImageView ivPause;
    @BindView(R.id.iv_end)
    ImageView ivEnd;
    @BindView(R.id.iv_map)
    ImageView ivMap;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sport_run);
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
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.button_out_left);
                Animation animation2 = AnimationUtils.loadAnimation(mContext, R.anim.button_out_right);
                ivStart.setVisibility(View.GONE);
                ivPause.startAnimation(animation);
                ivEnd.startAnimation(animation2);
                handler.sendEmptyMessageDelayed(0, 500);
            }
        });

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivPause.setEnabled(false);
                Animation animation2 = AnimationUtils.loadAnimation(mContext, R.anim.button_in_right);
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.button_in_left);
                ivPause.setVisibility(View.GONE);
                ivEnd.setVisibility(View.GONE);
                ivEnd.startAnimation(animation2);
                ivPause.startAnimation(animation);
                handler.sendEmptyMessageDelayed(1, 500);
            }
        });

        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(Location2Activity.class);
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                ivPause.setVisibility(View.VISIBLE);
                ivEnd.setVisibility(View.VISIBLE);
                ivStart.setEnabled(true);
            } else if (msg.what == 1) {
                ivStart.setVisibility(View.VISIBLE);
                ivPause.setEnabled(true);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
