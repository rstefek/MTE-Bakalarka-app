package info.stefkovi.studium.mte_bakalarka.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;

import androidx.annotation.NonNull;

import info.stefkovi.studium.mte_bakalarka.listeners.PositionUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class PositionService implements LocationListener {

    private long minTime = 10000;
    private LocationManager manager;
    private PositionUpdatedListener positionUpdatedListener;

    public PositionService(LocationManager manager) {
        this.manager = manager;
    }

    @SuppressLint("MissingPermission")
    public void activateGathering(Context ctx) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            LocationRequest.Builder builder = new LocationRequest.Builder(minTime);
            builder.setMinUpdateDistanceMeters(0);
            builder.setMinUpdateIntervalMillis(minTime);
            builder.setQuality(LocationRequest.QUALITY_HIGH_ACCURACY);
            LocationRequest lr = builder.build();
            manager.requestLocationUpdates("fused", lr, ctx.getMainExecutor(), this );
        } else {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this);
        }
    }

    public void deactivateGathering() {
        manager.removeUpdates(this);
    }

    private PositionApiModel convertLocation(Location location) {
        PositionApiModel apiModel = new PositionApiModel();
        if (location != null) {
            apiModel.lat = location.getLatitude();
            apiModel.lon = location.getLongitude();
            apiModel.speed = location.getSpeed();
            apiModel.accuracy = location.getAccuracy();
            apiModel.altitude = location.getAltitude();
            apiModel.bearing = location.getBearing();
            apiModel.provider = location.getProvider();
        }
        return apiModel;
    }

    public void setPositionUpdatedListener(PositionUpdatedListener listener) {
        positionUpdatedListener = listener;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(positionUpdatedListener != null) {
            positionUpdatedListener.onPositionUpdated(convertLocation(location));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}
