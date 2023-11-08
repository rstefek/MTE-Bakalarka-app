package info.stefkovi.studium.mte_bakalarka.listeners;

import java.util.EventListener;

import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public interface PositionUpdatedListener extends EventListener {
    public void onPositionUpdated(PositionApiModel position);
}
