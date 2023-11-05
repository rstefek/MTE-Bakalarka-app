package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import info.stefkovi.studium.mte_bakalarka.model.LoginApiModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class ApiCommuncationVolley {
    //Volley - knihovna na HTTP, Logger
    private RequestQueue _queue;
    private String baseURL = "https://bakalarka.beaverlyhills.eu/";

    public ApiCommuncationVolley(Context ctx) {
        this._queue = Volley.newRequestQueue(ctx);
    }

    private class GsonRequest<T> extends JsonRequest<T> {

        private Class<T> _tClass;

        public GsonRequest(int method, String url, @Nullable String requestBody, Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener, Class<T> classOfT) {
            super(method, url, requestBody, listener, errorListener);
            _tClass = classOfT;
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse response) {
            try {
                Gson gson = new Gson();
                String jsonString =
                        new String(
                                response.data,
                                HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                return Response.success(
                        gson.fromJson(jsonString, _tClass), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch ( JsonSyntaxException je) {
                return Response.error(new ParseError(je));
            }
        }
    }

    public <T> void requestGET(String urlPath, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Class<T> classOfT) {
       GsonRequest<T> req = new GsonRequest<T>(Request.Method.GET, baseURL + urlPath, null, responseListener, errorListener, classOfT);
       _queue.add(req);
    }

    public <T> void requestPOST(String urlPath, Object data, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Class<T> classOfT) {
        Gson gson = new Gson();
        GsonRequest<T> req = new GsonRequest<T>(Request.Method.POST, baseURL + urlPath, gson.toJson(data), responseListener, errorListener, classOfT);
        _queue.add(req);
    }

    public void Login(String user, String pwd, Response.Listener<LoginResultApiModel> responseListener, Response.ErrorListener errorListener) {
        LoginApiModel loginData = new LoginApiModel(user, pwd);
        requestPOST("auth/local", loginData, responseListener, errorListener, LoginResultApiModel.class);
    }

}
