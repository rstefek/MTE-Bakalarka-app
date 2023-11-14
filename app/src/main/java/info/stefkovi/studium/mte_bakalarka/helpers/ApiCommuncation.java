package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import info.stefkovi.studium.mte_bakalarka.model.EventApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventResultModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginApiModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class ApiCommuncation {
    //Volley - knihovna na HTTP, Logger
    private RequestQueue _queue;
    private Context _ctx;
    private String baseURL = "https://bakalarka.beaverlyhills.eu/";

    public ApiCommuncation(Context ctx) {
        this._ctx = ctx;
        this._queue = Volley.newRequestQueue(ctx);
    }

    private class GsonRequest<T> extends JsonRequest<T> {

        private Class<T> _tClass;
        private String _useToken;

        public GsonRequest(int method, String url, @Nullable String requestBody, Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener, Class<T> classOfT, String useToken) {
            super(method, url, requestBody, listener, errorListener);
            _tClass = classOfT;
            _useToken = useToken;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>(super.getHeaders());
            headers.put("Accept", "application/json");
            if(_useToken != null) {
                headers.put("Authorization", "Bearer "+_useToken);
            }
            return headers;
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

    private String getAPIToken() {
        SharedPreferencesHelper preferences = new SharedPreferencesHelper(_ctx);
        return preferences.readPrefString("jwt");
    }

    public <T> void requestGET(String urlPath, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Class<T> classOfT, String useToken) {
       GsonRequest<T> req = new GsonRequest<T>(Request.Method.GET, baseURL + urlPath, null, responseListener, errorListener, classOfT, useToken);
       _queue.add(req);
    }

    public <T> void requestPOST(String urlPath, Object data, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Class<T> classOfT, String useToken) {
        Gson gson = new Gson();
        GsonRequest<T> req = new GsonRequest<T>(Request.Method.POST, baseURL + urlPath, gson.toJson(data), responseListener, errorListener, classOfT, useToken);
        _queue.add(req);
    }

    public void login(String user, String pwd, Response.Listener<LoginResultApiModel> responseListener, Response.ErrorListener errorListener) {
        LoginApiModel loginData = new LoginApiModel(user, pwd);
        requestPOST("auth/local", loginData, responseListener, errorListener, LoginResultApiModel.class, null);
    }

    public void sendEvent(EventApiModel event, Response.Listener<EventResultModel> responseListener, Response.ErrorListener errorListener) {
        requestPOST("events", event, responseListener, errorListener, EventResultModel.class, getAPIToken());
    }

}
