package com.example.cosc195cst107finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * This class extends the functionality of SQLite for the purposes of the Remot.ly app's
 * stats saving functionality.
 *
 * @author Jonathan Cruz CST107
 * @version 1
 */
public class SessionDbHelper extends SQLiteOpenHelper
{
    // DATABASE PROPERTIES
    private static final String DB_NAME = "sessions.db";
    private static final String TABLE_NAME = "Sessions";
    private static final int DB_VERSION = 1;
    public SQLiteDatabase sqlDB; // ref to the db object being used

    // COLUMN NAMES
    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String CORRECT = "correct";

    // CONSTRUCTOR
    public SessionDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    // CONNECTION HELPERS
    public void open() throws SQLException
    {
        // Trigger onCreate and onUpgrade to open a connection to the database
        sqlDB = this.getWritableDatabase();
    }
    public void close() { sqlDB.close(); }

    //OVERRIDES
    @Override public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME +  " ("
                + ID + " integer primary key autoincrement, "
                + NAME + " text not null, "
                + CORRECT + " integer not null"
                + ");"
        );
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //CREATE SESSION, GET ALL SESSIONS (to calculate All-Time stat)
    public long createSession(Session session)
    {
        ContentValues cvs = new ContentValues();
        cvs.put(NAME, session.name);
        cvs.put(CORRECT, session.correct);

        session.id = sqlDB.insert(TABLE_NAME, null, cvs);
        return session.id;
    }

    public Cursor getAllSessionsBySQL()
    {
        String sqlSelect = "SELECT * FROM " + TABLE_NAME;
        return sqlDB.rawQuery(sqlSelect, null);
    }
}
