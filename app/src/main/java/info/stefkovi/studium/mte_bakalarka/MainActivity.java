package info.stefkovi.studium.mte_bakalarka;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.listeners.BackgroundServiceUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.DeviceApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventGroupApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;
import info.stefkovi.studium.mte_bakalarka.services.BackgroundWorkerService;

public class MainActivity extends AppCompatActivity {

    private BackgroundWorkerService bwService;
    private long currentEventId;
    private EventQueue eventQueue;
    private List<EventGroupApiModel> eventGroupsData;
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
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.WAKE_LOCK
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

                    IMapController mapController = mapView.getController();
                    GeoPoint startPoint = new GeoPoint(position.lat, position.lon);
                    mapController.setCenter(startPoint);

                    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                    OverlayItem item = new OverlayItem("Aktuální poloha", null, startPoint);
                    item.setMarker(AppCompatResources.getDrawable( getApplicationContext(), R.drawable.location_pin ));
                    items.add(item);

                    ItemizedIconOverlay<OverlayItem> mOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                            new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                                @Override
                                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                    //do something
                                    return true;
                                }
                                @Override
                                public boolean onItemLongPress(final int index, final OverlayItem item) {
                                    return false;
                                }
                            }, getApplicationContext());
                    mapView.getOverlays().clear();
                    mapView.getOverlays().add(mOverlay);

                    TextView tvPositionAngle = (TextView) findViewById(R.id.tvPositionAngle);
                    tvPositionAngle.setText((int) Math.floor(position.bearing) + getString(R.string.MapUnitBearing));

                    TextView tvPositionSpeed = (TextView) findViewById(R.id.tvPositionSpeed);
                    tvPositionSpeed.setText((int) Math.floor(position.speed * 3.6) + getString(R.string.MapUnitSpeed));

                    TextView tvPositionAccuracy = (TextView) findViewById(R.id.tvPositionAccuracy);
                    tvPositionAccuracy.setText((int) Math.floor(position.accuracy) + getString(R.string.MapUnitAccuracy));
                }

                @Override
                public void onCellsUpdated(List<CellInfoApiModel> cells) {

                    Optional<CellInfoApiModel> connectedCellOpt = cells.stream().filter(cell -> cell.registered == true).findFirst();
                    if(connectedCellOpt.isPresent()) {
                        CellInfoApiModel connectedCell = connectedCellOpt.get();

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

                        TextView tvCellSignalAsuValue = (TextView) findViewById(R.id.tvCellSignalAsuValue);
                        tvCellSignalAsuValue.setText(String.valueOf(connectedCell.signal.signal_asu));

                    } else {
                        ImageView ivSignal = (ImageView) findViewById(R.id.ivSignalStrength);
                        ivSignal.setImageDrawable(getDrawable(R.drawable.singal_disconnected));
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

            Spinner spEventGroup = (Spinner) findViewById(R.id.spEventGroup);
            if(eventGroupsData != null) {
                bwService.setEventGroupId(eventGroupsData.get(spEventGroup.getSelectedItemPosition()).id);
            }
            spEventGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    bwService.setEventGroupId(eventGroupsData.get(position).id);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bwService = null;
        }
    };

    private void updateEventQueue() {
        Switch swOffline = findViewById(R.id.swOffline);
        eventQueue.onEventAdded(!swOffline.isChecked());
    }

    private void enableActivityActions() {

        Intent serviceIntent = new Intent(this, BackgroundWorkerService.class);

        ApiCommuncation api = new ApiCommuncation(getApplicationContext());
        SharedPreferencesHelper preferences = new SharedPreferencesHelper(getApplicationContext());

        ArrayAdapter<String> eventGroupsAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.view_spinner_item, new ArrayList<>());
        Spinner spEventGroup = (Spinner) findViewById(R.id.spEventGroup);
        spEventGroup.setAdapter(eventGroupsAdapter);
        api.getEventGroups(new Response.Listener<List<EventGroupApiModel>>() {
            @Override
            public void onResponse(List<EventGroupApiModel> response) {
                eventGroupsData = response;
                eventGroupsAdapter.clear();
                eventGroupsAdapter.addAll(eventGroupsData.stream().map(eventGroupApiModel -> eventGroupApiModel.name).collect(Collectors.toList()));
                eventGroupsAdapter.notifyDataSetChanged();
                spEventGroup.invalidate();
                spEventGroup.setSelection(0);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        api.getDevices(new Response.Listener<List<DeviceApiModel>>() {
            @Override
            public void onResponse(List<DeviceApiModel> response) {
                UUID deviceUuid = preferences.readCreateDeviceUUID();
                int deviceApiId = 0;
                for (DeviceApiModel device:response) {
                    if(device.saved_uid.compareTo(deviceUuid) == 0) {
                        eventQueue.setDeviceId(device.id);
                        break;
                    }
                }
                if(deviceApiId == 0) { //nemáme zatím v seznamu, musíme poslat POSTem
                    api.createDevice(new DeviceApiModel(deviceUuid), new Response.Listener<DeviceApiModel>() {
                        @Override
                        public void onResponse(DeviceApiModel response) {
                            eventQueue.setDeviceId(response.id);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                }
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
        eventQueue.onEventAdded(false); //prvotní načtení

        Switch swActivate = (Switch) findViewById(R.id.swActivate);
        swActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchActivateConfirm), Toast.LENGTH_SHORT).show();

                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

                    startForegroundService(serviceIntent);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.SwitchDeactivateConfirm), Toast.LENGTH_SHORT).show();

                    unbindService(serviceConnection);

                    stopService(serviceIntent);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_main);

        eventQueue = EventQueue.getInstance(getApplicationContext());

        boolean accepted = PermissionHelper.AllPermissionsAccepted(this, permissionsWanted);
        if (!accepted) {
            requestPermissionLauncher.launch(permissionsWanted);
        } else {
            enableActivityActions();
        }

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(20d);

        ImageButton btnSettings = (ImageButton) findViewById(R.id.ibSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        ImageButton btnPositionList = (ImageButton) findViewById(R.id.ibPositionList);
        btnPositionList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PositionListActivity.class);
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