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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
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

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventGroupApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;
import info.stefkovi.studium.mte_bakalarka.services.BackgroundWorkerService;

public class MainActivity extends AppCompatActivity {

    private BackgroundWorkerService bwService;
    private long currentEventId;
    private EventQueue eventQueue;
    private List<String> eventGroupsSpinnerData;
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
            bwService = binder.getService();

            bwService.setUpdatedListener(new BackgroundServiceUpdatedListener() {
                @Override
                public void onPositionUpdated(PositionApiModel position) {
                    MapView mapView = (MapView) findViewById(R.id.mapView);
                    mapView.getMapAsync(googleMap -> {
                        LatLng latlng = new LatLng(position.lat, position.lon);
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        Marker marker = bwService.getPositionService().getMyPositionMarker();
                        if(marker == null) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latlng);
                            marker = googleMap.addMarker(markerOptions);
                            bwService.getPositionService().setMyPositionMarker(marker);
                        } else {
                            marker.setPosition(latlng);
                        }
                    });
                }

                @Override
                public void onCellsUpdated(List<CellInfoApiModel> cells) {
                    try {
                        CellInfoApiModel connectedCell = cells.stream().filter(cell -> cell.registered == true).findFirst().get();

                        ImageView ivSignal = (ImageView) findViewById(R.id.ivSignalStrength);
                        switch (connectedCell.signal.level) {
                            case 4:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.signal_4));
                                break;
                            case 3:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.signal_3));
                                break;
                            case 2:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.signal_2));
                                break;
                            case 1:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.signal_1));
                                break;
                            case 0:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.signal_0));
                                break;
                            default:
                                ivSignal.setImageDrawable(getDrawable(R.drawable.singal_disconnected));
                                break;
                        }

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
                    currentEventId = event.dbId;

                    TextView tvLastCellUpdate = (TextView) findViewById(R.id.tvLastCellUpdate);
                    tvLastCellUpdate.setText(event.happened.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));

                    updateEventQueue();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bwService = null;
        }
    };

    private void updateEventQueue() {
        eventQueue.onEventAdded();
    }

    private void enableActivityActions() {

        Intent serviceIntent = new Intent(this, BackgroundWorkerService.class);

        ApiCommuncation api = new ApiCommuncation(getApplicationContext());

        eventGroupsSpinnerData = new ArrayList<>();
        ArrayAdapter<String> eventGroupsAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.view_spinner_item, eventGroupsSpinnerData);
        Spinner spEventGroup = (Spinner) findViewById(R.id.spEventGroup);
        spEventGroup.setAdapter(eventGroupsAdapter);
        api.getEventGroups(new Response.Listener<List<EventGroupApiModel>>() {
            @Override
            public void onResponse(List<EventGroupApiModel> response) {
                List<String> eventGroupsList = response.stream().map(eventGroupApiModel -> eventGroupApiModel.name).collect(Collectors.toList());
                eventGroupsAdapter.clear();
                eventGroupsAdapter.addAll(eventGroupsList);
                eventGroupsAdapter.notifyDataSetChanged();
                spEventGroup.invalidate();
                spEventGroup.setSelection(0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        eventQueue.setUpdatedListener(new EventQueueUpdatedListener() {
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
        eventQueue.onEventAdded(); //prvotní načtení

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

        eventQueue = new EventQueue(getApplicationContext());

        boolean accepted = PermissionHelper.AllPermissionsAccepted(this, permissionsWanted);
        if (!accepted) {
            requestPermissionLauncher.launch(permissionsWanted);
        } else {
            enableActivityActions();
        }

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
                b.putLong("eventId", currentEventId);
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