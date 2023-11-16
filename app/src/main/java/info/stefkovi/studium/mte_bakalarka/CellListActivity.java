package info.stefkovi.studium.mte_bakalarka;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CellListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_list);

        long eventId = 0;
        Intent in = getIntent();
        if(in != null) {
            Bundle b = in.getExtras();
            eventId = b.getLong("eventId", 0);
        }

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        CellListAdapter adapter = new CellListAdapter( getApplicationContext(), eventId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}