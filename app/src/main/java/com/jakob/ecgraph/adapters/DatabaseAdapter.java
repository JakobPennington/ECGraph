/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakob.ecgraph.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jakob.ecgraph.objects.EventRecord;

import java.util.ArrayList;
import java.util.List;


/**
 * An adapter which interacts creates enables database creation and interaction via the
 * DatabaseHelper class.
 *
 * @author  Jakob Pennington
 * @version 1.0
 */
public class DatabaseAdapter {
    private static final String TAG = "DatabaseAdapter";
    private static final String QUERY = "SELECT * FROM " + DatabaseHelper.TABLE_NAME + " ORDER BY "
            + DatabaseHelper.ID + " DESC";
    private DatabaseHelper mDatabaseHelper;

    public DatabaseAdapter(Context context){
        mDatabaseHelper = new DatabaseHelper(context);
    }

    public long insertRecord(EventRecord record){
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.TIME, record.getTime());
        contentValues.put(DatabaseHelper.DATE, record.getDate());
        contentValues.put(DatabaseHelper.DURATION, record.getDuration());
        contentValues.put(DatabaseHelper.FILE_NAME, record.getFileName());
        contentValues.put(DatabaseHelper.LATITUDE, record.getLatitude());
        contentValues.put(DatabaseHelper.LONGITUDE, record.getLongitude());
        return mDatabase.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
    }

    public int deleteRecord(EventRecord mRecord) {
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        String[] whereArgs = new String[]{mRecord.getFileName()};
        return mDatabase.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.FILE_NAME + "=?",
                whereArgs);
    }

    public List<EventRecord> getEventHistory(){
        ArrayList<EventRecord> records = new ArrayList<>();

        // Query the database and iterate through the results, adding each result to the list
        Cursor cursor = getCursor();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            EventRecord eventRecord = new EventRecord(cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getDouble(5),
                    cursor.getDouble(6));
            records.add(eventRecord);
            cursor.moveToNext();
        }

        return records;
    }

    private Cursor getCursor(){
        /* Get a Cursor from the DatabaseHelper, which is a pointer to the beginning of a set of
         * results returned from a database query
         */

        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();
        try {
            Cursor mCursor = mDatabase.rawQuery(DatabaseAdapter.QUERY, null);
            if (mCursor != null){
                mCursor.moveToNext();
            }
            mDatabase.close();
            return mCursor;
        } catch (SQLException mSQLException){
            Log.e("DatabaseAdapter", "getQueryData >> " + mSQLException.toString());
            throw mSQLException;
        }
    }

    /**
     * The DatabaseHelper manages the creation, modification and version control of a database
     * containing events recorded by the application. Other classes interact with the DatabaseHelper
     * via methods in the outer DatabaseAdapter class.
     *
     * @author  Jakob Pennington
     * @version 1.0
     */
    static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "DatabaseHelper";
        private static final String DATABASE_NAME = "ECGDatabase.db";
        private static final int DATABASE_VERSION = 3;
        private static final String TABLE_NAME = "Record";
        private static final String ID = "_id";
        private static final String TIME = "time";
        private static final String DATE = "date";
        private static final String DURATION = "duration";
        private static final String FILE_NAME = "fileName";
        private static final String LATITUDE = "latitude";
        private static final String LONGITUDE = "longitude";
        private static final String CREATE_TABLE_RECORD = "CREATE TABLE " + TABLE_NAME + " (" + ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL, " + TIME + " VARCHAR (255), " +
                DATE + " VARCHAR (255), " + DURATION + " VARCHAR (255), " + FILE_NAME +
                " VARCHAR (255), " + LATITUDE + " DOUBLE, " + LONGITUDE + " DOUBLE);";
        private static final String DROP_TABLE_RECORD = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Create the database by executing the appropriate SQL
            try {
                db.execSQL(CREATE_TABLE_RECORD);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // When the database schema changes, recreate the database
            try {
                db.execSQL(DROP_TABLE_RECORD);
                onCreate(db);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
