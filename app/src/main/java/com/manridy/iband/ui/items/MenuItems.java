package com.manridy.iband.ui.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;

/**
 * 菜单选项item
 * Created by jarLiao on 17/5/4.
 */

public class MenuItems extends RelativeLayout {
    private ImageView menuCheck;
    private TextView menuState;

    public MenuItems(Context context) {
        super(context);
    }

    public MenuItems(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View view = View.inflate(context, R.layout.item_menu,this);
        ImageView menuIcon = (ImageView) view.findViewById(R.id.iv_menu_icon);
        TextView menuName = (TextView) view.findViewById(R.id.tv_menu_name);
        menuCheck = (ImageView) view.findViewById(R.id.iv_menu_check);
        menuState = (TextView) view.findViewById(R.id.tv_menu_state);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MenuItems);
        int icon = typedArray.getResourceId(R.styleable.MenuItems_menus_icon,-1);
        String name = typedArray.getString(R.styleable.MenuItems_menus_name);
        menuIcon.setImageResource(icon);
        menuName.setText(name);
        typedArray.recycle();
    }

    public void check(boolean check){
        menuCheck.setImageResource(check ? R.mipmap.appremind_ic_select : R.mipmap.appremind_ic_normal );
    }

    public void check(boolean check,boolean isEnable){
        check(check);
        this.setEnabled(isEnable);
    }

    public void setMenuOpenState(boolean isOpen){
        menuState.setVisibility(VISIBLE);
        menuState.setText(isOpen?getContext().getString(R.string.hint_opens):getContext().getString(R.string.hint_unopen));
        menuState.setTextColor(isOpen? Color.parseColor("#de2196f3"):Color.parseColor("#8a000000"));
    }
}
