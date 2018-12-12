package com.manridy.iband.common;

/**
 * 结果回调
 * Created by jarLiao.
 */

public abstract class OnResultCallBack<T> {
    public abstract void onResult(boolean result,T t);
}
