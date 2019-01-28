package com.manridy.iband.ui.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.ui.MarqueeTextView;

/**
 * Created by yw on 18/12/19.
 */

public class AlertBigItems4 extends RelativeLayout {

    private static final String TAG = "AlertBigItems4";
    ImageView alerMenuImg;
    TextView alertValue;
    LinearLayout id_ll_value;
    private boolean isOn = false;
    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                switch (v.getId()) {
                    case R.id.id_iv_menu_img:
                        isOn = !isOn;
                        setAlerMenuImgOnOrOff(isOn);
                        alertMenuSwitchOnClickListener.switchOnClick(v, isOn);
                        break;
                    case R.id.id_ll_value:
                        alertMenuValueOnClickListener.valueOnClick(v);
                        break;
                }
            }
            return true;
        }
    };


    public AlertBigItems4(Context context) {
        super(context);
    }

    public AlertBigItems4(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.item_alert_big4,this);
        MarqueeTextView alertName = view.findViewById(R.id.id_tv_menu_name);
        alertValue = view.findViewById(R.id.id_tv_value);
        alertValue.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//设置下划线
//        alertValue.getPaint().setAntiAlias(true);
        TextView alertUnit = view.findViewById(R.id.id_tv_unit);
        alerMenuImg = view.findViewById(R.id.id_iv_menu_img);
        id_ll_value = findViewById(R.id.id_ll_value);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.AlertBigItems);
        boolean is = typedArray.getBoolean(R.styleable.AlertBigItems_big_onoff,false);
        String name = typedArray.getString(R.styleable.AlertBigItems_big_name);
        String value = typedArray.getString(R.styleable.AlertBigItems_big_value);
        String unit = typedArray.getString(R.styleable.AlertBigItems_big_unit);
        this.isOn = is;
        setAlerMenuImgOnOrOff(this.isOn);
        if (name.equals("") || name == null) {
            alertName.setText(getResources().getString(R.string.hint_heart_rate_alarm));
        } else {
            alertName.setText(name);
        }
        if (value.equals("") || name == null) {
            alertName.setText("150");
        }
        else{
            alertValue.setText(value);
        }
        if (unit.equals("") || name == null) {
            alertName.setText("bmp");
        } else {
            alertUnit.setText(unit);
        }
        typedArray.recycle();
    }

    public AlertMenuSwitchOnClickListener alertMenuSwitchOnClickListener;
    public interface AlertMenuSwitchOnClickListener {
        void switchOnClick(View v, boolean isOnOff);
    }
    public void setAlertMenuSwitchOnClickListener(AlertMenuSwitchOnClickListener onClickListener) {
        alerMenuImg.setOnTouchListener(onTouchListener);
        alertMenuSwitchOnClickListener = onClickListener;
    }

    public AlertMenuValueOnClickListener alertMenuValueOnClickListener;
    public interface AlertMenuValueOnClickListener {
        void valueOnClick(View v);
    }
    public void setAlertMenuValueOnClickListener(AlertMenuValueOnClickListener onClickListener) {
        id_ll_value.setOnTouchListener(onTouchListener);
        alertMenuValueOnClickListener = onClickListener;
    }

    public void setAlerMenuImgOnOrOff(boolean isOn) {
        this.isOn = isOn;
        alerMenuImg.setImageResource(isOn ? R.mipmap.ic_on : R.mipmap.ic_off);
    }

    public void setAlertValue(String value) {
        alertValue.setText(value);
    }

}
