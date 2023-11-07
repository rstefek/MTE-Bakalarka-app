package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.List;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class DatabaseHelper {
    private SQLiteDatabase _db;
    private static DatabaseHelper _instance = null;

    // Static method
    // Static method to create instance of Singleton class
    public static synchronized DatabaseHelper getInstance(Context ctx)
    {
        if (_instance == null)
            _instance = new DatabaseHelper(ctx);

        return _instance;
    }

    private DatabaseHelper(Context ctx) {
        _db = new DatabaseStructureHelper(ctx).getWritableDatabase();
    }

    public long saveEventData(PositionApiModel pos, List<CellInfoApiModel> cells) {
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 0);
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION, gson.toJson(pos));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS, gson.toJson(cells));
        return _db.insert(DatabaseStructureHelper.EVENT_TABLE_NAME, null, values);
    }

    public long getLastEventId() {
        //název tabulky ani sloupce nejde dát jako parametr!!!
        Cursor c = _db.rawQuery("SELECT MAX("+DatabaseStructureHelper.EVENT_COLUMN_ID+") AS MAX FROM "+DatabaseStructureHelper.EVENT_TABLE_NAME, null);
        if(c.moveToFirst()) {
            long lastId = c.getLong(0);
            return c.getLong(0);
        } else {
            return 0;
        }
    }

    public String getEventDataCells(long eventId) {
        Cursor c = _db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
                DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS
        }, DatabaseStructureHelper.EVENT_COLUMN_ID + " = ?", new String[]{
                String.valueOf(eventId)
        }, null, null, null);
        if(c.moveToFirst()) {
            return c.getString(0);
        } else {
            return "{}";
        }
    }
}
