package info.stefkovi.studium.mte_bakalarka.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class EventModel {
    public long dbId;
    public UUID uid;
    public LocalDateTime happened;
    public List<CellInfoApiModel> cells;
    public PositionApiModel position;
    public int sent;

    public EventModel(List<CellInfoApiModel> cells, PositionApiModel position) {
        this.cells = cells;
        this.position = position;
        this.happened = LocalDateTime.now();
        this.uid = UUID.randomUUID();
        this.sent = 0;
    }

    public EventModel(long dbId) {
        this.dbId = dbId;
    }
}
