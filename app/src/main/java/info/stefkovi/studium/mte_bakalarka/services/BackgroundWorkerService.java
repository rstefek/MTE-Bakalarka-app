package info.stefkovi.studium.mte_bakalarka.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import java.util.List;

import info.stefkovi.studium.mte_bakalarka.R;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.PositionUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class BackgroundWorkerService extends Service {
    private TelephonyService _teleService;
    private PositionService _posService;

    private BackgroundServiceUpdatedListener serviceUpdatedListener;

    public class ServiceBinder extends Binder {
        public BackgroundWorkerService getService() {
            return BackgroundWorkerService.this;
        }
    }

    private void setupInternalServices() {
        if(_teleService == null) {
            _teleService = new TelephonyService((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        }
        if(_posService == null) {
            _posService = new PositionService((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            _posService.setPositionUpdatedListener(positionUpdatedListener);
        }
    }

    private final PositionUpdatedListener positionUpdatedListener = new PositionUpdatedListener() {
        @Override
        public void onPositionUpdated(PositionApiModel position) {
            List<CellInfoApiModel> cells = _teleService.getAllCellInfo();
            EventModel event = new EventModel(cells, position);

            DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
            event.dbId = db.saveEventData(event);

            if(serviceUpdatedListener != null) {
                serviceUpdatedListener.onCellsUpdated(cells);
                serviceUpdatedListener.onPositionUpdated(position);
                serviceUpdatedListener.onEvent(event);
            }
        }
    };

    public void setUpdatedListener(BackgroundServiceUpdatedListener listener) {
        serviceUpdatedListener = listener;
    }

    public TelephonyService getTelephonyService() {
        return _teleService;
    }

    public PositionService getPositionService() {
        return _posService;
    }

    public void start() {
        _posService.activateGathering();
    }

    public void stop() {
        _posService.deactivateGathering();
    }

    private final IBinder iBinder = new ServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        setupInternalServices();
        return iBinder;
    }

    private void createNotificationChannel() {
            CharSequence name = getString(R.string.BgChannelName);
            String description = getString(R.string.BgChannelDesc);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    private final String CHANNEL_ID = "MTEBakCH";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(startId, new Notification.Builder(getApplicationContext(), CHANNEL_ID).build());
        setupInternalServices();
        return START_STICKY;
    }
}