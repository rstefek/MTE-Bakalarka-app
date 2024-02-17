package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class DatabaseHelper {
    private SQLiteDatabase db;
    private static DatabaseHelper instance = null;

    public static synchronized DatabaseHelper getInstance(Context ctx)
    {
        if (instance == null)
            instance = new DatabaseHelper(ctx);

        return instance;
    }

    private DatabaseHelper(Context ctx) {
        db = new DatabaseStructureHelper(ctx).getWritableDatabase();
    }

    public long saveEventData(EventModel event) {
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, 0);
        values.put(DatabaseStructureHelper.EVENT_COLUMN_UUID, event.uid.toString());
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION, gson.toJson(event.position));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS, gson.toJson(event.cells));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP, DateTimeFormatter.ISO_DATE_TIME.format(event.happened));
        values.put(DatabaseStructureHelper.EVENT_COLUMN_EVENT_GROUP_ID, event.event_group);
        return db.insert(DatabaseStructureHelper.EVENT_TABLE_NAME, null, values);
    }

    public long getUnsentEventsCount() {
        //název tabulky ani sloupce nejde dát jako parametr!!!
        Cursor c = db.rawQuery("SELECT COUNT("+DatabaseStructureHelper.EVENT_COLUMN_ID+") AS CNT FROM "+DatabaseStructureHelper.EVENT_TABLE_NAME+" WHERE SENT = 0", null);
        if(c.moveToFirst()) {
            return c.getLong(0);
        } else {
            return 0;
        }
    }

    public ArrayList<CellInfoApiModel> getEventDataCells(long eventId) {
        Cursor c = db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
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

    public long markEventsSentStatus(int statusOld, int statusNew) {
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, statusNew);
        return db.update(DatabaseStructureHelper.EVENT_TABLE_NAME, values, DatabaseStructureHelper.EVENT_COLUMN_SENT + " = ?", new String[]{
                String.valueOf(statusOld)
        });
    }
    public long markEventSentStatus(UUID eventUid, int status) {
        ContentValues values = new ContentValues();
        values.put(DatabaseStructureHelper.EVENT_COLUMN_SENT, status);
        return db.update(DatabaseStructureHelper.EVENT_TABLE_NAME, values, DatabaseStructureHelper.EVENT_COLUMN_UUID + " = ?", new String[]{
                String.valueOf(eventUid)
        });
    }

    public List<EventModel> getEventsToSend(int num) {
        ArrayList<EventModel> events = getEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 0", null, DatabaseStructureHelper.EVENT_COLUMN_ID);
        return num > 0 ? events.subList(0, num) : events;
    }

    public ArrayList<EventModel> getAllEvents() {
        return getEvents(null, null, DatabaseStructureHelper.EVENT_COLUMN_ID + " DESC");
    }

    public ArrayList<EventModel> getAllEventsByDate(LocalDate dt) {
        return getEvents("date("+DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP + ") = date(?)", new String[]{ dt.format(DateTimeFormatter.ISO_DATE)}, DatabaseStructureHelper.EVENT_COLUMN_ID + " DESC");
    }

    private ArrayList<EventModel> getEvents(String where, String[] whereArgs, String orderBy) {
        Cursor c = db.query(DatabaseStructureHelper.EVENT_TABLE_NAME, new String[]{
                DatabaseStructureHelper.EVENT_COLUMN_ID,
                DatabaseStructureHelper.EVENT_COLUMN_UUID,
                DatabaseStructureHelper.EVENT_COLUMN_EVENT_GROUP_ID,
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
            event.event_group = c.getInt(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_EVENT_GROUP_ID));
            event.cells = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_CELLS)), TypeTokenHelper.getCellListType());
            event.position = gson.fromJson(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_DATA_POSITION)), PositionApiModel.class);
            event.happened = LocalDateTime.parse(c.getString(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_TIMESTAMP)), DateTimeFormatter.ISO_DATE_TIME);
            event.sent = c.getInt(c.getColumnIndexOrThrow(DatabaseStructureHelper.EVENT_COLUMN_SENT));
            events.add(event);
        }
        c.close();

        return events;
    }

    public int deleteProcessed() {
        return deleteSpecificEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 1", null);
    }

    public int deleteNotProcessed() {
        return deleteSpecificEvents(DatabaseStructureHelper.EVENT_COLUMN_SENT + " = 0", null);
    }

    private int deleteSpecificEvents(String where, String[] whereArgs) {
        return db.delete(DatabaseStructureHelper.EVENT_TABLE_NAME, where, whereArgs);
    }


}
