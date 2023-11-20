package info.stefkovi.studium.mte_bakalarka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import info.stefkovi.studium.mte_bakalarka.helpers.ApiCommuncation;
import info.stefkovi.studium.mte_bakalarka.helpers.JwtHelper;
import info.stefkovi.studium.mte_bakalarka.helpers.SharedPreferencesHelper;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferencesHelper preferences = new SharedPreferencesHelper(getApplicationContext());
        String token = preferences.readPrefString(SharedPreferencesHelper.PREF_JWT);

        boolean expired = token.isEmpty() || JwtHelper.isTokenExpired(token);
        if(expired) {
            //token je expirovaný, pokračujeme v práci na login obrazovce
            setContentView(R.layout.activity_login);

            Button loginBtn = (Button) findViewById(R.id.loginButton);
            EditText loginName = (EditText) findViewById(R.id.loginName);
            EditText loginPassword = (EditText) findViewById(R.id.loginPassword);
            loginBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Verze Volley
                    ApiCommuncation api = new ApiCommuncation(getApplicationContext());
                    api.login(loginName.getText().toString(), loginPassword.getText().toString(), new Response.Listener<LoginResultApiModel>() {
                        @Override
                        public void onResponse(LoginResultApiModel response) {
                            preferences.savePrefString(SharedPreferencesHelper.PREF_JWT, response.jwt);

                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(), error.networkResponse.toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                /*
                Handler handler = new Handler(Looper.getMainLooper());
                Executors.newSingleThreadExecutor().execute(() -> {

                    LoginResultApiModel res = ApiCommuncationOld.Login(loginName.getText().toString(), loginPassword.getText().toString());

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
                */
                }
            });
        } else {
            //jdeme na main screen pokud máme tokena. na login už se nevracíme
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(i);
            this.finish();
        }
    }
}