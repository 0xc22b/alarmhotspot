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
    }
    
    public static final String FROM = "from";
    
    public static final int FROM_WIDGET = 0;
    public static final int FROM_ALERT = 1;
    public static final int FROM_ALARM = 2;
    
    public static final long TEN_MINS = 600000l;
    public static final long TWO_MINS = 120000l;
    
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
        int from = bundle.getInt(FROM);
        
        if (from == FROM_WIDGET) {
            if (!wifiApManager.isWifiApEnabled()) {
                
                toastMakeText("Hotspot is starting...");
                
                wifiApManager.setWifiApEnabled(
                    wifiApManager.getWifiApConfiguration(), true);
                
                // Add to DB
                RxTx rxTx = getRxTx(true);
                long now = Calendar.getInstance().getTimeInMillis();
                TransferObj transferObj = new TransferObj(now, rxTx.rx, rxTx.tx,
                        now, rxTx.rx, rxTx.tx, false);
                AlarmHotspotDb.get(getApplicationContext()).addTransfer(transferObj);
                
                // if just started, make it 10 mins
                Bundle extras = AlarmHotspotService.generateServiceBundle(FROM_ALARM);
                setAlarm(extras, now + TEN_MINS);
            } else {
                toastMakeText("Hotspot has been turned off.");
                
                // RxTx will be changed when turn off hotspot.
                // Update the log before.
                TransferObj transferObj = AlarmHotspotDb.get(
                        getApplicationContext()).getLatestTransfer();
                RxTx rxTx = getRxTx(false);
                long now = Calendar.getInstance().getTimeInMillis();
                assert(rxTx.rx >= transferObj.endRx
                        && rxTx.tx >= transferObj.endTx);
                transferObj.endDate = now;
                transferObj.endRx = rxTx.rx;
                transferObj.endTx = rxTx.tx;
                AlarmHotspotDb.get(getApplicationContext()).editTransfer(transferObj);
                
                wifiApManager.setWifiApEnabled(
                    wifiApManager.getWifiApConfiguration(), false);
            }
        } else if(from == FROM_ALARM) {
            if (wifiApManager.isWifiApEnabled()) {

                TransferObj transferObj = AlarmHotspotDb.get(
                        getApplicationContext()).getLatestTransfer();
                RxTx rxTx = getRxTx(false);
                long dataLimit = getDataLimitFromPrefs();
                
                long now = Calendar.getInstance().getTimeInMillis();
                Bundle extras = AlarmHotspotService.generateServiceBundle(FROM_ALARM);
                
                // check if exceeded data limit
                if (TransferObj.didExceed(transferObj.startRx, transferObj.startTx,
                        rxTx.rx, rxTx.tx, dataLimit)) {
                    
                    if (!transferObj.didUserKnow) {
                        notifyDataExceeded();
                    }
                    
                    // if exceeded, make it 10 mins
                    setAlarm(extras, now + TEN_MINS);
                } else {
                    long interval = calculateInterval(
                            transferObj.endDate,
                            transferObj.endRx,
                            transferObj.endTx,
                            now,
                            rxTx.rx,
                            rxTx.tx,
                            TransferObj.getAmountToLimit(transferObj.startRx,
                                    transferObj.startTx, transferObj.endRx,
                                    transferObj.endTx, dataLimit));
                    setAlarm(extras, now + interval);
                }
                
                // Update to DB
                assert(rxTx.rx >= transferObj.endRx
                        && rxTx.tx >= transferObj.endTx);
                transferObj.endDate = now;
                transferObj.endRx = rxTx.rx;
                transferObj.endTx = rxTx.tx;
                AlarmHotspotDb.get(getApplicationContext()).editTransfer(transferObj);
            }
        } else if (from == FROM_ALERT) {
            // User already knew, stop notifying!
            TransferObj transferObj = AlarmHotspotDb.get(
                    getApplicationContext()).getLatestTransfer();
            transferObj.didUserKnow = true;
            AlarmHotspotDb.get(getApplicationContext()).editTransfer(transferObj);
        } else {
            throw new AssertionError(from);
        }
    }

    protected static Bundle generateServiceBundle(int from) {
        Bundle bundle = new Bundle();
        bundle.putInt(FROM, from);
        return bundle;
    }
    
    protected static Bundle generateAlertBundle(String title, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(AlertActivity.TITLE, title);
        bundle.putString(AlertActivity.MESSAGE, message);
        return bundle;
    }
    
    private long calculateInterval(long markDate, long markRx, long markTx,
            long now, long rx, long tx, long amountToLimit) {
        // Calculate the next interval from the current speed.
        // Maximum is 10 mins, minimum is 2 mins.
        long interval = TEN_MINS;
        interval = (now - markDate) / TransferObj.getAmountTransferred(markRx,
                markTx, rx , tx) * amountToLimit;
        interval += 30000;
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
    
    private RxTx getRxTx(boolean isStarting) {
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        if (rx == TrafficStats.UNSUPPORTED
                || tx == TrafficStats.UNSUPPORTED) {
            // TODO: Alert just once?
            if (isStarting) {
                // Start an activity to show the alert.
                Bundle extras = generateAlertBundle(
                        getResources().getString(R.string.no_traffic_stat),
                        getResources().getString(R.string.no_traffic_stat));

                Intent intent = new Intent(this, AlertActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent.putExtras(extras);
                startActivity(intent);
            }
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
        
        Bundle extras = generateAlertBundle(title, message);
        
        Intent intent = new Intent(this, AlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        
        Notification notification = new Notification(
                android.R.drawable.stat_notify_error, title,
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, title, message, pendingIntent);
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
