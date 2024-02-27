package info.stefkovi.studium.mte_bakalarka;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.EventApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventBulkResultModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;

public class EventQueue {
    private static EventQueue instance = null;

    public static synchronized EventQueue getInstance(Context ctx)
    {
        if (instance == null)
            instance = new EventQueue(ctx);

        return instance;
    }

    private EventQueue(Context ctx) {
        eventsToProcess = new ArrayList<>();
        this.ctx = ctx;
    }
    private EventQueueUpdatedListener updatedListener;
    private final int QUEUE_LENGTH = 15;
    private List<EventModel> eventsToProcess;
    private Context ctx;
    private long numInDb;
    private int deviceId;

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public void onEventAdded(boolean sendData) {
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);
        numInDb = db.getUnsentEventsCount();

        if(sendData && eventsToProcess.size() == 0 && numInDb >= QUEUE_LENGTH) {
            eventsToProcess = db.getEventsToSend(QUEUE_LENGTH);
            sendEvents();
            eventsToProcess.clear();
        }
        runUpdatedListener();
    }
    public void sendEventsBulk() {
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);
        List<EventModel> allEvents = db.getEventsToSend(0);
        for (EventModel event : allEvents) {
            eventsToProcess.add(event);
            if(eventsToProcess.size() >= QUEUE_LENGTH) {
                sendEvents();
                eventsToProcess.clear();
            }
        }
        sendEvents();
        eventsToProcess.clear();
    }
    private void sendEvents() {
        ApiCommuncation api = new ApiCommuncation(ctx);
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);

        int userId = api.getAPIUserId();

        api.sendEventsBulk(eventsToProcess.stream().map((eventModel -> new EventApiModel(eventModel, userId, deviceId) )).collect(Collectors.toList()), new Response.Listener<EventBulkResultModel>() {
            @Override
            public void onResponse(EventBulkResultModel response) {
                for (String uidSaved : response.saved) {
                    db.markEventSentStatus(UUID.fromString(uidSaved), 1);
                }
                for (String uidError : response.errors) {
                    db.markEventSentStatus(UUID.fromString(uidError), 2);
                }
                runUpdatedListener();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null) {
                    Log.e("API", error.networkResponse.toString(), error);
                }
                runUpdatedListener();
            }
        });
    }

    private EventQueueInfo createEventQueueInfo() {
        return new EventQueueInfo(numInDb, QUEUE_LENGTH,eventsToProcess.size());
    }
    public void setUpdatedListener(EventQueueUpdatedListener listener) {
        updatedListener = listener;
    }
    public void runUpdatedListener() {
        if(updatedListener != null) {
            updatedListener.onEventsQueueUpdated(createEventQueueInfo());
        }
    }
}
