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

    private LocationManager _manager;
    private Location _currentLocation;
    private PositionUpdatedListener positionUpdatedListener;

    private Marker myPositionMarker = null;

    @SuppressLint("MissingPermission")
    public PositionService(LocationManager manager) {
        this._manager = manager;
        _manager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 10000, 0, this);
    }

    public Marker getMyPositionMarker() {
        return myPositionMarker;
    }

    public void setMyPositionMarker(Marker myPositionMarker) {
        this.myPositionMarker = myPositionMarker;
    }
    public PositionApiModel getCurrentPosition() {
        return convertLocation(_currentLocation);
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

    @Override
    public void onLocationChanged(@NonNull Location location) {
        _currentLocation = location;
        if(positionUpdatedListener != null) {
            positionUpdatedListener.onPositionUpdated(convertLocation(location));
        }
    }
}
