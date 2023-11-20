package info.stefkovi.studium.mte_bakalarka.services;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.Marker;

import info.stefkovi.studium.mte_bakalarka.listeners.PositionUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class PositionService implements LocationListener {

    private long minTime = 10000;
    private LocationManager manager;
    private Location currentLocation;
    private PositionUpdatedListener positionUpdatedListener;

    private Marker myPositionMarker = null;

    public PositionService(LocationManager manager) {
        this.manager = manager;
    }

    @SuppressLint("MissingPermission")
    public void activateGathering() {
        manager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, minTime, 0, this);
    }

    public void deactivateGathering() {
        manager.removeUpdates(this);
    }

    public Marker getMyPositionMarker() {
        return myPositionMarker;
    }

    public void setMyPositionMarker(Marker myPositionMarker) {
        this.myPositionMarker = myPositionMarker;
    }
    public PositionApiModel getCurrentPosition() {
        return convertLocation(currentLocation);
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
        currentLocation = location;
        if(positionUpdatedListener != null) {
            positionUpdatedListener.onPositionUpdated(convertLocation(location));
        }
    }
}
