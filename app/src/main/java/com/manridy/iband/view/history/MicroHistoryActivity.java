package com.manridy.iband.view.history;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.applib.utils.TimeUtil;
import com.manridy.applib.view.dialog.DateDialog;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.adapter.HistoryAdapter;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.ui.items.DataItems;
import com.manridy.iband.view.base.BaseActionActivity;

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
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 微循环历史
 * Created by yw on 18/12/24.
 */

public class MicroHistoryActivity extends BaseActionActivity {


    @BindView(R.id.tv_hint)
    TextView tvHint;
    @BindView(R.id.tv_num)
    TextView tvNum;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_unit)
    TextView tvUnit;
    @BindView(R.id.line)
    TextView line;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.di_data1)
    DataItems diData1;
    @BindView(R.id.di_data2)
    DataItems diData2;
    @BindView(R.id.di_data3)
    DataItems diData3;
    @BindView(R.id.rl_history)
    RelativeLayout rlHistory;
    @BindView(R.id.rv_history)
    RecyclerView rvHistory;
    @BindView(R.id.tb_share)
    ImageView ivShare;

    private String filePath;

    HistoryAdapter historyAdapter;
    List<HistoryAdapter.Item> itemList = new ArrayList<>();

    int historyType;
    int color,lineColor;
    Calendar mCalendar;
    SimpleDateFormat mDateFormat;
    List<String> days;
    int dataAvg = 0, dataMax = 0, dataMin = 0;
    float dataAvg1 = 0, dataMax1 = 0, dataMin1 = 0;
    double boAvg,boMax,boMin;
    HistoryAdapter.Item curItem;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_history_micro);
        ButterKnife.bind(this);
        ivShare.setVisibility(View.VISIBLE);
    }

    private Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    ivShare.setEnabled(false);
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessageDelayed(message,1500);
                    showShare();
                    break;
                case 2:
                    ivShare.setEnabled(true);
                    break;
            }
        }
    };

    public void screenShot(){
        handler2.post(new Runnable() {
            @Override
            public void run() {
                View dView = getWindow().getDecorView();
                dView.setDrawingCacheEnabled(false);
                dView.destroyDrawingCache();
                dView.buildDrawingCache();
                Bitmap bitmap = Bitmap.createBitmap(dView.getDrawingCache());
                if (bitmap != null) {
                    try {
                        // 获取内置SD卡路径
                        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//                        String sdCardPath = getBaseContext().getCacheDir().getPath();
                        // 图片文件路径
                        filePath = sdCardPath + File.separator + "share_screenshot_"+System.currentTimeMillis()+".png";
                        File file = new File(filePath);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
//                        if(file.exists()){
//                            file.delete();
//                        }
                        FileOutputStream os = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.flush();
                        os.close();
                        Message message = handler2.obtainMessage();
                        message.what = 1;
                        handler2.sendMessageDelayed(message,500);
                    } catch (Exception e) {
                    }
                }
            }
        });


    }

    private void showShare() {

        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，微信、QQ和QQ空间等平台使用
//        oks.setTitle(getString(R.string.share));
//        oks.setTitle("分享");
        // titleUrl QQ和QQ空间跳转链接
//        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
//        oks.setText("我是分享文本");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

//        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
//        // 图片文件路径
//        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
        File file = new File(filePath);
        if(file.exists()){
            oks.setImagePath(filePath);//确保SDcard下面存在此张图片
            // url在微信、微博，Facebook等平台中使用
//        oks.setUrl("http://sharesdk.cn");
            // comment是我对这条分享的评论，仅在人人网使用
//        oks.setComment("我是测试评论文本");
            // 启动分享GUI

//            oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
//                @Override
//                public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
//                    if (platform.getName().equalsIgnoreCase(QQ.NAME)) {
//                        paramsToShare.setText(null);
//                        paramsToShare.setTitle(null);
//                        paramsToShare.setTitleUrl(null);
//
////                        String sdCardPath = Environment.getExternalStorageDirectory().getPath()+"/manridy/";
////                        // 图片文件路径
////                        String filePath = sdCardPath + File.separator + "share_screenshot_"+new Date()+".png";
//                        paramsToShare.setImagePath(filePath);
//                    }
//                }
//            });
            oks.setCallback(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }

                @Override
                public void onCancel(Platform platform, int i) {
                    Message message = handler2.obtainMessage();
                    message.what = 2;
                    handler2.sendMessage(message);
                }
            });
            oks.show(this);
        }

    }
    @Override
    protected void initVariables() {
        registerEventBus();
        historyType = getIntent().getIntExtra("history_type",0);
        mCalendar =Calendar.getInstance();
        mDateFormat = new SimpleDateFormat("yyyy-MM");
        historyAdapter = new HistoryAdapter(itemList);
        rvHistory.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        rvHistory.setAdapter(historyAdapter);
        initHistoryTitle();
        initHistoryData();
    }

    @Override
    protected void initListener() {
        ivShare.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                screenShot();
//                showShare();
            }
        });

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time;
                int[] times = new int[]{1999,07,01};
                if (tvDate.getText().equals(getString(R.string.hint_month_current))) {
                    time = mDateFormat.format(new Date());
                }else {
                    time = tvDate.getText().toString();
                }
                if (time.length() >= 7) {
                    int year = Integer.parseInt(time.substring(0,4));
                    int month = Integer.parseInt(time.substring(5,7));
                    times = new int[]{year,month-1};
                }
                new DateDialog(mContext,times , getString(R.string.hint_select_month), new DateDialog.DateDialogListener() {
                    @Override
                    public void getTime(int year, int monthOfYear, int dayOfMonth) {
                        String time = year + "-" + TimeUtil.zero(monthOfYear+1);
                        mCalendar.set(year, monthOfYear, dayOfMonth);
                        if (time.equals(mDateFormat.format(new Date()))) {
                            tvDate.setText(getString(R.string.hint_month_current));
                        }else {
                            tvDate.setText(time);
                        }
                        initHistoryData();
                    }
                }).show();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.REFRESH_VIEW_MICRO_HISTORY) {
            historyAdapter.setItemList(itemList);
            diData1.setItemData(getString(R.string.hint_hr_avg), dataAvg1 +"",getString(R.string.hint_unit_hr),lineColor);
            diData2.setItemData(getString(R.string.hint_hr_min),dataMin1 +"",getString(R.string.hint_unit_hr),lineColor);
            diData3.setItemData(getString(R.string.hint_hr_max),dataMax1 +"",getString(R.string.hint_unit_hr),lineColor);
            dataAvg1 = dataMin1 = dataMax1 = 0.0f;
            if (curItem !=null){
                tvNum.setText(curItem.itemNum);
                tvTime.setText(curItem.itemName);
            }else{
                tvNum.setText("--");
                tvTime.setText(getString(R.string.hint_date));
                diData1.setItemData("--");
                diData2.setItemData("--");
                diData3.setItemData("--");
            }
        }else if (event.getWhat() == EventGlobal.REFRESH_VIEW_HR_HISTORY) {
            historyAdapter.setItemList(itemList);
            diData1.setItemData(getString(R.string.hint_average_cycle), dataAvg +"",getString(R.string.hint_unit_hr),lineColor);
            diData2.setItemData(getString(R.string.hint_minimum_cycle),dataMin +"",getString(R.string.hint_unit_hr),lineColor);
            diData3.setItemData(getString(R.string.hint_highest_cycle),dataMax +"",getString(R.string.hint_unit_hr),lineColor);
            if (curItem !=null){
                tvNum.setText(curItem.itemNum);
                tvTime.setText(curItem.itemName);
            }else{
                tvNum.setText("--");
                tvTime.setText(getString(R.string.hint_date));
                diData1.setItemData("--");
                diData2.setItemData("--");
                diData3.setItemData("--");
            }
        }else if (event.getWhat() == EventGlobal.REFRESH_VIEW_BP_HISTORY) {
            historyAdapter.setItemList(itemList);
            String time = mDateFormat.format(mCalendar.getTime());
            diData1.setItemData(getString(R.string.hint_time),time,"",lineColor);
            diData2.setItemData(getString(R.string.hint_hp_avg),dataMax +"","mmHg",lineColor);
            diData3.setItemData(getString(R.string.hint_lp_avg),dataMin +"","mmHg",lineColor);
            if (curItem !=null){
                tvNum.setText(curItem.itemNum);
                tvTime.setText(curItem.itemName);
            }else{
                tvNum.setText("--");
                tvTime.setText(getString(R.string.hint_date));
                diData1.setItemData("--");
                diData2.setItemData("--");
                diData3.setItemData("--");
            }
        }else if (event.getWhat() == EventGlobal.REFRESH_VIEW_BO_HISTORY) {
            historyAdapter.setItemList(itemList);
            String str = String.format(Locale.US,"%.1f ", boAvg);
            diData1.setItemData(getString(R.string.hint_avg),str+"","%",lineColor);
            diData2.setItemData(getString(R.string.hint_min), boMin +"","%",lineColor);
            diData3.setItemData(getString(R.string.hint_max), boMax +"","%",lineColor);
            if (curItem !=null){
                tvNum.setText(curItem.itemNum);
                tvTime.setText(curItem.itemName);
            }else{
                tvNum.setText("--");
                tvTime.setText(getString(R.string.hint_date));
                diData1.setItemData("--");
                diData2.setItemData("--");
                diData3.setItemData("--");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event){
        if (event.getWhat() == EventGlobal.DATA_LOAD_HR_HISTORY) {
            curItem = null;
            days = TimeUtil.getMonthToDay(mCalendar);
            itemList = IbandDB.getInstance().getMonthHeart(days);
            if (itemList.size()>0) {
                curItem = itemList.get(0);
                dataMin =Integer.valueOf(curItem.itemNum);
            }
            for (HistoryAdapter.Item item : itemList) {
                int num = Integer.valueOf(item.itemNum);
                dataAvg += num;
                dataMax = dataMax > num ? dataMax : num;
                dataMin = dataMin < num ? dataMin : num;
            }
            if (itemList.size() != 0) {
                dataAvg /= itemList.size();
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_HR_HISTORY));
        }else if (event.getWhat() == EventGlobal.DATA_LOAD_BP_HISTORY) {
            curItem = null;
            days = TimeUtil.getMonthToDay(mCalendar);
            itemList = IbandDB.getInstance().getMonthBp(days);
            if (itemList.size()>0) {
                curItem = itemList.get(0);
            }
            for (HistoryAdapter.Item item : itemList) {
                String[] strs =item.itemNum.split("/");
                int hp = Integer.parseInt(strs[0]);
                int lp = Integer.parseInt(strs[1]);
                dataMax += hp;
                dataMin += lp;
            }
            if (itemList.size() != 0) {
                dataMax /= itemList.size();
                dataMin /= itemList.size();
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_BP_HISTORY));
        }else if (event.getWhat() == EventGlobal.DATA_LOAD_BO_HISTORY) {
            curItem = null;
            days = TimeUtil.getMonthToDay(mCalendar);
            itemList = IbandDB.getInstance().getMonthBo(days);
            if (itemList.size()>0) {
                curItem = itemList.get(0);
                boMin = Double.valueOf(curItem.itemNum);
            }
            double dataSum = 0;
            for (HistoryAdapter.Item item : itemList) {
                double num =Double.valueOf(item.itemNum);
                dataSum += num;
                boMax = boMax > num ? boMax : num;
                boMin = boMin < num ? boMin : num;
            }
            if (itemList.size()!=0) {
                boAvg = dataSum / itemList.size();
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_BO_HISTORY));
        }else if (event.getWhat() == EventGlobal.DATA_LOAD_MICRO_HISTORY) {
            curItem = null;
            days = TimeUtil.getMonthToDay(mCalendar);
            itemList = IbandDB.getInstance().getMonthMicro(days);
            if (itemList.size()>0) {
                curItem = itemList.get(0);
                dataMin1 =Float.valueOf(curItem.itemNum);
            }
            for (HistoryAdapter.Item item : itemList) {
                float num = Float.valueOf(item.itemNum);
                dataAvg1 += num;
                dataMax1 = dataMax1 > num ? dataMax1 : num;
                dataMin1 = dataMin1 < num ? dataMin1 : num;
            }
            if (itemList.size() != 0) {
                dataAvg1 /= itemList.size();
                dataAvg1 = (float) Math.round(dataAvg1 * 1000) / 1000;//保留三位小数
            }
            EventBus.getDefault().post(new EventMessage(EventGlobal.REFRESH_VIEW_MICRO_HISTORY));
        }
    }

    private void initHistoryTitle() {
        switch (historyType) {
            case 4:
                color = Color.parseColor("#3949ab");
                lineColor = Color.parseColor("#26ef5350");
                setTitleBar("微循环记录", color);
                setStatusBarColor(color);
                tvDate.setTextColor(color);
                break;
            case 1:
                color = Color.parseColor("#43a047");
                lineColor = Color.parseColor("#2643a047");
                setTitleBar(getString(R.string.title_bp_history), color);
                setStatusBarColor(color);
                ivIcon.setImageResource(R.mipmap.bloodpressure_ic02);
                tvUnit.setText("mmHg");
                tvDate.setTextColor(color);
                break;
            case 2:
                color = Color.parseColor("#ff4081");
                lineColor = Color.parseColor("#26ff4081");
                setTitleBar(getString(R.string.title_bo_history), color);
                setStatusBarColor(color);
                ivIcon.setImageResource(R.mipmap.bloodoxygen_ic02);
                tvUnit.setText("%");
                tvDate.setTextColor(color);
                break;
        }
    }

    private void initHistoryData(){
        switch (historyType) {
            case 0:
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_HR_HISTORY));
                break;
            case 1:
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_BP_HISTORY));
                break;
            case 2:
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_BO_HISTORY));
                break;

            case 4:
                EventBus.getDefault().post(new EventMessage(EventGlobal.DATA_LOAD_MICRO_HISTORY));
                break;
        }
    }

}
