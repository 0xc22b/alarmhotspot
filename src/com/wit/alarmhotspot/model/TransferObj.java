package com.wit.alarmhotspot.model;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class TransferObj implements Parcelable {

    public static final Parcelable.Creator<TransferObj> CREATOR = new Parcelable.Creator<TransferObj>() {
        public TransferObj createFromParcel(Parcel in) {
            return new TransferObj(in);
        }

        public TransferObj[] newArray(int size) {
            return new TransferObj[size];
        }
    };
    
    public long id;
    public long startDate;
    public long endDate;
    public long transfer;
    
    public TransferObj(long startDate, long endDate, long transfer) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.transfer = transfer;
    }
    
    public TransferObj(Cursor cursor) {
        this.id = cursor.getLong(0);
        this.startDate = cursor.getLong(1);
        this.endDate = cursor.getLong(2);
        this.transfer = cursor.getLong(3);
    }
    
    private TransferObj(Parcel in) {
        this.id = in.readLong();
        this.startDate = in.readLong();
        this.endDate = in.readLong();
        this.transfer = in.readLong();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeLong(transfer);
    }
    
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(AlarmHotspotDb.COLUMN_NAME_START_DATE, startDate);
        values.put(AlarmHotspotDb.COLUMN_NAME_END_DATE, endDate);
        values.put(AlarmHotspotDb.COLUMN_NAME_TRANSFER, transfer);
        return values;
    }
    
    public String getStartDateString() {
        return new Date(startDate).toString();
    }
    
    public String getEndDateString() {
        return new Date(endDate).toString();
    }
    
    public String getTransferString() {
        int transferInMB = (int) (transfer / 1000000);
        return transferInMB + " MB";
    }
}
