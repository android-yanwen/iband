package com.manridy.iband.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.common.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 菜单适配器
 * Created by jarLiao on 17/5/4.
 */

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.MyViewHolder> {
    private List<Menu> menuList;

    public HelpAdapter(List<Menu> menuList) {
        this.menuList = menuList;
    }

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help, parent, false);
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
       String menuName;

        public Menu() {
        }

        public Menu(String menuName) {
            this.menuName = menuName;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_menu_name)
        TextView tvMenuName;
        View itemView;
        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this,itemView);
        }

        public void  bindData(Menu menu, final int position){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(position);
                    }
                }
            });
            tvMenuName.setText(menu.menuName);
        }
    }


}
