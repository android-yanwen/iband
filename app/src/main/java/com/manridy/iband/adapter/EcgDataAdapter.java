package com.manridy.iband.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.manridy.iband.R;
import com.manridy.iband.ui.chars.SuperCharts4;

import java.util.ArrayList;
import java.util.List;

/**
 * 心电回放适配器
 * Created by jarLiao on 17/5/4.
 */

public class EcgDataAdapter extends RecyclerView.Adapter<EcgDataAdapter.MyViewHolder> {
//    // RecyclerView 的第一个item，肯定是展示StickyLayout的.
//    public static final int FIRST_STICKY_VIEW = 1;
//    // RecyclerView 除了第一个item以外，要展示StickyLayout的.
//    public static final int HAS_STICKY_VIEW = 2;
//    // RecyclerView 的不展示StickyLayout的item.
//    public static final int NONE_STICKY_VIEW = 3;
//    //  RecyclerView 的展示左右两个数据的item.
//    public static final int HAS_STICKY_VIEW_TWO_DATA = 4;


    private List<Item> itemList;

    public EcgDataAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ecg_view, parent, false);
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
        public ArrayList<Integer> mData;
        public int hrBaseLine;

                public Item() {
        }

        public Item(ArrayList<Integer> mData,int hrBaseLine) {
            this.mData = mData;
            this.hrBaseLine = hrBaseLine;
        }

//        public String itemDay;
//        public int itemIcon;
//        public String itemTime;
//        public String itemData;
//        public String itemDataRight;
//        public boolean itemArrows;
//        public int itemType;
//
//        public Item() {
//        }
//
//        public Item(String itemDay) {
//            this.itemDay = itemDay;
//            itemType = HAS_STICKY_VIEW;
//        }
//
//        public Item(String itemDay,int itemIcon, String itemTime, String itemData, boolean itemArrows) {
//            this.itemDay = itemDay;
//            this.itemIcon = itemIcon;
//            this.itemTime = itemTime;
//            this.itemData = itemData;
//            this.itemArrows = itemArrows;
//            itemType = NONE_STICKY_VIEW;
//        }
//
//        public Item(String itemDay,int itemIcon, String itemTime, String itemData,String itemDataRight, boolean itemArrows) {
//            this.itemDay = itemDay;
//            this.itemIcon = itemIcon;
//            this.itemTime = itemTime;
//            this.itemData = itemData;
//            this.itemArrows = itemArrows;
//            this.itemDataRight = itemDataRight;
//            itemType = HAS_STICKY_VIEW_TWO_DATA;
//        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
//          @BindView(R.id.chart_ecg_item)
            SuperCharts4 superCharts4;
//          @BindView(R.id.tv_test_update)
            TextView tv_test_update;
//        @BindView(R.id.tv_day)
//        TextView tvDay;
//        @BindView(R.id.rl_day)
//        RelativeLayout rlDay;
//        @BindView(R.id.tv_time)
//        TextView tvTime;
//        @BindView(R.id.iv_icon)
//        ImageView ivIcon;
//        @BindView(R.id.tv_data)
//        TextView tvData;
//        @BindView(R.id.iv_arrows)
//        ImageView ivArrows;
//        @BindView(R.id.rl_time)
//        RelativeLayout rlTime;
//        @BindView(R.id.tv_data_right)
//        TextView tvDataRight;
        View itemView;
//        String lastDay;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
//            ButterKnife.bind(this, itemView);
            superCharts4 = (SuperCharts4)itemView.findViewById(R.id.chart_ecg_item);
            tv_test_update = (TextView)itemView.findViewById(R.id.tv_test_update);
        }

        public void bindData(Item item) {
            Log.i("EcgDataAdapter",item.mData.toString());
            superCharts4.setmData(item.mData,item.hrBaseLine);
            tv_test_update.setText(""+item.mData.size());
//            if (item.itemType == HAS_STICKY_VIEW) {
//                rlDay.setVisibility(View.VISIBLE);
//                rlTime.setVisibility(View.GONE);
//                tvDay.setText(item.itemDay);
//                itemView.setTag(HAS_STICKY_VIEW);
//            }else if(item.itemType == HAS_STICKY_VIEW_TWO_DATA){
//                rlDay.setVisibility(View.GONE);
//                rlTime.setVisibility(View.VISIBLE);
//                ivIcon.setImageResource(item.itemIcon);
//                tvTime.setText(item.itemTime);
//                tvData.setText(item.itemData);
//                tvDataRight.setVisibility(View.VISIBLE);
//                tvDataRight.setText(item.itemDataRight);
//                ivArrows.setVisibility(item.itemArrows ? View.VISIBLE :View.GONE);
//                itemView.setTag(NONE_STICKY_VIEW);
//            }
//            else {
//                rlDay.setVisibility(View.GONE);
//                rlTime.setVisibility(View.VISIBLE);
//                ivIcon.setImageResource(item.itemIcon);
//                tvTime.setText(item.itemTime);
//                tvData.setText(item.itemData);
//                ivArrows.setVisibility(item.itemArrows ? View.VISIBLE :View.GONE);
//                itemView.setTag(NONE_STICKY_VIEW);
//            }
//            itemView.setContentDescription(item.itemDay);

        }
    }


}
