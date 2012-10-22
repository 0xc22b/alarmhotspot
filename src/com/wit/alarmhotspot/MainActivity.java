package com.wit.alarmhotspot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity implements
        View.OnClickListener {

    private CheckBox hotspotCheckBox;
    private TextView dataLimitTextView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        hotspotCheckBox = (CheckBox) findViewById(R.id.hotspotCheckBox);
        hotspotCheckBox.setOnClickListener(this);
        
        dataLimitTextView = (TextView) findViewById(R.id.data_limit_text_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main_activity, menu);
        return false;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        
        //Setup checkbox
        WifiApManager wifiApManager = WifiApManager.get(this.getApplicationContext());
        WifiApManager.WIFI_AP_STATE state = wifiApManager.getWifiApState();
        if (state.equals(WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)
                || state.equals(WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLING)) {
            hotspotCheckBox.setChecked(true);
        }
        
        // Set data limit
        String dataLimit = AlarmHotspotAppWidgetConfigure.loadDataLimitPref(
                getApplicationContext());
        dataLimit = dataLimit + " MB " + getResources().getString(
                R.string.data_limit_desc);
        dataLimitTextView.setText(dataLimit);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        // Set listener to update checkbox
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiApManager.WIFI_AP_STATE_CHANGED_ACTION);
        this.registerReceiver(wifiApBroadcastReceiver, filter);
    }
    
    @Override
    protected void onPause() {
        this.unregisterReceiver(wifiApBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.hotspotCheckBox:
            Intent intent = new Intent(this, AlarmHotspotService.class);
            intent.putExtras(AlarmHotspotService.generateBundle(true, 0l, 0l,
                    0l, 0l, 0l, 0l));
            this.startService(intent);
            break;
        }
    }

    public void goToConfigure(View view) {
        Intent intent = new Intent(this, AlarmHotspotAppWidgetConfigure.class);
        startActivity(intent);
    }
    
    public void goToLog(View view) {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }
    
    private BroadcastReceiver wifiApBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean enabled = false;
            int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
                    WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_FAILED.ordinal());
            if (state == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED.ordinal()
                    || state == WifiApManager.WIFI_AP_STATE.WIFI_AP_STATE_ENABLING.ordinal()) {
                enabled = true;
            }
            hotspotCheckBox.setChecked(enabled);
        }
    };
}
