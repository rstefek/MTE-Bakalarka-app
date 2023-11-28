package info.stefkovi.studium.mte_bakalarka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import info.stefkovi.studium.mte_bakalarka.listeners.EventQueueUpdatedListener;
import info.stefkovi.studium.mte_bakalarka.model.EventQueueInfo;

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
                EventQueue eventQueue = EventQueue.getInstance(getApplicationContext());
                eventQueue.setUpdatedListener(new EventQueueUpdatedListener() {
                    @Override
                    public void onEventsQueueUpdated(EventQueueInfo queue) {
                        if(queue.numInQToProcess == 0) {
                            adapter.reloadData();
                        }
                    }
                });
                eventQueue.sendEventsBulk();
            }
        });
    }
}