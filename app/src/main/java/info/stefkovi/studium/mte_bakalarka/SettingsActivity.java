package info.stefkovi.studium.mte_bakalarka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import info.stefkovi.studium.mte_bakalarka.helpers.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());

        Button btnDelete1 = (Button) findViewById(R.id.bDeleteNonSent);
        btnDelete1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteNotSent();
                Toast.makeText(getApplicationContext(), getString(R.string.SettingsDatabaseConfirm), Toast.LENGTH_SHORT).show();
            }
        });

        Button btnDelete2 = (Button) findViewById(R.id.bDeleteProcessed);
        btnDelete2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.deleteProcessed();
                Toast.makeText(getApplicationContext(), getString(R.string.SettingsDatabaseConfirm), Toast.LENGTH_SHORT).show();
            }
        });
    }
}