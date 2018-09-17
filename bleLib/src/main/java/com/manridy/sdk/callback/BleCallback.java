package com.manridy.sdk.callback;

import android.bluetooth.BluetoothGatt;

import com.manridy.sdk.exception.BleException;


/**
 * 蓝牙回调基类
 * Created by jarLiao on 2016/10/21.
 */

public abstract class BleCallback<T> {
//    protected BluetoothGatt bluetoothGatt;
//
//    protected BleCallback BleCallback(BluetoothGatt bluetoothGatt) {
//        this.bluetoothGatt = bluetoothGatt;
//        return this;
//    }
//
//    protected BluetoothGatt getBluetoothGatt() {
//        return bluetoothGatt;
//    }
//
//    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
//        this.bluetoothGatt = bluetoothGatt;
//    }
//
//    public abstract void onResult(BleException e);

    public abstract void onSuccess(T t);

    public abstract void onFailure(BleException exception);
}
