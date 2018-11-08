package com.manridy.iband.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manridy.iband.R;
import com.manridy.iband.bean.StepModel;
import com.manridy.sdk.common.TimeUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 训练适配器
 * Created by jarLiao on 17/5/4.
 */

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.MyViewHolder> {
    private List<StepModel> list;

    public TrainAdapter(List<StepModel> list) {
        this.list = list;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public ItemClickListener mListener;

    public void setOnItemClickListener(ItemClickListener listener) {
        this.mListener = listener;
    }

    public void setItemList(List<StepModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_train, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.bindData(list.get(position));
        if (mListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_icon)
        ImageView itemIcon;
        @BindView(R.id.item_time)
        TextView itemTime;
        @BindView(R.id.item_type)
        TextView itemType;
        @BindView(R.id.item_min)
        TextView itemMin;
        @BindView(R.id.item_step)
        TextView itemStep;
        @BindView(R.id.item_ka)
        TextView itemKa;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindData(StepModel step) {
            int itemRes = R.mipmap.train_run;
            int itemText = R.string.hint_run;
            String stepText = step.getStepNum()+(itemMin.getContext().getResources().getString(R.string.hint_unit_step));
            if (step.getSportMode() == 1) {
                itemRes =  R.mipmap.train_ic_bicycle;
                itemText = R.string.hint_cycling;
                stepText = "";
            }else if (step.getSportMode() == 2){
                itemRes =  R.mipmap.train_ic_swim;
                itemText = R.string.hint_swim;
                stepText = "";
            }else if (step.getSportMode() == 3){
                itemRes =  R.mipmap.train_ic_rs;
                itemText = R.string.hint_skip;
                stepText = step.getStepNum() + itemMin.getContext().getResources().getString(R.string.hint_unit_th);
            }else if (step.getSportMode() == 4){
                itemRes =  R.mipmap.train_ic_pu;
                itemText = R.string.hint_push;
                stepText = step.getStepNum() + itemMin.getContext().getResources().getString(R.string.hint_unit_th);
            }else if(step.getSportMode() == 1001){
                itemRes = R.mipmap.train_ic02;
                itemText = R.string.hint_outdoors_run;
                DecimalFormat df = new DecimalFormat("0.00");
                stepText = df.format((float) step.getStepMileage() / 1000)+(itemMin.getContext().getResources().getString(R.string.hint_unit_mi));
//                stepText = step.getStepMileage()+"m";
            }else if(step.getSportMode() == 1002){
                itemRes = R.mipmap.train_runin;
                itemText = R.string.hint_indoors_run;
                stepText = step.getStepNum()+(itemMin.getContext().getResources().getString(R.string.hint_unit_step));
            }else if(step.getSportMode() == 1003){
                itemRes = R.mipmap.train_ic_bicycle;
                itemText = R.string.hint_cycling;
                DecimalFormat df = new DecimalFormat("0.00");
                stepText = df.format((float) step.getStepMileage() / 1000)+(itemMin.getContext().getResources().getString(R.string.hint_unit_mi));
            }
            itemIcon.setImageResource(itemRes);

            Date date = TimeUtil.getDate(step.getStepDate());
            if(date==null){
                date = new Date();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            String start = simpleDateFormat.format(date);
            String end = simpleDateFormat.format((date.getTime()+step.getStepTime()*60*1000));
            itemTime.setText(start+"~"+end);
            itemType.setText(itemText);
            itemMin.setText(step.getStepTime()+(itemMin.getContext().getResources().getString(R.string.unit_min)));
            itemStep.setText(stepText);
            itemKa.setText(step.getStepCalorie()+(itemMin.getContext().getResources().getString(R.string.hint_unit_ka)));
            if(step.getSportMode() == 1001||step.getSportMode() == 1003){
                itemKa.setText(step.getPace()+(itemMin.getContext().getResources().getString(R.string.hint_unit_pace)));
            }else if(step.getSportMode() == 1002){
                itemKa.setText(step.getStepCalorie()+(itemMin.getContext().getResources().getString(R.string.hint_unit_ka)));
            }
        }
    }



}
