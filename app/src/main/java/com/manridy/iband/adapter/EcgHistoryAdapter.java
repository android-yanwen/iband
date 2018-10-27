package com.manridy.iband.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manridy.iband.IbandApplication;
import com.manridy.iband.R;
import com.manridy.iband.bean.EcgDataBean;
import com.manridy.iband.ui.chars.SuperCharts2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 心电历史适配器
 * Created by jarLiao on 17/5/4.
 */

public class EcgHistoryAdapter extends RecyclerView.Adapter<EcgHistoryAdapter.MyViewHolder> {

    private List<Item> itemList;

    public interface OnItemClickLitener {
        void onItemClick(String ecgDataId);
    }

    private EcgHistoryAdapter.OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(EcgHistoryAdapter.OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public EcgHistoryAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }



    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ecg_history, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.bindData(itemList.get(position));
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickLitener!=null){
                    mOnItemClickLitener.onItemClick(itemList.get(position).getItemEcgDataId());
                }
            }
        });

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

        public String getItemEcgDataId() {
            return itemEcgDataId;
        }

        public void setItemEcgDataId(String itemEcgDataId) {
            this.itemEcgDataId = itemEcgDataId;
        }

        public String itemEcgDataId;


        public List<EcgDataBean> getEcgDataBeanList() {
            return ecgDataBeanList;
        }

        public void setEcgDataBeanList(List<EcgDataBean> ecgDataBeanList) {
            this.ecgDataBeanList = ecgDataBeanList;
        }

        public List<EcgDataBean> ecgDataBeanList;

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
//        @BindView(R.id.tv_menu_content)
//        TextView tvMenuContent;
//        @BindView(R.id.tv_menu_unit)
//        TextView tvMenuUnit;
        @BindView(R.id.tv_menu_num)
        TextView tvMenuNum;
        @BindView(R.id.chart_ecg)
        SuperCharts2 chartEcg;
        @BindView(R.id.rl_item_ecg_history)
        RelativeLayout root;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(Item item) {
            tvMenuName.setText(item.itemName);
//            tvMenuContent.setText(item.itemContent);
//            tvMenuUnit.setText(item.itemUnit.isEmpty()?tvMenuNum.getContext().getString(R.string.hint_unit_hr):item.itemUnit);
//            tvMenuNum.setText(item.itemNum);
            EcgDataBean ecgDataBean;
            String str_ecg;
            List<String> list_str;


            List<EcgDataBean> ecgDataBeanList;
            ecgDataBeanList = item.getEcgDataBeanList();
            int hrBaseLine = 0;
            List<EcgHistoryAdapter.Item> curItemList2 = new ArrayList<>();
            List<Integer> all = new LinkedList<>();
            for(int i=0;i<ecgDataBeanList.size();i++){
                ecgDataBean = ecgDataBeanList.get(i);
                hrBaseLine = ecgDataBean.getRate_aided_signal();
                str_ecg = ecgDataBean.getEcg();
                list_str = java.util.Arrays.asList(str_ecg.split(","));
                List<Integer> list = new LinkedList<>();
                for(String ecg : list_str){
                    list.add(Integer.valueOf(ecg));
                }
                all.addAll(list);

//                        curItemList2.add(new EcgDataAdapter.Item(list,hrBaseLine));
            }

            int k = 0;
            ArrayList<Integer> ecgs = new ArrayList<>();
            for(int j = 0;j<all.size();j++){
                if(k<480){
                    ecgs.add(all.get(j));
                    k++;
                }else{
                    chartEcg.setmData(ecgs,hrBaseLine);
                    ecgs = new ArrayList<>();
                    k=0;
                }
            }
        }
    }


}
