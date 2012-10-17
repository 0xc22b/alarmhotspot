package com.wit.alarmhotspot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class AlertActivity extends Activity implements
        DialogInterface.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTheme(android.R.style.Theme_Dialog);
        
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Uh Oh!");
        alert.setMessage("Your device does not support traffic stat monitoring.");
        alert.setPositiveButton(android.R.string.ok, this);
        alert.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        finish();
    }
}
