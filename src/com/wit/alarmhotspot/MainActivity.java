package com.wit.alarmhotspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }
    
    public void goToConfigure(View view) {
        Intent intent = new Intent(this, AlarmHotspotAppWidgetConfigure.class);
        startActivity(intent);
    }
    
    public void goToLog(View view) {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }
}
