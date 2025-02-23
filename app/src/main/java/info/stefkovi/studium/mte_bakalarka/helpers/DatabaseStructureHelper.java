package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseStructureHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 5;

    public static final String DATABASE_NAME = "event_db";
    public static final String EVENT_TABLE_NAME = "events";
    public static final String EVENT_COLUMN_ID = "_id";
    public static final String EVENT_COLUMN_UUID = "uuid";
    public static final String EVENT_COLUMN_TIMESTAMP = "tstamp";
    public static final String EVENT_COLUMN_EVENT_GROUP_ID = "event_group_id";
    public static final String EVENT_COLUMN_SENT = "sent";
    public static final String EVENT_COLUMN_DATA_CELLS = "cells";
    public static final String EVENT_COLUMN_DATA_POSITION = "position";
    public static final String EVENT_COLUMN_USER_DESC = "user_desc";

    public DatabaseStructureHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + EVENT_TABLE_NAME + " (" +
                EVENT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EVENT_COLUMN_UUID + " TEXT," +
                EVENT_COLUMN_TIMESTAMP + " TEXT, " +
                EVENT_COLUMN_EVENT_GROUP_ID + " INT UNSIGNED," +
                EVENT_COLUMN_SENT + " INT UNSIGNED, " +
                EVENT_COLUMN_DATA_POSITION + " TEXT, " +
                EVENT_COLUMN_USER_DESC + " TEXT, " +
                EVENT_COLUMN_DATA_CELLS + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if(i < 3 && i1 >= 3) {
            sqLiteDatabase.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD " +
                    EVENT_COLUMN_UUID + " TEXT");

        }
        if(i < 4 && i1 >= 4) {
            sqLiteDatabase.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD " +
                    EVENT_COLUMN_EVENT_GROUP_ID + " INT UNSIGNED");

        }
        if(i < 5 && i1 >= 5) {
            sqLiteDatabase.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD " +
                    EVENT_COLUMN_USER_DESC + " TEXT");
        }
    }
}
