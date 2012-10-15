package com.wit.alarmhotspot;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

import com.wit.alarmhotspot.model.AlarmHotspotDb;
import com.wit.alarmhotspot.model.TransferObj;

public class AlarmHotspotService extends IntentService {

    private class RxTx {
        long rx;
        long tx;
        
        public RxTx(long rx, long tx) {
            this.rx = rx;
            this.tx = tx;
        }
        
        public boolean didExceed(RxTx startRxTx, long dataLimit) {
            long halfDataLimit = dataLimit / 2;
            long resultRx = startRxTx.rx + halfDataLimit - this.rx;
            long resultTx = startRxTx.tx + halfDataLimit - this.tx;
            return resultRx + resultTx > 0 ? false : true;
        }
        
        public long getAmountLesserThan(RxTx rxTx) {
            return (rxTx.rx - this.rx) + (rxTx.tx - this.tx);
        }
    }
    
    public static final String IS_FROM_WIDGET = "isFromWidget";
    public static final String START_DATE = "startDate";
    public static final String START_RX = "startRx";
    public static final String START_TX = "startTx";
    
    public static final long INTERVAL = 600000l;
    public static final String WAKE_LOCK_TAG = "AlarmHotspotServiceWakeLock";

    static WakeLock wakeLock;
    private WifiApManager wifiApManager;
    
    private Handler mainThreadHandler = null;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public AlarmHotspotService() {
        super("AlarmHotspotService");
        
        mainThreadHandler = new Handler();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiApManager = new WifiApManager(this);
    }
    
    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            wakeLock = null;
        }
        super.onDestroy();
    }

    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns,
     * IntentService stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle.getBoolean(IS_FROM_WIDGET)) {
            boolean enabled = !wifiApManager.isWifiApEnabled();
            wifiApManager.setWifiApEnabled(
                    wifiApManager.getWifiApConfiguration(), enabled);

            if (enabled) {
                RxTx startRxTx = getRxTx();
                setAlarm(Calendar.getInstance().getTimeInMillis(), startRxTx);
                
                toastMakeText("Hotspot is starting...");
            } else {
                toastMakeText("Hotspot has been turned off.");
            }
        } else {
            
            RxTx rxTx = getRxTx();
            RxTx startRxTx = new RxTx(bundle.getLong(START_RX),
                    bundle.getLong(START_TX));
            
            if (wifiApManager.isWifiApEnabled()) {
                // check if exceeded data limit
                if (rxTx.didExceed(startRxTx, getDataLimitFromPrefs())) {
                    // Notify the user.
                    int icon = android.R.drawable.stat_notify_error;
                    long when = System.currentTimeMillis();
                    CharSequence tickerText = "Data usage exceeded!";
                    CharSequence contentText = "Check what is consuming your data. The system might be automatically downloading newly updates.";
                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    
                    Notification notification = new Notification(icon, tickerText, when);
                    notification.setLatestEventInfo(getApplicationContext(), tickerText, contentText, contentIntent);
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, notification);
                }
            } else {
                // Keep log in DB.
                TransferObj transferObj = new TransferObj(
                        bundle.getLong(START_DATE),
                        Calendar.getInstance().getTimeInMillis(), 
                        startRxTx.getAmountLesserThan(rxTx));
                AlarmHotspotDb.get(getApplicationContext()).addTransfer(transferObj);
                
                // Cancel the alarm.
                cancelAlarm();
            }
        }
    }

    protected static Bundle generateBundle(boolean isFromWidget, long startDate,
            long startRx, long startTx) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FROM_WIDGET, isFromWidget);
        bundle.putLong(START_DATE, startDate);
        bundle.putLong(START_RX, startRx);
        bundle.putLong(START_TX, startTx);
        return bundle;
    }
    
    private long getDataLimitFromPrefs() {
        String dataLimitString =
                AlarmHotspotAppWidgetConfigure.
                loadDataLimitPref(getApplicationContext());
        
        // Let it throws the exception if couldn't be parsed.
        long dataLimit = Long.parseLong(dataLimitString);
        return dataLimit * 1000000;
    }

    private void setAlarm(long startDate, RxTx startRxTx) {

        Intent intent = new Intent(getApplicationContext(),
                AlarmHotspotBroadcastReceiver.class);
        intent.putExtras(AlarmHotspotService.generateBundle(false, startDate,
                startRxTx.rx, startRxTx.tx));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager =
                (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis() + INTERVAL, INTERVAL,
                pendingIntent);
    }
    
    private void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(),
                AlarmHotspotBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)
                getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    
    private RxTx getRxTx() {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        if (rx == TrafficStats.UNSUPPORTED
                || tx == TrafficStats.UNSUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Your device does not support traffic stat monitoring.");
            alert.show();
        }
        return new RxTx(rx, tx);
    }
    
    private void toastMakeText(final String text) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                    text,
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}
