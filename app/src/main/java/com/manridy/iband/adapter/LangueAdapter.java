package com.manridy.iband.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.common.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 应用提醒适配器
 * Created by jarLiao on 17/5/4.
 */

public class LangueAdapter extends RecyclerView.Adapter<LangueAdapter.MyViewHolder> {
    private List<Menu> menuList;
    private OnItemClickListener mOnItemClickListener;
    private boolean isEnable = true;

    public void setOnItemClickListener(OnItemClickListener OnItemClickListener) {
        this.mOnItemClickListener = OnItemClickListener;
    }

    public LangueAdapter(List<Menu> menuList) {
        this.menuList = menuList;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public List<Menu> getData() {
        return menuList;
    }

    public void setClickItem(int select){
        for (int i = 0; i < menuList.size(); i++) {
            menuList.get(i).menuCheck = (i == select);
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindData(menuList.get(position),position);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class Menu {
        public int menuId;
        public String menuName;
        public boolean menuCheck;

        public Menu() {
        }

        public Menu(String menuName, boolean menuCheck) {
            this.menuName = menuName;
            this.menuCheck = menuCheck;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_unit_text)
        TextView tvMenuName;
        @BindView(R.id.iv_unit_img)
        ImageView ivMenuCheck;
        @BindView(R.id.rl_unit_view)
        RelativeLayout rlMenuView;
        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        public void  bindData(Menu menu, final int position){
            tvMenuName.setText(menu.menuName);
            ivMenuCheck.setImageResource(menu.menuCheck? R.mipmap.ic_radiobuttonon_color : R.mipmap.ic_radiobuttonoff);
            rlMenuView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }


}
