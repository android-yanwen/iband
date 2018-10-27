package com.manridy.iband.ui.chars;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jarLiao on 17/11/2.
 */

public class SuperTable2 extends View {
    public static String TAG = "SuperTable";
    private Paint mPaint;//画笔
    private int mWidth,mHeight;//视图宽高
    private int mAcrossNum = 13;//横
    private int mVerticalNum = 20;//纵
    private Paint mDashPaint;


    //一般在直接New一个View时调用
    public SuperTable2(Context context) {
        super(context);
        initView();
    }

    //一般在layout文件中使用的时候会调用，关于它的所有属性都会包含在attrs中传递进来
    public SuperTable2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    //调用了三个参数的构造函数，明确指定第三个参数
    public SuperTable2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    //测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //widthMeasureSpec\heightMeasureSpec 32二进制，31-30表示模式，29-0表示宽高实际值
        int widthsize = MeasureSpec.getSize(widthMeasureSpec);//取出宽度的确切数值
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);//取出宽度的测量模式

        int heightsize = MeasureSpec.getSize(heightMeasureSpec);//取出高度的确切数值
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);//取出高度的测量模式
//        setMeasuredDimension(widthsize,heightsize);对View的宽高进行修改
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);//宽度，高度，上次宽度，上次高度
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        mWidth = w;//宽度
        mHeight = h;//高度
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i <= mAcrossNum; i++) {//横线
            float inY = mHeight * i / mAcrossNum;
            canvas.drawLine(0,inY,mWidth,inY,mPaint);
        }
        int space = mHeight/mAcrossNum;
        int mVerticalNum = (mWidth/space)+1;
        for (int i = 0; i <= mVerticalNum; i++) {//竖线
            float inX = mWidth * i / mVerticalNum;
            canvas.drawLine(inX,0,inX,mHeight,mPaint);
        }
    }


    PathEffect mPathEffect;
    private void initView() {
        mPathEffect = new DashPathEffect(new float[]{1,5,},1);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#898989"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(1.0f);
        //外部刻度线

        mDashPaint = new Paint();
        mDashPaint.setAntiAlias(true);
        mDashPaint.setColor(Color.parseColor("#898989"));
        mDashPaint.setStyle(Paint.Style.STROKE);
        mDashPaint.setStrokeWidth(1.0f);
    }


    /**
     * dip 转换成px
     * @param dip
     * @return
     */
    private int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int)(dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

}
