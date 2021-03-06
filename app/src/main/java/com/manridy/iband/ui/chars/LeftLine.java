package com.manridy.iband.ui.chars;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jarLiao on 17/11/2.
 */

public class LeftLine extends View {
    public static String TAG = "LeftLine";
    private Paint leftLinePaint;

    private int mWidth,mHeight, mTableHeight;//视图宽高
    private float mChangeX;//滑动查看x坐标
    private float mChangedX;//
    private float mStartX;//touchX坐标
    private float mOffsetX_MAX;

    private float mX;//原点
    private float mSpaceX;//两点横坐标间距
    private int mGridDataNum = 24;//每小格数据个数
    private float mGridSpace ;//网格间距
    private int mAcrossNum = 13;//横
    private int mVerticalNum = 20;//纵

    private int mCenter;
    private int mStartColor = Color.parseColor("#004D40");//渐变开始颜色
    private int mEndColor = Color.parseColor("#004D40");//渐变结束颜色
    private List<String> mLabelList;
    private Path mPath;

    boolean isFirstStart = true;

    //一般在直接New一个View时调用
    public LeftLine(Context context) {
        super(context);
        Log.i(TAG+"22","SuperCharts4(Context context)");
        initView();
    }

    //一般在layout文件中使用的时候会调用，关于它的所有属性都会包含在attrs中传递进来
    public LeftLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG+"22","SuperCharts4(Context context, @Nullable AttributeSet attrs)");
        initView();
    }

    //调用了三个参数的构造函数，明确指定第三个参数
    public LeftLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i(TAG+"22","SuperCharts4(Context context, @Nullable AttributeSet attrs, int defStyleAttr)");
        initView();
    }


    //测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG+"22","onMeasure");
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
        Log.i(TAG+"22","onSizeChanged");
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        mWidth = w;//宽度
        mHeight = h;//高度
        mTableHeight = mHeight - dipToPx(24);
        mCenter = Math.min(mWidth,mTableHeight)/2;//中心点
//        for (int i = 0; i <=180; i++) {
//            list.add((float)(Math.random() *400));
//        }
        mLabelList = new ArrayList<>();
        mGridSpace = mWidth/mVerticalNum;
//        for (int i = 0; i < 6; i++) {
//            mLabelList.add(new String("10:00:"+ TimeUtil.zero(i*5)));
//        }
    }

    int i = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            leftLinePaint = new Paint();
            leftLinePaint.setColor(Color.parseColor("#004D40"));                    //设置画笔颜色
//        canvas.drawColor(Color.WHITE);                  //设置背景颜色
            leftLinePaint.setStrokeWidth(20.0f);              //设置线宽
//        canvas.drawLine(50, 50, 450, 50, leftLinePaint);        //绘制直线
            float num = ((float)hrBaseLine/240)*mHeight;
            int centerHeight = mHeight / 2;
                canvas.drawLine(0, (float) centerHeight - num, 0, (float) centerHeight + num, leftLinePaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                mStartX = event.getX();
//                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
//                getParent().requestDisallowInterceptTouchEvent(true);
//                mChangeX = event.getX() - mStartX;
//                invalidate();
                break;
        }
        return true;
    }

    float dataMax = 200f;
    private float getCurrentY(int num) {
//        Log.i(TAG+"22","getCurrentY");
        float y = mTableHeight/2+40 + (mTableHeight/2*((10000-num) / dataMax));
        y = y<0?0:y;
        y = y>mTableHeight ? mTableHeight:y;
//        Log.d(TAG, "getCurrentY() called with: y = [" + y + "]  num = ["+num+"] ");
        return y;
    }


    int hrBaseLine = 20;
    public void setmData(int hrBaseLine) {
        this.hrBaseLine = hrBaseLine;
        invalidate();
    }


    public void setmLabelList(List<String> mLabelList) {
        this.mLabelList = mLabelList;
    }

    //    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        getParent().requestDisallowInterceptTouchEvent(true);
//
//        return super.dispatchTouchEvent(event);
//    }

    boolean isEnd;
    public void setAnim(){
        if (mPath != null) {
            PathAnimator  mPathAnimator = new PathAnimator(mPath);
            mPathAnimator.setDuration(100);//动画时间
//            mPathAnimator.startDelay(1000);
            mPathAnimator.addUpdateListener(new PathAnimator.PathAnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(float pathPrecent, Path path) {
                    mPath = path;//更新当前路径
                    invalidate();
                    isEnd = pathPrecent==1;
                }
            });
            mPathAnimator.start();
        }

    }




    private void initView() {
    }


    /**
     * dip 转换成px
     * @param dip
     * @return
     */
    private int dipToPx(float dip) {
        Log.i(TAG+"22","dipToPx");
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int)(dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    private int sp2px(final float spValue) {
//        Log.i(TAG+"22","sp2px");
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 得到渐变的颜色值
     * @param mStartColor 开始值
     * @param mEndColor 结束值
     * @param radio 百分比
     * @return
     */
    public int getColor(int mStartColor,int mEndColor,float radio) {
        int redStart = Color.red(mStartColor);
        int blueStart = Color.blue(mStartColor);
        int greenStart = Color.green(mStartColor);
        int redEnd = Color.red(mEndColor);
        int blueEnd = Color.blue(mEndColor);
        int greenEnd = Color.green(mEndColor);

        int red = (int) (redStart + ((redEnd - redStart) * radio + 0.5));
        int greed = (int) (greenStart + ((greenEnd - greenStart) * radio + 0.5));
        int blue = (int) (blueStart + ((blueEnd - blueStart) * radio + 0.5));
        return Color.argb(255,red, greed, blue);
    }
}
