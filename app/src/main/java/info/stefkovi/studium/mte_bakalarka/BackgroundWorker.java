package info.stefkovi.studium.mte_bakalarka;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BackgroundWorker extends Service {
    public BackgroundWorker() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}