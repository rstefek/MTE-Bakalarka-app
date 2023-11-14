package info.stefkovi.studium.mte_bakalarka;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventResultModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;
import info.stefkovi.studium.mte_bakalarka.services.BackgroundWorkerService;
import info.stefkovi.studium.mte_bakalarka.services.PositionService;
import info.stefkovi.studium.mte_bakalarka.services.TelephonyService;

public class MainActivity extends AppCompatActivity {

    private BackgroundWorkerService _bwService;
    private ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGrantedList -> {
                //TODO: kontrola zda se aktivita má rozjet
                enableActivityActions();
                /*if (isGrantedList.containsKey("A")) {} */
            });
    private String[] permissionsWanted = {
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BackgroundWorkerService.ServiceBinder binder = (BackgroundWorkerService.ServiceBinder) service;
            _bwService = binder.getService();

            PositionService positionService = _bwService.getPositionService();

            positionService.setPositionUpdatedListener(positionApiModel -> {
                //Toast.makeText(getApplicationContext(), "Position changed", Toast.LENGTH_LONG).show();
                MapView mapView = (MapView) findViewById(R.id.mapView);
                mapView.getMapAsync(googleMap -> {
                    LatLng latlng = new LatLng(positionApiModel.lat, positionApiModel.lon);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    Marker marker = positionService.getMyPositionMarker();
                    if(marker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latlng);
                        marker = googleMap.addMarker(markerOptions);
                        positionService.setMyPositionMarker(marker);
                    } else {
                        marker.setPosition(latlng);
                    }
                });
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _bwService = null;
        }
    };

    private void enableActivityActions() {

        Intent serviceIntent = new Intent(this, BackgroundWorkerService.class);

        startForegroundService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        Switch swActivate = (Switch) findViewById(R.id.swActivate);
        swActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchActivateConfirm), Toast.LENGTH_SHORT).show();
                    _bwService.getPositionService().activateGathering();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchDeactivateConfirm), Toast.LENGTH_SHORT).show();
                    _bwService.getPositionService().deactivateGathering();
                }
            }
        });

        Button btn = (Button) findViewById(R.id.buttonGetCells);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionApiModel pos = _bwService.getPositionService().getCurrentPosition();
                List<CellInfoApiModel> cells = _bwService.getTelephonyService().getAllCellInfo();

                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                long rowId = db.saveEventData(pos, cells);

                try {
                    CellInfoApiModel connectedCell = cells.stream().filter(cell -> cell.registered == true).findFirst().get();

                    TextView tvLastCellUpdate = (TextView) findViewById(R.id.tvLastCellUpdate);
                    //tvLastCellUpdate.setText(String.valueOf(connectedCell));

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

                Toast.makeText(getApplicationContext(), String.valueOf(rowId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                /*
                Bundle b = new Bundle();
                b.putString("klic1","hodnota");
                i.putExtras(b);*/
                startActivity(i);
            }
        });

        Button btnSend = (Button) findViewById(R.id.buttonSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                ArrayList<EventModel> events = db.getEventsToSend();
                for ( EventModel event: events) {
                    ApiCommuncation api = new ApiCommuncation(getApplicationContext());
                    api.sendEvent(event, new Response.Listener<EventResultModel>() {
                        @Override
                        public void onResponse(EventResultModel response) {
                            db.markEventAsSend(event.dbId);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), error.networkResponse.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
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