package org.developerworks.daytrader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDbAdapter {

	public static final String KEY_TAG = "tag";
    public static final String KEY_NAME = "name";
    public static final String KEY_PRICE = "price";
    public static final String KEY_ROWID = "_id";

    private static final String TAG = "DbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE =
        "create table stock (_id integer primary key autoincrement, "
        + "tag text not null, name text not null, price text not null);";

    private static final String DATABASE_NAME = "data15";
    private static final String DATABASE_TABLE = "stock";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;
    
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS stock");
            onCreate(db);
        }
    }

    public MyDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public MyDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    public long createStackRow(String tag, String name, String price) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TAG, tag);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_PRICE, price);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public boolean deleteNote(long rowId) {
        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllData() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_TAG, KEY_NAME,
                KEY_PRICE}, null, null, null, null, null);
    }

    public boolean updateStack(String tag, String name, String price) {
        ContentValues args = new ContentValues();
        args.put(KEY_TAG, tag);
        args.put(KEY_NAME, name);
        args.put(KEY_PRICE, price);

        return mDb.update(DATABASE_TABLE, args, KEY_TAG + "=" + tag, null) > 0;
    }
}
