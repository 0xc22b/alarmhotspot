package com.wit.alarmhotspot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

public class AlarmHotspotAppWidgetProvider extends AppWidgetProvider {
    
    public static final String TAG = "AlarmHotspotAppWidgetProvider";
    
    private WifiApManager wifiApManager;
    
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        
        // Receive broadcast network state changed to change button's status.
        //IntentFilter wifiStateFilter = new IntentFilter(WifiApManager.WIFI_AP_STATE_CHANGED_ACTION);
        //context.getApplicationContext().registerReceiver(wifiApStateChangedReceiver, wifiStateFilter);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        
        /*try {
            context.getApplicationContext().unregisterReceiver(wifiApStateChangedReceiver);
        } catch (IllegalArgumentException e) {
            // Not registered, do nothing.
        }*/
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds,
                getWifiApManager(context).isWifiApEnabled());
    }
    
    /*private BroadcastReceiver wifiApStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent wifiApIntent) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context); 
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    AlarmHotspotAppWidgetProvider.class));
            
            updateAppWidget(context, appWidgetManager, appWidgetIds, wifiApIntent);
        }
    };*/
    
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds, boolean enabled) {
        
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            
            Intent intent = new Intent(context, AlarmHotspotService.class);
            intent.putExtras(AlarmHotspotService.generateBundle(true, 0l, 0l, 0l));
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            
            if (enabled) {
                views.setImageViewResource(R.id.widget_button, R.drawable.ic_launcher);
            } else {
                views.setImageViewResource(R.id.widget_button, R.drawable.ic_launcher_grey);
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    
    private WifiApManager getWifiApManager(Context context) {
        if (wifiApManager == null) {
            wifiApManager = new WifiApManager(context);
        }
        return wifiApManager;
    }
}
