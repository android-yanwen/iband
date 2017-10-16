package com.manridy.iband.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import static android.service.notification.NotificationListenerService.requestRebind;

public class NotificationCollectorMonitorService extends Service {
    private static final String TAG = NotificationCollectorMonitorService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        ensureCollectorRunning();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    //确认NotificationMonitor是否开启
    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, NotificationService2.class);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null ) {
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                if (service.pid == android.os.Process.myPid() ) {
                    collectorRunning = true;
                }
            }
        }
        if (collectorRunning) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(new ComponentName(this,com.manridy.iband.service.NotificationService2.class));
        }else {
            toggleNotificationListenerService();
        }
    }
    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this,com.manridy.iband.service.NotificationService2.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}