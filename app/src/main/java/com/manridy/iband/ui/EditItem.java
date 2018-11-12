package com.manridy.iband.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;


/**
 *
 * Created by Administrator on 2016/8/5.
 */
public class EditItem extends RelativeLayout{
    private TextView tvTitle;
    private TextView tvUnit;
    private TextView tvText;
    private EditText etText;
    private TextView tvAlert;

    private View view;


    public EditItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        view = View.inflate(context, R.layout.ui_edit_item,this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditItem);
        String title = typedArray.getString(R.styleable.EditItem_ei_title);
        String etTxt = typedArray.getString(R.styleable.EditItem_ei_et_text);
        String etHint = typedArray.getString(R.styleable.EditItem_ei_et_hint);
        String text = typedArray.getString(R.styleable.EditItem_ei_text);
        String alert = typedArray.getString(R.styleable.EditItem_ei_alert);
        String unit = typedArray.getString(R.styleable.EditItem_ei_unit);
        int color = typedArray.getColor(R.styleable.EditItem_ei_color, Color.BLACK);
        int textColor = typedArray.getColor(R.styleable.EditItem_ei_text_color, Color.BLACK);
        int inputType = typedArray.getInt(R.styleable.EditItem_ei_input_type,0);
        boolean enable = typedArray.getBoolean(R.styleable.EditItem_ei_enable,true);
        int rightColor = typedArray.getColor(R.styleable.EditItem_ei_right_color,Color.BLACK);
        int maxLength = typedArray.getInteger(R.styleable.EditItem_ei_max_length,3);
        boolean isSingleLine = typedArray.getBoolean(R.styleable.EditItem_ei_single_line, true);

        tvTitle = (TextView) view.findViewById(R.id.menu_title);
        tvUnit = (TextView) view.findViewById(R.id.menu_unit);
        etText = (EditText) view.findViewById(R.id.menu_et);
        tvText = (TextView) view.findViewById(R.id.menu_text);
        tvAlert = (TextView) view.findViewById(R.id.menu_alert);

        tvTitle.setTextColor(color);
        tvText.setTextColor(textColor);
        tvUnit.setTextColor(rightColor);
        tvTitle.setText(title);
        tvUnit.setText(unit == null?"":unit);
        etText.setHint(etHint == null?"":etHint);
        etText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        if (inputType == 1) {
            etText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        }
        etText.setEnabled(enable);
        if (etTxt != null) {
            this.etText.setText(etTxt);
        }
        etText.setSingleLine(isSingleLine);
        if (text != null) {
            etText.setVisibility(GONE);
            tvText.setVisibility(VISIBLE);
            tvText.setText(text);
        }
    }

    //菜单点击事件
    public void setItemOnclickListener(OnClickListener clickListener){
       view.setOnClickListener(clickListener);
    }

    //得到文字内容
    public String getContent(){
        return etText.getText().toString();
    }

    //设置文字内容
    public void setContent(String str){
        etText.setText(str);
    }

    public void setTitle(String str){
        tvTitle.setText(str);
    }

    public void setText(String str){
        tvText.setText(str);
    }

    public String getText() {
        return tvText.getText().toString();
    }

    public void setUnit(String unit) {
        this.tvUnit.setText(unit);
    }

    public void  setAlert(String alert){
        tvAlert.setText(alert);
        tvAlert.setVisibility(VISIBLE);
    }

    public TextView getTvUnit(){
        return this.tvUnit;
    }
}
