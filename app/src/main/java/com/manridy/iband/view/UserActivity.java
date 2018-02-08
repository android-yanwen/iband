package com.manridy.iband.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.EditText;

import com.facebook.drawee.view.SimpleDraweeView;
import com.manridy.applib.utils.BitmapUtil;
import com.manridy.applib.utils.CheckUtil;
import com.manridy.applib.utils.SPUtil;
import com.manridy.applib.view.dialog.ListDialog;
import com.manridy.applib.view.dialog.NumDialog;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.UserModel;
import com.manridy.iband.common.Utils;
import com.manridy.iband.ui.items.HelpItems;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.bean.User;
import com.manridy.sdk.callback.BleCallback;
import com.manridy.sdk.exception.BleException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.manridy.iband.common.AppGlobal.DATA_USER_HEAD;

/**
 * 用户信息
 * Created by jarLiao on 17/5/11.
 */

public class UserActivity extends BaseActionActivity {
    /**
     * 请求打开摄像头
     **/
    private final int REQUEST_CODE_CAMERA = 1000;
    /**
     * 请求打开相册
     **/
    private final int REQUEST_CODE_GALLERY = 1001;

    private final int REQUEST_CODE_CROP = 1002;

    @BindView(R.id.iv_user_icon)
    SimpleDraweeView ivUserIcon;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.hi_sex)
    HelpItems hiSex;
    @BindView(R.id.hi_age)
    HelpItems hiAge;
    @BindView(R.id.hi_height)
    HelpItems hiHeight;
    @BindView(R.id.hi_weight)
    HelpItems hiWeight;

    private int mSex = 0;
    private String mAge = "18";
    private String mHeight = "170";
    private String mWeight = "65";
    private Uri uriSrc;
    private File imgFile;
    private UserModel curUser;
    private int unit;

    @Override

    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_user_info);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        unit = (int) SPUtil.get(mContext, AppGlobal.DATA_SETTING_UNIT, 0);
        initUser();
        setTitleBar();
        setTitleAndMenu(getString(R.string.hint_user_info), getString(R.string.hint_save));
    }

    private void initUser() {
        curUser = IbandDB.getInstance().getUser();
        if (curUser == null) {
            curUser = new UserModel();
        } else {
            etName.setText(curUser.getUserName());
            etName.setSelection(curUser.getUserName().length());
            mSex = curUser.getUserSex();
            mAge = curUser.getUserAge();
            mHeight = curUser.getUserHeight();
            mWeight = curUser.getUserWeight();
            hiSex.setMenuContent(mSex == 0 ? getString(R.string.hint_man) : getString(R.string.hint_woman));
            hiAge.setMenuContent(mAge);
        }
        if (unit == 1){
            int height = Integer.parseInt(mHeight);
            int weight = Integer.parseInt(mWeight);
            String in = CheckUtil.cmToIn(height)+"";
            String lb = CheckUtil.kgToLb(weight)+"";
            hiHeight.setMenuUnit("(in)");
            hiWeight.setMenuUnit("(lb)");
            hiHeight.setMenuContent(in);
            hiWeight.setMenuContent(lb);
            mHeight = in;
            mWeight = lb;
        }else{
            hiHeight.setMenuContent(mHeight);
            hiWeight.setMenuContent(mWeight);
        }
        String path = (String) SPUtil.get(mContext, DATA_USER_HEAD, "");
        File file = new File(Environment.getExternalStorageDirectory() + "/iband" + path);
        if (file.exists()) {
            ivUserIcon.setImageURI("file://" + file.getPath());
        }
        imgFile = new File(Environment.getExternalStorageDirectory(), "image.jpg");
    }

    @Override
    protected void initListener() {

    }

    @OnClick({R.id.iv_user_icon, R.id.hi_sex, R.id.hi_age, R.id.hi_height, R.id.hi_weight, R.id.tb_menu})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_user_icon:
                new ListDialog(mContext, getIconData(), getString(R.string.hint_select_pic), new ListDialog.ListDialogListener() {
                    @Override
                    public void onItemClick(ListDialog listDialog, int position) {
                        if (position == 0) {
                            startActivityForResult(BitmapUtil.getPickIntent(), REQUEST_CODE_GALLERY);
                        } else if (position == 1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                uriSrc = FileProvider.getUriForFile(mContext.getApplicationContext(), "com.manridy.iband.fileprovider",
                                        imgFile);
                            } else {
                                uriSrc = Uri.fromFile(imgFile);
                            }
                            startActivityForResult(BitmapUtil.getCameraIntent(uriSrc), REQUEST_CODE_CAMERA);
                        }
                        listDialog.dismiss();

                    }
                }).show();
                break;
            case R.id.hi_sex:
                String str = mSex == 0 ? getString(R.string.hint_man) : getString(R.string.hint_woman);
                new NumDialog(mContext,getSexData(), str, getString(R.string.hint_select_sex), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        mSex = num.equals( getString(R.string.hint_man)) ? 0 : 1;
                        hiSex.setMenuContent(num);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.hi_age:
                new NumDialog(mContext, Utils.getAgeData(), mAge, getString(R.string.hint_select_age), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        mAge = num;
                        hiAge.setMenuContent(mAge);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.hi_height:
                new NumDialog(mContext, Utils.getHeightData(unit), mHeight, getString(R.string.hint_select_height), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        mHeight = num;
                        hiHeight.setMenuContent(mHeight);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.hi_weight:
                new NumDialog(mContext, Utils.getWeightData(unit), mWeight, getString(R.string.hint_select_weight), new NumDialog.NumDialogListener() {
                    @Override
                    public void getNum(String num) {
                        mWeight = num;
                        hiWeight.setMenuContent(mWeight);
                        isChange = true;
                    }
                }).show();
                break;
            case R.id.tb_menu:
                curUser.setUserName(etName.getText().toString());
                curUser.setUserSex(mSex);
                curUser.setUserAge(mAge);
                if (unit == 1) {
                    mHeight = CheckUtil.inToCm(Double.parseDouble(mHeight))+"";
                    mWeight = CheckUtil.lbToKg(Double.parseDouble(mWeight))+"";
                }
                curUser.setUserHeight(mHeight);
                curUser.setUserWeight(mWeight);
                curUser.save();
                eventSend(EventGlobal.DATA_CHANGE_USER);
                ibandApplication.service.watch.setUserInfo(new User(mHeight, mWeight), new BleCallback() {
                    @Override
                    public void onSuccess(Object o) {

                    }

                    @Override
                    public void onFailure(BleException exception) {
//                        SPUtil.put(mContext, AppGlobal.DATA_USER_SEND, false);
                    }
                });
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GALLERY:
                    startActivityForResult(BitmapUtil.startPhotoZoom(data.getData()), REQUEST_CODE_CROP);
                    break;
                case REQUEST_CODE_CAMERA:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        startActivityForResult(BitmapUtil.startPhotoZoom(mContext, imgFile), REQUEST_CODE_CROP);
                    } else {
                        startActivityForResult(BitmapUtil.startPhotoZoom(uriSrc), REQUEST_CODE_CROP);
                    }
                    break;
                case REQUEST_CODE_CROP:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap bitmap = extras.getParcelable("data");
                        Bitmap photo = BitmapUtil.toRoundBitmap(bitmap);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
                        String name = "/IMG_" + simpleDateFormat.format(new Date()) + ".jpg";
                        Utils.setPicToView(bitmap, name);
                        SPUtil.put(mContext,DATA_USER_HEAD, name);
                        eventSend(EventGlobal.DATA_CHANGE_USER);
                        ivUserIcon.setImageBitmap(photo);
                    }
                    break;
            }
        }
    }

    private String[] getIconData(){
        String[] datas = new String[]{getString(R.string.local),getString(R.string.camera),getString(R.string.hint_cancel)};
        return datas;
    }

    private String[] getSexData(){
        String[] datas = new String[]{getString(R.string.hint_man),getString(R.string.hint_woman)};
        return datas;
    }
}
