package com.manridy.applib.callback;

import android.location.Location;

public abstract class LocationCallBack {
    public abstract void onGpsStatus(int satellites);
    public abstract void onLocation(Location location);
}
