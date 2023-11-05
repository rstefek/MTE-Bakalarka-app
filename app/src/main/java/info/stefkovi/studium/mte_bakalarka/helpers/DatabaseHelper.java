package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.List;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class DatabaseHelper {
    private SQLiteDatabase _db;

    public DatabaseHelper(Context ctx) {
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
}
