package com.wit.alarmhotspot;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class AlarmHotspotAppWidgetConfigure extends Activity {

    public static final String TAG = "AlarmHotspotAppWidgetConfigure";
    public static final String PREFS_NAME =
            "com.wit.alarmhotspot.AlarmHotspotAppWidgetProvider";
    public static final String DATA_LIMIT = "dataLimit";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mDataLimitEditText;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.appwidget_configure);
        mDataLimitEditText = (EditText) findViewById(R.id.data_limit_edit_text);
        
        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set data limit
        String dataLimit = AlarmHotspotAppWidgetConfigure.loadDataLimitPref(
                getApplicationContext());
        mDataLimitEditText.setText(dataLimit);
        
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                mDataLimitEditText.dispatchTouchEvent(MotionEvent.obtain(
                        SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_DOWN , 0, 0, 0));
                mDataLimitEditText.dispatchTouchEvent(MotionEvent.obtain(
                        SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                        MotionEvent.ACTION_UP , 0, 0, 0)); 
            }
        }, 200);
    }
    
    @Override
    public void onStop() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mDataLimitEditText.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        super.onStop();
    }

    public void onSaveBtnClicked(View v) {
        final Context context =
                AlarmHotspotAppWidgetConfigure.this.getApplicationContext();

        // When the button is clicked, save the string in our prefs and return
        // that they
        // clicked OK.
        String dataLimit = mDataLimitEditText.getText().toString();
        saveDataLimitPref(context, dataLimit);

        // Push widget update to surface if the configure changes the widget's
        // display.
        // onUpdate was already called before configure.
        /*
         * if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
         * AppWidgetManager appWidgetManager =
         * AppWidgetManager.getInstance(context); RemoteViews views = new
         * RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
         * appWidgetManager.updateAppWidget(mAppWidgetId, views); }
         */

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    // Read the data limit from the SharedPreferences object.
    // If there is no preference saved, get the default from a resource
    static String loadDataLimitPref(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String dataLimit = prefs.getString(DATA_LIMIT, null);
        if (dataLimit != null) {
            return dataLimit;
        } else {
            return "30";
        }
    }

    // Write the data limit to the SharedPreferences object.
    static void saveDataLimitPref(Context context, String text) {
        SharedPreferences.Editor prefs =
                context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(DATA_LIMIT, text);
        prefs.commit();
    }
}
