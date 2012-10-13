package com.wit.alarmhotspot.model;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class AlarmHotspotDb {

    // Used for debugging and logging
    public static final String TAG = "AlarmHotspotDb";

    public static final String DATABASE_NAME = "alarm_hotspot.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "transfer";

    public static final String COLUMN_NAME_START_DATE = "_startDate";
    public static final String COLUMN_NAME_END_DATE = "_endDate";
    public static final String COLUMN_NAME_TRANSFER = "_transfer";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        /**
         * 
         * Creates the underlying database with table name and column names
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_NAME_START_DATE + " INTEGER,"
                    + COLUMN_NAME_END_DATE + " INTEGER,"
                    + COLUMN_NAME_TRANSFER + " INTEGER"
                    + ");");
        }

        /**
         * 
         * Demonstrates that the provider must consider what happens when the
         * underlying datastore is changed. In this sample, the database is
         * upgraded the database by destroying the existing data. A real
         * application should upgrade the database in place.
         */
        @Override
        public void
                onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            // Logs that the database is being upgraded
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            // Kills the table and existing data
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            // Recreates the database with a new version
            onCreate(db);
        }

    }

    private DatabaseHelper databaseHelper;

    private AlarmHotspotDb(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }
    
    public ArrayList<TransferObj> fetchTransferListFromDb() {
        ArrayList<TransferObj> transferList = new ArrayList<TransferObj>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String[] columns =
                new String[] { BaseColumns._ID, COLUMN_NAME_START_DATE,
                        COLUMN_NAME_END_DATE, COLUMN_NAME_TRANSFER };
        Cursor cursor =
                db.query(TABLE_NAME, columns, null, null,
                        null, null, COLUMN_NAME_START_DATE);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TransferObj transferObj = new TransferObj(cursor);
            transferList.add(transferObj);
            cursor.moveToNext();
        }
        cursor.close();
        return transferList;
    }

    public long addTransfer(TransferObj transferObj) {

        // Opens the database object in "write" mode.
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new transfer.
        long rowId = db.insert(TABLE_NAME, null, transferObj.getContentValues());

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            return rowId;
        } else {
            // If the insert didn't succeed, then the rowID is <= 0. Throws
            // an exception.
            throw new SQLException("Failed to insert a new transfer.");
        }
    }

    public int editTransfer(TransferObj transferObj) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String where = BaseColumns._ID + " = " + transferObj.id;
        return db.update(TABLE_NAME, transferObj.getContentValues(), where, null);
    }

    public int deleteTransfer(long id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        String where = BaseColumns._ID + " = " + id;
        return db.delete(TABLE_NAME, where, null);
    }
    
    private void closeDb() {
        databaseHelper.close();
    }
    
    private static AlarmHotspotDb instance = null;
    
    public static AlarmHotspotDb get(Context context) {
        if (instance == null) {
            instance = new AlarmHotspotDb(context);
        }
        return instance;
    }
    
    public static void close() {
        if (instance != null) {
            instance.closeDb();
        }
        instance = null;
    }
}
