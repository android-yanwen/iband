package com.manridy.iband.ui.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;

/**
 * 提醒大item
 * Created by jarLiao on 17/5/4.
 */

public class AlertBigItems3 extends RelativeLayout {

    ImageView alertImg;
    TextView alertCenter;

    private AlertBigItemsOnClick alertImgClick;
    private AlertBigItemsOnClick alertCenterClick;

    public AlertBigItems3(Context context) {
        super(context);
    }

    public AlertBigItems3(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setIvMenuCenterIsView(boolean isView){
        if(isView){
            alertCenter.setVisibility(View.VISIBLE);
        }else{
            alertCenter.setVisibility(View.GONE);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.item_alert_big3,this);
        alertImg = (ImageView) view.findViewById(R.id.iv_menu_img);
        TextView alertName = (TextView) view.findViewById(R.id.tv_menu_name);
        TextView alertHint = (TextView) view.findViewById(R.id.tv_menu_hint);
        TextView alertLine = (TextView) view.findViewById(R.id.tv_menu_line);
        alertCenter = (TextView) view.findViewById(R.id.iv_menu_center);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.AlertBigItems);
        boolean is = typedArray.getBoolean(R.styleable.AlertBigItems_big_onoff,false);
        boolean showLine = typedArray.getBoolean(R.styleable.AlertBigItems_big_line,true);
        String name = typedArray.getString(R.styleable.AlertBigItems_big_name);
        String hint = typedArray.getString(R.styleable.AlertBigItems_big_hint);
        String center = typedArray.getString(R.styleable.AlertBigItems_big_center);
        alertImg.setImageResource(is ? R.mipmap.ic_on : R.mipmap.ic_off);
        alertLine.setVisibility(showLine ? VISIBLE:GONE);
        alertName.setText(name);
        alertHint.setText(hint);

        alertImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(alertImgClick!=null)alertImgClick.onClick();
            }
        });

        if(hint==null||"".equals(hint)){
            alertName.setGravity(Gravity.CENTER_VERTICAL);
            alertHint.setVisibility(GONE);
        }
        if(center==null||"".equals(center)){
            alertCenter.setVisibility(View.GONE);
        }else{
            alertCenter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(alertCenterClick!=null)alertCenterClick.onClick();
                }
            });
        }
        typedArray.recycle();
    }
    public void setAlertCheck(boolean check) {
        alertImg.setImageResource(check?R.mipmap.ic_on:R.mipmap.ic_off);
    }

    public void setAlertCenterContent(String content){
        alertCenter.setText(content);
    }

    public void setAlertCenterClickListen(AlertBigItemsOnClick onClick){
        alertCenterClick = onClick;
    }

    public void setAlertImgClickListen(AlertBigItemsOnClick onClick){
        alertImgClick = onClick;
    }

}
