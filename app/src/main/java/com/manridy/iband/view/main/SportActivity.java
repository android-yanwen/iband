package com.manridy.iband.view.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.applib.base.BaseActivity;
import com.manridy.iband.R;
import com.manridy.iband.adapter.MenuAdapter;
import com.manridy.iband.adapter.PageAdapter;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.SuperViewPager;
import com.manridy.iband.view.model.sport.BikingFragment;
import com.manridy.iband.view.model.sport.IndoorRunFragment;
import com.manridy.iband.view.model.sport.OutdoorRunFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SportActivity extends BaseActivity {
    @BindView(R.id.vp_view)
    SuperViewPager vpView;
    MenuAdapter menuAdapter;
    @BindView(R.id.st_tab)
    SmartTabLayout stTab;
    @BindView(R.id.rl_top)
    RelativeLayout rlTop;
    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;
    @BindView(R.id.iv_line)
    ImageView ivLine;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_more)
    ImageView ivMore;

    boolean isRegistEventBus;



    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sport);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        initViewPager();
        setStatusBar();
    }

    @Override
    protected void initListener() {

    }

    @OnClick({R.id.iv_back, R.id.iv_more})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                startActivity(new Intent().setClass(mContext,MainActivity.class));
                break;
            case R.id.iv_more:
                startActivity(new Intent().setClass(mContext,SportFunctionActivity.class));
                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {
        switch (event.getWhat()){
                case EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_GONE:
                    rlTop.setVisibility(View.GONE);
                    tvToolbar.setVisibility(View.GONE);
                    ivLine.setVisibility(View.GONE);
                    break;
                case EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_VISIBLE:
                    rlTop.setVisibility(View.VISIBLE);
                    tvToolbar.setVisibility(View.VISIBLE);
                    ivLine.setVisibility(View.VISIBLE);
                    break;
        }

    }

    private void initViewPager() {

        PageAdapter adapter = new PageAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(getResources().getString(R.string.hint_outdoors_run), OutdoorRunFragment.class)
                .add(getResources().getString(R.string.hint_indoors_run), IndoorRunFragment.class)
                .add(getResources().getString(R.string.hint_cycling), BikingFragment.class)
//                .add("血压", BpFragment.class)
//                .add("血氧", BoFragment.class)
                .create());
        vpView.setAdapter(adapter);
        stTab.setViewPager(vpView);
        vpView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                EventBus.getDefault().post(new EventMessage(EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_VISIBLE));

            }
        });
    }

    protected void registerEventBus() {
        isRegistEventBus = true;
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerEventBus();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegistEventBus) {
            EventBus.getDefault().unregister(this);
        }
    }




}
