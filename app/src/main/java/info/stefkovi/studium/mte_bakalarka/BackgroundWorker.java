package info.stefkovi.studium.mte_bakalarka;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class BackgroundWorker extends Service {
    private TelephonyService _teleService;
    private PositionService _posService;

    public BackgroundWorker() {
        this._teleService = new TelephonyService((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        this._posService = new PositionService((LocationManager) getSystemService(Context.LOCATION_SERVICE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}