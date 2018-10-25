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
 * 心电历史适配器
 * Created by jarLiao on 17/5/4.
 */

public class EcgHistoryAdapter extends RecyclerView.Adapter<EcgHistoryAdapter.MyViewHolder> {

    private List<Item> itemList;

    public EcgHistoryAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
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
        public String itemName;
        public String itemContent;
        public String itemNum;
        public String itemUnit;

        public Item() {
        }

        public Item(String itemName, String itemContent, String itemNum) {
            this.itemName = itemName;
            this.itemContent = itemContent;
            this.itemNum = itemNum;

        }

        public Item(String itemName, String itemContent, String itemNum, String itemUnit) {
            this.itemName = itemName;
            this.itemContent = itemContent;
            this.itemNum = itemNum;
            this.itemUnit = itemUnit;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_menu_name)
        TextView tvMenuName;
        @BindView(R.id.tv_menu_content)
        TextView tvMenuContent;
        @BindView(R.id.tv_menu_unit)
        TextView tvMenuUnit;
        @BindView(R.id.tv_menu_num)
        TextView tvMenuNum;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Item item) {
            tvMenuName.setText(item.itemName);
            tvMenuContent.setText(item.itemContent);
            tvMenuUnit.setText(item.itemUnit.isEmpty()?tvMenuNum.getContext().getString(R.string.hint_unit_hr):item.itemUnit);
            tvMenuNum.setText(item.itemNum);
        }
    }


}
