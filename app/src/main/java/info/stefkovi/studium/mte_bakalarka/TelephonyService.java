package info.stefkovi.studium.mte_bakalarka;

import android.telephony.TelephonyManager;

public class TelephonyService {

    private TelephonyManager _manager;

    public TelephonyService(TelephonyManager manager) {
        this._manager = manager;
    }

    public String GetBasicDeviceInfo() {
        return "";
    }
}
