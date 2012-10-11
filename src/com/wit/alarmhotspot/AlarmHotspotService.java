package com.wit.alarmhotspot;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmHotspotService extends IntentService {
    
    public static final String IS_FROM_WIDGET = "isFromWidget";
    public static final long INTERVAL = 600000l;
    
    /** 
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public AlarmHotspotService() {
        super("AlarmHotspotService");
    }
    
    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle.getBoolean(IS_FROM_WIDGET)) {
            WifiApManager wifiApManager = new WifiApManager(this);
            boolean enabled = !wifiApManager.isWifiApEnabled();
            wifiApManager.setWifiApEnabled(wifiApManager.getWifiApConfiguration(), enabled);
            
            
            // if on, start counting data usage
            // if exceed limit, fire an alarm
            // if hotspot turns off, keep log in db.
            // if off, do thing.
            if (enabled) {
                setAlarm();
            }
        } else {
            
            
            
            
            
            setAlarm();
        }
    }
    
    private void setAlarm() {
        
        Intent intent = new Intent(this, AlarmHotspotService.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FROM_WIDGET, false);
        
        intent.putExtras(bundle);
        
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pendingIntent);
    }
}
