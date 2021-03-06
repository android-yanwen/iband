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
 * 短信提醒
 * Created by jarLiao on 17/5/4.
 */

public class SmsActivity extends BaseActionActivity {

    @BindView(R.id.tb_menu)
    TextView tbMenu;
    @BindView(R.id.ai_alert)
    AlertBigItems aiAlert;
    boolean onOff;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sms);
        ButterKnife.bind(this);
    }

    @Override
    protected void initVariables() {
        setStatusBarColor(Color.parseColor("#2196f3"));
        setTitleAndMenu(getString(R.string.hint_menu_alert_sms), getString(R.string.hint_save));
        onOff = (boolean) SPUtil.get(mContext, AppGlobal.DATA_ALERT_SMS,false);
        aiAlert.setAlertCheck(onOff);
    }

    @Override
    protected void initListener() {
        aiAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PermissionItem> permissonItems = new ArrayList<PermissionItem>();
                permissonItems.add(new PermissionItem(Manifest.permission.RECEIVE_SMS, getString(R.string.hint_sms), R.mipmap.permission_ic_message));
                permissonItems.add(new PermissionItem(Manifest.permission.READ_CONTACTS, getString(R.string.hint_contacts), R.mipmap.permission_ic_contacts));
                HiPermission.create(mContext)
                        .title(getString(R.string.hint_alert_open))
                        .msg(getString(R.string.hint_request_sms_alert))
                        .style(R.style.PermissionBlueStyle)
                        .permissions(permissonItems)
                        .checkMutiPermission(permissionCallback);
            }
        });
        tbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtil.put(mContext, AppGlobal.DATA_ALERT_SMS,onOff);
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
