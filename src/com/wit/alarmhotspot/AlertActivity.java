package com.wit.alarmhotspot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AlertActivity extends Activity implements
        DialogInterface.OnClickListener,
        DialogInterface.OnCancelListener {
    
    public final static String TITLE = "title";
    public final static String MESSAGE = "message";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTheme(android.R.style.Theme_Dialog);
        
        Bundle extras = getIntent().getExtras();
        assert(extras != null);
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(extras.getString(TITLE));
        alert.setMessage(extras.getString(MESSAGE));
        alert.setPositiveButton(android.R.string.ok, this);
        alert.setNeutralButton(R.string.to_log, this);
        alert.setOnCancelListener(this);
        alert.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        
        Intent intent = new Intent(this, AlarmHotspotService.class);
        intent.putExtras(AlarmHotspotService.generateServiceBundle(
                AlarmHotspotService.FROM_ALERT));
        this.startService(intent);
        
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            Intent toLogIntent = new Intent(this, LogActivity.class);
            toLogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(toLogIntent);
        }

        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
