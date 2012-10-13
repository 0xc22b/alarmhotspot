package com.wit.alarmhotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class AlarmHotspotBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        
        Intent serviceIntent = new Intent(context, AlarmHotspotService.class);
        serviceIntent.putExtras(intent.getExtras());
        
        context.startService(serviceIntent);
        
        // If your alarm receiver called Context.startService(), it is possible
        // that the phone will sleep before the requested service is launched.
        // To prevent this, your BroadcastReceiver and Service will need to 
        // implement a separate wake lock policy to ensure that the phone 
        // continues running until the service becomes available.
        // http://developer.android.com/reference/android/app/AlarmManager.html
        //     #set(int, long, android.app.PendingIntent)
        if(AlarmHotspotService.wakeLock == null) {
            PowerManager powerManager = (PowerManager)context.getSystemService(
                    Context.POWER_SERVICE);
            AlarmHotspotService.wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK, 
                    AlarmHotspotService.WAKE_LOCK_TAG);
        }
        if(!AlarmHotspotService.wakeLock.isHeld()) {
            AlarmHotspotService.wakeLock.acquire();
        }
    }
}
