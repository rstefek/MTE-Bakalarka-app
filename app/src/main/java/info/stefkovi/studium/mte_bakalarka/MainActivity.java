package info.stefkovi.studium.mte_bakalarka;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;
import info.stefkovi.studium.mte_bakalarka.services.BackgroundWorkerService;

public class MainActivity extends AppCompatActivity {

    private BackgroundWorkerService _bwService;
    private long _currentEventId;
    private EventQueue _eventQueue;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGrantedList -> {
                //TODO: kontrola zda se aktivita má rozjet
                enableActivityActions();
                /*if (isGrantedList.containsKey("A")) {} */
            });
    private String[] permissionsWanted = {
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundWorkerService.ServiceBinder binder = (BackgroundWorkerService.ServiceBinder) service;
            _bwService = binder.getService();

            _bwService.setUpdatedListener(new BackgroundServiceUpdatedListener() {
                @Override
                public void onPositionUpdated(PositionApiModel position) {
                    MapView mapView = (MapView) findViewById(R.id.mapView);
                    mapView.getMapAsync(googleMap -> {
                        LatLng latlng = new LatLng(position.lat, position.lon);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        Marker marker = _bwService.getPositionService().getMyPositionMarker();
                        if(marker == null) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latlng);
                            marker = googleMap.addMarker(markerOptions);
                            _bwService.getPositionService().setMyPositionMarker(marker);
                        } else {
                            marker.setPosition(latlng);
                        }
                    });
                }

                @Override
                public void onCellsUpdated(List<CellInfoApiModel> cells) {
                    try {
                        CellInfoApiModel connectedCell = cells.stream().filter(cell -> cell.registered == true).findFirst().get();

                        TextView tvCellCIDValue = (TextView) findViewById(R.id.tvCellCIDValue);
                        tvCellCIDValue.setText(String.valueOf(connectedCell.identity.cid));

                        TextView tvCellTACValue = (TextView) findViewById(R.id.tvCellTACValue);
                        tvCellTACValue.setText(String.valueOf(connectedCell.identity.tac));

                        TextView tvCellLACValue = (TextView) findViewById(R.id.tvCellLACValue);
                        tvCellLACValue.setText(String.valueOf(connectedCell.identity.lac));

                        TextView tvCellSignalValue = (TextView) findViewById(R.id.tvCellSignalValue);
                        tvCellSignalValue.setText(String.valueOf(connectedCell.signal.signal_dbm));
                    }
                    catch (Exception e) {

                    }
                }

                @Override
                public void onEvent(EventModel event) {
                    _currentEventId = event.dbId;

                    TextView tvLastCellUpdate = (TextView) findViewById(R.id.tvLastCellUpdate);
                    tvLastCellUpdate.setText(event.happened.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

                    updateEventQueue();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _bwService = null;
        }
    };

    private void updateEventQueue() {
        _eventQueue.onEventAdded();
    }

    private void enableActivityActions() {

        Intent serviceIntent = new Intent(this, BackgroundWorkerService.class);

        _eventQueue.setUpdatedListener(new EventQueueUpdatedListener() {
            @Override
            public void onEventsQueueUpdated(EventQueueInfo queue) {
                //fronta
                TextView tvQueue = (TextView) findViewById(R.id.tvQueue);
                tvQueue.setText(getString(R.string.Queue) + ": " + String.valueOf(queue.numInDbToProcess));

                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setMax(queue.numInQTotal);
                if(queue.numInQToProcess == 0) {
                    progressBar.setProgress((int) queue.numInDbToProcess);
                    progressBar.setProgressTintList(ColorStateList.valueOf(getColor(R.color.primary)));
                } else {
                    progressBar.setProgress(queue.numInQToProcess);
                    progressBar.setProgressTintList(ColorStateList.valueOf(getColor(R.color.secondary)));
                }
            }
        });
        _eventQueue.onEventAdded(); //prvotní načtení

        Switch swActivate = (Switch) findViewById(R.id.swActivate);
        swActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchActivateConfirm), Toast.LENGTH_SHORT).show();

                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

                    startForegroundService(serviceIntent);
                    //_bwService.start();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchDeactivateConfirm), Toast.LENGTH_SHORT).show();

                    unbindService(serviceConnection);

                    stopService(serviceIntent);
                    //_bwService.stop();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _eventQueue = new EventQueue(getApplicationContext());

        boolean accepted = PermissionHelper.AllPermissionsAccepted(this, permissionsWanted);
        if (!accepted) {
            requestPermissionLauncher.launch(permissionsWanted);
        } else {
            enableActivityActions();
        }

        ImageView ivSignal = (ImageView) findViewById(R.id.ivSignalStrength);
        ivSignal.setImageDrawable(getDrawable(R.drawable.signal_3));

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(googleMap -> {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        });

        ImageButton btnSettings = (ImageButton) findViewById(R.id.ibSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        Button btnDetail = (Button) findViewById(R.id.buttonDetail);
        btnDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CellListActivity.class);
                Bundle b = new Bundle();
                b.putLong("eventId", _currentEventId);
                i.putExtras(b);
                startActivity(i);
            }
        });

        Button btnSend = (Button) findViewById(R.id.buttonSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TransferListActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onResume();
    }
}