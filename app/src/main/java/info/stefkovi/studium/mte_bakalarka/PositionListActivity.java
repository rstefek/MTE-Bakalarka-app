package info.stefkovi.studium.mte_bakalarka;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

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

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_position_list);

        MapView mapViewHistory = (MapView) findViewById(R.id.mapViewHistory);
        mapViewHistory.setTileSource(TileSourceFactory.MAPNIK);
        mapViewHistory.setMultiTouchControls(true);

        IMapController mapController = mapViewHistory.getController();
        mapController.setZoom(9.5);

        Button bFilter = (Button) findViewById(R.id.bFilter);
        bFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedDate != null) {
                    ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                    ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
                    DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                    ArrayList<EventModel> events = db.getAllEventsByDate(selectedDate);
                    if (events.size() > 0) {
                        for (EventModel event : events) {
                            GeoPoint point = new GeoPoint(event.position.lat,event.position.lon);
                            points.add(point);
                            OverlayItem item = new OverlayItem(null, null, point);
                            item.setMarker(AppCompatResources.getDrawable( ctx, R.drawable.location_pin ));
                            items.add(item);
                        }
                    }

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
                            }, ctx);
                    mapViewHistory.getOverlays().clear();
                    mapViewHistory.getOverlays().add(mOverlay);
                    mapViewHistory.zoomToBoundingBox(BoundingBox.fromGeoPoints(points),false);

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