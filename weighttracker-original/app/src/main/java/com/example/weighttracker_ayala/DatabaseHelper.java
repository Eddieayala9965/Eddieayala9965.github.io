package com.example.weighttracker_ayala;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weight_tracker.db";
    private static final int DB_VERSION = 1;

    // users table columns
    static final String TABLE_USERS = "users";
    static final String COL_USER_ID = "id";
    static final String COL_USERNAME = "username";
    static final String COL_PASSWORD = "password";
    static final String COL_GOAL_WEIGHT = "goal_weight";
    static final String COL_PHONE = "phone_number";

    // weight entries table columns
    static final String TABLE_WEIGHTS = "weights";
    static final String COL_ENTRY_ID = "id";
    static final String COL_USER_FK = "user_id";
    static final String COL_DATE = "date";
    static final String COL_WEIGHT = "weight";
    static final String COL_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create users table with goal weight and phone number stored per account
        db.execSQL(
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COL_PASSWORD + " TEXT NOT NULL, " +
            COL_GOAL_WEIGHT + " REAL DEFAULT 0, " +
            COL_PHONE + " TEXT DEFAULT ''" +
            ")"
        );

        // create weights table linked to users by foreign key
        db.execSQL(
            "CREATE TABLE " + TABLE_WEIGHTS + " (" +
            COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_FK + " INTEGER NOT NULL, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_WEIGHT + " REAL NOT NULL, " +
            COL_NOTES + " TEXT DEFAULT ''" +
            ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop and recreate tables on schema version change
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // saves new user credentials and returns the generated row ID
    public long registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    // returns user ID on match, or -1 if credentials are incorrect
    public long loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        long userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exists;
    }

    public double getGoalWeight(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_GOAL_WEIGHT},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        double goal = 0;
        if (cursor.moveToFirst()) {
            goal = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return goal;
    }

    public void updateGoalWeight(long userId, double goalWeight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GOAL_WEIGHT, goalWeight);
        db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public String getPhoneNumber(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_PHONE},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        String phone = "";
        if (cursor.moveToFirst()) {
            phone = cursor.getString(0);
            if (phone == null) phone = "";
        }
        cursor.close();
        db.close();
        return phone;
    }

    public void updatePhoneNumber(long userId, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PHONE, phone);
        db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public long addWeightEntry(long userId, String date, double weight, String notes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_FK, userId);
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);
        values.put(COL_NOTES, notes);
        long id = db.insert(TABLE_WEIGHTS, null, values);
        db.close();
        return id;
    }

    public boolean updateWeightEntry(long entryId, String date, double weight, String notes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_WEIGHT, weight);
        values.put(COL_NOTES, notes);
        int rows = db.update(TABLE_WEIGHTS, values,
                COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteWeightEntry(long entryId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_WEIGHTS,
                COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
        db.close();
        return rows > 0;
    }

    // returns entries sorted newest first so the most recent weight shows at the top
    public List<WeightEntry> getWeightEntries(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<WeightEntry> entries = new ArrayList<>();
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_USER_FK + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_DATE + " DESC");
        while (cursor.moveToNext()) {
            entries.add(new WeightEntry(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID)),
                    userId,
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES))
            ));
        }
        cursor.close();
        db.close();
        return entries;
    }

    public WeightEntry getWeightEntry(long entryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHTS, null,
                COL_ENTRY_ID + "=?",
                new String[]{String.valueOf(entryId)},
                null, null, null);
        WeightEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = new WeightEntry(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_FK)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES))
            );
        }
        cursor.close();
        db.close();
        return entry;
    }
}
