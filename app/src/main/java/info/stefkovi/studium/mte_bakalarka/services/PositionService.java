package info.stefkovi.studium.mte_bakalarka.services;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

public class PositionService implements LocationListener {

    private LocationManager _manager;
    private Location _currentLocation;

    @SuppressLint("MissingPermission")
    public PositionService(LocationManager manager) {
        this._manager = manager;
        _manager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 10000, 0, this);
    }

    public PositionApiModel GetCurrentPosition() {
        PositionApiModel apiModel = new PositionApiModel();
        if (_currentLocation != null) {
            apiModel.lat = _currentLocation.getLatitude();
            apiModel.lon = _currentLocation.getLongitude();
            apiModel.speed = _currentLocation.getSpeed();
            apiModel.accuracy = _currentLocation.getAccuracy();
            apiModel.altitude = _currentLocation.getAltitude();
            apiModel.bearing = _currentLocation.getBearing();
            apiModel.provider = _currentLocation.getProvider();
        }
        return apiModel;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        _currentLocation = location;
    }
}
