package info.stefkovi.studium.mte_bakalarka.model;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class EventApiModel {
    public String uid;
    public String happened;
    public String network_type;
    public int user;
    public int device;
    public List<CellInfoApiModel> cells;
    public PositionApiModel position;
    public int event_group;
    public String user_description;

    public EventApiModel(EventModel eventModel, int userId, int deviceId) {
        this.uid = eventModel.uid.toString();
        this.happened = DateTimeFormatter.ISO_DATE_TIME.format(eventModel.happened);
        this.cells = eventModel.cells;
        this.position = eventModel.position;
        this.user = userId;
        this.device = deviceId;
        this.event_group = eventModel.event_group;
        this.user_description = eventModel.user_desc;
        Optional<CellInfoApiModel> regCell = eventModel.cells.stream().filter(cellInfoApiModel -> cellInfoApiModel.registered == true).findFirst();
        if(regCell.isPresent()) {
            this.network_type = regCell.get().network_type;
        }
    }
}

