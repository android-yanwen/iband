package com.manridy.iband.view.model.sport;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseEventFragment;
import com.manridy.iband.view.main.CountDownActivity;
import com.manridy.iband.view.main.TrainActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IndoorRunMainFragment extends BaseEventFragment {

    @BindView(R.id.iv_start_run)
    ImageView iv_start_run;
    @BindView(R.id.iv_ic_his)
    ImageView iv_ic_his;
    @BindView(R.id.tv_exercise_time)
    TextView tv_exercise_time;
    @BindView(R.id.tv_pace)
    TextView tv_pace;
    @BindView(R.id.tv_distance)
    TextView tv_distance;

    StepModel stepModel;

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sport_indoor_run_main, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {
//        setStatusBarColor(Color.parseColor("#151515"));

    }

    @Override
    protected void initListener() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {

    }

    @OnClick({R.id.iv_start_run,R.id.iv_ic_his})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_start_run:
                    Intent intent = new Intent();
                    intent.putExtra("objectActivity",2);
                    intent.setClass(getActivity(),CountDownActivity.class);
                    startActivity(intent);
                break;
            case R.id.iv_ic_his:
                startActivity(TrainActivity.class);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stepModel = IbandDB.getInstance().getLastIndoorRunData();
        if(stepModel!=null) {
            DecimalFormat df = new DecimalFormat("0.00");
            String str_distance_km = df.format((float) stepModel.getStepMileage() / 1000);
            tv_distance.setText(str_distance_km);

            tv_pace.setText(stepModel.getPace());

            tv_exercise_time.setText(stepModel.getRunTime());
        }
    }
}
