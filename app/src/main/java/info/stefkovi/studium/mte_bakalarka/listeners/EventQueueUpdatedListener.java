package info.stefkovi.studium.mte_bakalarka.listeners;

import java.util.EventListener;

import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;

public interface EventQueueUpdatedListener extends EventListener {
    public void onEventsQueueUpdated(EventQueueInfo queue);
}
