package info.stefkovi.studium.mte_bakalarka;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.EventApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;
import info.stefkovi.studium.mte_bakalarka.model.EventResultModel;

public class EventQueue {
    private EventQueueUpdatedListener updatedListener;
    private final int QUEUE_LENGTH = 15;
    private HashMap<UUID, EventModel> eventsToProcess;
    private Context ctx;
    private long numInDb;
    public EventQueue(Context ctx) {
        eventsToProcess = new HashMap<>();
        this.ctx = ctx;
    }
    public void onEventAdded() {
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);
        numInDb = db.getUnsentEventsCount();

        if(eventsToProcess.size() == 0 && numInDb >= QUEUE_LENGTH) {
            eventsToProcess = (HashMap<UUID, EventModel>) db.getEventsToSend(QUEUE_LENGTH).stream().collect(Collectors.toMap(eventModel -> eventModel.uid, eventModel -> eventModel));
            sendEvents();
        }
        runUpdatedListener();
    }
    public void sendEventsBulk() {
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);
        eventsToProcess = (HashMap<UUID, EventModel>) db.getEventsToSend(0).stream().collect(Collectors.toMap(eventModel -> eventModel.uid, eventModel -> eventModel));
        sendEvents();
    }
    private void sendEvents() {
        ApiCommuncation api = new ApiCommuncation(ctx);
        DatabaseHelper db = DatabaseHelper.getInstance(ctx);

        for ( UUID eventUid: eventsToProcess.keySet()) {
            EventModel event = eventsToProcess.get(eventUid);
            api.sendEvent(new EventApiModel(event, api.getAPIUserId()), new Response.Listener<EventResultModel>() {
                @Override
                public void onResponse(EventResultModel response) {
                    db.markEventAsSend(response.uid);
                    eventsToProcess.remove(response.uid);
                    runUpdatedListener();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error.networkResponse != null) {
                        Log.e("API", error.networkResponse.toString(), error);
                    }
                    eventsToProcess.remove(eventUid);
                    runUpdatedListener();
                }
            });
        }
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
