package com.wit.alarmhotspot;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class AlarmHotspotAppWidgetProvider extends AppWidgetProvider {
    public static final String TAG = "AlarmHotspotAppWidgetProvider";
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            
            Bundle bundle = new Bundle();
            bundle.putBoolean(AlarmHotspotService.IS_FROM_WIDGET, true);
            
            Intent intent = new Intent(context, AlarmHotspotService.class);
            intent.putExtras(bundle);
            
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);

            //TODO Receive broadcast network state changed to change button's status.
            
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
