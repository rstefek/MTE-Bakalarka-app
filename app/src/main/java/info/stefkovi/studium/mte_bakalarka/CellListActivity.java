package info.stefkovi.studium.mte_bakalarka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

//import info.stefkovi.studium.mte_bakalarka

public class CellListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_list);

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        CellListAdapter adapter = new CellListAdapter( getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        /*
        Intent in = getIntent();
        if(in != null) {
            Bundle b = in.getExtras();
            b.getString("klic1", "kdyby nahodou");
        }
         */
    }

}