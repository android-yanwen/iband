package com.manridy.iband.view.model.sport;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.manridy.iband.R;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseEventFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OutdoorRunFragment extends BaseEventFragment {
    @BindView(R.id.fl_content)
    FrameLayout flContent;

    @Override
    protected View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        root = inflater.inflate(R.layout.fragment_sport_outdoor_run, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    protected void initVariables() {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment mFragment = new OutdoorRunMainFragment();
        transaction.replace(R.id.fl_content, mFragment);
        transaction.commit();
    }

    @Override
    protected void initListener() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventMessage event) {

    }

    @Override
    public void onResume() {
        super.onResume();
//        EventBus.getDefault().post(new EventMessage(EventGlobal.VIEW_SPORTACTIVITY_HEADBAR_VISIBLE));
    }
}
