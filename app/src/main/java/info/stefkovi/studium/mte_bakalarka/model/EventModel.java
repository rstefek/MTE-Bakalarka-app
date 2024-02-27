package info.stefkovi.studium.mte_bakalarka.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EventModel {
    public long dbId;
    public UUID uid;
    public LocalDateTime happened;
    public List<CellInfoApiModel> cells;
    public PositionApiModel position;
    public int sent;
    public int event_group;
    public String user_desc;

    public EventModel(List<CellInfoApiModel> cells, PositionApiModel position, int eventGroupId) {
        this.cells = cells;
        this.position = position;
        this.happened = LocalDateTime.now();
        this.uid = UUID.randomUUID();
        this.sent = 0;
        this.event_group = eventGroupId;
    }

    public EventModel(List<CellInfoApiModel> cells, String desc, int eventGroupId) {
        this.cells = cells;
        this.user_desc = desc;
        this.happened = LocalDateTime.now();
        this.uid = UUID.randomUUID();
        this.sent = 0;
        this.event_group = eventGroupId;
    }

    public EventModel(long dbId) {
        this.dbId = dbId;
    }
}
