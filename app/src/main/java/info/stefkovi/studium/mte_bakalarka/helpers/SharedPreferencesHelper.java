package info.stefkovi.studium.mte_bakalarka.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import info.stefkovi.studium.mte_bakalarka.R;

public class SharedPreferencesHelper {

    private SharedPreferences _sharedPrefs;

    public SharedPreferencesHelper(Context ctx) {
        _sharedPrefs = ctx.getSharedPreferences(ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public void savePrefString(String key, String value) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void savePrefJson(String key, Object data) {
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        Gson gson = new Gson();
        editor.putString(key, gson.toJson(data));
        editor.apply();
    }

    public String readPrefString(String key) {
        return _sharedPrefs.getString(key, "");
    }

    public <T> T readPrefJson(String key, Class<T> tClass) {
        Gson gson = new Gson();
        return gson.fromJson(_sharedPrefs.getString(key, "{}"), tClass);
    }

}
