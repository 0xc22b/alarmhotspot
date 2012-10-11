package com.wit.alarmhotspot;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RemoteViews;


public class AlarmHotspotAppWidgetConfigure extends Activity {
    public static final String TAG = "AlarmHotspotAppWidgetConfigure";
    public static final String PREFS_NAME =
            "com.wit.alarmhotspot.AlarmHotspotAppWidgetProvider";
    public static final String DATA_LIMIT = "dataLimit";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mDataLimitEditText;

    public AlarmHotspotAppWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.appwidget_configure);

        // Find the EditText
        mDataLimitEditText = (EditText)findViewById(R.id.data_limit_edit_text);

        // Bind the action for the save button.
        findViewById(R.id.save_btn).setOnClickListener(mOnSaveBtnClickedListener);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        mDataLimitEditText.setText(loadDataLimitPref(AlarmHotspotAppWidgetConfigure.this, mAppWidgetId));
    }

    private View.OnClickListener mOnSaveBtnClickedListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AlarmHotspotAppWidgetConfigure.this;

            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            String dataLimit = mDataLimitEditText.getText().toString();
            saveDataLimitPref(context, mAppWidgetId, dataLimit);

            // Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_provider);
            appWidgetManager.updateAppWidget(mAppWidgetId, views);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the data limit to the SharedPreferences object for this widget
    private static void saveDataLimitPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(DATA_LIMIT + appWidgetId, text);
        prefs.commit();
    }

    // Read the data limit from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    private static String loadDataLimitPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String dataLimit = prefs.getString(DATA_LIMIT + appWidgetId, null);
        if (dataLimit != null) {
            return dataLimit;
        } else {
            return "0";
        }
    }
}
