package com.wit.alarmhotspot;

import java.util.Calendar;

import android.app.AlarmManager;
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
    
    public static class RxTx {
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
        
        public long getAmountToLimit(RxTx startRxTx, long dataLimit) {
            long halfDataLimit = dataLimit / 2;
            long resultRx = startRxTx.rx + halfDataLimit - this.rx;
            long resultTx = startRxTx.tx + halfDataLimit - this.tx;
            return resultRx + resultTx;
        }
    }
    
    public static final String IS_FROM_WIDGET = "isFromWidget";
    public static final String START_DATE = "startDate";
    public static final String START_RX = "startRx";
    public static final String START_TX = "startTx";
    public static final String MARK_DATE = "markDate";
    public static final String MARK_RX = "markRx";
    public static final String MARK_TX = "markTx";
    
    //public static final long TEN_MINS = 600000l;
    public static final long TEN_MINS = 60000l;
    public static final long TWO_MINS = 120000l;
    public static final long SEVEN_MINS = 420000l;
    
    public static final String WAKE_LOCK_TAG = "AlarmHotspotServiceWakeLock";

    static WakeLock wakeLock;
    
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
        
        WifiApManager wifiApManager = WifiApManager.get(getApplicationContext());
        Bundle bundle = intent.getExtras();
        
        if (bundle.getBoolean(IS_FROM_WIDGET)) {
            boolean enabled = !wifiApManager.isWifiApEnabled();
            wifiApManager.setWifiApEnabled(
                    wifiApManager.getWifiApConfiguration(), enabled);

            if (enabled) {
                long now = Calendar.getInstance().getTimeInMillis();
                RxTx rxTx = getRxTx();
                
                Bundle extras = AlarmHotspotService.generateBundle(false,
                        now, rxTx.rx, rxTx.tx,
                        now, rxTx.rx, rxTx.tx);
                // if just started, make it 10 mins
                setAlarm(extras, now + TEN_MINS);
                
                toastMakeText("Hotspot is starting...");
            } else {
                toastMakeText("Hotspot has been turned off.");
            }
        } else {
            long now = Calendar.getInstance().getTimeInMillis();
            long startDate = bundle.getLong(START_DATE);
            RxTx rxTx = getRxTx();
            RxTx startRxTx = new RxTx(bundle.getLong(START_RX),
                    bundle.getLong(START_TX));
                
            if (wifiApManager.isWifiApEnabled()) {
                
                long dataLimit = getDataLimitFromPrefs();
                Bundle extras = AlarmHotspotService.generateBundle(false,
                        startDate, startRxTx.rx, startRxTx.tx,
                        now, rxTx.rx, rxTx.tx);
                
                // check if exceeded data limit
                if (rxTx.didExceed(startRxTx, dataLimit)) {
                    notifyDataExceeded();
                    
                    // if already exceeded, make it 7 mins
                    setAlarm(extras, now + SEVEN_MINS);
                } else {
                    long interval = calculateInterval(
                            bundle.getLong(MARK_DATE),
                            new RxTx(bundle.getLong(MARK_RX), bundle.getLong(MARK_TX)),
                            now,
                            rxTx,
                            rxTx.getAmountToLimit(startRxTx, dataLimit));
                    setAlarm(extras, now + interval);
                }
            } else {
                // Keep log in DB.
                TransferObj transferObj = new TransferObj(startDate, now, 
                        startRxTx.getAmountLesserThan(rxTx));
                AlarmHotspotDb.get(getApplicationContext()).addTransfer(transferObj);
            }
        }
    }

    protected static Bundle generateBundle(boolean isFromWidget, long startDate,
            long startRx, long startTx, long markDate, long markRx,
            long markTx) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FROM_WIDGET, isFromWidget);
        bundle.putLong(START_DATE, startDate);
        bundle.putLong(START_RX, startRx);
        bundle.putLong(START_TX, startTx);
        bundle.putLong(MARK_DATE, markDate);
        bundle.putLong(MARK_RX, markRx);
        bundle.putLong(MARK_TX, markTx);
        return bundle;
    }
    
    private long calculateInterval(long markDate, RxTx markRxTx,
            long now, RxTx rxTx, long amountToLimit) {
        // Calculate the next interval from the current speed.
        // Maximum is 10 mins, minimum is 2 mins.
        long interval = TEN_MINS;
        interval = (now - markDate) / markRxTx.getAmountLesserThan(rxTx)
                * amountToLimit;
        interval += 60000;
        interval = interval > TEN_MINS ? TEN_MINS :
            interval < TWO_MINS ? TWO_MINS : interval;
        return interval;
    }
    
    private long getDataLimitFromPrefs() {
        String dataLimitString =
                AlarmHotspotAppWidgetConfigure.
                loadDataLimitPref(getApplicationContext());
        
        // Let it throws the exception if couldn't be parsed.
        long dataLimit = Long.parseLong(dataLimitString);
        return dataLimit * 1000000;
    }

    private void setAlarm(Bundle extras, long triggerAtMillis) {

        Intent intent = new Intent(this, AlarmHotspotBroadcastReceiver.class);
        intent.putExtras(extras);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }
    
    private RxTx getRxTx() {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        if (rx == TrafficStats.UNSUPPORTED
                || tx == TrafficStats.UNSUPPORTED) {
            // Start an activity to show the alert.
            Bundle extras = new Bundle();
            extras.putString(AlertActivity.TITLE, getResources().getString(R.string.no_traffic_stat));
            extras.putString(AlertActivity.MESSAGE, getResources().getString(R.string.no_traffic_stat_desc));
            
            Intent intent = new Intent(this, AlertActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtras(extras);
            startActivity(intent);
        }
        
        return new RxTx(rx, tx);
    }
    
    private void toastMakeText(final String text) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        AlarmHotspotService.this,
                        text,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @SuppressWarnings("deprecation")
    private void notifyDataExceeded() {
        // Notify the user that data exceeded.
        String title = getResources().getString(R.string.data_limit_exceeded);
        String message = getResources().getString(R.string.data_limit_exceeded_desc);
        
        Bundle extras = new Bundle();
        extras.putString(AlertActivity.TITLE, title);
        extras.putString(AlertActivity.MESSAGE, message);
        
        Intent intent = new Intent(this, AlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        
        Notification notification = new Notification(
                android.R.drawable.stat_notify_error, title,
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, title, message, pendingIntent);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
