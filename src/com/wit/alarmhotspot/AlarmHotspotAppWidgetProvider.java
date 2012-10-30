package com.wit.alarmhotspot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AlarmHotspotAppWidgetProvider extends AppWidgetProvider {
    
    public static final String TAG = "AlarmHotspotAppWidgetProvider";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        updateAppWidget(context, appWidgetManager, appWidgetIds,
                WifiApManager.get(context.getApplicationContext())
                .isWifiApEnabled());
    }
    
    protected static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds, boolean enabled) {
        
        final int N = appWidgetIds.length;
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            
            Intent intent = new Intent(context, AlarmHotspotService.class);
            intent.putExtras(AlarmHotspotService.generateServiceBundle(
                    AlarmHotspotService.FROM_WIDGET));
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
}
