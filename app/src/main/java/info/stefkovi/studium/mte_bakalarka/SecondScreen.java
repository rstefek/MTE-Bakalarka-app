package info.stefkovi.studium.mte_bakalarka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SecondScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);

        Intent in = getIntent();
        if(in != null) {
            Bundle b = in.getExtras();
            b.getString("klic1", "kdyby nahodou");
        }
    }
}