package info.stefkovi.studium.mte_bakalarka.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.PositionUpdatedListener;
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
            if(serviceUpdatedListener != null) {
                serviceUpdatedListener.onPositionUpdated(position);
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

    private final IBinder iBinder = new ServiceBinder();
    @Override
    public IBinder onBind(Intent intent) {
        setupInternalServices();
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupInternalServices();
        return START_STICKY;
    }
}