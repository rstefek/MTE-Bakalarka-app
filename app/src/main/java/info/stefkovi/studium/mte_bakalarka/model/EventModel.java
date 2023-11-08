package info.stefkovi.studium.mte_bakalarka.model;

import java.util.List;
import java.util.UUID;

public class EventModel {
    public long dbId;
    public UUID uid;
    public String happened;
    public String network_type;
    public int user;
    public List<CellInfoApiModel> cells;
    public PositionApiModel position;

}
