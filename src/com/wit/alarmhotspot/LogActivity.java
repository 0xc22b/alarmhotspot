package com.wit.alarmhotspot;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

import com.wit.alarmhotspot.model.AlarmHotspotDb;
import com.wit.alarmhotspot.model.TransferObj;

public class LogActivity extends ListActivity {

    public static final String TRANSFER_LIST = "transferList";
    
    private ArrayList<TransferObj> transferList;
    private ArrayList<TransferObj> displayList;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_activity);
        
        if (savedInstanceState != null) {
            transferList = savedInstanceState.getParcelableArrayList(TRANSFER_LIST);
        }
        
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
        
        if (transferList == null) {
            transferList = AlarmHotspotDb.get(this).fetchTransferListFromDb();
        }
        
        updateDisplayList();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(TRANSFER_LIST, transferList);
        
        super.onSaveInstanceState(outState);
    }

    private void updateDisplayList() {
        assert(transferList != null);
        
        displayList.clear();
        for (TransferObj transferObj : transferList) {
            displayList.add(transferObj);
        }
        ((TransferListAdapter) getListAdapter()).notifyDataSetChanged();
    }
}
