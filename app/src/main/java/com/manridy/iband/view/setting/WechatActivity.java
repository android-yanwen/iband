package com.manridy.iband.view.setting;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.OnResultCallBack;
import com.manridy.iband.R;
import com.manridy.iband.adapter.WechatAdapter;
import com.manridy.iband.bean.WechatBean;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.common.EventMessage;
import com.manridy.iband.service.HttpService;
import com.manridy.iband.view.base.BaseActionActivity;
import com.tencent.mm.opensdk.modelbiz.JumpToBizProfile;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 遥控拍照页面
 * Created by jarLiao on 17/5/4.
 */

public class WechatActivity extends BaseActionActivity {

    @BindView(R.id.iv_qrcode)
    ImageView ivQrcode;
    @BindView(R.id.iv_qrcode2)
    ImageView ivQrcode2;
    @BindView(R.id.tv_mac)
    TextView tvMac;
    @BindView(R.id.rl_qr)
    RelativeLayout rlQr;
    @BindView(R.id.rv_wechat)
    RecyclerView rvWechat;

    private IWXAPI api;
    private static final String QR_CODE = "http://we.qq.com/d/AQAMj-aODjiAZeqaBa46Npk8sdayEsk2CudEf-P8";
    private String extMsg = "weiqi";
    private String qr = "";
    private String curMac;
    File file;
    List<WechatAdapter.Item> itemList = new ArrayList<>();

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_wechat);
        ButterKnife.bind(this);
    }


    @Override
    protected void initVariables() {
        registerEventBus();
        setStatusBarColor(Color.parseColor("#2196f3"));
        file = new File(galleryPath, "iband_wechat.jpg");
        qr = (String) SPUtil.get(mContext, AppGlobal.DATA_WECHAT_QR, "");
        if (qr != null && !qr.isEmpty()) {
            showQrCode();
        }
        setTitleBar(getString(R.string.hint_menu_wechat));

        WechatAdapter wechatAdapter = new WechatAdapter(getWechatHelpData());
        rvWechat.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.VERTICAL,false));
        rvWechat.setAdapter(wechatAdapter);

    }

    private List<WechatAdapter.Item> getWechatHelpData() {
        List<WechatAdapter.Item> list = new ArrayList<>();
        String[] titles =getResources().getStringArray(R.array.wechat_titles);
        String[] contents =getResources().getStringArray(R.array.wechat_contents);
        for (int i = 0; i < titles.length; i++) {
            list.add(new WechatAdapter.Item(titles[i],contents[i]));
        }
        return list;
    }

    @Override
    protected void initListener() {
//        btMore.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                jumbWechat();
////                jumb2Wechat(qr);
//            }
//        });

        ivQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSuc = setPicToView(createViewBitmap(rlQr), galleryPath, "/iband_wechat.jpg");
                if (isSuc) {
                    showToast(getString(R.string.hint_save_success));
                }
                sync(file, mContext);
            }
        });

        curMac = (String) SPUtil.get(this, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        if (!curMac.isEmpty()) {
            curMac = curMac.replace(":", "");
            eventSend(EventGlobal.ACTION_WECHAT_QUERY);
        }
    }

    private void jumbWechat() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.error_not_install, Toast.LENGTH_LONG).show();
        }
    }

    private void jumb2Wechat(String qr) {
        if (api == null) {
            api = WXAPIFactory.createWXAPI(mContext, "wx8dda48129cdf2ba5", false);//AppID
        }
        if (!api.isWXAppInstalled()) {
            // 提醒用户没有安装微信
            Toast.makeText(mContext, R.string.error_not_install, Toast.LENGTH_SHORT).show();
            return;
        }
        api.registerApp("wx8dda48129cdf2ba5");//AppID
        JumpToBizProfile.Req req = new JumpToBizProfile.Req();
        req.toUserName = "gh_6bc241fa53ba";//公众号原始ID
        req.profileType = JumpToBizProfile.JUMP_TO_HARD_WARE_BIZ_PROFILE;
        req.extMsg = qr;//二维码路径 + 自定义数据
        api.sendReq(req);
    }

    private void showRegistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(R.string.hint_wechart_alert);
        builder.setNegativeButton(R.string.hint_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setPositiveButton(R.string.hint_activate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showProgress(getString(R.string.hint_activateing));
                eventSend(EventGlobal.ACTION_WECHAT_REGIST);
            }
        });
        builder.create().show();
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onBackgroundEvent(EventMessage event) {
        if (event.getWhat() == EventGlobal.ACTION_WECHAT_QUERY) {
            String firmType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
            if (firmType.isEmpty()) {
                return;//没有固件型号信息
            }
            boolean isOld = firmType.equals("0001");
            HttpService.getInstance().wechatQuery(curMac, isOld, new OnResultCallBack() {
                @Override
                public void onResult(boolean result, final Object o) {
                    if (result) {
                        try {
                            JSONObject jsonObject = new JSONObject(o.toString());
                            Log.d(TAG, "onResult() called with: result = [" + result + "], o = [" + o + "]");
                            final int count = jsonObject.getInt("count");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (count == 0) {
//                                        showRegistDialog();
                                        showProgress(getString(R.string.hint_activateing));
                                        eventSend(EventGlobal.ACTION_WECHAT_REGIST);
                                    } else {
                                        WechatBean wechatBean = new Gson().fromJson(o.toString(), WechatBean.class);
                                        if (wechatBean.getResult().size() > 0) {
                                            WechatBean.ResultBean resultBean = wechatBean.getResult().get(0);
                                            qr = resultBean.getDevice_qr();
                                            SPUtil.put(mContext, AppGlobal.DATA_WECHAT_QR, qr);
                                            showQrCode();
                                        }
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgress();
                                showError();
                            }
                        });
                    }

                    Log.d(TAG, "wechatQuery() called with: result = [" + result + "], o = [" + o == null ? "null" : o.toString() + "]");
                }
            });
        } else if (event.getWhat() == EventGlobal.ACTION_WECHAT_REGIST) {
            String firmType = (String) SPUtil.get(mContext, AppGlobal.DATA_FIRMWARE_TYPE, "");
            if (firmType.isEmpty()) {
                return;//没有固件型号信息
            }
            String productId = "35788";
            String deviceName = "N109";
            if (!firmType.equals("0001")) {//固件型号非0001
                productId = "40166";
                deviceName = "MRD-Sports";
            }
            HttpService.getInstance().wechatRegister(productId, deviceName, curMac, new OnResultCallBack() {
                @Override
                public void onResult(final boolean result, final Object o) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                try {
                                    JSONObject jsonObject = new JSONObject(o.toString());
                                    int statusCode = jsonObject.getInt("state");
                                    if (statusCode == 20000) {
                                        dismissProgress();
                                        showToast(getString(R.string.hint_install_success));
                                        qr = jsonObject.getString("device_qr");
                                        SPUtil.put(mContext, AppGlobal.DATA_WECHAT_QR, qr);
                                        showQrCode();
                                    } else {
                                        showError();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showError();
                            }
                        }
                    });
                    Log.d(TAG, "wechatRegister() called with: result = [" + result + "], o = [" + o == null ? "null" : o.toString() + "]");
                }
            });
        }
    }

    private void showQrCode() {
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon);
        Bitmap mBitmap = CodeUtils.createImage(qr, 400, 400, logo);
        ivQrcode.setImageBitmap(mBitmap);
        ivQrcode2.setImageBitmap(mBitmap);
        String mac = (String) SPUtil.get(mContext, AppGlobal.DATA_DEVICE_BIND_MAC, "");
        tvMac.setText("iband(微信运动)\n MAC:" + mac);
    }


    String galleryPath = Environment.getExternalStorageDirectory()
            + File.separator + Environment.DIRECTORY_DCIM
            + File.separator + "Camera" + File.separator;

    public static boolean setPicToView(Bitmap mBitmap, String path, String name) {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
            return false;
        }
        FileOutputStream b = null;
        File file = new File(path);
        file.mkdir();// 创建文件夹
        String fileName = file.getPath() + name;// 图片名字
        try {
            b = new FileOutputStream(fileName);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {// 关闭流
                b.flush();
                b.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Bitmap createViewBitmap(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }


    public void sync(File pictureFile, Context mContext) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(pictureFile);
        intent.setData(uri);
        mContext.sendBroadcast(intent);
    }

    private void showError() {
        showToast(getString(R.string.hint_net_error));
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
