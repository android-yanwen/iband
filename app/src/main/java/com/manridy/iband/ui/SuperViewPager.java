package com.manridy.iband.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 继承viewPager
 * 解决和侧滑菜单滑动冲突
 * Created by jarLiao on 17/12/2.
 */

public class SuperViewPager extends ViewPager {
    private static final String TAG = "SuperViewPager";

    public void isEnableSlide(boolean isEnableSlide){
        isEnableSlide = isEnableSlide;
    }

    public SuperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.e(TAG, "onInterceptTouchEvent" );
        return isCanScroll && super.onInterceptTouchEvent(ev);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.e(TAG, "onTouchEvent event = "+ event.getX() );
        return isCanScroll && super.onTouchEvent(event);
    }

    private float xDistance, yDistance, xLast, yLast,mLeft;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                mLeft = ev.getX();
//                Log.d("touch", "ACTION_DOWN xLast =" +xLast);
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                int start = (int) xLast;
//                Log.d("touch", "ACTION_DOWN start =" +start);
                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                if (start<30) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(true);
//                    Log.d(TAG, "requestDisallowInterceptTouchEvent()");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    private boolean isCanScroll = true;
    /**
     * 设置其是否能滑动换页
     * @param isCanScroll false 不能换页， true 可以滑动换页
     */
    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }


}
