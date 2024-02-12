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
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import java.util.List;

import info.stefkovi.studium.mte_bakalarka.R;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.PositionUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class BackgroundWorkerService extends Service {
    private TelephonyService teleService;
    private PositionService posService;
    private int eventGroupId;
    private BackgroundServiceUpdatedListener serviceUpdatedListener;
    private PowerManager.WakeLock wakeLock;

    public class ServiceBinder extends Binder {
        public BackgroundWorkerService getService() {
            return BackgroundWorkerService.this;
        }
    }

    public void setEventGroupId(int eventGroupId) {
        this.eventGroupId = eventGroupId;
    }

    private void setupInternalServices() {
        if(teleService == null) {
            teleService = new TelephonyService((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        }
        if(posService == null) {
            posService = new PositionService((LocationManager) getSystemService(Context.LOCATION_SERVICE));
            posService.setPositionUpdatedListener(positionUpdatedListener);

            SharedPreferencesHelper preferencesHelper = new SharedPreferencesHelper(getApplicationContext());
            String interval = preferencesHelper.readPrefString(SharedPreferencesHelper.PREF_GATHER_INTERVAL);
            if(!interval.isEmpty()) {
                posService.setMinTime(Long.valueOf(interval)*1000);
            }
        }
        if(wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CHANNEL_ID+":WakeLock");
        }
    }

    private final PositionUpdatedListener positionUpdatedListener = new PositionUpdatedListener() {
        @Override
        public void onPositionUpdated(PositionApiModel position) {
            List<CellInfoApiModel> cells = teleService.getAllCellInfo();
            EventModel event = new EventModel(cells, position, eventGroupId);

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

    private final IBinder iBinder = new ServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        setupInternalServices();
        return iBinder;
    }

    private void createNotificationChannel() {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    private final String CHANNEL_ID = "MTEBakCH";
    private final CharSequence CHANNEL_NAME = "MTE Bakalarka APP";
    private final String CHANNEL_DESC = "Kanál pro aplikaci";

    @Override
    public void onDestroy() {
        wakeLock.release();
        posService.deactivateGathering();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(startId, new Notification.Builder(getApplicationContext(), CHANNEL_ID).setContentTitle(getString(R.string.BgChannelName)).setContentText(getString(R.string.BgChannelDesc)).build());
        setupInternalServices();
        posService.activateGathering(getApplicationContext());
        wakeLock.acquire();
        return START_STICKY;
    }
}