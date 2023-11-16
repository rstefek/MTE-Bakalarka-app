package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class DatabaseHelper {
    private SQLiteDatabase _db;
    private static DatabaseHelper _instance = null;

    public static synchronized DatabaseHelper getInstance(Context ctx)
    {
        if (_instance == null)
            _instance = new DatabaseHelper(ctx);

        return _instance;
    }

    private DatabaseHelper(Context ctx) {
        _db = new DatabaseStructureHelper(ctx).getWritableDatabase();
    }

    public long saveEventData(EventModel event) {
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 0);
        values.put(DatabaseStructureHelper.EVENT_COLUMN_UUID, event.uid.toString());
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION, gson.toJson(event.position));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS, gson.toJson(event.cells));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP, DateTimeFormatter.ISO_DATE_TIME.format(event.happened));
        return _db.insert(DatabaseStructureHelper.EVENT_TABLE_NAME, null, values);
    }

    public long getUnsentEventsCount() {
        //název tabulky ani sloupce nejde dát jako parametr!!!
        Cursor c = _db.rawQuery("SELECT COUNT("+DatabaseStructureHelper.EVENT_COLUMN_ID+") AS CNT FROM "+DatabaseStructureHelper.EVENT_TABLE_NAME+" WHERE SENT = 0", null);
        if(c.moveToFirst()) {
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

    public long markEventAsSend(UUID eventUid) {
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 1);
        return _db.update(DatabaseStructureHelper.EVENT_TABLE_NAME, values, DatabaseStructureHelper.EVENT_COLUMN_UUID + " = ?", new String[]{
                String.valueOf(eventUid)
        });
    }

    public ArrayList<EventModel> getEventsToSend() {
        return getEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 0", null, DatabaseStructureHelper.EVENT_COLUMN_ID);
    }

    public ArrayList<EventModel> getAllEvents() {
        return getEvents(null, null, DatabaseStructureHelper.EVENT_COLUMN_ID + " DESC");
    }

    private ArrayList<EventModel> getEvents(String where, String[] whereArgs, String orderBy) {
        Cursor c = _db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
                DatabaseStructureHelper.EVENT_COLUMN_ID,
                DatabaseStructureHelper.EVENT_COLUMN_UUID,
                DatabaseStructureHelper.EVENT_COLUMN_SENT,
                DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS,
                DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION,
                DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP
        }, where, whereArgs, null, null, orderBy);

        Gson gson = new Gson();
        ArrayList<EventModel> events = new ArrayList<>();

        while (c.moveToNext()) {
            EventModel event = new EventModel(c.getLong(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_ID)));
            String uid = c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_UUID));
            event.uid = (uid != null ? UUID.fromString(uid) : null);
            event.cells = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS)), TypeTokenHelper.getCellListType());
            event.position = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION)), PositionApiModel.class);
            event.happened = LocalDateTime.parse(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP)), DateTimeFormatter.ISO_DATE_TIME);
            event.sent = c.getInt(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_SENT));
            events.add(event);
        }

        return events;
    }

    public int deleteProcessed() {
        return deleteSpecificEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 1", null);
    }

    public int deleteNotSent() {
        return deleteSpecificEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 0", null);
    }

    private int deleteSpecificEvents(String where, String[] whereArgs) {
        return _db.delete(DatabaseStructureHelper.EVENT_TABLE_NAME, where, whereArgs);
    }


}
