package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.manridy.iband.IbandDB;
import com.manridy.iband.bean.ViewModel;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.iband.R;
import com.manridy.iband.adapter.ViewAdapter;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 界面选择页面
 * Created by jarLiao on 17/5/4.
 */

public class ViewActivity extends BaseActionActivity {

    @BindView(R.id.rv_view)
    RecyclerView rvView;
    @BindView(R.id.tb_menu)
    TextView tbMenu;

    ViewAdapter viewAdapter;
    List<ViewModel> viewList = new ArrayList<>();

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_view);
        ButterKnife.bind(this);
        setTitleAndMenu(getString(R.string.hint_menu_view),getString(R.string.hint_save));
        setStatusBarColor(Color.parseColor("#2196f3"));
    }

    @Override
    protected void initVariables() {
        initRecyclerView();

    }

    private void initRecyclerView() {
        viewList = IbandDB.getInstance().getView();
        if (viewList == null || viewList.size() == 0) {
            viewList = new ArrayList<>();
            viewList.add(new ViewModel(0,getString(R.string.hint_view_stand), R.mipmap.selection_standby,true,false));
            viewList.add(new ViewModel(1,getString(R.string.hint_view_step), R.mipmap.selection_step,true));
            viewList.add(new ViewModel(2,getString(R.string.hint_view_sport), R.mipmap.selection_sport,true));
            viewList.add(new ViewModel(3,getString(R.string.hint_view_hr), R.mipmap.selection_heartrate,true));
            viewList.add(new ViewModel(4,getString(R.string.hint_view_sleep), R.mipmap.selection_sleep,true));
            viewList.add(new ViewModel(9,getString(R.string.hint_view_clock), R.mipmap.selection_alarmclock,true));
            viewList.add(new ViewModel(7,getString(R.string.hint_view_find), R.mipmap.selection_find,true));
            viewList.add(new ViewModel(6,getString(R.string.hint_view_info), R.mipmap.selection_about,true));
            viewList.add(new ViewModel(5,getString(R.string.hint_view_off), R.mipmap.selection_turnoff,true));
        }else {
            Map<Integer,ViewModel> map = getMap();
            for (ViewModel viewModel : viewList) {
                int viewId = viewModel.getViewId();
                if (map.containsKey(viewId)) {
                    viewModel.setViewIcon(map.get(viewId).getViewIcon());
                }
            }
        }
        viewAdapter = new ViewAdapter(mContext,viewList);
        rvView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        ((SimpleItemAnimator)rvView.getItemAnimator()).setSupportsChangeAnimations(false);//去掉默认动画解决瞬闪问题
        rvView.setAdapter(viewAdapter);
        viewAdapter.onAttachedToRecyclerView(rvView);//依附RecyclerView添加grid头部
        viewAdapter.addHeaderView(LayoutInflater.from(mContext).inflate(R.layout.hander_view,null));
    }

    @Override
    protected void initListener() {
        viewAdapter.setOnItemClickListener(new ViewAdapter.onItemClickListener() {
            @Override
            public void itemClick(int dataPosition,int position) {
                ViewModel viewModel = (ViewModel) viewAdapter.getItemData(dataPosition);
                viewModel.setSelect(!viewModel.isSelect());
                viewAdapter.notifyItemChanged(position);
            }
        });

        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showProgress(getString(R.string.hint_saveing));
                int size = viewList.size();
                int[] onOffs = new int[size];
                int[] ids = new int[size];
                for (int i = 0; i < viewList.size(); i++) {
                    ids[i] = viewList.get(i).getViewId();
                    onOffs[i] = viewList.get(i).isSelect()? 1:0;
                }
                ibandApplication.service.watch.sendCmd(BleCmd.getWindowsSet(ids, onOffs), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        for (ViewModel viewModel : viewList) {
                            viewModel.save();
                        }
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_save_success));
                            }
                        });
                        finish();
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        dismissProgress();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast(getString(R.string.hint_save_fail));
                            }
                        });
                    }
                });
            }
        });
    }

    private  Map<Integer,ViewModel> getMap(){
        Map<Integer,ViewModel> map = new HashMap<>();
        map.put(0,new ViewModel(0,getString(R.string.hint_view_stand), R.mipmap.selection_standby,true,false));
        map.put(1,new ViewModel(1,getString(R.string.hint_view_step), R.mipmap.selection_step,true));
        map.put(2,new ViewModel(2,getString(R.string.hint_view_sport), R.mipmap.selection_sport,true));
        map.put(3,new ViewModel(3,getString(R.string.hint_view_hr), R.mipmap.selection_heartrate,true));
        map.put(4,new ViewModel(4,getString(R.string.hint_view_sleep), R.mipmap.selection_sleep,true));
        map.put(5,new ViewModel(5,getString(R.string.hint_view_off), R.mipmap.selection_turnoff,true));
        map.put(6,new ViewModel(6,getString(R.string.hint_view_info), R.mipmap.selection_about,true));
        map.put(7,new ViewModel(7,getString(R.string.hint_view_find), R.mipmap.selection_find,true));
        map.put(8,new ViewModel(8,getString(R.string.hint_view_hp), R.mipmap.selection_standby,true));
        map.put(9,new ViewModel(9,getString(R.string.hint_view_clock), R.mipmap.selection_alarmclock,true));
        map.put(10,new ViewModel(10,getString(R.string.hint_view_mac), R.mipmap.selection_standby,true));
        map.put(11,new ViewModel(10,getString(R.string.hint_view_download), R.mipmap.selection_standby,true));
        map.put(12,new ViewModel(11,getString(R.string.hint_view_div), R.mipmap.selection_standby,true));
        map.put(13,new ViewModel(12,getString(R.string.hint_view_run), R.mipmap.selection_standby,true));
        map.put(14,new ViewModel(13,getString(R.string.hint_view_hill), R.mipmap.selection_standby,true));
        map.put(15,new ViewModel(14,getString(R.string.hint_view_badminton), R.mipmap.selection_standby,true));
        map.put(16,new ViewModel(15,getString(R.string.hint_view_basketball), R.mipmap.selection_standby,true));
        map.put(17,new ViewModel(16,getString(R.string.hint_view_pingpong), R.mipmap.selection_standby,true));
        return map;
    }

    public  List<ViewModel> getMenuData(int[] ids) {
        List<ViewModel> viewList = new ArrayList<>();
        Map<Integer,ViewModel> map = getMap();
        for (int i = 0; i < ids.length; i++) {
            viewList.add(map.get(ids[i]));
        }
        return viewList;
    }


    public void syncViewData(){
        ibandApplication.service.watch.sendCmd(BleCmd.getWindowsChild(128), new BleCallback() {
            @Override
            public void onSuccess(Object o) {
                Type type = new TypeToken<ArrayList<com.manridy.sdk.bean.View>>() {}.getType();
                List<com.manridy.sdk.bean.View> views = new Gson().fromJson(o.toString(),type);
                Map<Integer,ViewModel> map = getMap();
                viewList.clear();
                for (com.manridy.sdk.bean.View view : views) {
                    ViewModel viewModel = map.get(view.getId());
                    viewModel.setSelect(view.isSelect());
                    viewList.add(viewModel);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

}
