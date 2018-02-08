package com.manridy.iband.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manridy.iband.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 心率/血压/血氧历史适配器
 * Created by jarLiao on 17/5/4.
 */

public class WechatAdapter extends RecyclerView.Adapter<WechatAdapter.MyViewHolder> {


    private List<Item> itemList;

    public WechatAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wechat, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bindData(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class Item {
        public String itemTitle;
        public String itemContent;


        public Item() {
        }

        public Item(String itemTitle, String itemContent) {
            this.itemTitle = itemTitle;
            this.itemContent = itemContent;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_content)
        TextView tvContent;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Item item) {
            tvTitle.setText(item.itemTitle);
            tvContent.setText(item.itemContent);
        }
    }


}
