package info.stefkovi.studium.mte_bakalarka.helpers;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.DeviceApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventGroupApiModel;

public class TypeTokenHelper {
    public static Type getCellListType() {
        return new TypeToken<ArrayList<CellInfoApiModel>>(){}.getType();
    }

    public static Type getEventGroupsListType() {
        return new TypeToken<ArrayList<EventGroupApiModel>>(){}.getType();
    }

    public static Type getEventsUidsListType() {
        return new TypeToken<ArrayList<UUID>>(){}.getType();
    }

    public static Type getDevicesListType() {
        return new TypeToken<ArrayList<DeviceApiModel>>(){}.getType();
    }
}
