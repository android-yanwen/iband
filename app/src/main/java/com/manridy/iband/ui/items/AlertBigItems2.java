package com.manridy.iband.ui.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;

/**
 * 提醒大item
 * Created by jarLiao on 17/5/4.
 */

public class AlertBigItems2 extends RelativeLayout {

    ImageView alertImg;

    public AlertBigItems2(Context context) {
        super(context);
    }

    public AlertBigItems2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.item_alert_big2,this);
        alertImg = (ImageView) view.findViewById(R.id.iv_menu_img);
        TextView alertName = (TextView) view.findViewById(R.id.tv_menu_name);
        TextView alertHint = (TextView) view.findViewById(R.id.tv_menu_hint);
        TextView alertLine = (TextView) view.findViewById(R.id.tv_menu_line);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.AlertBigItems);
//        boolean is = typedArray.getBoolean(R.styleable.AlertBigItems_big_onoff,false);
        boolean showLine = typedArray.getBoolean(R.styleable.AlertBigItems_big_line,false);
        String name = typedArray.getString(R.styleable.AlertBigItems_big_name);
        String hint = typedArray.getString(R.styleable.AlertBigItems_big_hint);
//        alertImg.setImageResource(is ? R.mipmap.ic_on : R.mipmap.ic_off);
        alertImg.setImageResource(R.mipmap.ic_chevron_right);
        alertLine.setVisibility(showLine ? VISIBLE:GONE);
        alertName.setText(name);
        if(alertHint==null||"".equals(alertHint)){
            alertHint.setVisibility(View.GONE);
        }else{
            alertHint.setText(hint);
        }
        typedArray.recycle();
    }
//    public void setAlertCheck(boolean check) {
//        alertImg.setImageResource(check?R.mipmap.ic_on:R.mipmap.ic_off);
//    }

}
