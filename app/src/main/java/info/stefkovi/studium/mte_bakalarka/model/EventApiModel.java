package info.stefkovi.studium.mte_bakalarka.model;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventApiModel {
    public String uid;
    public String happened;
    public String network_type;
    public int user;
    public List<CellInfoApiModel> cells;
    public PositionApiModel position;

    public EventApiModel(EventModel eventModel, int userId) {
        this.uid = eventModel.uid.toString();
        this.happened = DateTimeFormatter.ISO_DATE_TIME.format(eventModel.happened);
        this.cells = eventModel.cells;
        this.position = eventModel.position;
        this.user = userId;
    }
}

