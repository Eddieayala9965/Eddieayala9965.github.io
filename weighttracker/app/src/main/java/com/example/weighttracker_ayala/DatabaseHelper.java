package com.example.weighttracker_ayala;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "weight_tracker.db";
    private static final int DB_VERSION = 3;

    private static final String TABLE_WEIGHTS_TEMP = "weights_new";
    private static final String INDEX_WEIGHTS_USER_DATE = "idx_weights_user_date";

    // PBKDF2 password hashing parameters.
    // SHA-1 (not SHA-256) is used as the PRF because PBKDF2WithHmacSHA256 is only
    // available on API 26+, while this app supports minSdk 24. SHA-1's known weaknesses
    // are about collision resistance, which does not apply to its use as an HMAC inside
    // PBKDF2, so this keeps the app working on every supported OS version.
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int PBKDF2_KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    // users table columns
    static final String TABLE_USERS = "users";
    static final String COL_USER_ID = "id";
    static final String COL_USERNAME = "username";
    static final String COL_PASSWORD_HASH = "password_hash";
    static final String COL_SALT = "salt";
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
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite has foreign key enforcement off by default; without this, the weights
        // table's ON DELETE CASCADE would silently do nothing
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create users table with goal weight and phone number stored per account
        db.execSQL(
            "CREATE TABLE " + TABLE_USERS + " (" +
            COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USERNAME + " TEXT UNIQUE NOT NULL, " +
            COL_PASSWORD_HASH + " TEXT NOT NULL, " +
            COL_SALT + " TEXT NOT NULL, " +
            COL_GOAL_WEIGHT + " REAL DEFAULT 0, " +
            COL_PHONE + " TEXT DEFAULT ''" +
            ")"
        );

        // create weights table, linked to users by a real foreign key with cascading
        // deletes; dates are stored as yyyy-MM-dd so COL_DATE + " DESC" sorts correctly
        db.execSQL(
            "CREATE TABLE " + TABLE_WEIGHTS + " (" +
            COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_FK + " INTEGER NOT NULL, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_WEIGHT + " REAL NOT NULL, " +
            COL_NOTES + " TEXT DEFAULT '', " +
            "FOREIGN KEY(" + COL_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ")"
        );

        createWeightsIndex(db);
    }

    private void createWeightsIndex(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX IF NOT EXISTS " + INDEX_WEIGHTS_USER_DATE +
                " ON " + TABLE_WEIGHTS + "(" + COL_USER_FK + ", " + COL_DATE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onUpgrade already runs inside a transaction managed by SQLiteOpenHelper, but the
        // steps are wrapped explicitly here so the intent of an atomic migration is obvious
        db.beginTransaction();
        try {
            int version = oldVersion;

            // v1 predates password hashing and was never used by real accounts, so there
            // is no data worth preserving; rebuild from scratch on the current schema
            if (version < 2) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
                onCreate(db);
                version = DB_VERSION;
            }

            // v2 -> v3: add the missing foreign key and switch dates to a sortable format.
            // SQLite cannot add a foreign key to an existing table with ALTER TABLE, so the
            // weights table is rebuilt and its rows are copied across.
            if (version == 2) {
                migrateV2ToV3(db);
                version = 3;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void migrateV2ToV3(SQLiteDatabase db) {
        // step 1: create the new table with the foreign key constraint
        db.execSQL(
            "CREATE TABLE " + TABLE_WEIGHTS_TEMP + " (" +
            COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_USER_FK + " INTEGER NOT NULL, " +
            COL_DATE + " TEXT NOT NULL, " +
            COL_WEIGHT + " REAL NOT NULL, " +
            COL_NOTES + " TEXT DEFAULT '', " +
            "FOREIGN KEY(" + COL_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" +
            ")"
        );

        // step 2: copy each row, converting its date to the new storage format; rows whose
        // user_id has no matching user are orphans left over from before the foreign key
        // existed, and are dropped rather than copied
        Cursor cursor = db.query(TABLE_WEIGHTS, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_FK));
            if (!userExists(db, userId)) {
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(COL_ENTRY_ID, cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID)));
            values.put(COL_USER_FK, userId);
            values.put(COL_DATE, DateFormats.toStorage(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))));
            values.put(COL_WEIGHT, cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)));
            values.put(COL_NOTES, cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
            db.insert(TABLE_WEIGHTS_TEMP, null, values);
        }
        cursor.close();

        // step 3: swap the old table out for the new one
        db.execSQL("DROP TABLE " + TABLE_WEIGHTS);
        db.execSQL("ALTER TABLE " + TABLE_WEIGHTS_TEMP + " RENAME TO " + TABLE_WEIGHTS);

        // step 4: the index was dropped along with the old table, so recreate it
        createWeightsIndex(db);
    }

    private boolean userExists(SQLiteDatabase db, long userId) {
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // a single unit of work to run against an open database connection
    private interface DatabaseOperation<T> {
        T run(SQLiteDatabase db);
    }

    // opens a writable connection, runs the operation, and guarantees the connection closes
    private <T> T executeWritable(DatabaseOperation<T> operation) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            return operation.run(db);
        } finally {
            db.close();
        }
    }

    // opens a readable connection, runs the operation, and guarantees the connection closes
    private <T> T executeReadable(DatabaseOperation<T> operation) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            return operation.run(db);
        } finally {
            db.close();
        }
    }

    // generates a fresh cryptographically random salt for a new user
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // derives a PBKDF2 hash from a password and salt
    private static byte[] hashPassword(String password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // indicates a broken platform crypto provider, not a normal runtime condition
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    // saves new user credentials and returns the generated row ID
    public long registerUser(String username, String password) {
        byte[] salt = generateSalt();
        byte[] hash = hashPassword(password, salt);
        String saltEncoded = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashEncoded = Base64.encodeToString(hash, Base64.NO_WRAP);

        return executeWritable(db -> {
            ContentValues values = new ContentValues();
            values.put(COL_USERNAME, username);
            values.put(COL_PASSWORD_HASH, hashEncoded);
            values.put(COL_SALT, saltEncoded);
            return db.insert(TABLE_USERS, null, values);
        });
    }

    // returns user ID on match, or -1 if credentials are incorrect
    public long loginUser(String username, String password) {
        return executeReadable(db -> {
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_USER_ID, COL_PASSWORD_HASH, COL_SALT},
                    COL_USERNAME + "=?",
                    new String[]{username},
                    null, null, null);
            long userId = -1;
            if (cursor.moveToFirst()) {
                long candidateId = cursor.getLong(0);
                byte[] storedHash = Base64.decode(cursor.getString(1), Base64.NO_WRAP);
                byte[] storedSalt = Base64.decode(cursor.getString(2), Base64.NO_WRAP);
                byte[] attemptedHash = hashPassword(password, storedSalt);
                // constant-time comparison guards against timing attacks
                if (MessageDigest.isEqual(storedHash, attemptedHash)) {
                    userId = candidateId;
                }
            }
            cursor.close();
            return userId;
        });
    }

    public boolean usernameExists(String username) {
        return executeReadable(db -> {
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COL_USER_ID},
                    COL_USERNAME + "=?",
                    new String[]{username},
                    null, null, null);
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        });
    }

    public double getGoalWeight(long userId) {
        return executeReadable(db -> {
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
            return goal;
        });
    }

    public void updateGoalWeight(long userId, double goalWeight) {
        executeWritable(db -> {
            ContentValues values = new ContentValues();
            values.put(COL_GOAL_WEIGHT, goalWeight);
            return db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        });
    }

    public String getPhoneNumber(long userId) {
        return executeReadable(db -> {
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
            return phone;
        });
    }

    public void updatePhoneNumber(long userId, String phone) {
        executeWritable(db -> {
            ContentValues values = new ContentValues();
            values.put(COL_PHONE, phone);
            return db.update(TABLE_USERS, values, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
        });
    }

    public long addWeightEntry(long userId, String date, double weight, String notes) {
        // reject invalid dates here instead of saving junk; the UI always passes MM/dd/yyyy
        String storedDate = DateFormats.toStorage(date);
        return executeWritable(db -> {
            ContentValues values = new ContentValues();
            values.put(COL_USER_FK, userId);
            values.put(COL_DATE, storedDate);
            values.put(COL_WEIGHT, weight);
            values.put(COL_NOTES, notes);
            return db.insert(TABLE_WEIGHTS, null, values);
        });
    }

    public boolean updateWeightEntry(long entryId, String date, double weight, String notes) {
        String storedDate = DateFormats.toStorage(date);
        return executeWritable(db -> {
            ContentValues values = new ContentValues();
            values.put(COL_DATE, storedDate);
            values.put(COL_WEIGHT, weight);
            values.put(COL_NOTES, notes);
            int rows = db.update(TABLE_WEIGHTS, values,
                    COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
            return rows > 0;
        });
    }

    public boolean deleteWeightEntry(long entryId) {
        return executeWritable(db -> {
            int rows = db.delete(TABLE_WEIGHTS,
                    COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
            return rows > 0;
        });
    }

    // returns entries sorted newest first so the most recent weight shows at the top
    public List<WeightEntry> getWeightEntries(long userId) {
        return executeReadable(db -> {
            List<WeightEntry> entries = new ArrayList<>();
            Cursor cursor = db.query(TABLE_WEIGHTS, null,
                    COL_USER_FK + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, COL_DATE + " DESC");
            while (cursor.moveToNext()) {
                entries.add(new WeightEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID)),
                        userId,
                        DateFormats.toDisplay(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES))
                ));
            }
            cursor.close();
            return entries;
        });
    }

    public WeightEntry getWeightEntry(long entryId) {
        return executeReadable(db -> {
            Cursor cursor = db.query(TABLE_WEIGHTS, null,
                    COL_ENTRY_ID + "=?",
                    new String[]{String.valueOf(entryId)},
                    null, null, null);
            WeightEntry entry = null;
            if (cursor.moveToFirst()) {
                entry = new WeightEntry(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ENTRY_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_FK)),
                        DateFormats.toDisplay(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WEIGHT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES))
                );
            }
            cursor.close();
            return entry;
        });
    }
}
