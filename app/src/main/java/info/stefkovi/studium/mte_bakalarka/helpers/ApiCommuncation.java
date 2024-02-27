package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.stefkovi.studium.mte_bakalarka.model.DeviceApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventBulkResultModel;
import info.stefkovi.studium.mte_bakalarka.model.EventGroupApiModel;
import info.stefkovi.studium.mte_bakalarka.model.EventResultModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginApiModel;
import info.stefkovi.studium.mte_bakalarka.model.LoginResultApiModel;

public class ApiCommuncation {
    //Volley - knihovna na HTTP, Logger
    private RequestQueue queue;
    private Context ctx;
    private final String BASE_URL = "https://bakalarka.beaverlyhills.eu/";
    private final int CONNECTION_TIMEOUT = 20000;

    public ApiCommuncation(Context ctx) {
        this.ctx = ctx;
        this.queue = Volley.newRequestQueue(ctx);
    }

    private class GsonRequest<T> extends JsonRequest<T> {

        private Type typeOfT;
        private String useToken;

        public GsonRequest(int method, String url, @Nullable String requestBody, Response.Listener<T> listener, @Nullable Response.ErrorListener errorListener, Type typeOfT, String useToken) {
            super(method, url, requestBody, listener, errorListener);
            this.typeOfT = typeOfT;
            this.useToken = useToken;
        }

        @Override
        public RetryPolicy getRetryPolicy() {
            return new DefaultRetryPolicy(CONNECTION_TIMEOUT, 3, 0);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>(super.getHeaders());
            headers.put("Accept", "application/json");
            if(useToken != null) {
                headers.put("Authorization", "Bearer "+ useToken);
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
                        gson.fromJson(jsonString, typeOfT), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch ( JsonSyntaxException je) {
                return Response.error(new ParseError(je));
            }
        }
    }

    private String getAPIToken() {
        SharedPreferencesHelper preferences = new SharedPreferencesHelper(ctx);
        return preferences.readPrefString(SharedPreferencesHelper.PREF_JWT);
    }

    public int getAPIUserId() {
        return JwtHelper.getUserId(getAPIToken());
    }

    public <T> void requestGET(String urlPath, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Type typeOfT, String useToken) {
       GsonRequest<T> req = new GsonRequest<T>(Request.Method.GET, BASE_URL + urlPath, null, responseListener, errorListener, typeOfT, useToken);
       queue.add(req);
    }

    public <T> void requestPOST(String urlPath, Object data, Response.Listener<T> responseListener, Response.ErrorListener errorListener, Type typeOfT, String useToken) {
        Gson gson = new Gson();
        GsonRequest<T> req = new GsonRequest<T>(Request.Method.POST, BASE_URL + urlPath, gson.toJson(data), responseListener, errorListener, typeOfT, useToken);
        queue.add(req);
    }

    public void login(String user, String pwd, Response.Listener<LoginResultApiModel> responseListener, Response.ErrorListener errorListener) {
        LoginApiModel loginData = new LoginApiModel(user, pwd);
        requestPOST("auth/local", loginData, responseListener, errorListener, LoginResultApiModel.class, null);
    }

    public void sendEvent(EventApiModel event, Response.Listener<EventResultModel> responseListener, Response.ErrorListener errorListener) {
        requestPOST("events", event, responseListener, errorListener, EventResultModel.class, getAPIToken());
    }

    public void sendEventsBulk(List<EventApiModel> events, Response.Listener<EventBulkResultModel> responseListener, Response.ErrorListener errorListener) {
        requestPOST("events/bulk", events, responseListener, errorListener, EventBulkResultModel.class, getAPIToken());
    }

    public void getEventGroups(Response.Listener<List<EventGroupApiModel>> responseListener, Response.ErrorListener errorListener) {
        requestGET("event-groups", responseListener, errorListener, TypeTokenHelper.getEventGroupsListType(), getAPIToken());
    }

    public void getDevices(Response.Listener<List<DeviceApiModel>> responseListener, Response.ErrorListener errorListener) {
        requestGET("devices", responseListener, errorListener, TypeTokenHelper.getDevicesListType(), getAPIToken());
    }

    public void createDevice(DeviceApiModel device, Response.Listener<DeviceApiModel> responseListener, Response.ErrorListener errorListener) {
        requestPOST("devices", device, responseListener, errorListener, DeviceApiModel.class, getAPIToken());
    }

}
