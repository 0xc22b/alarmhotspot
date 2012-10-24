package com.wit.alarmhotspot.model;

import java.text.SimpleDateFormat;
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
    public long startRx;
    public long startTx;
    public long endDate;
    public long endRx;
    public long endTx;
    
    public TransferObj(long startDate, long startRx, long startTx, long endDate,
            long endRx, long endTx) {
        this.startDate = startDate;
        this.startRx = startRx;
        this.startTx = startTx;
        this.endDate = endDate;
        this.endRx = endRx;
        this.endTx = endTx;
    }
    
    public TransferObj(Cursor cursor) {
        this.id = cursor.getLong(0);
        this.startDate = cursor.getLong(1);
        this.startRx = cursor.getLong(2);
        this.startTx = cursor.getLong(3);
        this.endDate = cursor.getLong(4);
        this.endRx = cursor.getLong(5);
        this.endTx = cursor.getLong(6);
    }
    
    private TransferObj(Parcel in) {
        this.id = in.readLong();
        this.startDate = in.readLong();
        this.startRx = in.readLong();
        this.startTx = in.readLong();
        this.endDate = in.readLong();
        this.endRx = in.readLong();
        this.endTx = in.readLong();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(startDate);
        dest.writeLong(startRx);
        dest.writeLong(startTx);
        dest.writeLong(endDate);
        dest.writeLong(endRx);
        dest.writeLong(endTx);
    }
    
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(AlarmHotspotDb.COLUMN_NAME_START_DATE, startDate);
        values.put(AlarmHotspotDb.COLUMN_NAME_START_RX, startRx);
        values.put(AlarmHotspotDb.COLUMN_NAME_START_TX, startTx);
        values.put(AlarmHotspotDb.COLUMN_NAME_END_DATE, endDate);
        values.put(AlarmHotspotDb.COLUMN_NAME_END_RX, endRx);
        values.put(AlarmHotspotDb.COLUMN_NAME_END_TX, endTx);
        return values;
    }
    
    public String getStartDateString(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(startDate));
    }
    
    public String getEndDateString(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(new Date(endDate));
    }
    
    public String getTransferString() {
        double transferInMB = (double) getAmountTransferred(startRx, startTx,
                endRx, endTx) / 1000000;
        return String.format("%.1f", transferInMB) + " MB";
    }
    
    public static boolean didExceed(long startRx, long startTx,
            long endRx, long endTx, long dataLimit) {
        return getAmountToLimit(startRx, startTx, endRx, endTx, dataLimit) > 0
                ? false : true;
    }
    
    public static long getAmountToLimit(long startRx, long startTx,
            long endRx, long endTx, long dataLimit) {
        long halfDataLimit = dataLimit / 2;
        long resultRx = startRx + halfDataLimit - endRx;
        long resultTx = startTx + halfDataLimit - endTx;
        return resultRx + resultTx;
    }
    
    public static long getAmountTransferred(long startRx, long startTx,
            long endRx, long endTx) {
        return (endRx - startRx) + (endTx - startTx);
    }
}
