package com.manridy.iband.view.history;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.applib.utils.TimeUtil;
import com.manridy.applib.view.dialog.DateDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.adapter.EcgDataAdapter;
import com.manridy.iband.adapter.EcgHistoryAdapter;
import com.manridy.iband.bean.EcgDataBean;
import com.manridy.iband.bean.EcgHistoryModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.chars.LeftLine;
import com.manridy.iband.ui.chars.SimpleDividerItemDecoration;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 心电回放
 * Created by jarLiao on 17/5/11.
 */

public class EcgRePlayHistoryActivity extends BaseActionActivity {

    private String filePath;

    EcgHistoryAdapter historyAdapter;
    List<EcgHistoryAdapter.Item> itemList = new ArrayList<>();

    int historyType;
    int color,lineColor;
    Calendar mCalendar;
    SimpleDateFormat mDateFormat;
    List<String> days;
    int dataAvg = 0, dataMax = 0, dataMin = 0;
    double boAvg,boMax,boMin;
    EcgHistoryAdapter.Item curItem;
    String ecg_data_id;
    @BindView(R.id.rv_history)
    RecyclerView rvHistory;
    @BindView(R.id.loading)
    ProgressBar pb_loading;
    @BindView(R.id.i_seekbar)
    IndicatorSeekBar indicatorSeekBar;
    @BindView(R.id.ll_leftline)
    LeftLine leftLine;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_history_replay_ecg);
        ButterKnife.bind(this);
        ecg_data_id = getIntent().getStringExtra("ecg_data_id");
        color = Color.parseColor("#26a69a");
        lineColor = Color.parseColor("#D2D2D2");
        setTitleBar(getString(R.string.title_ecg_history), color);
        setStatusBarColor(color);
        ecg_data_id = getIntent().getStringExtra("ecgDataId");

    }

    @Override
    protected void initVariables() {
        registerEventBus();
    }

    @Override
    protected void initListener() {
        findViewById(R.id.tb_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(getBaseContext(),EcgHistoryActivity.class);
//                startActivity(intent);
                finish();
            }
        });
        indicatorSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        rvHistory.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager l = (LinearLayoutManager)recyclerView.getLayoutManager();
                int adapterNowPos = l.findFirstVisibleItemPosition();
                int allItems = l.getItemCount();
                float item = ((float)adapterNowPos/(allItems-1))*100;
                if(hrBaseLines.size()>0){
                    float num = ((float)adapterNowPos/(allItems))*hrBaseLines.size();
                    leftLine.setmData(hrBaseLines.get((int)num));
                }

                indicatorSeekBar.setProgress(item);
                Log.i("indicatorSeekBar","adapterNowPos:"+adapterNowPos+";allItems:"+allItems+";item:"+item);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event){
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event){
    }

    private boolean isRunning = true;
    Thread thread;

    List<EcgDataBean> ecgDataBeanList;
    int ecgDataBeanListItem;
    @Override
    public void onResume() {
        super.onResume();
//        handler.post(200,runnable);
        thread = new Thread(runnable);
        thread.start();
//        handler.postDelayed(runnable,100);
        isRunning = true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {


            List<EcgHistoryModel> ecgHistoryModelList = IbandDB.getInstance().getEcgHistory(ecg_data_id);
//            if(ecgHistoryModelList.size()>0){
//                EcgHistoryModel ecgHistoryModel = ecgHistoryModelList.get(0);
//                tvNum.setText(""+ecgHistoryModel.getAvgHr());
//                tvTime.setText(ecgHistoryModel.getEcgStartDate().substring(11)+"-"+ecgHistoryModel.getEcgEndDate().substring(11));
//            }
            ecgDataBeanList = null;
            ecgDataBeanList = IbandDB.getInstance().getEcgDataBean(ecg_data_id);
            ecgDataBeanListItem = 0;
            if(ecgDataBeanList!=null&&ecgDataBeanListItem>=ecgDataBeanList.size()){
                return;
            }
            EcgDataBean ecgDataBean;
            String str_ecg;
            List<String> list_str;
            List<Integer> all = new LinkedList<>();
            hrBaseLines = new ArrayList<>();
            for(int i=0;i<ecgDataBeanList.size();i++){
                ecgDataBean = ecgDataBeanList.get(i);
                hrBaseLine = ecgDataBean.getRate_aided_signal();
                str_ecg = ecgDataBean.getEcg();
                list_str = java.util.Arrays.asList(str_ecg.split(","));
                List<Integer> list = new LinkedList<>();
                for(String ecg : list_str){
                    list.add(Integer.valueOf(ecg));
                }
                all.addAll(list);
                hrBaseLines.add(hrBaseLine);
//                        curItemList2.add(new EcgDataAdapter.Item(list,hrBaseLine));
            }

            int k = 0;
            ArrayList<Integer> ecgs = new ArrayList<>();
            for(int j = 0;j<all.size();j++){
                if(k<480){
                    ecgs.add(all.get(j));
                    k++;
                }else{
                    curItemList2.add(new EcgDataAdapter.Item(ecgs,hrBaseLine));
                    ecgs = new ArrayList<>();
                    ecgs.add(all.get(j));
                    k=1;
                }
            }


            if(isRunning) {
                Message message = handler.obtainMessage();
                handler.removeMessages(1);
                message.what = 1;
                handler.sendMessage(message);
                Log.i(TAG, "ecg_data_id:" + ecg_data_id);
                handler.sendMessage(handler.obtainMessage(2));
            }
        }
    };

    EcgDataAdapter historyDataAdapter;
    private int hrBaseLine;
    private List<EcgDataAdapter.Item> curItemList2 = new ArrayList<>();
    private List<Integer> hrBaseLines = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    historyDataAdapter = new EcgDataAdapter(curItemList2);

                    rvHistory.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false));
                    rvHistory.setAdapter(historyDataAdapter);
                    rvHistory.addItemDecoration(new SimpleDividerItemDecoration());
                    rvHistory.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            historyDataAdapter.notifyDataSetChanged();
                        }
                    });
                    if(hrBaseLines.size()>0){
                        leftLine.setmData(hrBaseLines.get(0));
                    }
                    break;
                case 2:
                    pb_loading.setVisibility(View.GONE);
                    break;
            }
        }
    };
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        handler.removeMessages(1);
        if(thread!=null){
            isRunning = false;
            thread.interrupt();
        }
    }
}
