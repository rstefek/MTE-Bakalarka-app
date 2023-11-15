package info.stefkovi.studium.mte_bakalarka;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.JwtHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.model.EventApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventModel;
import info.stefkovi.studium.mte_bakalarka.model.EventResultModel;

public class TransferListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_list);

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        TransferListAdapter adapter = new TransferListAdapter( getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnSend = (Button) findViewById(R.id.buttonSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferencesHelper preferences = new SharedPreferencesHelper(getApplicationContext());
                String token = preferences.readPrefString("jwt");

                DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                ArrayList<EventModel> events = db.getEventsToSend();
                for ( EventModel event: events) {
                    ApiCommuncation api = new ApiCommuncation(getApplicationContext());
                    api.sendEvent(new EventApiModel(event, JwtHelper.getUserId(token)), new Response.Listener<EventResultModel>() {
                        @Override
                        public void onResponse(EventResultModel response) {
                            db.markEventAsSend(response.uid);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.networkResponse != null) {
                                Log.e("API", error.networkResponse.toString(), error);
                            }
                        }
                    });
                }
            }
        });
    }
}