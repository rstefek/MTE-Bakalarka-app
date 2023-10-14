package info.stefkovi.studium.mte_bakalarka;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import info.stefkovi.studium.mte_bakalarka.helpers.PermissionHelper;
import info.stefkovi.studium.mte_bakalarka.model.PositionApiModel;

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

        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionApiModel pos = _positionService.GetCurrentPosition();
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

        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SecondScreen.class);
                Bundle b = new Bundle();
                b.putString("klic1","hodnota");
                i.putExtras(b);
                startActivity(i);
            }
        });
    }
}