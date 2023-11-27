package info.stefkovi.studium.mte_bakalarka;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;

public class PositionListActivity extends AppCompatActivity {

    private LocalDate selectedDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_list);

        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onCreate(savedInstanceState);
        mapViewHistory.getMapAsync(googleMap -> {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        });

        Button bFilter = (Button) findViewById(R.id.bFilter);
        bFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedDate != null) {
                    mapViewHistory.getMapAsync(googleMap -> {
                        googleMap.clear();
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                        ArrayList<EventModel> events = db.getAllEventsByDate(selectedDate);
                        if (events.size() > 0) {
                            for (EventModel event : events) {
                                LatLng pos = new LatLng(event.position.lat, event.position.lon);
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(pos);
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_pin));
                                boundsBuilder.include(pos);
                                googleMap.addMarker(markerOptions);
                            }
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 20));
                        }
                    });
                }
            }
        });

        ImageButton ibSelectDateFrom = (ImageButton) findViewById(R.id.ibSelectDateFrom);
        Context window = this;
        ibSelectDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(window);
                datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month+1, dayOfMonth); //demence Javy v měsících
                    TextView tvDateFrom = (TextView) findViewById(R.id.tvDateFrom);
                    tvDateFrom.setText(selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
                });
                datePickerDialog.show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.onResume();
    }
}