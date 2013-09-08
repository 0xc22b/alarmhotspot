package com.wit.alarmhotspot;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class WifiApBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent wifiApIntent) {

        boolean enabled = false;
        int tmp = wifiApIntent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
                WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_FAILED.ordinal());
        
        // Fix for Android 4
        if (tmp >= 10) {
            tmp = tmp - 10;
        }
        
        WifiApManager.WIFI_AP_STATE state = WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        try {
            state = WifiApManager.WIFI_AP_STATE.class.getEnumConstants()[tmp];
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        
        if (state == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED
                || state == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLING) {
            enabled = true;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context); 
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                AlarmHotspotAppWidgetProvider.class));
        AlarmHotspotAppWidgetProvider.updateAppWidget(context, appWidgetManager, appWidgetIds, enabled);
    }
}
