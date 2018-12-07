package com.manridy.iband.ui.chars;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.manridy.iband.R;

public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;     //分割线Drawable
    private int mDividerHeight;  //分割线高度

//    /**
//     * 使用line_divider中定义好的颜色
//     *
//     * @param context
//     * @param dividerHeight 分割线高度
//     */
//    public SimpleDividerItemDecoration(Context context, int dividerHeight) {
//        mDivider = ContextCompat.getDrawable(context, R.drawable.ba_divider);
//        mDividerHeight = dividerHeight;
//    }
//
//    /**
//     * @param context
//     * @param divider       分割线Drawable
//     * @param dividerHeight 分割线高度
//     */
//    public SimpleDividerItemDecoration(Context context, Drawable divider, int dividerHeight) {
//        if (divider == null) {
//            mDivider = ContextCompat.getDrawable(context, R.drawable.ba_divider);
//        } else {
//            mDivider = divider;
//        }
//        mDividerHeight = dividerHeight;
//    }

    //获取分割线尺寸
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);

//        int childAdapterPosition = parent.getChildAdapterPosition(view);
//
//        int lastCount = parent.getAdapter().getItemCount() - 1;
//
//        if (childAdapterPosition == 0) {
//            outRect.set(0, 0, 0, 0);
//            return;
//        }
//
//        if (childAdapterPosition == lastCount) {
//            outRect.set(0, 0, 0, 0);
//            return;
//        }
        outRect.set(0, 0, 0, 0);

    }
}