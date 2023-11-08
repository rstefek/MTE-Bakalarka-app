package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class DatabaseHelper {
    private SQLiteDatabase _db;
    private DateTimeFormatter _dateTimeFormatter;
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
        _dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    }

    public long saveEventData(PositionApiModel pos, List<CellInfoApiModel> cells) {
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 0);
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION, gson.toJson(pos));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS, gson.toJson(cells));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP, _dateTimeFormatter.format(LocalDateTime.now()));
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

    public ArrayList<CellInfoApiModel> getEventDataCells(long eventId) {
        Cursor c = _db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
                DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS
        }, DatabaseStructureHelper.EVENT_COLUMN_ID + " = ?", new String[]{
                String.valueOf(eventId)
        }, null, null, null);
        if(c.moveToFirst()) {
            Gson gson = new Gson();
            String cellData = c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS));
            return gson.fromJson(cellData, TypeTokenHelper.getCellListType());
        } else {
            return new ArrayList<CellInfoApiModel>();
        }
    }

    public long markEventAsSend(long eventId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 1);
        return _db.update(DatabaseStructureHelper.EVENT_TABLE_NAME, values, DatabaseStructureHelper.EVENT_COLUMN_ID + " = ?", new String[]{
                String.valueOf(eventId)
        });
    }

    public ArrayList<EventModel> getEventsToSend() {
        Cursor c = _db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
                DatabaseStructureHelper.EVENT_COLUMN_ID,
                DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS,
                DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION,
                DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP
        }, DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 0", null, null, null, null);

        Gson gson = new Gson();
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        ArrayList<EventModel> events = new ArrayList<>();

        while (c.moveToNext()) {
            EventModel event = new EventModel();
            event.dbId = c.getLong(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_ID));
            event.uid = UUID.randomUUID();
            event.cells = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS)), TypeTokenHelper.getCellListType());
            event.position = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION)), PositionApiModel.class);
            event.happened = c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP));
            events.add(event);
        }

        return events;
    }
}
