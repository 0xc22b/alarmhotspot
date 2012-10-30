package com.wit.alarmhotspot;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

import com.wit.alarmhotspot.model.AlarmHotspotDb;
import com.wit.alarmhotspot.model.TransferObj;

public class LogActivity extends ListActivity {

    private ArrayList<TransferObj> displayList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);
        
        // Create an array adapter for the list view        
        displayList = new ArrayList<TransferObj>();
        setListAdapter(new TransferListAdapter(this, displayList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.log_activity, menu);
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        ArrayList<TransferObj> transferList = AlarmHotspotDb.get(this).fetchTransferListFromDb();
        updateDisplayList(transferList);
    }

    private void updateDisplayList(ArrayList<TransferObj> transferList) {
        assert(transferList != null);
        
        displayList.clear();
        for (TransferObj transferObj : transferList) {
            displayList.add(transferObj);
        }
        ((TransferListAdapter) getListAdapter()).notifyDataSetChanged();
    }
}
