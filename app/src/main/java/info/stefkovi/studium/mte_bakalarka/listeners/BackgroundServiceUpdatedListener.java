package info.stefkovi.studium.mte_bakalarka.listeners;

import java.util.EventListener;
import java.util.List;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public interface BackgroundServiceUpdatedListener extends EventListener {
    public void onPositionUpdated(PositionApiModel position);
    public void onCellsUpdated(List<CellInfoApiModel> cells);
    public void onEvent(EventModel event);

}
