package com.manridy.iband.view.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import com.manridy.applib.utils.FileUtil;
import com.manridy.applib.utils.LogUtil;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.BoModel;
import com.manridy.iband.bean.BpModel;
import com.manridy.iband.bean.HeartModel;
import com.manridy.iband.bean.SleepModel;
import com.manridy.iband.bean.StepModel;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.view.base.BaseActionActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


/**
 * Created by jarLiao on 17/5/4.
 */

public class DataExportActivity extends BaseActionActivity {
    @BindView(R.id.bt_data)
    Button btData;
    @BindView(R.id.tv_look)
    TextView tvLook;
    WritableWorkbook wwb;
    File file;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_data_export);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleBar(getString(R.string.hint_data_export));
        registerEventBus();
        String path = Environment.getExternalStorageDirectory().getPath();
        LogUtil.e(TAG, "内置SD卡路径 = " + path );
        File dir = new File(path, "/iband");
        FileUtil.mkDir(dir.getPath());
        file = new File(dir, "database" + ".xls");
        tvLook.setVisibility(file.exists()?View.VISIBLE:View.GONE);
    }

    @Override
    protected void initListener() {
        btData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileUtil.getAvailableStorage(mContext) > 1024 * 1024) {
                    showProgress(getString(R.string.hint_exporting));
                    try {

                        OutputStream os = new FileOutputStream(file);
                        wwb = Workbook.createWorkbook(os);
                        eventSend(EventGlobal.DATA_EXPORT_EXCEL);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tvLook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFile();
            }
        });
    }

    private void startFile() {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uriSrc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uriSrc = FileProvider.getUriForFile(mContext.getApplicationContext(), "com.manridy.iband.fileprovider",file);
        } else {
            uriSrc = Uri.fromFile(file);
        }
        intent.setDataAndType(uriSrc, "application/vnd.ms-excel");
        startActivity(intent);
    }




    String[] stepTitles = new String[]{"Date", "Step", "Calorie", "Mileage"};
    String[] sleepTitles = new String[]{"Date", "D-Sleep", "L-Sleep", "Sum"};
    String[] hrTitles = new String[]{"Time", "HR"};
    String[] bpTitles = new String[]{"Time", "SBP", "DBP"};
    String[] boTitles = new String[]{"Time", "SpO2"};
    String[] sheets = new String[]{"Step", "Sleep", "HR", "BP", "SpO2"};
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_EXPORT_EXCEL) {
            List<StepModel> stepList = IbandDB.getInstance().getStepSeltionList();
            List<SleepModel> sleepList = IbandDB.getInstance().getSleepList();
            List<HeartModel> hrList = IbandDB.getInstance().getHrList();
            List<BpModel> bpList = IbandDB.getInstance().getBpList();
            List<BoModel> boList = IbandDB.getInstance().getBoList();
            List<List<List<String>>> datas = new ArrayList<>();
            datas.add(getStepList(stepList));
            datas.add(getSleepList(sleepList));
            datas.add(getHrList(hrList));
            datas.add(getBpList(bpList));
            datas.add(getBoList(boList));
            saveData(sheets, loadTitleData(), datas);
            LogUtil.d(TAG, "onBackgroundEvent() called with: saveData ");
            eventSend(EventGlobal.DATA_EXPORT_EXCEL_SUCCES);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.DATA_EXPORT_EXCEL_SUCCES) {
            tvLook.setVisibility(file.exists()?View.VISIBLE:View.GONE);
            dismissProgress();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.hint_data_export_succes);
            builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startFile();
                }
            });
            builder.create().show();
        }
    }

    private List<String[]> loadTitleData() {
        List<String[]> titleList = new ArrayList<>();
        titleList.add(stepTitles);
        titleList.add(sleepTitles);
        titleList.add(hrTitles);
        titleList.add(bpTitles);
        titleList.add(boTitles);
        return titleList;
    }

    private List<List<String>> getStepList(List<StepModel> stepList) {
        List<List<String>> stepStringList = new ArrayList<>();
        StepModel oldStep = new StepModel();
        for (StepModel stepModel : stepList) {
            if (null != oldStep.getStepDay()) {
                if (oldStep.getStepDay().equals(stepModel.getStepDay())) {
                    oldStep.setStepNum(oldStep.getStepNum() + stepModel.getStepNum());
                    oldStep.setStepMileage(oldStep.getStepMileage() + stepModel.getStepMileage());
                    oldStep.setStepCalorie(oldStep.getStepCalorie() + stepModel.getStepCalorie());
                } else {
                    List<String> stringList = new ArrayList<>();
                    stringList.add(oldStep.getStepDay());
                    stringList.add(oldStep.getStepNum() + "");
                    stringList.add(oldStep.getStepCalorie() + "");
                    stringList.add(oldStep.getStepMileage() + "");
                    stepStringList.add(stringList);
                    oldStep = new StepModel();
                }
            } else {
                oldStep.setStepDay(stepModel.getStepDay());
                oldStep.setStepNum(stepModel.getStepNum());
                oldStep.setStepMileage(stepModel.getStepMileage());
                oldStep.setStepCalorie(stepModel.getStepCalorie());
            }
        }
        return stepStringList;
    }

    private List<List<String>> getSleepList(List<SleepModel> sleepList) {
        List<List<String>> sleepStringList = new ArrayList<>();
        SleepModel oldSleep = new SleepModel();
        for (SleepModel sleepModel : sleepList) {
            if (null != oldSleep.getSleepDay()) {
                if (oldSleep.getSleepDay().equals(sleepModel.getSleepDay())) {
                    oldSleep.setSleepDeep(oldSleep.getSleepDeep() + sleepModel.getSleepDeep());
                    oldSleep.setSleepLight(oldSleep.getSleepLight() + sleepModel.getSleepLight());
                } else {
                    List<String> stringList = new ArrayList<>();
                    stringList.add(oldSleep.getSleepDay());
                    stringList.add(oldSleep.getSleepDeep() + "");
                    stringList.add(oldSleep.getSleepLight() + "");
                    stringList.add((oldSleep.getSleepDeep() + oldSleep.getSleepLight()) + "");
                    sleepStringList.add(stringList);
                    oldSleep = new SleepModel();
                }
            } else {
                oldSleep.setSleepDay(sleepModel.getSleepDay());
                oldSleep.setSleepNum(sleepModel.getSleepNum());
                oldSleep.setSleepDeep(sleepModel.getSleepDeep());
                oldSleep.setSleepLight(sleepModel.getSleepLight());
            }
        }
        return sleepStringList;
    }

    private List<List<String>> getHrList(List<HeartModel> hrList) {
        List<List<String>> hrStringList = new ArrayList<>();
        for (HeartModel heartModel : hrList) {
            List<String> stringList = new ArrayList<>();
            stringList.add(heartModel.getHeartDate());
            stringList.add(heartModel.getHeartRate() + "");
            hrStringList.add(stringList);
        }
        return hrStringList;
    }

    private List<List<String>> getBpList(List<BpModel> bpList) {
        List<List<String>> bpStringList = new ArrayList<>();
        for (BpModel bpModel : bpList) {
            List<String> stringList = new ArrayList<>();
            stringList.add(bpModel.getBpDate());
            stringList.add(bpModel.getBpHp() + "");
            stringList.add(bpModel.getBpLp() + "");
            bpStringList.add(stringList);
        }
        return bpStringList;
    }

    private List<List<String>> getBoList(List<BoModel> boList) {
        List<List<String>> boStringList = new ArrayList<>();
        for (BoModel boModel : boList) {
            List<String> stringList = new ArrayList<>();
            stringList.add(boModel.getboDate());
            stringList.add(boModel.getboRate() + "");
            boStringList.add(stringList);
        }
        return boStringList;
    }

    private void saveData(String[] sheetTitle, List<String[]> dataTitle, List<List<List<String>>> datas) {
        for (int i = 0; i < sheets.length; i++) {
            saveSheetData(sheetTitle[i], i, dataTitle.get(i), datas.get(i));
        }
        try {
            wwb.write();
            wwb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void saveSheetData(String sheetName, int sheetId, String[] titles, List<List<String>> datas) {
        try {
            WritableSheet sheet = wwb.createSheet(sheetName, sheetId);
            for (int i = 0; i < titles.length; i++) {
                sheet.addCell(new Label(i, 0, titles[i]));
            }
            for (int i = 0; i < datas.size(); i++) {
                List<String> stringList = datas.get(i);
                for (int i1 = 0; i1 < stringList.size(); i1++) {
                    String str = stringList.get(i1);
                    sheet.addCell(new Label(i1, i + 1, str));
                    LogUtil.d(TAG, "saveSheetData() called with: i = [" + i + "], i1 = [" + i1 + "]");
                }
            }
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
