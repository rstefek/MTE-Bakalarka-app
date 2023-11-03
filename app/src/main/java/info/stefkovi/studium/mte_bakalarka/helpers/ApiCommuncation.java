package info.stefkovi.studium.mte_bakalarka.helpers;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONStringer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import info.stefkovi.studium.mte_bakalarka.model.LoginApiModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class ApiCommuncation {
    private static String baseURL = "https://bakalarka.beaverlyhills.eu/";

    private static <T> T Request(String urlPath, String method, Object data, Class<T> classOfT) {
        return Request(urlPath,method,data,classOfT,true);
    }

    private static <T> T Request(String urlPath, String method, Object data, Class<T> classOfT, boolean addToken) {
        URL url = null;
        try {
            url = new URL(baseURL + urlPath);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            Gson gson = new Gson();
            urlConnection.setDoInput(true);

            if(method.equals("POST")) {
                //zaslání JSON dat
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                try {
                    OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                    out.write(gson.toJson(data));
                    out.flush();
                    out.close();
                } catch (Exception e){
                    Log.e("API", e.getMessage());
                }
            }
            //odpověď
            try {
                if(urlConnection.getResponseCode() != 200) {
                    Log.e("API", urlConnection.getResponseMessage());
                    return null;
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    T returnData = gson.fromJson(in, classOfT);
                    Log.d("API", returnData.toString());
                    return returnData;
                }
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static LoginResultApiModel Login(String user, String pwd) {
        LoginApiModel loginData = new LoginApiModel(user, pwd);
        return Request("auth/local", "POST", loginData, LoginResultApiModel.class, false);
    }
}
