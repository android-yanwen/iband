package com.manridy.iband.view.alert;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.manridy.applib.utils.SPUtil;
import com.manridy.iband.common.AppGlobal;
import com.manridy.iband.common.EventGlobal;
import com.manridy.iband.R;
import com.manridy.iband.ui.items.AlertBigItems;
import com.manridy.iband.view.base.BaseActionActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

/**
 * 来电提醒
 * Created by jarLiao on 17/5/4.
 */

public class PhoneActivity extends BaseActionActivity {

    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.ai_alert)
    AlertBigItems aiAlert;
    boolean onOff;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_phone);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_menu_alert_phone),getString(R.string.hint_save));
        onOff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_PHONE,false);
        aiAlert.setAlertCheck(onOff);
    }

    @Override
    protected void initListener() {
        aiAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PermissionItem> permissonItems = new ArrayList<PermissionItem>();
                permissonItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, getString(R.string.hint_phone), R.mipmap.permission_ic_phone));
                permissonItems.add(new PermissionItem(Manifest.permission.CALL_PHONE, getString(R.string.hint_call), R.mipmap.permission_ic_phone_remind));
                permissonItems.add(new PermissionItem(Manifest.permission.READ_CONTACTS, getString(R.string.hint_contacts), R.mipmap.permission_ic_contacts));
//                permissonItems.add(new PermissionItem(Manifest.permission.WAKE_LOCK,"WAKE_LOCK",R.mipmap.permission_ic_notice));
                HiPermission.create(mContext)
                        .title(getString(R.string.hint_alert_open))
                        .msg(getString(R.string.hint_permisson_phone))
                        .style(R.style.PermissionBlueStyle)
                        .permissions(permissonItems)
                        .checkMutiPermission(permissionCallback);
            }
        });
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.put(mContext, AppGlobal.DATA_ALERT_PHONE,onOff);
                eventSend(EventGlobal.DATA_CHANGE_MENU);
                showToast(getString(R.string.hint_save_success));
                finish();
            }
        });
    }

    PermissionCallback permissionCallback = new PermissionCallback() {
        @Override
        public void onClose() {

        }

        @Override
        public void onFinish() {
            onOff = !onOff;
            aiAlert.setAlertCheck(onOff);
            isChange = true;
        }

        @Override
        public void onDeny(String permisson, int position) {

        }

        @Override
        public void onGuarantee(String permisson, int position) {

        }
    };

}
