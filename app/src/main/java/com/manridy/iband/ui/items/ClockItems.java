package com.manridy.iband.ui.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.applib.utils.TimeUtil;
import com.manridy.iband.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 闹钟item
 * Created by jarLiao on 17/5/4.
 */

public class ClockItems extends RelativeLayout {

    private Context mContext;
    private TextView tvTime;
    private TextView tvHint;
    private ImageView ivImg;
    private boolean onOff;
    private CompoundButton.OnCheckedChangeListener listener;

    public ClockItems(Context context) {
        super(context);
        mContext = context;
    }

    public ClockItems(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.item_clock,this);
        tvTime = (TextView) view.findViewById(R.id.tv_clock_time);
        tvHint = (TextView) view.findViewById(R.id.tv_clock_hint);
        ivImg = (ImageView) view.findViewById(R.id.iv_menu_img);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.ClockItems);
        onOff = typedArray.getBoolean(R.styleable.ClockItems_clock_onoff,false);
        String time = typedArray.getString(R.styleable.ClockItems_clock_time);
        setClockTime(time);
        setClockOnOff(onOff);
        ivImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setClockOnOff(!onOff);
                if (listener != null) {
                    listener.onCheckedChanged(null,onOff);
                }
            }
        });
        typedArray.recycle();
    }

    public void setCheckClockListener(final CompoundButton.OnCheckedChangeListener listener){
        this.listener = listener;
    }

    public ClockItems setClockTime(String time){
        tvTime.setText(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        long ms = 0;
        try {
            String newTime = dateFormat.format(new Date());
            Date clockDate = dateFormat.parse(time);
            Date date = dateFormat.parse(newTime);
            if (clockDate.after(date)) {
                ms = clockDate.getTime() - date.getTime();
            }else {
                ms = 24*60*60*1000 - (date.getTime() - clockDate.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Locale curLocale = getResources().getConfiguration().locale;
        if (curLocale.getLanguage().equals(Locale.ENGLISH) || curLocale.toString().equals("en_US")) {
            tvHint.setText(getContext().getString(R.string.hint_time_after)+" "+ formatTime(ms));
        }else {
            tvHint.setText(formatTime(ms)+" "+getContext().getString(R.string.hint_time_after));
        }
        return this;
    }

    public ClockItems setClockOnOff(boolean onOff){
        ivImg.setImageResource(onOff ? R.mipmap.ic_on : R.mipmap.ic_off);
        this.onOff = onOff;
        return this;
    }

    private String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if(day > 0) {
            sb.append(day+"天");
        }
        if(hour > 0) {
            sb.append(hour+mContext.getString(R.string.hint_unit_sleep)+" ");
        }
        if(minute > 0) {
            sb.append(minute+mContext.getString(R.string.unit_min)+" ");
        }
        if(second > 0) {
            sb.append(second+"秒");
        }
        if(milliSecond > 0) {
            sb.append(milliSecond+"毫秒");
        }
        return sb.toString();
    }

    public String getClockTime(){
        return tvTime.getText().toString();
    }

    public boolean isOnOff() {
        return onOff;
    }
}
