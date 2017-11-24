package com.manridy.iband.view.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.manridy.iband.R;
import com.manridy.iband.adapter.HelpAdapter;
import com.manridy.iband.common.OnItemClickListener;
import com.manridy.iband.view.base.BaseActionActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class HelpActivity extends BaseActionActivity {

    @BindView(R.id.rv_help)
    RecyclerView rvHelp;

    Map<String,String> curHelpList = new HashMap<>();
    ArrayList<HelpAdapter.Menu> helpTitleList = new ArrayList<>();
    HelpAdapter helpAdapter;



    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_menu_help));
        String[] titleStrs = getResources().getStringArray(R.array.helpTitleList);
        for (String titleStr : titleStrs) {
            helpTitleList.add(new HelpAdapter.Menu(titleStr));
        }
        helpAdapter = new HelpAdapter(helpTitleList);
        rvHelp.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        rvHelp.setAdapter(helpAdapter);
    }

    @Override
    protected void initListener() {
        helpAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(HelpItemActivity.class,position);
            }
        });
    }

}
