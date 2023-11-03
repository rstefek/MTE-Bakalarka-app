package info.stefkovi.studium.mte_bakalarka;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.Executors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.model.CellInfoApiModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;
import info.stefkovi.studium.mte_bakalarka.services.PositionService;
import info.stefkovi.studium.mte_bakalarka.services.TelephonyService;

public class MainActivity extends AppCompatActivity {

    private TelephonyService _telephonyService;
    private PositionService _positionService;
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

    private void enableActivityActions() {

        _telephonyService = new TelephonyService((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        _positionService = new PositionService((LocationManager) getSystemService(Context.LOCATION_SERVICE));

        SharedPreferencesHelper preferences = new SharedPreferencesHelper(getApplicationContext());

        MapView mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(null);
        mapView.getMapAsync(googleMap -> {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(49.8336850, 18.1636014)));
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(11));
        });

        Button loginBtn = (Button) findViewById(R.id.loginButton);
        EditText loginName = (EditText) findViewById(R.id.loginName);
        EditText loginPassword = (EditText) findViewById(R.id.loginPassword);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Handler handler = new Handler(Looper.getMainLooper());
                Executors.newSingleThreadExecutor().execute(() -> {
                    //Background work here
                    LoginResultApiModel res = ApiCommuncation.Login(loginName.getText().toString(), loginPassword.getText().toString());
                    if(res != null) {
                        //RecyclerView

                        //uložení tokenu
                        preferences.savePrefString("jwt", res.jwt);

                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), res.toString(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });

        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionApiModel pos = _positionService.GetCurrentPosition();
                List<CellInfoApiModel> cells = _telephonyService.getAllCellInfo();

                Toast.makeText(getApplicationContext(), pos.provider, Toast.LENGTH_LONG).show();
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

        MapView mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CellListActivity.class);
                Bundle b = new Bundle();
                b.putString("klic1","hodnota");
                i.putExtras(b);
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