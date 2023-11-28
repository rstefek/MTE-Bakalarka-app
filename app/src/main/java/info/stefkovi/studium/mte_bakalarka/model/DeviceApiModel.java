package info.stefkovi.studium.mte_bakalarka.model;

import java.util.UUID;

public class DeviceApiModel {
    public int id;
    public String name;
    public UUID saved_uid;

    public DeviceApiModel(UUID saved_uid) {
        this.saved_uid = saved_uid;
    }
}
