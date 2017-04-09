package com.scheidegger.jegplayer.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.scheidegger.jegplayer.model.JegMusic;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    private final String TAG = DBHandler.class.getSimpleName();

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "musicInfo";

    // Music table name
    private static final String TABLE_MUSIC = "music";

    // Music Table Columns names
    private static final String FLD_ID = "id";
    private static final String FLD_NAME = "name";
    private static final String FLD_FILE_NAME = "fileName";
    private static final String FLD_COUNTRY = "country";
    private static final String FLD_LENGTH = "length";
    private static final String FLD_DESCRIPTION = "description";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "in Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "in On-Create");
        String CREATE_MUSIC_TABLE = "CREATE TABLE " + TABLE_MUSIC + "("
                + FLD_ID + " INTEGER PRIMARY KEY," + FLD_NAME + " TEXT,"
                + FLD_FILE_NAME + " TEXT," + FLD_COUNTRY + " TEXT,"
                + FLD_LENGTH + " INTEGER," + FLD_DESCRIPTION + " TEXT)";
        db.execSQL(CREATE_MUSIC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "in On-Upgrade");
        Log.d(TAG, "Updating from [" + oldVersion + "] to version [" + newVersion + "]");
        // This should be the first version of the DB
        // Any older implementation is a bug - drop and recreate

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC);
        // Creating tables again
        onCreate(db);
    }

    // Adding new music
    public void addMusic(JegMusic music) {
        Log.i(TAG, "in Add-Music");

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FLD_NAME, music.getName());
        values.put(FLD_FILE_NAME, music.getFileName());
        values.put(FLD_COUNTRY, music.getCountry());
        values.put(FLD_LENGTH, music.getLength());
        values.put(FLD_DESCRIPTION, music.getDescription());

        // Inserting Row
        Log.d(TAG, "Adding values in Music Table");
        db.insert(TABLE_MUSIC, null, values);
        db.close(); // Closing database connection
    }

    // Getting one music
    public JegMusic getMusic(int id) {
        Log.i(TAG, "in Get-Music");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MUSIC, new String[] { FLD_ID, FLD_NAME,
                        FLD_FILE_NAME, FLD_COUNTRY, FLD_LENGTH, FLD_DESCRIPTION},
                FLD_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        JegMusic music = new JegMusic(cursor.getInt(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getInt(4),
                cursor.getString(5));

        cursor.close();
        db.close();

        // return music
        return music;
    }

    // Getting all musics
    public List<JegMusic> getAllMusics() {
        List<JegMusic> musicList = new ArrayList<>();

        // Select All Query
        String selectQuery = String.format("SELECT %1$s, %2$s, %3$s, %4$s, %5$s, %6$s FROM %7$s ",
                FLD_ID, FLD_NAME, FLD_FILE_NAME, FLD_COUNTRY, FLD_LENGTH, FLD_DESCRIPTION,
                TABLE_MUSIC);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                //Adding to music list
                musicList.add(new JegMusic(cursor.getInt(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getInt(4),
                        cursor.getString(5)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // return contact list
        return musicList;
    }

    // Updating a music
    public int updateMusic(JegMusic music) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FLD_NAME, music.getName());
        values.put(FLD_FILE_NAME, music.getFileName());
        values.put(FLD_COUNTRY, music.getCountry());
        values.put(FLD_LENGTH, music.getLength());
        values.put(FLD_DESCRIPTION, music.getDescription());

        // updating row
        int result = db.update(TABLE_MUSIC, values, FLD_ID + " = ?",
                new String[]{String.valueOf(music.getId())});
        db.close();

        return result;
    }

}
