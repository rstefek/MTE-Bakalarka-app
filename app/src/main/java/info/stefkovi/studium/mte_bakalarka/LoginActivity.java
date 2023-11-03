package info.stefkovi.studium.mte_bakalarka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.Executors;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferencesHelper preferences = new SharedPreferencesHelper(getApplicationContext());

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
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        });
                    }
                });
            }
        });
    }
}