package com.manridy.iband.view.setting;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.manridy.applib.common.AppManage;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.R;
import com.manridy.iband.adapter.LangueAdapter;
import com.manridy.iband.adapter.ViewAdapter;
import com.manridy.iband.bean.ViewModel;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.OnItemClickListener;
import com.manridy.iband.ui.items.UnitItems;
import com.manridy.iband.view.MainActivity;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.ble.BleCmd;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class LangueActivity extends BaseActionActivity {

    public static final Locale Locale_Ru = new Locale("RU","ru","");//自定义俄语
    public static final Locale Locale_Es = new Locale("ES","es","");//自定义俄语
    public static final Locale Locale_Pt = new Locale("PT","pt","");//自定义俄语
    public static final Locale Locale_Nl = new Locale("NL","nl","");//自定义俄语

    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.rv_langue)
    RecyclerView rvView;
    LangueAdapter langueAdapter;
    private int curSelect;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_langue);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_langue), getString(R.string.hint_save));
        initRecycler();
    }

    private void initRecycler() {
        langueAdapter = new LangueAdapter(getMenuData(mContext));
        rvView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        rvView.setItemAnimator(new DefaultItemAnimator());//设置动画效果
        ((SimpleItemAnimator) rvView.getItemAnimator()).setSupportsChangeAnimations(false);
        rvView.setAdapter(langueAdapter);
        langueAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                List<LangueAdapter.Menu> viewModels = langueAdapter.getData();
                for (int i = 0; i < viewModels.size(); i++) {
                    if (position == i) {
                        viewModels.get(i).menuCheck = true;
                        curSelect = i;
                    } else {
                        viewModels.get(i).menuCheck = false;
                    }
                }
                langueAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    protected void initListener() {
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.hint_switch_langue));
                Resources res = mContext.getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = res.getConfiguration();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (curSelect == 0) { //选择跟随系统
                        conf.setLocale(conf.getLocales().get(0));
                    } else { //设置选择的语言
                        conf.setLocale(getLocale(curSelect));
                    }
                } else {
                    conf.locale = getLocale(curSelect);
                }
                res.updateConfiguration(conf, dm);
                SPUtil.put(mContext,AppGlobal.DATA_APP_LANGUE,curSelect);
                AppManage.getInstance().finishAllActivity();
                startActivity(MainActivity.class);
            }
        });
    }

    public static Locale getLocale(int position) {
        List<Locale> localeList = new ArrayList<>();
        localeList.add(Locale.getDefault());
        localeList.add(Locale.CHINESE);
        localeList.add(Locale.TRADITIONAL_CHINESE);
        localeList.add(Locale.ENGLISH);
        localeList.add(Locale.GERMAN);
        localeList.add(Locale.FRENCH);
        localeList.add(Locale.JAPANESE);
        localeList.add(Locale.KOREAN);
        localeList.add(Locale.ITALIAN);
        localeList.add(Locale_Ru);
        localeList.add(Locale_Es);
        localeList.add(Locale_Pt);
        localeList.add(Locale_Nl);
        return localeList.get(position);
    }


//    英/德/俄/法/意/日/韩/葡/西/中/繁

    public static List<LangueAdapter.Menu> getMenuData(Context context) {
        List<LangueAdapter.Menu> menuList = new ArrayList<>();
        menuList.add(new LangueAdapter.Menu(context.getString(R.string.hint_langue_normal),true));
        menuList.add(new LangueAdapter.Menu("简体中文",false));
        menuList.add(new LangueAdapter.Menu("繁體中文",false));
        menuList.add(new LangueAdapter.Menu("English",false));
        menuList.add(new LangueAdapter.Menu("Deutsch",false));
        menuList.add(new LangueAdapter.Menu("France",false));
        menuList.add(new LangueAdapter.Menu("日本语",false));
        menuList.add(new LangueAdapter.Menu("한국어",false));
        menuList.add(new LangueAdapter.Menu("Italiano",false));
        menuList.add(new LangueAdapter.Menu("Русский язык",false));
        menuList.add(new LangueAdapter.Menu("español",false));
        menuList.add(new LangueAdapter.Menu("Português",false));
        menuList.add(new LangueAdapter.Menu("Dutch",false));
        return menuList;
    }

    @Override
    protected void loadData() {
        super.loadData();
        curSelect = (int) SPUtil.get(mContext,AppGlobal.DATA_APP_LANGUE,0);
        langueAdapter.setClickItem(curSelect);
    }


}
